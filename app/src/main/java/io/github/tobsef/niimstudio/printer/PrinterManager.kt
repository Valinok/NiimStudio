package io.github.tobsef.niimstudio.printer

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import io.github.tobsef.niimstudio.data.Store
import io.github.tobsef.niimstudio.model.TransportKind
import io.github.tobsef.niimstudio.protocol.CloudRoll
import io.github.tobsef.niimstudio.protocol.EncodedImage
import io.github.tobsef.niimstudio.protocol.LabelRoll
import io.github.tobsef.niimstudio.protocol.NiimbotClient
import io.github.tobsef.niimstudio.protocol.PrintJob
import io.github.tobsef.niimstudio.protocol.PrintOptions
import io.github.tobsef.niimstudio.protocol.PrintProgress
import io.github.tobsef.niimstudio.protocol.PrintStage
import io.github.tobsef.niimstudio.protocol.PrinterInfo
import io.github.tobsef.niimstudio.protocol.PrinterStatus
import io.github.tobsef.niimstudio.protocol.RollCatalog
import io.github.tobsef.niimstudio.protocol.Rsp
import io.github.tobsef.niimstudio.transport.BleTransport
import io.github.tobsef.niimstudio.transport.SppTransport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class FailReason { BLUETOOTH_OFF, NO_PERMISSION, CONNECT_FAILED }

sealed class ConnState {
    data object Disconnected : ConnState()
    data class Connecting(val name: String) : ConnState()
    data class Connected(val name: String, val info: PrinterInfo) : ConnState()
    data class Failed(val reason: FailReason, val detail: String? = null) : ConnState()
}

private const val HEARTBEAT_INTERVAL_MS = 1_000L
private const val HEARTBEAT_TIMEOUT_MS = 1_000L
private const val HEARTBEAT_FAILURES_TO_DISCONNECT = 2
private const val ROLL_POLL_INTERVAL_MS = 4_000L

/** App-wide printer connection, keep-alive and print execution. */
@SuppressLint("MissingPermission")
object PrinterManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val connectLock = Mutex()

    private val _state = MutableStateFlow<ConnState>(ConnState.Disconnected)
    val state: StateFlow<ConnState> = _state

    private val _log = MutableStateFlow<List<String>>(emptyList())
    val log: StateFlow<List<String>> = _log

    private val _printing = MutableStateFlow<PrintProgress?>(null)
    val printing: StateFlow<PrintProgress?> = _printing

    private val _roll = MutableStateFlow<LabelRoll?>(null)
    val roll: StateFlow<LabelRoll?> = _roll

    /** Catalogue metadata for the roll currently in the printer, fetched by SKU only. */
    private val _catalog = MutableStateFlow<CloudRoll?>(null)
    val catalog: StateFlow<CloudRoll?> = _catalog

    private val _status = MutableStateFlow<PrinterStatus?>(null)
    val status: StateFlow<PrinterStatus?> = _status

    private var client: NiimbotClient? = null
    private var heartbeatJob: Job? = null
    private var rollWatchJob: Job? = null
    private var catalogWatchJob: Job? = null
    private var lastAutoCatalogSku: String? = null
    private var printJob: Job? = null
    private var connectJob: Job? = null
    private val timeFmt = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    val isConnected get() = _state.value is ConnState.Connected
    val info get() = (state.value as? ConnState.Connected)?.info

    init {
        startCatalogWatch()
    }

    fun log(msg: String) {
        android.util.Log.d("NiimStudio", msg)
        synchronized(this) {
            // SimpleDateFormat is not thread-safe; log() is called from BLE/SPP threads and coroutines.
            val line = "${timeFmt.format(Date())} $msg"
            val l = _log.value
            _log.value = (if (l.size >= 3000) l.drop(500) else l) + line
        }
    }

    fun clearLog() {
        _log.value = emptyList()
    }

    fun hasPermissions(ctx: Context) = requiredPermissions().all {
        ctx.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
    }

    fun requiredPermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= 31) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }

    fun adapter(ctx: Context): BluetoothAdapter? =
        (ctx.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

    /** Connects to the device stored in the settings. */
    fun connectSaved(ctx: Context) {
        val s = Store.settings.value
        val addr = s.deviceAddress ?: return
        connect(ctx, addr, s.deviceName ?: addr, s.transport)
    }

    fun connect(ctx: Context, address: String, name: String, kind: TransportKind) {
        connectJob?.cancel()
        connectJob = scope.launch { connectNow(ctx, address, name, kind) }
    }

    /** Ensures a connection to the saved printer. Returns true when connected. */
    suspend fun ensureConnected(ctx: Context): Boolean {
        if (isConnected) return true
        val s = Store.settings.value
        val addr = s.deviceAddress ?: return false
        connectNow(ctx, addr, s.deviceName ?: addr, s.transport)
        return isConnected
    }

    private suspend fun connectNow(ctx: Context, address: String, name: String, kind: TransportKind) = connectLock.withLock {
        if ((_state.value as? ConnState.Connected)?.name == name && client != null) return@withLock
        closeClient()
        val ad = adapter(ctx)
        if (ad == null || !ad.isEnabled) {
            _state.value = ConnState.Failed(FailReason.BLUETOOTH_OFF)
            return@withLock
        }
        if (!hasPermissions(ctx)) {
            _state.value = ConnState.Failed(FailReason.NO_PERMISSION)
            return@withLock
        }
        val device = ad.getRemoteDevice(address)
        val transport = if (kind == TransportKind.BLE) BleTransport(ctx.applicationContext, device, ::log) else SppTransport(ad, device, ::log)
        val c = NiimbotClient(transport, ::log)
        c.packetIntervalMs = Store.settings.value.packetDelayMs.toLong()
        client = c
        _state.value = ConnState.Connecting(name)
        try {
            c.connect { err ->
                log("Connection closed${err?.let { ": ${it.message}" } ?: ""}")
                if (client === c) {
                    client = null
                    // A powered-off printer must not leave stale roll/catalogue data behind.
                    resetConnectionState()
                    _state.value = ConnState.Disconnected
                }
            }
            val info = c.negotiate()
            log("Printer: $info")
            _state.value = ConnState.Connected(name, info)
            startHeartbeat(c)
            startRollWatch(c)
            refreshRollFrom(c)
        } catch (e: Exception) {
            log("Connect failed: ${e.message}")
            c.close()
            if (client === c) client = null
            _state.value = ConnState.Failed(FailReason.CONNECT_FAILED, e.message)
        }
    }

    fun disconnect() {
        // Cancel an in-flight handshake: otherwise disconnect waits for its timeouts.
        connectJob?.cancel()
        scope.launch { connectLock.withLock { closeClient() } }
    }

    /** Single place for all per-connection state, used by disconnect and by transport close. */
    private fun resetConnectionState() {
        heartbeatJob?.cancel()
        rollWatchJob?.cancel()
        printJob?.cancel()
        _printing.value = null
        _roll.value = null
        _catalog.value = null
        lastAutoCatalogSku = null
        _status.value = null
    }

    private fun closeClient() {
        resetConnectionState()
        client?.let {
            log("Disconnect")
            it.close()
        }
        client = null
        _state.value = ConnState.Disconnected
    }

    private fun startHeartbeat(c: NiimbotClient) {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            var failures = 0
            var lastRfidOk: Boolean? = null
            while (isActive) {
                delay(HEARTBEAT_INTERVAL_MS)
                if (!Store.settings.value.keepAlive || _printing.value != null || client !== c) continue
                try {
                    val hb = c.heartbeat(HEARTBEAT_TIMEOUT_MS)
                    if (hb.command == Rsp.HEARTBEAT_ADV2) {
                        val st = PrinterStatus.parse(hb.data)
                        _status.value = st
                        // Fast path: the printer reports a freshly read chip right after insertion.
                        if (st?.rfidOk == true && lastRfidOk == false) refreshRollFrom(c)
                        lastRfidOk = st?.rfidOk
                    }
                    failures = 0
                } catch (e: Exception) {
                    if (++failures < HEARTBEAT_FAILURES_TO_DISCONNECT) {
                        log("Heartbeat failed: ${e.message}")
                    } else {
                        log("Printer not responding, disconnecting: ${e.message}")
                        // One retry at 1 s intervals is enough: the UI must not keep a dead connection.
                        closeClient()
                        break
                    }
                }
            }
        }
    }

    suspend fun refreshInfo(): PrinterInfo? {
        val c = client ?: return null
        return try {
            val hb = c.heartbeat()
            log("Heartbeat %02X: %s".format(hb.command, hb.data.joinToString(" ") { "%02X".format(it) }))
            if (hb.command == Rsp.HEARTBEAT_ADV2) {
                _status.value = PrinterStatus.parse(hb.data)
            }
            val info = c.negotiate()
            (_state.value as? ConnState.Connected)?.let { _state.value = it.copy(info = info) }
            refreshRollFrom(c)
            info
        } catch (e: Exception) {
            log("Status failed: ${e.message}")
            null
        }
    }

    /** Attaches catalogue data to the loaded roll; cleared when a different SKU appears. */
    fun applyCatalog(catalog: CloudRoll?) {
        _catalog.value = catalog
        log("Catalog applied: ${catalog?.name ?: "cleared"}")
    }

    /**
     * With automatic reading enabled, loads the catalogue once per SKU of the detected
     * roll. No serial number, UUID or device id is sent — only the SKU.
     */
    private fun startCatalogWatch() {
        catalogWatchJob?.cancel()
        catalogWatchJob = scope.launch {
            combine(Store.settings, _roll) { settings, roll -> settings.autoCatalog to roll?.barcode }
                .collect { (enabled, sku) ->
                    if (!enabled || sku.isNullOrEmpty()) {
                        lastAutoCatalogSku = null
                        return@collect
                    }
                    if (sku == lastAutoCatalogSku) return@collect
                    lastAutoCatalogSku = sku
                    var attempt = 0
                    while (attempt < 2 && isActive) {
                        attempt++
                        try {
                            val c = RollCatalog.fetch(sku)
                            // The roll or the switch may have changed while the request ran.
                            if (_roll.value?.barcode != sku || !Store.settings.value.autoCatalog) {
                                log("Catalog auto discarded for $sku: roll or setting changed")
                                break
                            }
                            applyCatalog(c)
                            log(
                                "Catalog auto SKU $sku: name=${c.name}, color=${c.color}, " +
                                    "cable=${c.cable}, length=${c.cableLength}",
                            )
                            break
                        } catch (e: Exception) {
                            if (attempt == 2) {
                                // Allow a retry when this SKU is seen again after a failure.
                                if (lastAutoCatalogSku == sku) lastAutoCatalogSku = null
                                log("Catalog auto error for $sku: ${e.message}")
                            } else {
                                delay(1_000)
                            }
                        }
                    }
                }
        }
    }

    /** Manual reread of the roll; keeps the last known value when the tag cannot be read. */
    suspend fun refreshRoll(): LabelRoll? {
        val c = client ?: return null
        return refreshRollFrom(c)
    }

    private suspend fun refreshRollFrom(c: NiimbotClient): LabelRoll? = applyRoll(c.getRoll())

    private fun applyRoll(roll: LabelRoll?): LabelRoll? {
        if (roll == null) {
            log("Label roll: not readable (last value kept)")
            return null
        }
        val previous = _roll.value
        if (previous != null && previous.uuid != roll.uuid) {
            log("Roll changed: tag UUID differs")
        }
        if (previous?.barcode != roll.barcode && _catalog.value != null) {
            _catalog.value = null
        }
        _roll.value = roll
        log("Label roll: $roll")
        return roll
    }

    /** Polls the chip while connected so a newly inserted roll appears without a button press. */
    private fun startRollWatch(c: NiimbotClient) {
        rollWatchJob?.cancel()
        rollWatchJob = scope.launch {
            while (isActive) {
                delay(ROLL_POLL_INTERVAL_MS)
                if (client !== c || _printing.value != null) continue
                try {
                    val previous = _roll.value
                    val roll = c.getRoll()
                    if (roll != null && (roll.uuid != previous?.uuid || roll.used != previous.used)) {
                        if (roll.uuid != previous?.uuid) log("Roll auto-detected (new tag)")
                        else log("Remaining auto-updated: ${roll.remaining}/${roll.total}")
                        applyRoll(roll)
                    }
                } catch (e: Exception) {
                    log("Roll watch failed: ${e.message}")
                }
            }
        }
    }

    fun print(pages: List<EncodedImage>, opts: PrintOptions, onDone: (Throwable?) -> Unit = {}) {
        val c = client ?: return onDone(IllegalStateException("Not connected"))
        if (printJob?.isActive == true) return onDone(IllegalStateException("A print job is already running"))
        c.packetIntervalMs = Store.settings.value.packetDelayMs.toLong()
        val expected = pages.size * opts.copies
        var rollBefore: LabelRoll? = null
        _printing.value = PrintProgress(0, expected, PrintStage.STARTING, 0f)
        printJob = scope.launch {
            var error: Throwable? = null
            try {
                rollBefore = withContext(NonCancellable) { refreshRollFrom(c) } ?: _roll.value
                PrintJob(c, ::log) { _printing.value = it }.print(pages, opts)
                log("Print finished")
            } catch (e: Throwable) {
                error = e
                log("PRINT FAILED: ${e.message}")
            } finally {
                _printing.value = null
                withContext(NonCancellable) {
                    val rollAfter = refreshRollFrom(c)
                    val before = rollBefore
                    if (before != null && rollAfter != null) {
                        val delta = rollAfter.used - before.used
                        if (delta > 0) {
                            log("Chip counter +$delta (expected $expected), remaining ${rollAfter.remaining}")
                        } else {
                            log("Chip counter unchanged after print (expected $expected): no RFID write detected")
                        }
                    }
                }
                onDone(error)
            }
        }
    }

    fun cancelPrint() {
        val job = printJob?.takeIf { it.isActive } ?: return
        _printing.value = _printing.value?.copy(stage = PrintStage.CANCELLED)
        scope.launch {
            // Tell the printer first: cancelling the coroutine alone leaves it printing.
            client?.let { c ->
                try {
                    c.cancelPrint()
                } catch (e: Exception) {
                    log("Cancel command failed: ${e.message}")
                }
            }
            job.cancel()
        }
    }
}

/** Classic discovery + BLE scan, reporting every device once with merged info. */
@SuppressLint("MissingPermission")
class BluetoothScanner(private val ctx: Context, private val onUpdate: (List<FoundDevice>) -> Unit) {
    data class FoundDevice(val address: String, val name: String, val bonded: Boolean, val ble: Boolean) {
        val isB18 get() = name.uppercase().startsWith("B18")
    }

    private val adapter = PrinterManager.adapter(ctx)
    private val found = linkedMapOf<String, FoundDevice>()
    private var registered = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != BluetoothDevice.ACTION_FOUND) return
            val d: BluetoothDevice = (if (Build.VERSION.SDK_INT >= 33) {
                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
            } else {
                @Suppress("DEPRECATION") intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            }) ?: return
            add(d, intent.getStringExtra(BluetoothDevice.EXTRA_NAME), false)
        }
    }

    private val bleCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            add(result.device, result.scanRecord?.deviceName, true)
        }
    }

    private fun add(d: BluetoothDevice, name: String?, ble: Boolean) {
        val prev = found[d.address]
        val n = name ?: d.name ?: prev?.name ?: d.address
        found[d.address] = FoundDevice(d.address, n, d.bondState == BluetoothDevice.BOND_BONDED, ble || prev?.ble == true)
        onUpdate(found.values.sortedWith(compareByDescending<FoundDevice> { it.isB18 }.thenByDescending { it.bonded }))
    }

    fun start() {
        val ad = adapter ?: return
        ad.bondedDevices.orEmpty().forEach { add(it, it.name, false) }
        ctx.registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        registered = true
        ad.cancelDiscovery()
        ad.startDiscovery()
        try {
            ad.bluetoothLeScanner?.startScan(bleCallback)
        } catch (e: Exception) {
            PrinterManager.log("BLE scan not started: ${e.message}")
        }
    }

    fun stop() {
        try {
            adapter?.cancelDiscovery()
            adapter?.bluetoothLeScanner?.stopScan(bleCallback)
        } catch (_: Exception) {}
        if (registered) {
            try { ctx.unregisterReceiver(receiver) } catch (_: Exception) {}
            registered = false
        }
    }
}

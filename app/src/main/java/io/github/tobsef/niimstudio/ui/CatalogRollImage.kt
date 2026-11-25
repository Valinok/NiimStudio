package io.github.tobsef.niimstudio.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Preview loaded from the catalogue URL at runtime. No vendor PNG is bundled in the app;
 * when the URL is missing or the download fails, the caller's vector art is shown.
 */
@Composable
fun CatalogRollImage(
    url: String?,
    modifier: Modifier = Modifier,
    fallback: @Composable () -> Unit,
) {
    var bitmap by remember(url) { mutableStateOf(url?.let { CatalogImageCache.cached(it) }) }
    LaunchedEffect(url) {
        if (url.isNullOrEmpty()) {
            bitmap = null
        } else {
            // Cached previews appear immediately, without a blank frame.
            bitmap = CatalogImageCache.cached(url) ?: withContext(Dispatchers.IO) { CatalogImageCache.load(url) }
        }
    }
    val loaded = bitmap
    if (loaded != null) {
        Image(
            bitmap = loaded.asImageBitmap(),
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Fit,
        )
    } else {
        fallback()
    }
}

private object CatalogImageCache {
    private const val TIMEOUT_MS = 8_000
    private const val MAX_BYTES = 2 * 1024 * 1024
    private const val TARGET_SIZE = 160
    private val cache = LruCache<String, Bitmap>(4)

    fun cached(url: String): Bitmap? = synchronized(cache) { cache.get(url) }

    suspend fun load(url: String): Bitmap? = withContext(Dispatchers.IO) {
        cached(url)?.let { return@withContext it }
        // Catalogue URLs are CDN links; only HTTPS is accepted and no identifier is added.
        if (!url.startsWith("https://")) return@withContext null
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = TIMEOUT_MS
        connection.readTimeout = TIMEOUT_MS
        try {
            val bytes = ByteArrayOutputStream()
            connection.inputStream.use { input ->
                val buffer = ByteArray(8 * 1024)
                while (true) {
                    val n = input.read(buffer)
                    if (n < 0) break
                    if (bytes.size() + n > MAX_BYTES) return@withContext null
                    bytes.write(buffer, 0, n)
                }
            }
            if (!isActive) return@withContext null
            val data = bytes.toByteArray()
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(data, 0, data.size, bounds)
            var sample = 1
            while (bounds.outWidth / sample > TARGET_SIZE || bounds.outHeight / sample > TARGET_SIZE) {
                sample *= 2
            }
            val bitmap = BitmapFactory.decodeByteArray(
                data, 0, data.size, BitmapFactory.Options().apply { inSampleSize = sample },
            ) ?: return@withContext null
            synchronized(cache) { cache.put(url, bitmap) }
            bitmap
        } catch (e: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }
}

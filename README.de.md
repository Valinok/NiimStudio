<p align="right">
  <a href="README.md"><img src="docs/media/flags/gb.svg" height="14" alt=""> English</a>
  &nbsp;·&nbsp;
  <img src="docs/media/flags/de.svg" height="14" alt=""> <b>Deutsch</b>
  &nbsp;·&nbsp;
  <a href="README.ru.md"><img src="docs/media/flags/ru.svg" height="14" alt=""> Русский</a>
</p>

> **Korrektur: Das ist kompletter KI-Slop („нейрослоп“), kein sorgfältig geprüfter Fork.**

> **Fork-Hinweis.** Dies ist ein experimenteller Fork von [TobseF/NiimStudio](https://github.com/TobseF/NiimStudio)
> mit russischer Oberfläche, überarbeiteter unterer Editorleiste und Drucker-Korrekturen. Teile der Änderungen
> wurden mit KI-Unterstützung erstellt („нейрослоп“) und nicht so gründlich geprüft wie das Original —
> Verwendung auf eigenes Risiko. Proprietäre Microsoft-Schriften und vom Original abgemalte Icons sind
> hier **nicht** enthalten: es werden Systemschriften und eingebaute Material-Icons verwendet.
> Releases erscheinen in diesem Fork, nicht im upstream.

<p align="center">
  <a href="https://github.com/Valinok/NiimStudio/releases/latest"><img src="https://img.shields.io/github/v/release/Valinok/NiimStudio?style=flat-square&color=F5413D&label=release" alt="Neueste Version"></a>
  <img src="https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Android 8.0+">
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-GPL--3.0-blue?style=flat-square" alt="Lizenz: GPL-3.0"></a>
  <img src="https://img.shields.io/badge/internet-not%20required-555?style=flat-square" alt="Keine Internet-Berechtigung">
  <a href="https://valinok.github.io/NiimStudio/fdroid/repo/"><img src="https://img.shields.io/badge/Droid--ify-add%20repo-3DDC84?style=flat-square" alt="F-Droid-Repository hinzufügen"></a>
</p>

## Was sich in diesem Fork geändert hat

- **Russische Oberfläche** (Englisch und Deutsch bleiben verfügbar); diese README gibt es in allen drei Sprachen.
- **Stabile untere Leiste** — Tabs und Eingabefeld springen bei geöffneter Tastatur nicht mehr; das Etikett bleibt oben auf dem Canvas.
- **Druckerstatus auf einen Blick** — Modell, Akkustand (4 Balken) und Bluetooth-Status in einem antippbaren Kopfbereich; ein Tap zeigt Firmware und Verbindungsdetails.
- **NIIMBOT D110_M** funktioniert mit der Legacy/D110-Variante (203 dpi, Dichte 1–5).
- **Keine proprietären Assets** — keine Microsoft-TTF-Schriften und keine nachgezeichneten SVG-Icons; Systemschriften und Apache-2.0-Material-Icons als Fallback.
- **Editor-/Drucker-Korrekturen** — gedrosselte Entwürfe und Vorschau, Rückgängig/Wiederholen, Abbrechen des Drucks, Aufräumen nach dem Druck, Bluetooth-Heartbeat und Trennung.
- Komplett offline: kein Konto, keine Cloud und nicht einmal eine Internet-Berechtigung.

## Screenshots

| | | | | |
|---|---|---|---|---|
| <img src="fastlane/metadata/android/de-DE/images/phoneScreenshots/1.png" width="160" alt=""><br><sub>Alle Elemente</sub> | <img src="fastlane/metadata/android/de-DE/images/phoneScreenshots/2.png" width="160" alt=""><br><sub>Symbolbibliothek</sub> | <img src="fastlane/metadata/android/de-DE/images/phoneScreenshots/3.png" width="160" alt=""><br><sub>Stil-Leiste</sub> | <img src="fastlane/metadata/android/de-DE/images/phoneScreenshots/4.png" width="160" alt=""><br><sub>Druckerstatus / Akku</sub> | <img src="fastlane/metadata/android/de-DE/images/phoneScreenshots/5.png" width="160" alt=""><br><sub>Tastatur geöffnet</sub> |

## Download & Installation

NiimStudio braucht **Android 8.0+** und Bluetooth.

- **GitHub Releases:** `NiimStudio-<Version>-release.apk` aus dem [neuesten Release](https://github.com/Valinok/NiimStudio/releases/latest) laden und öffnen.
- **Droid-ify (oder ein anderer F-Droid-Client):** [Repository mit einem Tap hinzufügen](fdroidrepos://valinok.github.io/NiimStudio/fdroid/repo/?fingerprint=F04731C891EBFB25CD323255D0E873C950CCFF3386A46C069B8D02D38C12F733). Dort gibt es dieselbe signierte APK mit automatischen Updates. Manuell: URL `https://valinok.github.io/NiimStudio/fdroid/repo/`, Fingerprint `F04731C8 91EBFB25 CD323255 D0E873C9 50CCFF33 86A46C06 9B8D02D3 8C12F733`.

Dieser Fork nutzt einen eigenen Signaturschlüssel. Eine bereits installierte Version aus dem upstream oder einer anderen Quelle muss vorher deinstalliert werden.

## Unterstützte Drucker

| Drucker | Status |
|---|---|
| **NIIMBOT B18** | ✅ im upstream getestet |
| **NIIMBOT D110_M** | ✅ in diesem Fork getestet (Variante Legacy/D110, 203 dpi, Dichte 1–5) |

## Danksagung

Öffentliche Repositories, die bei den Änderungen in diesem Fork verwendet wurden:

- [TobseF/NiimStudio](https://github.com/TobseF/NiimStudio) — Upstream-Projekt, auf dem dieser Fork basiert.
- [KunalBhargava182/OpenNiimbot-NiimBridge](https://github.com/KunalBhargava182/OpenNiimbot-NiimBridge) — MIT-Treiber und D110M_V4-Drucksequenz als Protokollreferenz für die Drucker-Korrekturen.
- [MultiMote/niimbluelib](https://github.com/MultiMote/niimbluelib) — Druckerprotokoll (MIT).
- [ZXing](https://github.com/zxing/zxing) — Barcodes und QR-Codes.
- [BigBadaboom/androidsvg](https://github.com/BigBadaboom/androidsvg) — SVG-Darstellung.
- [google/material-design-icons](https://github.com/google/material-design-icons) — Material-Symbole und -Icons (Apache 2.0).

Alle Lizenzen stehen in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

## Lizenz

GPL-3.0 — siehe [LICENSE](LICENSE). NiimStudio ist ein unabhängiges Projekt und steht in keiner Verbindung zu NIIMBOT.

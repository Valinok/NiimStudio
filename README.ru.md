<p align="right">
  <a href="README.md"><img src="docs/media/flags/gb.svg" height="14" alt=""> English</a>
  &nbsp;·&nbsp;
  <a href="README.de.md"><img src="docs/media/flags/de.svg" height="14" alt=""> Deutsch</a>
  &nbsp;·&nbsp;
  <img src="docs/media/flags/ru.svg" height="14" alt=""> <b>Русский</b>
</p>

> **О форке.** Это экспериментальный форк [TobseF/NiimStudio](https://github.com/TobseF/NiimStudio):
> русский интерфейс, переработанная нижняя панель редактора и исправления в драйвере принтера.
> Часть изменений сделана с помощью ИИ («нейрослоп») и прошла не такое тщательное ревью, как
> оригинал — используйте на свой страх и риск. Проприетарные шрифты Microsoft и иконки,
> обведённые с родного приложения, **не входят** в репозиторий: используются системные шрифты
> и встроенные иконки Material. Релизы публикуются в этом форке, а не в upstream.

<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="docs/media/banner-dark.svg">
    <img src="docs/media/banner-light.svg" width="100%" alt="NiimStudio — создавайте и печатайте этикетки на принтере NIIMBOT">
  </picture>
</p>

<p align="center">
  <a href="https://github.com/Valinok/NiimStudio/releases/latest"><img src="https://img.shields.io/github/v/release/Valinok/NiimStudio?style=flat-square&color=F5413D&label=release" alt="Последний релиз"></a>
  <img src="https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Android 8.0+">
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-GPL--3.0-blue?style=flat-square" alt="Лицензия: GPL-3.0"></a>
  <img src="https://img.shields.io/badge/internet-not%20required-555?style=flat-square" alt="Без доступа в интернет">
</p>

<p align="center">
  <b>Свободный редактор этикеток для принтеров NIIMBOT.</b><br>
  Создавайте кабельные бирки, этикетки для хранения и ценники прямо на телефоне и печатайте по Bluetooth —<br>
  без аккаунта, без облака, без рекламы.
</p>

<p align="center">
  <a href="https://github.com/Valinok/NiimStudio/releases/latest"><img src="https://raw.githubusercontent.com/Kunzisoft/Github-badge/main/get-it-on-github.png" height="60" alt="Скачать с GitHub"></a>
</p>

## <img src="docs/media/icons/features.svg" width="26" align="top" alt=""> Возможности

- **Что видишь, то и печатаешь** — перетаскивание, масштабирование, направляющие, отмена/повтор и 1-битный предпросмотр, точно соответствующий этикетке.
- **Всё для этикетки** — текст, изображения, более 200 символов, дата и срок годности, штрихкоды, QR-коды, серийные номера, фигуры и линии.
- **Точная компоновка** — выравнивание по этикетке, сдвиг кнопками и редактирование значений прямо на холсте с живым предпросмотром.
- **Кабельные бирки** — 17 символов разъёмов (USB, HDMI, RJ45, DC и др.) и бирки, печатаемые с двух сторон и оборачиваемые вокруг кабеля.
- **Профили и шаблоны** — редактируемые размеры этикеток и сохранённые макеты с предпросмотром.
- **Пакетная печать** — заполнение заполнителей из списка или CSV и автоматическая нумерация.
- **Приватность** — работает полностью офлайн, без аккаунта и даже без разрешения на интернет.
- **Состояние принтера на виду** — модель, заряд (4 полоски) и Bluetooth собраны в один нажимаемый блок сверху; по нажатию видны прошивка и детали подключения.
- **Ваш язык** — английский, немецкий и русский; добавить новый язык несложно.

## <img src="docs/media/icons/screens.svg" width="26" align="top" alt=""> Скриншоты

<table>
  <tr>
    <td align="center"><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" width="160" alt=""><br><sub>Все элементы</sub></td>
    <td align="center"><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" width="160" alt=""><br><sub>Библиотека символов</sub></td>
    <td align="center"><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" width="160" alt=""><br><sub>QR-коды</sub></td>
    <td align="center"><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/4.png" width="160" alt=""><br><sub>Направляющие</sub></td>
    <td align="center"><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/5.png" width="160" alt=""><br><sub>Предпросмотр печати</sub></td>
  </tr>
</table>

## <img src="docs/media/icons/download.svg" width="26" align="top" alt=""> Скачивание и установка

NiimStudio работает на **Android 8.0 и новее** и требует Bluetooth. Выберите удобный способ:

| | Как | Обновления |
|---|---|---|
| **GitHub Releases** | Скачайте `NiimStudio-<версия>-release.apk` из [последнего релиза](https://github.com/Valinok/NiimStudio/releases/latest) и откройте его. | вручную |
| **Komi Store** | Найдите **NiimStudio** в [Komi Store](https://github.com/komi-store/komi-store) (бывший GitHub Store). | автоматически |
| **F-Droid** | Скоро. | автоматически |

<details>
<summary><b>Установка через Komi Store</b></summary>

1. Установите [Komi Store](https://github.com/komi-store/komi-store) (бывший GitHub Store) из
   [F-Droid](https://f-droid.org/packages/zed.rainxch.githubstore/) или его релизов на GitHub.
2. Найдите **NiimStudio** (или откройте репозиторий `Valinok/NiimStudio`).
3. Нажмите **Install**. Komi Store будет следить за обновлениями.

</details>

<details>
<summary><b>F-Droid</b></summary>

<a href="#"><img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" height="60" alt="F-Droid (скоро)"></a>

NiimStudio подготовлен для F-Droid (тексты и графика для магазина лежат в `fastlane/`). Ссылка появится
здесь после публикации приложения.

</details>

## <img src="docs/media/icons/printer.svg" width="26" align="top" alt=""> Поддерживаемые принтеры

| Принтер | Статус |
|---|---|
| **NIIMBOT B18** | ✅ проверен в upstream |
| **NIIMBOT D110_M** | ✅ проверен в этом форке (вариант Legacy/D110, 203 dpi, плотность 1–5) |
| Другие модели NIIMBOT | вероятно работают при том же протоколе — не проверено, [пишите нам](https://github.com/Valinok/NiimStudio/issues) |

## <img src="docs/media/icons/install.svg" width="26" align="top" alt=""> Первая печать за минуту

1. Включите принтер и откройте NiimStudio. Разрешите доступ к Bluetooth.
2. Нажмите кнопку **Bluetooth** и выберите принтер (например, `B18-…`).
3. Задайте размер этикетки сверху (например, **12×40**), добавьте текст, символы или разъёмы.
4. Нажмите **Печать**.

---

## <img src="docs/media/icons/build.svg" width="26" align="top" alt=""> Разработка

Kotlin, Jetpack Compose и Material 3. Требуются JDK 17 и Android SDK.

```bash
./gradlew assembleDebug          # app/build/outputs/apk/debug/NiimStudio-<версия>-debug.apk
./gradlew testDebugUnitTest      # юнит-тесты (протокол, вёрстка, переводы)
```

| Папка | Содержимое |
|---|---|
| `model/`, `data/` | модель этикетки и сохранение данных |
| `render/` | один рендерер для редактора и принтера (8 точек/мм) |
| `printer/`, `protocol/`, `transport/` | подключение, протокол NIIMBOT, SPP/BLE — см. [заметки о протоколе](docs/protocol.md) |
| `ui/` | экраны на Jetpack Compose |

<details>
<summary><img src="docs/media/icons/language.svg" width="18" align="top" alt=""> <b>Добавить язык</b></summary>

1. Скопируйте `app/src/main/res/values/strings.xml` в `app/src/main/res/values-<код>/strings.xml` (например, `values-fr`).
2. Переведите строки. Сохраняйте заполнители `%1$s`, `%1$d` и `{N}`. Названия символов (`sym_…`)
   содержат после `|` список поисковых слов.
3. Запустите `./gradlew testDebugUnitTest`.

Этого достаточно: сборка сама найдёт новую папку и добавит язык в список. `TranslationsTest`
покажет отсутствующие ключи, несовпадающие заполнители и пропущенные формы числа или массивы.

</details>

<details>
<summary><img src="docs/media/icons/plug.svg" width="18" align="top" alt=""> <b>Изменить или добавить символы разъёмов</b></summary>

Символы разъёмов — обычные SVG-файлы в `app/src/main/assets/connectors/` (`usba.svg`, `hdmi.svg`, …).
Редактируйте их в любом векторном редакторе (например, Inkscape) и пересоберите приложение.

- Высота 100 единиц (`viewBox="0 0 <ширина> 100"`), чёрные контуры с `stroke-width="9"`, контакты —
  чёрные заливки. Пропорции задаются viewBox.
- Новый разъём — просто добавьте SVG. Имя файла служит ключом; название берётся из строки
  `conn_<имя файла>`, если она есть (переводимая), иначе из тега `<title>` в SVG.
- Не переименовывайте существующие файлы — сохранённые этикетки ссылаются на них по имени.

</details>

<details>
<summary><img src="docs/media/icons/download.svg" width="18" align="top" alt=""> <b>Публикация релиза</b></summary>

В этом форке релизы собираются и публикуются вручную из ветки `main`:
подписанный APK прикрепляется к тегу с версией. В upstream этим занимается GitHub Actions
([release.yml](.github/workflows/release.yml)), но для его работы нужны секреты репозитория,
которые сюда не переносятся.

1. Повысьте `versionCode` и `versionName` в `app/build.gradle.kts`.
2. Добавьте `fastlane/metadata/android/ru-RU/changelogs/<versionCode>.txt` (и `en-US`, `de-DE`).
3. Закоммитьте изменения, создайте тег с версией (например, `0.7.1`) и запушьте его.
4. Соберите `./gradlew assembleRelease` с локальным `keystore.properties` и загрузите APK в релиз.

Для локальной сборки release скопируйте `keystore.properties.example` в `keystore.properties`
и заполните пути/пароли; файл игнорируется git.

</details>

<details>
<summary><img src="docs/media/icons/film.svg" width="18" align="top" alt=""> <b>Обновить скриншоты для магазина и графику README</b></summary>

Скриншоты для магазина снимаются на телефоне и масштабируются до 720×1600. Графику README
(баннер, иконки, флаги) создаёт `python tools/readme/make_assets.py`.

</details>

## <img src="docs/media/icons/heart.svg" width="26" align="top" alt=""> Благодарности

Публичные репозитории, которые использовались при правках в этом форке:

- [TobseF/NiimStudio](https://github.com/TobseF/NiimStudio) — upstream-проект, на котором основан форк.
- [KunalBhargava182/OpenNiimbot-NiimBridge](https://github.com/KunalBhargava182/OpenNiimbot-NiimBridge) — MIT-драйвер и последовательность печати D110M_V4: основной эталон протокола при правках D110_M.
- [MultiMote/niimbluelib](https://github.com/MultiMote/niimbluelib) — протокол принтера (MIT).
- [ZXing](https://github.com/zxing/zxing) — штрихкоды и QR-коды.
- [BigBadaboom/androidsvg](https://github.com/BigBadaboom/androidsvg) — рендеринг SVG.
- [google/material-design-icons](https://github.com/google/material-design-icons) — символы и иконки Material (Apache 2.0).

Все лицензии перечислены в [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

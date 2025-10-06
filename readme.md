## RealSense Capture

Android приложение для записи и просмотра RGB + Depth с камер Intel RealSense.

### Текущий статус

* **Интерфейс Compose подключён.** `MainActivity` разворачивает `RealSenseCaptureApp`, где настроена навигация между экранами предпросмотра, галереи, деталей и настроек, а также прокидываются зависимости `SessionRepository` и `VoiceNoteController`. 【F:app/src/main/java/com/example/realsensecapture/MainActivity.kt†L46-L118】【F:ui/src/main/java/com/example/realsensecapture/ui/RealSenseCaptureApp.kt†L13-L60】
* **Предпросмотр и запись бурста доступны.** Экран предпросмотра отображает поток через `PreviewSurface`, управляет правами и позволяет запускать `SessionRepository.createSession()`, предварительно приостанавливая стрим через `NativeBridge.stopStreaming()` и возобновляя его по завершении. 【F:ui/src/main/java/com/example/realsensecapture/ui/PreviewScreen.kt†L33-L118】
* **Ширина предпросмотра выровнена с нативным буфером.** Вместо жёсткого 1280×480 `PreviewSurface` теперь отрисовывает фактический кадр 1488×480 (640 RGB + 848 Depth) и пропускает повреждённые буферы, поэтому глубина не обрезается. 【F:ui/src/main/java/com/example/realsensecapture/ui/PreviewSurface.kt†L33-L69】【F:rsnative/src/main/cpp/native-lib.cpp†L73-L116】
* **Перед записью проверяется свободное место (F‑8).** `PreviewScreen` читает порог из `SettingsRepository`, сверяет его с `SessionRepository.getAvailableSpaceBytes()` и блокирует бурст, если памяти < порога. Пустые каталоги теперь очищаются, если `captureBurst` вернул `false`. 【F:ui/src/main/java/com/example/realsensecapture/ui/PreviewScreen.kt†L36-L107】【F:ui/src/main/java/com/example/realsensecapture/data/SessionRepository.kt†L44-L77】【F:ui/src/main/java/com/example/realsensecapture/ui/SettingsRepository.kt†L13-L24】

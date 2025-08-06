## RealSense Capture

Android приложение для записи и просмотра RGB + Depth с камер Intel RealSense.

### Выполнено
- Изучено техническое задание `task.txt`.
- Создан каркас проекта на Kotlin + Jetpack Compose.
  - Настроены Gradle, NDK и зависимости.
  - Подготовлены пустые модули `app`, `ui` и `rsnative`.
  - Добавлен Compose-экран предпросмотра RGB + Depth (пока заглушки).

### План следующего шага
- Подключить RealSense SDK к native-модулю и вывести реальные кадры в `PreviewScreen`.

### Источник требований
См. файл [`task.txt`](./task.txt) с подробным описанием функционала и технологий.

### Загрузка Gradle Wrapper
В репозитории не хранится бинарный файл `gradle-wrapper.jar`. После клонирования выполните

```bash
brew install gradle   # или любой другой способ установить Gradle
gradle wrapper        # создаст gradle/wrapper/gradle-wrapper.jar
```

Это позволит использовать `./gradlew` без добавления бинарников в Git.

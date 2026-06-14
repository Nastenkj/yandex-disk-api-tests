# Yandex.Disk API — автотесты

Автоматизированные тесты REST API Яндекс.Диска на Java + JUnit 5.
Покрыты HTTP-методы **GET, PUT, POST, DELETE**, позитивные и негативные сценарии.

## Стек

- Java 17
- JUnit 5 (Jupiter)
- Встроенный `java.net.http.HttpClient` (без сторонних HTTP-библиотек)
- Jackson — разбор JSON-ответов
- Maven
- GitHub Actions — CI

## Структура

```
src/
  main/java/yandex/disk/YandexDiskClient.java     — клиент API
  test/java/yandex/disk/
    BaseApiTest.java        — базовый класс (токен, клиент, утилиты)
    DiskInfoTest.java       — информация о диске и список файлов
    FolderCrudTest.java     — CRUD папок (create/move/copy/delete)
    FileUploadTest.java     — загрузка файлов и публикация
    NegativeTest.java       — негативные сценарии (401/404/409)
.github/workflows/maven.yml — CI
pom.xml
```

## Что проверяетсяы

**Информация о диске (DiskInfoTest)**
- GET `/v1/disk` — 200, корректная квота (`used_space` ≤ `total_space`)
- GET `/resources/files` — 200, список с учётом `limit`

**Папки (FolderCrudTest)**
- PUT — создание 201, повтор 409
- POST `/move` — перемещение 201/202 + проверка нового пути
- POST `/copy` — копирование 201/202, оригинал остаётся
- DELETE — удаление 204/202 + проверка, что ресурс исчез (404)

**Файлы (FileUploadTest)**
- GET `/upload` — 200, наличие `href`
- Полный цикл загрузки: ссылка → PUT файла → файл существует
- PUT `/publish` и `/unpublish` — публикация и снятие, появление `public_url`

**Негативные сценарии (NegativeTest)**
- Невалидный токен — 401
- GET / DELETE несуществующего ресурса — 404
- Создание в несуществующем родителе — 409
- Копирование несуществующего источника — 404

## OAuth-токен

Тесты обращаются к реальному API, поэтому нужен OAuth-токен.

**Токен не должен быть от вашего личного аккаунта.** Используйте отдельный
тестовый аккаунт. Токен удобно получить через полигон Яндекс.Диска:
https://yandex.ru/dev/disk/poligon/

Токен передаётся одним из двух способов:

- переменная окружения `YANDEX_DISK_TOKEN`
- системное свойство `-Dyandex.token=...`

> **Важно:** если токен не задан, тесты **пропускаются** (skipped), а сборка
> остаётся зелёной. Чтобы тесты реально выполнились, токен обязателен — в выводе
> ищите строку `Tests run: N ... Skipped: 0`.

## Запуск

С переменной окружения (Linux/macOS):

```bash
export YANDEX_DISK_TOKEN=ваш_токен
mvn test
```

Windows (PowerShell):

```powershell
$env:YANDEX_DISK_TOKEN = "ваш_токен"
mvn test
```

Через системное свойство (кроссплатформенно):

```bash
mvn test -Dyandex.token=ваш_токен
```

## CI

При пуше в `main`/`master` GitHub Actions запускает `mvn test`.
Токен подставляется из секрета репозитория `YANDEX_DISK_TOKEN`
(Settings → Secrets and variables → Actions).

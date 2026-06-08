# Yandex.Disk API — автотесты
 
Автоматизированные тесты REST API Яндекс.Диска на Java + JUnit 5.
Покрыты HTTP-методы **GET, PUT, POST, DELETE**.
 
## Стек
 
- Java 17
- JUnit 5 (Jupiter)
- Встроенный `java.net.http.HttpClient` (без сторонних HTTP-библиотек)
- Jackson — разбор JSON-ответов
- Maven
## Структура
 
```
src/
  main/java/yandex/disk/YandexDiskClient.java   — клиент API (GET/PUT/POST/DELETE)
  test/java/yandex/disk/YandexDiskApiTests.java — тесты
.github/workflows/maven.yml                     — CI (GitHub Actions)
pom.xml
```
 
## Что проверяется
 
| Метод  | Эндпоинт                     | Проверка                                   |
|--------|------------------------------|--------------------------------------------|
| GET    | `/v1/disk`                   | 200, наличие квоты (`total_space`)         |
| PUT    | `/v1/disk/resources`         | создание папки 201, повтор — 409           |
| GET    | `/v1/disk/resources`         | несуществующий ресурс — 404                |
| POST   | `/v1/disk/resources/move`    | перемещение 201/202 + проверка нового пути |
| DELETE | `/v1/disk/resources`         | удаление 204/202 + проверка, что ресурс исчез |
 
## OAuth-токен
 
Тесты обращаются к реальному API, поэтому нужен OAuth-токен.
 
**Токен не должен быть от вашего личного аккаунта.** Используйте отдельный
тестовый аккаунт. Токен удобно получить через полигон Яндекс.Диска:
https://yandex.ru/dev/disk/poligon/
 
Токен передаётся одним из двух способов:
 
- переменная окружения `YANDEX_DISK_TOKEN`
- системное свойство `-Dyandex.token=...`
> **Важно:** если токен не задан, тесты **пропускаются** (skipped), а сборка
> остаётся зелёной. Это сделано намеренно, чтобы CI не падал из-за отсутствия
> секрета. Чтобы тесты реально выполнились, токен обязателен — в выводе ищите
> строку `Tests run: 5`, а не `Skipped`.
 
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
 
Либо через системное свойство (кроссплатформенно):
 
```bash
mvn test -Dyandex.token=ваш_токен
```
 
## CI
 
При пуше в `main`/`master` GitHub Actions запускает `mvn test`.
Токен подставляется из секрета репозитория `YANDEX_DISK_TOKEN`
(Settings → Secrets and variables → Actions).
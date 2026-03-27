# Explore With Me
## Модули
- **`stats/stats-dto`** — общие DTO для API статистики
- **`stats/stats-server`** — HTTP-сервис статистики (порт `9090`)
- **`stats/stats-client`** — HTTP-клиент (`RestTemplate`) для вызова stats-server
- **`ewm-main-service`** — основной сервис
## Сборка
```bash
mvn clean install -P check
```
## Docker
После `mvn package -pl stats/stats-server -am`:
```bash
docker compose build
docker compose up -d
```
После остановить контейнер
```bash
docker compose down
```
## Спецификации API
- `ewm-stats-service-spec.json` — сервис статистики
## Дополнительная функциональность (будет реализована на этапе 3)
Комментарии к событиям с модерацией
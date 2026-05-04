## Сервис для отзывов о ресторанах

Что запускается в Docker:

- `api-service` - HTTP API
- `data-service` - читает Kafka и работает с базой
- `postgres` - база данных
- `kafka` и `zookeeper` - брокер сообщений
- `kafka-init` - разово создает топик (чтобы сервисы стартовали стабильно)

## Структура

- `docker-compose.yml` - запуск всех контейнеров
- `.env` - переменные окружения (в репе есть `.env.example`)
- `db/init.sql` - таблицы и стартовые данные
- `api-service/` - API и отправка сообщений в Kafka
- `data-service/` - consumer, запись в Postgres, поиск и отчеты

## Запуск

Из папки `3`.

Если нет `.env`, можно взять пример:

```powershell
Copy-Item .env.example .env
# в .env заменить POSTGRES_PASSWORD
```

Дальше:

```powershell
docker compose up -d --build
```

API доступно с хоста:

- `http://localhost:8080`

Наружу проброшен только API (8080). PostgreSQL и Kafka доступны только внутри Docker-сети.

Остановка:

```powershell
docker compose down
```

Полный сброс (включая volume Postgres):

```powershell
docker compose down -v
```

## Примеры запросов

Добавить отзыв (сообщение уходит в Kafka):

```powershell
curl -X POST http://localhost:8080/api/reviews ^
  -H "Content-Type: application/json" ^
  -d "{\"restaurantName\":\"Pasta Corner\",\"city\":\"Saratov\",\"author\":\"Ivan\",\"rating\":5,\"comment\":\"Nice place\",\"visitedOn\":\"2026-05-01\"}"
```

Поиск:

```powershell
curl "http://localhost:8080/api/reviews/search?city=Saratov&min_rating=4&sort_by=created_at&direction=desc&limit=10"
```

Отчеты:

```powershell
curl "http://localhost:8080/api/reports/reviews-per-day?days=14"
curl "http://localhost:8080/api/reports/top-restaurants-by-avg-rating?limit=10&min_reviews=1"
curl "http://localhost:8080/api/reports/most-reviewed-restaurants?limit=10&days=30"
```
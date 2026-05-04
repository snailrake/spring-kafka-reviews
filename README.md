## Описание

Система состоит из 4 компонентов:

- `api-service` (Spring Boot): HTTP API наружу. Принимает данные и отправляет их в Kafka, а также проксирует поиск/отчеты в `data-service`.
- `data-service` (Spring Boot): читает сообщения из Kafka, сохраняет данные в PostgreSQL и строит отчеты.
- `postgres` (Database): хранит рестораны и отзывы.
- `kafka` + `zookeeper` (Broker): очередь сообщений.

Тема: отзывы о ресторанах.

## Структура

- `docker-compose.yml` - запуск всех компонентов
- `.env` - переменные окружения (в репе лежит `.env.example`)
- `db/init.sql` - схема БД (2 таблицы + FK) и стартовые данные
- `api-service/` - producer + proxy (Spring Boot + Gradle)
- `data-service/` - consumer + запросы к БД + отчеты (Spring Boot + Gradle)

## Запуск

Из папки `3`.

Если у вас нет `.env`, можно взять пример:

```powershell
Copy-Item .env.example .env`r`n# в .env поменяйте POSTGRES_PASSWORD на свой
```

Дальше запуск:

```powershell
docker compose up -d --build
```

API доступно с хоста:

- `http://localhost:8080`

Важно: наружу проброшен только API (8080). PostgreSQL/Kafka/ZooKeeper доступны только внутри Docker-сети.

Остановка:

```powershell
docker compose down
```

Полный сброс (включая volume Postgres):

```powershell
docker compose down -v
```

## API Service (наружу)

Добавить отзыв (сообщение уходит в Kafka):

```powershell
curl -X POST http://localhost:8080/api/reviews ^
  -H "Content-Type: application/json" ^
  -d "{\"restaurantName\":\"Pasta Corner\",\"city\":\"Saratov\",\"author\":\"Ivan\",\"rating\":5,\"comment\":\"Nice place\",\"visitedOn\":\"2026-05-01\"}"
```

Поиск (прокси в data-service):

```powershell
curl "http://localhost:8080/api/reviews/search?city=Saratov&min_rating=4&sort_by=created_at&direction=desc"
```

Отчеты (прокси в data-service):

```powershell
curl "http://localhost:8080/api/reports/reviews-per-day?days=14"
curl "http://localhost:8080/api/reports/top-restaurants-by-avg-rating?limit=10&min_reviews=2"
curl "http://localhost:8080/api/reports/most-reviewed-restaurants?limit=10&days=30"
```

## Data Service (внутри сети)

Data Service поднимает Kafka consumer и читает сообщения из топика. Каждое сообщение сохраняется в Postgres:

- `restaurants` (уникальность по `name + city`)
- `reviews` (FK на `restaurants`)

## Отчеты

Минимум 3 отчета:

- `reviews-per-day` - количество отзывов по дням
- `top-restaurants-by-avg-rating` - топ ресторанов по средней оценке (с фильтром `min_reviews`)
- `most-reviewed-restaurants` - топ ресторанов по количеству отзывов (опционально за последние N дней)
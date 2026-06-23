# News CRUD Service

Локальный CRUD-сервис новостей на Spring Boot + PostgreSQL.

## Стек
- Spring Boot 3.x
- Spring Data JPA
- PostgreSQL
- Bean Validation

## Запуск

1. Установите PostgreSQL, создайте базу данных:
```sql
CREATE DATABASE news_db;
CREATE USER news_user WITH PASSWORD 'ваш_пароль';
GRANT ALL PRIVILEGES ON DATABASE news_db TO news_user;
GRANT ALL PRIVILEGES ON SCHEMA public TO news_user;
```

2. Скопируйте `.env.example` → `.env`, укажите свои данные

3. В IntelliJ IDEA: Run → Edit Configurations → добавьте Environment variables из `.env`

4. Запустите: `mvn spring-boot:run` или через Run в IDE

## API
- `POST /api/news` — создать новость
- `GET /api/news` — список новостей
- `GET /api/news/{id}` — новость по id
- `PUT /api/news/{id}` — обновить новость
- `DELETE /api/news/{id}` — удалить новость

## Меры защиты (OWASP)
| Риск | Решение |
|---|---|
| SQL Injection | Spring Data JPA, без конкатенации строк |
| Mass Assignment | DTO вместо прямого биндинга Entity |
| Утечка данных в ошибках | @ControllerAdvice, общие сообщения без стектрейсов |
| Секреты в коде | Переменные окружения через .env (не коммитится) |
| Excessive Data Exposure | Только нужные поля через ResponseDto |
| Валидация входных данных | Bean Validation (@NotBlank, @Size, @PastOrPresent) |

## Тестирование
Коллекция запросов: `requests.http`
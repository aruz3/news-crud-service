# News CRUD Service — JWT Authorization (TASK-003)

Backend на Spring Boot + PostgreSQL с JWT-авторизацией для CRUD новостей.

## Технологии
- Java 17, Spring Boot 4.1
- Spring Security 6 + JWT (io.jsonwebtoken / jjwt 0.12.6)
- PostgreSQL
- BCrypt для хеширования паролей

## Запуск проекта

### 1. Подготовка базы данных
Создать базу и пользователя в PostgreSQL:
```sql
CREATE DATABASE news_db;
CREATE USER news_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE news_db TO news_user;
```

### 2. Переменные окружения
Скопировать `.env.example` в `.env` и заполнить реальными значениями:
Сгенерировать `JWT_SECRET` можно так (PowerShell):
```powershell
powershell -command "[Convert]::ToBase64String((1..48 | %{Get-Random -Maximum 256}))"
```

Задать переменные в текущей сессии терминала перед запуском:
```powershell
$env:DB_NAME="news_db"
$env:DB_USER="news_user"
$env:DB_PASSWORD="your_password"
$env:JWT_SECRET="сгенерированная_строка"
```

### 3. Запуск
Сервер поднимется на `http://localhost:8080`. Таблицы создаются автоматически (`ddl-auto: update`).

## Эндпоинты

### Аутентификация
| Метод | Путь | Описание | Доступ |
|---|---|---|---|
| POST | `/auth/register` | Регистрация (email, password, role: STUDENT/ADMIN) | Открыт |
| POST | `/auth/login` | Логин, возвращает accessToken + refreshToken | Открыт |
| POST | `/auth/refresh` | Обновление accessToken по refreshToken | Открыт |
| POST | `/auth/logout` | Инвалидация refreshToken | Открыт |

### Новости
| Метод | Путь | Доступ |
|---|---|---|
| GET | `/api/news`, `/api/news/{id}` | STUDENT, ADMIN |
| POST, PUT, DELETE | `/api/news`, `/api/news/{id}` | ADMIN только |

Все защищённые эндпоинты требуют заголовок `Authorization: Bearer <accessToken>`.

## Безопасность
- Пароли — BCrypt (cost factor 10)
- JWT secret только через переменную окружения, не в коде
- Алгоритм подписи — HS256, `alg: none` автоматически отклоняется
- Access token — 15 минут, Refresh token — 7 дней
- Refresh token хранится в БД как SHA-256 хеш, инвалидируется при logout
- Rate limiting на `/auth/login` — блокировка на 1 минуту после 5 неудачных попыток

## Тестирование
Примеры запросов — в файле `requests.http` (можно выполнять прямо в IntelliJ IDEA).
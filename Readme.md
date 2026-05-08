# Система голосования за ресторан

## Описание проекта

В рамках выпускной квалификационной работы учебного курса TopJava разработано REST API приложение для голосования за ресторан, где пользователи могут выбрать место для обеда.

В системе есть два типа пользователей: администратор и обычные пользователи. Администратор может добавлять рестораны и меню на текущий день. Меню состоит из 2-5 блюд, для каждого указывается название и цена. Меню обновляется каждый день.

Пользователи могут отдать один голос за ресторан, в котором они хотят пообедать сегодня. Если пользователь голосует повторно в тот же день до 11:00, его голос обновляется - считается что он передумал. Если голосование происходит после 11:00, изменить голос уже нельзя.

Каждый ресторан предоставляет новое меню каждый день. Администратор может обновлять меню на текущий день.

В приложении реализованы следующие возможности: просмотр ресторанов с сегодняшним меню, голосование, просмотр результатов голосования за сегодня, определение победителя, а также административные функции: управление ресторанами, меню, пользователями и статистика голосования за любые даты.

## Технологии

- Java 21
- Spring Boot 4.0.6
- Spring Data JPA / Hibernate
- Spring Security (Basic Authentication)
- H2 Database
- Maven
- Lombok
- SpringDoc OpenAPI (Swagger)

## Как запустить проект

Для запуска нужен JDK 21 и Maven.

Команды для запуска:

```bash
mvn clean compile
mvn test
mvn spring-boot:run
```


После запуска приложение будет доступно по адресу http://localhost:8080

## Тестовые аккаунты

Для тестирования можно использовать следующие учетные записи:

| Email | Пароль | Роль |
|-------|--------|------|
| admin@gmail.com | admin | ADMIN |
| user@yandex.ru | password | USER |
| guest@gmail.com | guest | USER |

Аутентификация Basic. Почти все запросы требуют авторизации, кроме регистрации и Swagger.

## API Endpoints

### Рестораны (доступно USER и ADMIN)

| Метод | URL | Описание |
|-------|-----|----------|
| GET | /api/restaurants | Список ресторанов с сегодняшним меню |
| GET | /api/restaurants/{id} | Ресторан по ID |
| GET | /api/restaurants/{id}/menu | Сегодняшнее меню ресторана |
| GET | /api/restaurants/{id}/menu-by-date | Меню по дате (параметр date) |

### Управление ресторанами (только ADMIN)

| Метод | URL | Описание |
|-------|-----|----------|
| POST | /api/admin/restaurants | Создать ресторан |
| PUT | /api/admin/restaurants/{id} | Обновить ресторан |
| DELETE | /api/admin/restaurants/{id} | Удалить ресторан |
| POST | /api/admin/restaurants/{id}/menu | Добавить меню |

### Голосование (доступно USER)

| Метод | URL | Описание |
|-------|-----|----------|
| POST | /api/votes?restaurantId={id} | Проголосовать или изменить голос |
| GET | /api/votes/results/today | Результаты голосования за сегодня |
| GET | /api/votes/results/winner | Победитель сегодняшнего голосования |

### Администрирование голосов (только ADMIN)

| Метод | URL | Описание |
|-------|-----|----------|
| GET | /api/admin/votes/results?date={date} | Результаты за конкретную дату |
| GET | /api/admin/votes/results/range?start={start}&end={end} | Результаты за период |
| GET | /api/admin/votes/stats | Общая статистика голосования |

### Пользователи (только ADMIN)

| Метод | URL | Описание |
|-------|-----|----------|
| GET | /api/admin/users | Список всех пользователей |
| GET | /api/admin/users/{id} | Пользователь по ID |
| GET | /api/admin/users/{id}/votes | История голосов пользователя |
| GET | /api/admin/users/by-email?email={email} | Поиск по email |
| POST | /api/admin/users | Создать пользователя |
| PUT | /api/admin/users/{id} | Обновить пользователя |
| PATCH | /api/admin/users/{id}?enabled={true/false} | Блокировка/разблокировка |
| DELETE | /api/admin/users/{id} | Удалить пользователя |

### Профиль пользователя

| Метод | URL | Доступ | Описание |
|-------|-----|--------|----------|
| GET | /api/profile | USER | Получить свой профиль |
| PUT | /api/profile | USER | Обновить свой профиль |
| DELETE | /api/profile | USER | Удалить свой профиль |
| POST | /api/profile | PUBLIC | Регистрация нового пользователя |

## Примеры curl команд


# 1. Получить список ресторанов
```bash
curl -X GET "http://localhost:8080/api/restaurants" -u user@yandex.ru:password
````
# 2. Проголосовать за ресторан
```bash
curl -X POST "http://localhost:8080/api/votes?restaurantId=1" -u user@yandex.ru:password
````
# 3. Получить результаты голосования
```bash
curl -X GET "http://localhost:8080/api/votes/results/today" -u user@yandex.ru:password
````
# 4. Создать ресторан (только ADMIN)
```bash
curl -X POST "http://localhost:8080/api/admin/restaurants" \
  -H "Content-Type: application/json" \
  -d '{"name":"Новый ресторан"}' \
  -u admin@gmail.com:admin
````
# 5. Добавить меню (только ADMIN)
```bash
curl -X POST "http://localhost:8080/api/admin/restaurants/1/menu" \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2026-05-09",
    "dishes": [
      {"name": "Ролл Филадельфия", "price": 550},
      {"name": "Ролл Калифорния", "price": 480}
    ]
  }' \
  -u admin@gmail.com:admin
````
# 6. Зарегистрировать нового пользователя
```bash
curl -X POST "http://localhost:8080/api/profile" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Новый пользователь",
    "email": "newuser@mail.ru",
    "password": "password"
  }'
````
# 7. Получить свой профиль
```bash
curl -X GET "http://localhost:8080/api/profile" -u user@yandex.ru:password

# 8. Получить статистику голосования (только ADMIN)
curl -X GET "http://localhost:8080/api/admin/votes/stats" -u admin@gmail.com:admin
```
# 8. Получить всех пользователей (только ADMIN)
```bash
curl -X GET "http://localhost:8080/api/admin/users" -u admin@gmail.com:admin
````
# 9. Заблокировать пользователя (только ADMIN)
```bash
curl -X PATCH "http://localhost:8080/api/admin/users/2?enabled=false" \
-H "Content-Type: application/json" \
-u admin@gmail.com:admin
````
# 10. Получить результаты за конкретную дату (только ADMIN)
```bash
curl -X GET "http://localhost:8080/api/admin/votes/results?date=2026-05-07" -u admin@gmail.com:admin
````
# 11. Получить историю голосов пользователя (только ADMIN)
```bash
curl -X GET "http://localhost:8080/api/admin/users/2/votes" -u admin@gmail.com:admin
````
# 12. Обновить свой профиль
```bash
curl -X PUT "http://localhost:8080/api/profile" \
-H "Content-Type: application/json" \
-d '{
"name": "Новое имя",
"email": "user@yandex.ru",
"password": "newpassword"
}' \
-u user@yandex.ru:password
````
# 13. Удалить ресторан (только ADMIN)
```bash
curl -X DELETE "http://localhost:8080/api/admin/restaurants/1" -u admin@gmail.com:admin
````
# 14. Обновить ресторан (только ADMIN)
```bash
curl -X PUT "http://localhost:8080/api/admin/restaurants/1" \
-H "Content-Type: application/json" \
-d '{"id":1,"name":"Обновленная Япошка"}' \
-u admin@gmail.com:admin
````
# 15. Получить победителя голосования
```bash
curl -X GET "http://localhost:8080/api/votes/results/winner" -u user@yandex.ru:password
````
## Swagger документация

После запуска приложения Swagger UI доступен по адресу:

```bash
http://localhost:8080/swagger-ui.html
````

## HTTP статусы ответов

| Статус | Описание |
|--------|----------|
| 200 | Успешный запрос (GET) |
| 201 | Ресурс успешно создан |
| 204 | Успешное удаление или обновление |
| 400 | Неверный запрос |
| 401 | Требуется аутентификация |
| 403 | Недостаточно прав |
| 404 | Ресурс не найден |
| 409 | Конфликт (голосование после дедлайна или дубликат) |
| 422 | Ошибка валидации |

## Заключение

Проект выполнен в рамках выпускной квалификационной работы.
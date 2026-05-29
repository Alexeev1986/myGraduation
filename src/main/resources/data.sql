DELETE FROM menu_item;
DELETE FROM menu;
DELETE FROM restaurant;
DELETE FROM vote;
DELETE FROM user_role;
DELETE FROM users;
DELETE FROM dish;

ALTER TABLE menu_item ALTER COLUMN id RESTART WITH 1;
ALTER TABLE menu ALTER COLUMN id RESTART WITH 1;
ALTER TABLE restaurant ALTER COLUMN id RESTART WITH 1;
ALTER TABLE vote ALTER COLUMN id RESTART WITH 1;
ALTER TABLE users ALTER COLUMN id RESTART WITH 1;
ALTER TABLE dish ALTER COLUMN id RESTART WITH 1;

INSERT INTO users (name, email, password, enabled, registered) VALUES
    ('Admin', 'admin@gmail.com', '{noop}admin', true, now()),
    ('User', 'user@yandex.ru', '{noop}password', true, now()),
    ('Voter', 'voter@mail.ru', '{noop}voter', true, now());


INSERT INTO user_role (user_id, role) VALUES
    (1, 'ADMIN'), (1, 'USER'),
    (2, 'USER'),
    (3, 'USER');


INSERT INTO restaurant (name) VALUES
    ('Япошка'),
    ('Итальянский дворик'),
    ('Узбекский плов');


INSERT INTO dish (name, price) VALUES
    ('Ролл Филадельфия', 550), ('Ролл Калифорния', 480), ('Суши лосось', 320),
    ('Мисо суп', 250), ('Гёдза', 380),
    ('Пицца Маргарита', 450), ('Пицца Пепперони', 520), ('Паста Карбонара', 480),
    ('Лазанья', 550), ('Тирамису', 320),
    ('Плов', 400), ('Самса', 250), ('Манты', 380), ('Шурпа', 350), ('Лагман', 420);


INSERT INTO menu (restaurant_id, date) VALUES
    -- TODAY (ID 1,2,3)
    (1, CURRENT_DATE), (2, CURRENT_DATE), (3, CURRENT_DATE),
    -- YESTERDAY (ID 4,5,6)
    (1, CURRENT_DATE - 1), (2, CURRENT_DATE - 1), (3, CURRENT_DATE - 1),
    -- TWO_DAYS_AGO (ID 7,8,9)
    (1, CURRENT_DATE - 2), (2, CURRENT_DATE - 2), (3, CURRENT_DATE - 2);


INSERT INTO menu_item (menu_id, dish_id) VALUES
    -- TODAY
    (1, 1), (1, 2), (1, 4), (1, 5),
    (2, 6), (2, 8), (2, 9), (2, 10),
    (3, 11), (3, 12), (3, 13), (3, 14),
    -- YESTERDAY
    (4, 1), (4, 2), (4, 4), (4, 5),
    (5, 6), (5, 8), (5, 9), (5, 10),
    (6, 11), (6, 12), (6, 13), (6, 14),
    -- TWO_DAYS_AGO
    (7, 1), (7, 2), (7, 4), (7, 5),
    (8, 6), (8, 8), (8, 9), (8, 10),
    (9, 11), (9, 12), (9, 13), (9, 14);

INSERT INTO vote (user_id, restaurant_id, vote_date, vote_time) VALUES
    (2, 2, CURRENT_DATE - 2, '10:15:00'),
    (1, 1, CURRENT_DATE - 2, '10:00:00'),
    (3, 1, CURRENT_DATE - 2, '09:45:00'),
    (2, 1, CURRENT_DATE - 1, '10:30:00'),
    (1, 3, CURRENT_DATE - 1, '09:15:00'),
    (3, 2, CURRENT_DATE - 1, '11:30:00'),
    (2, 1, CURRENT_DATE, '10:30:00'),
    (1, 2, CURRENT_DATE, '09:15:00');
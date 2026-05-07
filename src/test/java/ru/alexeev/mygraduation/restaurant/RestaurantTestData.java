package ru.alexeev.mygraduation.restaurant;

import ru.alexeev.mygraduation.MatcherFactory;
import ru.alexeev.mygraduation.restaurant.model.Dish;
import ru.alexeev.mygraduation.restaurant.model.Menu;
import ru.alexeev.mygraduation.restaurant.model.Restaurant;
import ru.alexeev.mygraduation.restaurant.to.MenuTo;
import ru.alexeev.mygraduation.restaurant.to.RestaurantTo;

import java.time.LocalDate;
import java.util.List;

import static ru.alexeev.mygraduation.restaurant.util.RestaurantUtil.newMenuTo;

public class RestaurantTestData {
    public static final MatcherFactory.Matcher<Restaurant> RESTAURANT_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(Restaurant.class, "menus", "votes");

    public static final MatcherFactory.Matcher<RestaurantTo> RESTAURANT_TO_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(RestaurantTo.class);

    public static final MatcherFactory.Matcher<MenuTo> MENU_TO_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(MenuTo.class);

    public static final MatcherFactory.Matcher<Dish> DISH_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(Dish.class, "id");

    public static final MatcherFactory.Matcher<Dish> DISH_WITH_ID_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(Dish.class);

    public static final LocalDate TOMORROW = LocalDate.now().plusDays(1);

    public static final int RESTAURANT1_ID = 1;
    public static final int RESTAURANT2_ID = 2;
    public static final int RESTAURANT3_ID = 3;
    public static final int NOT_FOUND = 100;

    public static final Restaurant restaurant1 = new Restaurant(RESTAURANT1_ID, "Япошка");
    public static final Restaurant restaurant2 = new Restaurant(RESTAURANT2_ID, "Итальянский дворик");
    public static final Restaurant restaurant3 = new Restaurant(RESTAURANT3_ID, "Узбекский плов");

    public static final Dish dish1 = new Dish(1, "Ролл Филадельфия", 550);
    public static final Dish dish2 = new Dish(2, "Ролл Калифорния", 480);
    public static final Dish dish3 = new Dish(3, "Суши лосось", 320);
    public static final Dish dish4 = new Dish(4, "Мисо суп", 250);
    public static final Dish dish5 = new Dish(5, "Гёдза", 380);
    public static final Dish dish6 = new Dish(6, "Пицца Маргарита", 450);
    public static final Dish dish7 = new Dish(7, "Пицца Пепперони", 520);
    public static final Dish dish8 = new Dish(8, "Паста Карбонара", 480);
    public static final Dish dish9 = new Dish(9, "Лазанья", 550);
    public static final Dish dish10 = new Dish(10, "Тирамису", 320);
    public static final Dish dish11 = new Dish(11, "Плов", 400);
    public static final Dish dish12 = new Dish(12, "Самса", 250);
    public static final Dish dish13 = new Dish(13, "Манты", 380);
    public static final Dish dish14 = new Dish(14, "Шурпа", 350);
    public static final Dish dish15 = new Dish(15, "Лагман", 420);

    public static final Menu menu1 = new Menu(1, restaurant1, LocalDate.now());
    public static final Menu menu2 = new Menu(2, restaurant2, LocalDate.now());
    public static final Menu menu3 = new Menu(3, restaurant3, LocalDate.now());

    static {
        menu1.setDishes(List.of(dish1, dish2, dish4, dish5));
        menu2.setDishes(List.of(dish6, dish8, dish9, dish10));
        menu3.setDishes(List.of(dish11, dish12, dish13, dish14));

        restaurant1.setMenus(List.of(menu1));
        restaurant2.setMenus(List.of(menu2));
        restaurant3.setMenus(List.of(menu3));
    }

    public static List<Restaurant> getAllRestaurants() {
        return List.of(restaurant2, restaurant3, restaurant1);
    }

    public static Restaurant getNew() {
        return new Restaurant(null, "Новый ресторан");
    }

    public static Restaurant getInvalidName() {
        return new Restaurant(null, "");
    }

    public static Restaurant getUpdated() {
        return new Restaurant(RESTAURANT1_ID, "Обновленная Япошка");
    }

    public static MenuTo getNewMenuToForRestaurant1() {
        return newMenuTo(TOMORROW, List.of(dish1, dish2, dish4, dish5));
    }

    public static MenuTo getUpdatedMenuToForRestaurant1() {
        return newMenuTo(TOMORROW, List.of(dish1, dish2, dish3, dish4, dish5));
    }

    public static MenuTo getMenuWithNullDate() {
        return newMenuTo(null, List.of(dish1, dish2));
    }

    public static MenuTo getMenuWithEmptyDishes() {
        return new MenuTo(null, TOMORROW, List.of());
    }
}

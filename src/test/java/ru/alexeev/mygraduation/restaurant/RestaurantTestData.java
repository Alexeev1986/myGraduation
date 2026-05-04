package ru.alexeev.mygraduation.restaurant;

import ru.alexeev.mygraduation.MatcherFactory;
import ru.alexeev.mygraduation.restaurant.model.Restaurant;

public class RestaurantTestData {
    public static final MatcherFactory.Matcher<Restaurant> RESTAURANT_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(Restaurant.class, "menus", "votes");

    public static final int RESTAURANT1_ID = 1;
    public static final int RESTAURANT2_ID = 2;
    public static final int RESTAURANT3_ID = 3;
    public static final int NOT_FOUND = 100;

    public static final Restaurant restaurant1 = new Restaurant(RESTAURANT1_ID, "Япошка");
    public static final Restaurant restaurant2 = new Restaurant(RESTAURANT2_ID, "Итальянский дворик");
    public static final Restaurant restaurant3 = new Restaurant(RESTAURANT3_ID, "Узбекский плов");

    public static Restaurant getNew() {
        return new Restaurant(null, "Новый ресторан");
    }

    public static Restaurant getUpdated() {
        return new Restaurant(RESTAURANT1_ID, "Обновленная Япошка");
    }
}

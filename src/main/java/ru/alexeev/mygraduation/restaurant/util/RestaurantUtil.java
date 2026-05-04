package ru.alexeev.mygraduation.restaurant.util;

import lombok.experimental.UtilityClass;
import ru.alexeev.mygraduation.restaurant.model.Menu;
import ru.alexeev.mygraduation.restaurant.model.Restaurant;
import ru.alexeev.mygraduation.restaurant.to.DishTo;
import ru.alexeev.mygraduation.restaurant.to.MenuTo;
import ru.alexeev.mygraduation.restaurant.to.RestaurantTo;
import java.time.LocalDate;
import java.util.List;

@UtilityClass
public class RestaurantUtil {
    public static RestaurantTo toRestaurantTo(Restaurant restaurant) {
        List<MenuTo> menuTo = restaurant.getMenus().stream()
                .filter(menu -> menu.getDate().equals(LocalDate.now()))
                .map(RestaurantUtil::toMenuTo)
                .toList();
        return new RestaurantTo(restaurant.getId(), restaurant.getName(), menuTo);
    }

    public static List<RestaurantTo> toRestaurantTosWithTodayMenu(List<Restaurant> restaurants) {
        return restaurants.stream()
                .map(RestaurantUtil::toRestaurantTo)
                .toList();
    }

    public static MenuTo toMenuTo(Menu menu) {
        List<DishTo> dishTos = menu.getDishes().stream()
                .map(dish -> new DishTo(dish.getId(), dish.getName(), dish.getPrice()))
                .toList();
        return new MenuTo(menu.getId(), menu.getDate(), dishTos);
    }
}

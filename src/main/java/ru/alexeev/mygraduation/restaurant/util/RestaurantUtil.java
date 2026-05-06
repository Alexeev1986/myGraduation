package ru.alexeev.mygraduation.restaurant.util;

import lombok.experimental.UtilityClass;
import ru.alexeev.mygraduation.restaurant.model.Dish;
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
        List<DishTo> dishTos = toDishTos(menu.getDishes());
        return new MenuTo(menu.getId(), menu.getDate(), dishTos);
    }

    public static Menu toMenu(MenuTo menuTo, Restaurant restaurant) {
        if (menuTo == null) return null;
        Menu menu = new Menu(menuTo.getId(), restaurant, menuTo.getDate());
        List<Dish> dishes = toDishes(menuTo.getDishes());
        menu.setDishes(dishes);
        return menu;
    }

    public static MenuTo newMenuTo(LocalDate date, List<Dish> dishes) {
        return new MenuTo(null, date, toDishTos(dishes));
    }

    public static DishTo toDishTo(Dish dish) {
        return new DishTo(dish.getId(), dish.getName(), dish.getPrice());
    }

    public static Dish toDish(DishTo dishTo) {
        return new Dish(dishTo.getId(), dishTo.getName(), dishTo.getPrice());
    }

    public static List<Dish> toDishes(List<DishTo> dishTos) {
        return dishTos.stream()
                .map(RestaurantUtil::toDish)
                .toList();
    }

    public static List<DishTo> toDishTos(List<Dish> dishes) {
        return dishes.stream()
                .map(RestaurantUtil::toDishTo)
                .toList();
    }
}

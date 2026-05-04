package ru.alexeev.mygraduation.restaurant.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.alexeev.mygraduation.restaurant.model.Menu;
import ru.alexeev.mygraduation.restaurant.model.Restaurant;
import ru.alexeev.mygraduation.restaurant.service.RestaurantService;
import ru.alexeev.mygraduation.restaurant.to.MenuTo;
import ru.alexeev.mygraduation.restaurant.to.RestaurantTo;

import java.time.LocalDate;
import java.util.List;

import static ru.alexeev.mygraduation.restaurant.util.RestaurantUtil.*;

@RestController
@RequestMapping(value = RestaurantController.REST_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
public class RestaurantController {
    static final String REST_URL = "/api/restaurants";

    private final RestaurantService restaurantService;

    @GetMapping
    public List<RestaurantTo> getAllWithTodayMenus() {
        log.info("getAll restaurants with today menus");
        List<Restaurant> restaurants = restaurantService.getAllWithTodayMenu();
        return toRestaurantTosWithTodayMenu(restaurants);
    }

    @GetMapping("/{id}")
    public RestaurantTo get(@PathVariable int id) {
        log.info("get restaurant {}", id);
        Restaurant restaurant = restaurantService.get(id);
        return toRestaurantTo(restaurant);
    }

    @GetMapping("/{id}/menu")
    public MenuTo getTodayMenu(@PathVariable int id) {
        log.info("get today menu for restaurant {}", id);
        Menu menu = restaurantService.getMenuByRestaurantAndDate(id, LocalDate.now());
        return toMenuTo(menu);
    }

    @GetMapping("/{id}/menu-by-date")
    public MenuTo getMenuByDate(@PathVariable int id,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("get menu for restaurant {} on {}", id, date);
        Menu menu = restaurantService.getMenuByRestaurantAndDate(id, date);
        return toMenuTo(menu);
    }
}

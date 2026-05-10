package ru.alexeev.mygraduation.restaurant.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alexeev.mygraduation.common.error.DataConflictException;
import ru.alexeev.mygraduation.restaurant.model.Dish;
import ru.alexeev.mygraduation.restaurant.model.Menu;
import ru.alexeev.mygraduation.restaurant.model.Restaurant;
import ru.alexeev.mygraduation.restaurant.repository.DishRepository;
import ru.alexeev.mygraduation.restaurant.repository.MenuRepository;
import ru.alexeev.mygraduation.restaurant.repository.RestaurantRepository;
import ru.alexeev.mygraduation.restaurant.to.MenuTo;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class RestaurantService {

    @Autowired
    private MenuValidator menuValidator;

    private final RestaurantRepository restaurantRepository;
    private final MenuRepository menuRepository;
    private final DishRepository dishRepository;

    public List<Restaurant> getAll() {
        log.info("getAll restaurants");
        return restaurantRepository.findAll();
    }

    public Restaurant get(int id) {
        log.info("get restaurant {}", id);
        return restaurantRepository.getExisted(id);
    }

    @Cacheable("restaurant_with_menu")
    public List<Restaurant> getAllWithTodayMenu() {
        log.info("getAllWithTodayMenu");
        return restaurantRepository.findAllWithTodayMenus();
    }

    @Transactional
    @CacheEvict(value = {"restaurant_with_menu", "menus_by_restaurant"}, allEntries = true)
    public Restaurant create(Restaurant restaurant) {
        log.info("create restaurant {}", restaurant);
        return restaurantRepository.save(restaurant);
    }

    @Transactional
    @CacheEvict(value = {"restaurant_with_menu", "menus_by_restaurant"}, allEntries = true)
    public void update(Restaurant restaurant, int id) {
        log.info("update restaurant {} with id={}", restaurant, id);
        Restaurant existing = restaurantRepository.getExisted(id);
        existing.setName(restaurant.getName());
    }

    @Transactional
    @CacheEvict(value = {"restaurant_with_menu", "menus_by_restaurant"}, allEntries = true)
    public void delete(int id) {
        log.info("delete restaurant {}", id);
        restaurantRepository.deleteExisted(id);
    }

    @CacheEvict(value = {"restaurant_with_menu", "menus_by_restaurant"}, allEntries = true)
    public void addMenu(int restaurantId, MenuTo menuTo) {
        log.info("add menu for restaurant {} on {}", restaurantId, menuTo.getDate());
        Restaurant restaurant = restaurantRepository.getExisted(restaurantId);
        menuValidator.validate(menuTo);

        LocalDate date = menuTo.getDate();
        menuRepository.getByRestaurantAndDate(restaurantId, date)
                .ifPresent(menu -> menuRepository.deleteExisted(menu.getId()));

        Menu menu = menuRepository.save(new Menu(null, restaurant, date));

        menuTo.getDishes().forEach(dishTo -> {
            Dish dish = dishRepository.findByNameIgnoreCase(dishTo.getName())
                    .orElseGet(() -> dishRepository.save(new Dish(null, dishTo.getName(), dishTo.getPrice())));
            menu.getDishes().add(dish);
        });
        menuRepository.save(menu);
    }

    @Cacheable(value = "menus_by_restaurant", key = "#restaurantId + '_' + #date.toString()")
    public Menu getMenuByRestaurantAndDate(int restaurantId, LocalDate date) {
        log.info("get menu for restaurant {} on {}", restaurantId, date);
        return menuRepository.getByRestaurantAndDate(restaurantId, date)
                .orElseThrow(() -> new DataConflictException("Menu not found for restaurant " + restaurantId + " on " + date));
    }
}

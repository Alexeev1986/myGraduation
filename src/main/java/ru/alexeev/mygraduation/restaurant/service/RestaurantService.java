package ru.alexeev.mygraduation.restaurant.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public List<Restaurant> getAllWithTodayMenu() {
        log.info("getAllWithTodayMenu");
        return restaurantRepository.findAllWithTodayMenus();
    }

    @Transactional
    public Restaurant create(Restaurant restaurant) {
        log.info("create restaurant {}", restaurant);
        return restaurantRepository.save(restaurant);
    }

    @Transactional
    public void update(Restaurant restaurant, int id) {
        log.info("update restaurant {} with id={}", restaurant, id);
        Restaurant existing = restaurantRepository.getExisted(id);
        existing.setName(restaurant.getName());
    }

    @Transactional
    public void delete(int id) {
        log.info("delete restaurant {}", id);
        restaurantRepository.deleteExisted(id);
    }

    public void addMenu(int restaurantId, MenuTo menuTo) {
        log.info("add menu for restaurant {} on {}", restaurantId, menuTo.getDate());
        Restaurant restaurant = restaurantRepository.getExisted(restaurantId);
        LocalDate date = menuTo.getDate();

        if (date.isBefore(LocalDate.now())) {
            throw new DataConflictException("Cannot add menu for past date");
        }

        long distinctCount = menuTo.getDishes().stream()
                .map(dishTo -> dishTo.getName().toLowerCase() + "|" + dishTo.getPrice())
                .distinct()
                .count();

        if (distinctCount != menuTo.getDishes().size()) {
            throw new DataConflictException("Menu cannot contain duplicate dishes");
        }
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

    public Menu getMenuByRestaurantAndDate(int restaurantId, LocalDate date) {
        log.info("get menu for restaurant {} on {}", restaurantId, date);
        return menuRepository.getByRestaurantAndDate(restaurantId, date)
                .orElseThrow(() -> new DataConflictException("Menu not found for restaurant " + restaurantId + " on " + date));
    }
}

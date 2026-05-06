package ru.alexeev.mygraduation.restaurant.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.alexeev.mygraduation.common.error.DataConflictException;
import ru.alexeev.mygraduation.common.error.NotFoundException;
import ru.alexeev.mygraduation.restaurant.model.Menu;
import ru.alexeev.mygraduation.restaurant.model.Restaurant;
import ru.alexeev.mygraduation.restaurant.to.DishTo;
import ru.alexeev.mygraduation.restaurant.to.MenuTo;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.alexeev.mygraduation.restaurant.RestaurantTestData.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class RestaurantServiceTest {

    @Autowired
    private RestaurantService restaurantService;

    @Test
    void getAll() {
        List<Restaurant> restaurants = restaurantService.getAll();
        assertThat(restaurants).hasSize(3);
        RESTAURANT_MATCHER.assertMatch(restaurants, getAllRestaurants());
    }

    @Test
    void get() {
        Restaurant restaurant = restaurantService.get(RESTAURANT1_ID);
        RESTAURANT_MATCHER.assertMatch(restaurant, restaurant1);
    }

    @Test
    void getNotFound() {
        assertThatThrownBy(() -> restaurantService.get(NOT_FOUND))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Entity with id=" + NOT_FOUND + " not found");
    }

    @Test
    void getAllWithTodayMenu() {
        List<Restaurant> restaurants = restaurantService.getAllWithTodayMenu();
        assertThat(restaurants).isNotNull();
        restaurants.forEach(restaurant -> assertThat(restaurant.getMenus()).isNotNull());
    }

    @Test
    void create() {
        Restaurant newRestaurant = getNew();
        Restaurant created = restaurantService.create(newRestaurant);
        int newId = created.id();
        newRestaurant.setId(newId);
        RESTAURANT_MATCHER.assertMatch(created, newRestaurant);
        RESTAURANT_MATCHER.assertMatch(restaurantService.get(newId), newRestaurant);
    }

    @Test
    void update() {
        Restaurant updated = getUpdated();
        restaurantService.update(updated, RESTAURANT1_ID);
        RESTAURANT_MATCHER.assertMatch(restaurantService.get(RESTAURANT1_ID), updated);
    }

    @Test
    void delete() {
        restaurantService.delete(RESTAURANT1_ID);
        assertThatThrownBy(() -> restaurantService.get(RESTAURANT1_ID))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteNotFound() {
        assertThatThrownBy(() -> restaurantService.delete(NOT_FOUND))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void addMenu() {
        List<DishTo> dishes = List.of(
                new DishTo(null, "Роллы", 500),
                new DishTo(null, "Суши", 500)
        );
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        MenuTo menuTo = new MenuTo(null, tomorrow, dishes);
        restaurantService.addMenu(RESTAURANT1_ID, menuTo);
        Menu menu = restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, tomorrow);
        assertThat(menu).isNotNull();
        assertThat(menu.getDishes()).hasSize(2);
        assertThat(menu.getDishes()).extracting("name").containsExactlyInAnyOrder("Роллы", "Суши");
    }

    @Test
    void addMenuForPastDate() {
        List<DishTo> dishes = List.of(new DishTo(null,"Роллы", 500));
        LocalDate yesterday = LocalDate.now().minusDays(1);
        MenuTo menuTo = new MenuTo(null, yesterday, dishes);

        assertThatThrownBy(() -> restaurantService.addMenu(RESTAURANT1_ID, menuTo))
                .isInstanceOf(DataConflictException.class)
                .hasMessageContaining("Cannot add menu for past date");
    }

    @Test
    void addMenuReplacesExisting() {
        List<DishTo> dishes1 = List.of(new DishTo(null, "Роллы", 500));
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        MenuTo menuTo1 = new MenuTo(null, tomorrow, dishes1);
        restaurantService.addMenu(RESTAURANT1_ID, menuTo1);

        List<DishTo> dishes2 = List.of(new DishTo(null, "Пицца", 600));
        MenuTo menuTo2 = new MenuTo(null, tomorrow, dishes2);
        restaurantService.addMenu(RESTAURANT1_ID, menuTo2);

        Menu menu = restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, tomorrow);
        assertThat(menu.getDishes()).hasSize(1);
        assertThat(menu.getDishes().getFirst().getName()).isEqualTo("Пицца");
    }

    @Test
    void getMenuByRestaurantAndDateNotFound() {
        LocalDate pastDate = LocalDate.of(2000, 1, 1);
        assertThatThrownBy(() -> restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, pastDate))
                .isInstanceOf(DataConflictException.class)
                .hasMessageContaining("Menu not found");
    }

    @Test
    void getMenuByRestaurantAndDate() {
        List<DishTo> dishes = List.of(
                new DishTo(null, "Роллы", 500),
                new DishTo(null, "Суши", 400)
        );

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        MenuTo menuTo = new MenuTo(null, tomorrow, dishes);
        restaurantService.addMenu(RESTAURANT1_ID, menuTo);

        Menu menu = restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, tomorrow);
        assertThat(menu).isNotNull();
        assertThat(menu.getRestaurant().getId()).isEqualTo(RESTAURANT1_ID);
        assertThat(menu.getDate()).isEqualTo(tomorrow);
        assertThat(menu.getDishes()).hasSize(2);
    }
}
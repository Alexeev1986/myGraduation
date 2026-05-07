package ru.alexeev.mygraduation.restaurant.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.alexeev.mygraduation.common.error.DataConflictException;
import ru.alexeev.mygraduation.common.error.IllegalRequestDataException;
import ru.alexeev.mygraduation.common.error.NotFoundException;
import ru.alexeev.mygraduation.restaurant.model.Dish;
import ru.alexeev.mygraduation.restaurant.model.Menu;
import ru.alexeev.mygraduation.restaurant.model.Restaurant;
import ru.alexeev.mygraduation.restaurant.to.DishTo;
import ru.alexeev.mygraduation.restaurant.to.MenuTo;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.alexeev.mygraduation.restaurant.RestaurantTestData.*;
import static ru.alexeev.mygraduation.restaurant.util.RestaurantUtil.newMenuTo;
import static ru.alexeev.mygraduation.restaurant.util.RestaurantUtil.toDishTos;

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
        MenuTo menuTo = new MenuTo(null, TOMORROW, dishes);
        restaurantService.addMenu(RESTAURANT1_ID, menuTo);
        Menu menu = restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, TOMORROW);
        assertThat(menu).isNotNull();
        assertThat(menu.getDishes()).hasSize(2);
        assertThat(menu.getDishes()).extracting("name").containsExactlyInAnyOrder("Роллы", "Суши");
    }


    @ParameterizedTest
    @CsvSource({
            "-2, false",
            "-1, false",
            "0, true",
            "1, true",
            "2, true",
    })
    void addMenuForDifferentDates(int daysOffset, boolean shouldSucceed) {
        LocalDate date = LocalDate.now().plusDays(daysOffset);
        List<Dish> dishes = List.of(dish1, dish2);
        MenuTo menuTo = newMenuTo(date, dishes);
        if (shouldSucceed) {
            restaurantService.addMenu(RESTAURANT1_ID, menuTo);
            Menu created = restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, date);
            assertThat(created).isNotNull();
            assertThat(created.getDate()).isEqualTo(date);
            DISH_MATCHER.assertMatch(created.getDishes(), dishes);
        } else {
            assertThatThrownBy(() -> restaurantService.addMenu(RESTAURANT1_ID, menuTo))
                    .isInstanceOf(DataConflictException.class)
                    .hasMessageContaining("Cannot add menu for past date");
        }
    }

    @ParameterizedTest
    @CsvSource({
            "0, false",
            "1, false",
            "2, true",
            "3, true",
            "4, true",
            "5, true",
            "6, false"
    })
    void addMenuWithDifferentDishCounts(int dishCount, boolean shouldSucceed) {
        List<Dish> dishes = new ArrayList<>();
        for (int i = 1; i <= dishCount; i++) {
            dishes.add(new Dish(null, "Новое блюдо № " + i, 100 + i));
        }

        MenuTo menuTo = newMenuTo(TOMORROW, dishes);

        if (shouldSucceed) {
            restaurantService.addMenu(RESTAURANT1_ID, menuTo);
            Menu created = restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, menuTo.getDate());
            DISH_MATCHER.assertMatch(created.getDishes(), dishes);
        } else {
            assertThatThrownBy(() -> restaurantService.addMenu(RESTAURANT1_ID, menuTo))
                    .isInstanceOf(IllegalRequestDataException.class)
                    .hasMessageContaining("Menu must contain between 2 and 5 dishes");
        }
    }

    @Test
    void addMenuReusesExistingDish() {
        List<DishTo> dishes1 = List.of(
                new DishTo(null, "Роллы", 500),
                new DishTo(null, "Суши", 500)
        );
        restaurantService.addMenu(RESTAURANT1_ID, new MenuTo(null, TOMORROW, dishes1));

        Menu firstMenu = restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, TOMORROW);
        List<Dish> expectedDishes = firstMenu.getDishes();

        List<DishTo> dishes2 = List.of(
                new DishTo(null, "Роллы", 500),
                new DishTo(null, "Суши", 500)
        );
        restaurantService.addMenu(RESTAURANT1_ID, new MenuTo(null, TOMORROW.plusDays(1), dishes2));

        Menu secondMenu = restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, TOMORROW.plusDays(1));
        List<Dish> actualDishes = secondMenu.getDishes();

        DISH_WITH_ID_MATCHER.assertMatch(actualDishes, expectedDishes);
    }

    @Test
    void addMenuWithDuplicateDishesShouldFail() {
        List<DishTo> duplicateDishes = toDishTos(List.of(dish7, dish14, dish15, dish7));
        MenuTo menuTo = new MenuTo(null, TOMORROW, duplicateDishes);

        assertThatThrownBy(() -> restaurantService.addMenu(RESTAURANT1_ID, menuTo))
                .isInstanceOf(DataConflictException.class)
                .hasMessageContaining("Menu cannot contain duplicate dishes");
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

        MenuTo menuTo = new MenuTo(null, TOMORROW, dishes);
        restaurantService.addMenu(RESTAURANT1_ID, menuTo);

        Menu menu = restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, TOMORROW);
        assertThat(menu).isNotNull();
        assertThat(menu.getRestaurant().getId()).isEqualTo(RESTAURANT1_ID);
        assertThat(menu.getDate()).isEqualTo(TOMORROW);
        assertThat(menu.getDishes()).hasSize(2);
    }
}
package ru.alexeev.mygraduation.restaurant.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import ru.alexeev.mygraduation.AbstractServiceTest;
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
import static ru.alexeev.mygraduation.restaurant.util.RestaurantUtil.*;

class RestaurantServiceTest extends AbstractServiceTest {

    @Autowired
    private RestaurantService restaurantService;

    @Test
    void getAll() {
        List<Restaurant> restaurants = restaurantService.getAll();
        assertThat(restaurants).hasSize(3);
        RESTAURANT_MATCHER.assertMatch(restaurants, getAllRestaurantsData());
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
    void createWithDuplicateName() {
        Restaurant duplicate = new Restaurant(null, restaurant1.getName());
        assertThatThrownBy(() -> restaurantService.create(duplicate))
                .isInstanceOf(DataConflictException.class);
    }

    @Test
    void update() {
        Restaurant updated = getUpdated();
        restaurantService.update(updated, RESTAURANT1_ID);
        RESTAURANT_MATCHER.assertMatch(restaurantService.get(RESTAURANT1_ID), updated);
    }

    @Test
    void updateWithDuplicateName() {
        Restaurant updated = new Restaurant(RESTAURANT1_ID, restaurant2.getName());
        assertThatThrownBy(() -> restaurantService.update(updated, RESTAURANT1_ID))
                .isInstanceOf(DataConflictException.class)
                .hasMessageContaining("Restaurant with name Итальянский дворик already exists");
    }

    @Test
    void updateWithSameName() {
        Restaurant updated = new Restaurant(RESTAURANT1_ID, restaurant1.getName());
        restaurantService.update(updated, RESTAURANT1_ID);

        Restaurant result = restaurantService.get(RESTAURANT1_ID);
        assertThat(result.getName()).isEqualTo(restaurant1.getName());
    }

    @Test
    void updateNotFound() {
        Restaurant updated = getUpdated();
        assertThatThrownBy(() -> restaurantService.update(updated, NOT_FOUND))
                .isInstanceOf(NotFoundException.class);
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
        List<DishTo> dishes = getNewTwoDishTo();
        MenuTo menuTo = new MenuTo(null, TOMORROW, dishes);
        restaurantService.addMenu(RESTAURANT1_ID, menuTo);
        Menu menu = restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, TOMORROW);
        assertThat(menu).isNotNull();
        assertThat(menu.getMenuItems()).hasSize(2);
        assertThat(menu.getMenuItems()).extracting(mi -> mi.getDish().getName()).containsExactlyInAnyOrder("Роллы", "Суши");
    }

    @Test
    void addMenuForRestaurantNotFound() {
        MenuTo menuTo = getNewMenuTo();
        assertThatThrownBy(() -> restaurantService.addMenu(NOT_FOUND, menuTo))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Entity with id=" + NOT_FOUND + " not found");
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
            List<Dish> actualItems = createdDishFromMenuItem(created.getMenuItems());
            DISH_MATCHER.assertMatch(actualItems, dishes);
        } else {
            assertThatThrownBy(() -> restaurantService.addMenu(RESTAURANT1_ID, menuTo))
                    .isInstanceOf(DataConflictException.class)
                    .hasMessageContaining("Cannot add menu for past date");
        }
    }

    @ParameterizedTest
    @CsvSource({
            "0, false",
            "1, true",
            "2, true",
            "10, true"
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
            List<Dish> actualDishes = createdDishFromMenuItem(created.getMenuItems());
            DISH_MATCHER.assertMatch(actualDishes, dishes);
        } else {
            assertThatThrownBy(() -> restaurantService.addMenu(RESTAURANT1_ID, menuTo))
                    .isInstanceOf(IllegalRequestDataException.class)
                    .hasMessageContaining("Menu must contain at least one dish");
        }
    }

    @Test
    void addMenuReusesExistingDish() {
        List<DishTo> dishes1 = getNewTwoDishTo();
        restaurantService.addMenu(RESTAURANT1_ID, new MenuTo(null, TOMORROW, dishes1));

        Menu firstMenu = restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, TOMORROW);
        List<Dish> expectedDishes = createdDishFromMenuItem(firstMenu.getMenuItems());

        List<DishTo> dishes2 = getNewTwoDishTo();
        restaurantService.addMenu(RESTAURANT1_ID, new MenuTo(null, TOMORROW.plusDays(1), dishes2));

        Menu secondMenu = restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, TOMORROW.plusDays(1));
        List<Dish> actualDishes = createdDishFromMenuItem(secondMenu.getMenuItems());

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
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Menu not found");
    }

    @Test
    void getMenuByRestaurantAndDate() {
        List<DishTo> dishes = getNewTwoDishTo();

        MenuTo menuTo = new MenuTo(null, TOMORROW, dishes);
        restaurantService.addMenu(RESTAURANT1_ID, menuTo);

        Menu menu = restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, TOMORROW);
        assertThat(menu).isNotNull();
        assertThat(menu.getRestaurant().getId()).isEqualTo(RESTAURANT1_ID);
        assertThat(menu.getDate()).isEqualTo(TOMORROW);
        assertThat(menu.getMenuItems()).hasSize(2);
    }

    @Test
    void updateMenu() {
        Menu beforeUpdate = restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, TODAY);
        assertThat(beforeUpdate).isNotNull();
        assertThat(beforeUpdate.getMenuItems()).hasSize(4);

        List<DishTo> updatedDishes = getUpdatedDishesTo();

        MenuTo updatedMenuTo = new MenuTo(beforeUpdate.getId(), TODAY, updatedDishes);
        restaurantService.updateMenu(RESTAURANT1_ID, beforeUpdate.id(), updatedMenuTo);

        Menu afterUpdate = restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, TODAY);
        assertThat(afterUpdate.getMenuItems()).hasSize(3);

        List<Dish> actualDishes = createdDishFromMenuItem(afterUpdate.getMenuItems());
        List<Dish> expectedDishes = List.of(dish6, dish10, dish13);
        DISH_MATCHER.assertMatch(actualDishes, expectedDishes);
    }

    @Test
    void updateMenuForMenuNotFound() {
        List<DishTo> dishes = getNewTwoDishTo();
        MenuTo menuTo = new MenuTo(NOT_FOUND, TODAY, dishes);
        assertThatThrownBy(() -> restaurantService.updateMenu(RESTAURANT1_ID, NOT_FOUND, menuTo))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Entity with id=" + NOT_FOUND + " not found");
    }

    @Test
    void updateMenuForWrongRestaurant() {
        Menu menu = restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, TODAY);
        List<DishTo> dishes = getNewDishTo();
        MenuTo menuTo = new MenuTo(menu.id(), TODAY, dishes);
        assertThatThrownBy(() -> restaurantService.updateMenu(RESTAURANT2_ID, menu.id(), menuTo))
                .isInstanceOf(DataConflictException.class)
                .hasMessageContaining("Menu does not belong to restaurant");
    }

    @Test
    void updateMenuWithDuplicateDishes() {
        Menu menu = restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, TODAY);
        List<DishTo> duplicateDishes = getDuplicatedTwoDishTo();
        MenuTo menuTo = new MenuTo(menu.id(), TODAY, duplicateDishes);
        assertThatThrownBy(() -> restaurantService.updateMenu(RESTAURANT1_ID, menu.id(), menuTo))
                .isInstanceOf(DataConflictException.class)
                .hasMessageContaining("Menu cannot contain duplicate dishes");
    }

    @Test
    void updateMenuWithPastDate() {
        Menu menu = restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, TODAY);
        LocalDate pastDate = LocalDate.now().minusDays(1);
        List<DishTo> dishes = getNewTwoDishTo();
        MenuTo menuTo = new MenuTo(menu.id(), pastDate, dishes);

        assertThatThrownBy(() -> restaurantService.updateMenu(RESTAURANT1_ID, menu.id(), menuTo))
                .isInstanceOf(DataConflictException.class);
    }

    @Test
    void updateMenuWithEmptyDishes() {
        Menu menu = restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, TODAY);
        MenuTo menuTo = new MenuTo(menu.id(), TODAY, List.of());

        assertThatThrownBy(() -> restaurantService.updateMenu(RESTAURANT1_ID, menu.id(), menuTo))
                .isInstanceOf(IllegalRequestDataException.class);
    }


    @Test
    void deleteMenu() {
        Menu menu = restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, TODAY);
        assertThat(menu).isNotNull();

        restaurantService.deleteMenu(RESTAURANT1_ID, menu.id());

        assertThatThrownBy(() -> restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, TODAY))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Menu not found");
    }

    @Test
    void deleteMenuWithMenuNotFound() {
        assertThatThrownBy(() -> restaurantService.deleteMenu(RESTAURANT1_ID, NOT_FOUND))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Entity with id=" + NOT_FOUND + " not found");
    }

    @Test
    void deleteMenuWrongRestaurant() {
        Menu menu = restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, TODAY);
        assertThat(menu).isNotNull();

        assertThatThrownBy(() -> restaurantService.deleteMenu(RESTAURANT2_ID, menu.id()))
                .isInstanceOf(DataConflictException.class)
                .hasMessageContaining("Menu does not belong to restaurant");
    }
}
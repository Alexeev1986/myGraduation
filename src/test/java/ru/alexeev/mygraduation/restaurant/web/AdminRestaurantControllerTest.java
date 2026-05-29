package ru.alexeev.mygraduation.restaurant.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.alexeev.mygraduation.AbstractControllerTest;
import ru.alexeev.mygraduation.common.error.NotFoundException;
import ru.alexeev.mygraduation.common.util.JsonUtil;
import ru.alexeev.mygraduation.restaurant.model.Dish;
import ru.alexeev.mygraduation.restaurant.model.Menu;
import ru.alexeev.mygraduation.restaurant.model.Restaurant;
import ru.alexeev.mygraduation.restaurant.service.RestaurantService;
import ru.alexeev.mygraduation.restaurant.to.DishTo;
import ru.alexeev.mygraduation.restaurant.to.MenuTo;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.alexeev.mygraduation.restaurant.RestaurantTestData.*;
import static ru.alexeev.mygraduation.restaurant.util.RestaurantUtil.createdDishFromMenuItem;
import static ru.alexeev.mygraduation.restaurant.util.RestaurantUtil.createdDishToFromMenuItem;
import static ru.alexeev.mygraduation.restaurant.util.RestaurantUtil.newMenuTo;
import static ru.alexeev.mygraduation.restaurant.util.RestaurantUtil.toDishes;
import static ru.alexeev.mygraduation.restaurant.web.AdminRestaurantController.REST_URL;

class AdminRestaurantControllerTest extends AbstractControllerTest {

    private static final String REST_URL_SLASH = REST_URL + '/';

    @Autowired
    private RestaurantService restaurantService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllRestaurants() throws Exception {
        ResultActions createAction = perform(MockMvcRequestBuilders.get(REST_URL + "/all"))
                .andDo(print())
                .andExpect(status().isOk());
        List<Restaurant> expectedRestaurants = RESTAURANT_MATCHER.readListFromJson(createAction);
        RESTAURANT_MATCHER.assertMatch(expectedRestaurants, getAllRestaurantsData());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllRestaurantsWithUser() throws Exception {
        ResultActions createAction = perform(MockMvcRequestBuilders.get(REST_URL + "/all"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllRestaurantsWithUnauthorized() throws Exception {
        ResultActions createAction = perform(MockMvcRequestBuilders.get(REST_URL + "/all"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create() throws Exception{
        Restaurant newRestaurant = getNew();
        ResultActions actions = perform(MockMvcRequestBuilders.post(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(newRestaurant)))
                .andDo(print())
                .andExpect(status().isCreated());
        Restaurant created = RESTAURANT_MATCHER.readFromJson(actions);
        int newId = created.id();
        newRestaurant.setId(newId);
        RESTAURANT_MATCHER.assertMatch(newRestaurant, created);
        RESTAURANT_MATCHER.assertMatch(restaurantService.get(newId), newRestaurant);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createWithInvalidName() throws Exception{
        Restaurant newRestaurant = getInvalidName();
        ResultActions actions = perform(MockMvcRequestBuilders.post(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(newRestaurant)))
                .andDo(print())
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createWithDuplicateName() throws Exception{
        Restaurant duplicate = new Restaurant(null, restaurant1.getName());
        perform(MockMvcRequestBuilders.post(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(duplicate)))
                .andExpect(status().isConflict());
    }


        @Test
    @WithMockUser(roles = "USER")
    void createWithUser() throws Exception{
        Restaurant newRestaurant = getNew();
        ResultActions actions = perform(MockMvcRequestBuilders.post(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(newRestaurant)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void createWithUnauthorized() throws Exception{
        Restaurant newRestaurant = getNew();
        ResultActions actions = perform(MockMvcRequestBuilders.post(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(newRestaurant)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update() throws Exception {
        Restaurant updated = getUpdated();
        perform(MockMvcRequestBuilders.put(REST_URL_SLASH + RESTAURANT1_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(updated)))
                .andDo(print())
                .andExpect(status().isNoContent());

        RESTAURANT_MATCHER.assertMatch(restaurantService.get(updated.getId()), updated);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateNotFound() throws Exception {
        Restaurant updated = getUpdated();
        updated.setId(NOT_FOUND);
        perform(MockMvcRequestBuilders.put(REST_URL_SLASH + NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(updated)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateWithInvalidId() throws Exception {
        Restaurant updated = getUpdated();
        updated.setId(RESTAURANT1_ID + 1);
        perform(MockMvcRequestBuilders.put(REST_URL_SLASH + RESTAURANT1_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(updated)))
                .andDo(print())
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateWithDuplicateName() throws Exception {
        Restaurant updated = new Restaurant(RESTAURANT1_ID, restaurant2.getName());
        perform(MockMvcRequestBuilders.put(REST_URL_SLASH + RESTAURANT1_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(updated)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateWithInvalidName() throws Exception {
        Restaurant invalid = getInvalidName();
        perform(MockMvcRequestBuilders.put(REST_URL_SLASH + RESTAURANT1_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(invalid)))
                .andDo(print())
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateWithUser() throws Exception {
        Restaurant updated = getUpdated();
        perform(MockMvcRequestBuilders.put(REST_URL_SLASH + RESTAURANT1_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(updated)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void delete() throws Exception {
        perform(MockMvcRequestBuilders.delete(REST_URL_SLASH + RESTAURANT1_ID))
                .andDo(print())
                .andExpect(status().isNoContent());
        assertThatThrownBy(() -> restaurantService.get(RESTAURANT1_ID))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteNotFound() throws Exception {
        perform(MockMvcRequestBuilders.delete(REST_URL_SLASH + NOT_FOUND))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteUser() throws Exception {
        perform(MockMvcRequestBuilders.delete(REST_URL_SLASH + RESTAURANT1_ID))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.delete(REST_URL_SLASH + RESTAURANT1_ID))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addMenu() throws Exception {
        MenuTo menuTo = getNewMenuToForRestaurant1();
        perform(MockMvcRequestBuilders.post(REST_URL_SLASH + RESTAURANT1_ID + "/menus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(menuTo)))
                .andDo(print())
                .andExpect(status().isCreated());
        Menu created = restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, menuTo.getDate());
        assertThat(created).isNotNull();
        assertThat(created.getDate()).isEqualTo(menuTo.getDate());

        List<Dish> actualDishes = createdDishFromMenuItem(created.getMenuItems());
        List<Dish> expectedDishes = toDishes(menuTo.getDishes());
        DISH_MATCHER.assertMatch(actualDishes, expectedDishes);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addMenuAndUpdateExisting() throws Exception {
        MenuTo firstMenu = getNewMenuToForRestaurant1();
        perform(MockMvcRequestBuilders.post(REST_URL_SLASH + RESTAURANT1_ID + "/menus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(firstMenu)))
                .andDo(print())
                .andExpect(status().isCreated());

        MenuTo updateMenuTo = getUpdatedMenuToForRestaurant1();
        perform(MockMvcRequestBuilders.post(REST_URL_SLASH + RESTAURANT1_ID + "/menus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(updateMenuTo)))
                .andDo(print())
                .andExpect(status().isCreated());

        Menu created = restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, updateMenuTo.getDate());
        assertThat(created).isNotNull();
        List<Dish> actualDishes = createdDishFromMenuItem(created.getMenuItems());
        List<Dish> expectedDishes = toDishes(updateMenuTo.getDishes());
        DISH_MATCHER.assertMatch(actualDishes, expectedDishes);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addMenuWithNullDate() throws Exception {
        MenuTo menuWithNullDate = getMenuWithNullDate();

        perform(MockMvcRequestBuilders.post(REST_URL_SLASH + RESTAURANT1_ID + "/menus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(menuWithNullDate)))
                .andDo(print())
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addMenuWithEmptyDishes() throws Exception {
        MenuTo menuWithEmptyDishes = getMenuWithEmptyDishes();

        perform(MockMvcRequestBuilders.post(REST_URL_SLASH + RESTAURANT1_ID + "/menus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(menuWithEmptyDishes)))
                .andDo(print())
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addMenuForNotFoundRestaurant() throws Exception {
        MenuTo menuTo = getNewMenuToForRestaurant1();

        perform(MockMvcRequestBuilders.post(REST_URL_SLASH + NOT_FOUND + "/menus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(menuTo)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void addMenuWithUserRole() throws Exception {
        MenuTo menuTo = getNewMenuToForRestaurant1();

        perform(MockMvcRequestBuilders.post(REST_URL_SLASH + RESTAURANT1_ID + "/menus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(menuTo)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void addMenuUnauthorized() throws Exception {
        MenuTo menuTo = getNewMenuToForRestaurant1();

        perform(MockMvcRequestBuilders.post(REST_URL_SLASH + RESTAURANT1_ID + "/menus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(menuTo)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @CsvSource({
            "0, 422, false",
            "1, 201, true",
            "2, 201, true",
            "10, 201, true"
    })
    @WithMockUser(roles = "ADMIN")
    void addMenuWithDifferentDishCounts(int dishCount, int expectedStatus, boolean shouldSucceed) throws Exception {
        List<Dish> dishes = new ArrayList<>();
        for (int i = 1; i <= dishCount; i++) {
            dishes.add(new Dish(null, "Новое блюдо № " + i, 100 + i));
        }

        MenuTo menuTo = newMenuTo(TOMORROW, dishes);

        perform(MockMvcRequestBuilders.post(REST_URL_SLASH + RESTAURANT1_ID + "/menus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(menuTo)))
                .andDo(print())
                .andExpect(status().is(expectedStatus));

        if (shouldSucceed) {
            Menu created = restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, menuTo.getDate());
            List<Dish> actualDishes = createdDishFromMenuItem(created.getMenuItems());
            DISH_MATCHER.assertMatch(actualDishes, dishes);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "-2, 422, false",
            "-1, 422, false",
            "0, 201, true",
            "1, 201, true",
            "2, 201, true",
    })
    @WithMockUser(roles = "ADMIN")
    void addMenuForDifferentDates(int daysOffset, int expectedStatus, boolean shouldSucceed) throws Exception {
        LocalDate date = LocalDate.now().plusDays(daysOffset);
        List<Dish> dishes = List.of(dish1, dish2);
        MenuTo menuTo = newMenuTo(date, dishes);

        perform(MockMvcRequestBuilders.post(REST_URL_SLASH + RESTAURANT1_ID + "/menus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(menuTo)))
                .andDo(print())
                .andExpect(status().is(expectedStatus));

        if (shouldSucceed) {
            Menu created = restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, date);
            assertThat(created).isNotNull();
            assertThat(created.getDate()).isEqualTo(date);
            List<Dish> actualDishes = createdDishFromMenuItem(created.getMenuItems());
            DISH_MATCHER.assertMatch(actualDishes, dishes);
        }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addMenuReusesExistingDish() throws Exception {
        MenuTo menuTo = newMenuTo(TOMORROW, List.of(dish1, dish2));

        perform(MockMvcRequestBuilders.post(REST_URL_SLASH + RESTAURANT1_ID + "/menus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(menuTo)))
                .andDo(print())
                .andExpect(status().isCreated());

        Menu created = restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, menuTo.getDate());

        List<Dish> actualDishes = createdDishFromMenuItem(created.getMenuItems());
        DISH_MATCHER.assertMatch(actualDishes, List.of(dish1, dish2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addMenuWithDuplicateDishInMenuShouldFail() throws Exception {
        List<Dish> duplicateDishes = List.of(dish1, dish2, dish1);
        MenuTo menuTo = newMenuTo(TOMORROW, duplicateDishes);

        perform(MockMvcRequestBuilders.post(REST_URL_SLASH + RESTAURANT1_ID + "/menus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(menuTo)))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateMenu() throws Exception {

        Menu existingMenu = restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, TODAY);

        List<DishTo> updateDishes = getUpdatedDishesTo();
        MenuTo updatedMenuTo = new MenuTo(existingMenu.id(), TODAY, updateDishes);

        perform(MockMvcRequestBuilders.put(REST_URL_SLASH + RESTAURANT1_ID + "/menus/" + existingMenu.id())
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(updatedMenuTo)))
                .andDo(print())
                .andExpect(status().isNoContent());

        Menu afterUpdate = restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, TODAY);
        List<DishTo> afterUpdateDishes = createdDishToFromMenuItem(afterUpdate.getMenuItems());
        DISH_TO_MATCHER.assertMatch(afterUpdateDishes, updateDishes);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateMenuWithEmptyDishes() throws Exception {
        Menu existingMenu = restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, TODAY);
        MenuTo emptyMenuTo = new MenuTo(existingMenu.id(), TODAY, List.of());

        perform(MockMvcRequestBuilders.put(REST_URL_SLASH + RESTAURANT1_ID + "/menus/" + existingMenu.id())
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(emptyMenuTo)))
                .andExpect(status().isUnprocessableContent());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void updateMenuNotFound() throws Exception {
        MenuTo menuTo = getNotFoundMenuTo();

        perform(MockMvcRequestBuilders.put(REST_URL_SLASH + RESTAURANT1_ID + "/menus/" + NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(menuTo)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateMenuWithInvalidDate() throws Exception {
        Menu existingMenu = restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, TODAY);

        MenuTo invalidMenuTo = new MenuTo(existingMenu.getId(), YESTERDAY, List.of(new DishTo(null, "Блюдо", 100)));
        perform(MockMvcRequestBuilders.put(REST_URL_SLASH + RESTAURANT1_ID + "/menus/" + existingMenu.id())
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(invalidMenuTo)))
                .andDo(print())
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateMenuWithUserRole() throws Exception {
        MenuTo menuTo = getNewMenuTo();

        perform(MockMvcRequestBuilders.put(REST_URL_SLASH + RESTAURANT1_ID + "/menus/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(menuTo)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteMenu() throws Exception {
        Menu existingMenu = restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, TODAY);

        perform(MockMvcRequestBuilders.delete(REST_URL_SLASH + RESTAURANT1_ID + "/menus/" + existingMenu.id()))
                .andDo(print())
                .andExpect(status().isNoContent());

        assertThatThrownBy(() -> restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, TODAY))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteMenuNotFound() throws Exception {
        perform(MockMvcRequestBuilders.delete(REST_URL_SLASH + RESTAURANT1_ID + "/menus/" + NOT_FOUND))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteMenuWithUserRole() throws Exception {
        perform(MockMvcRequestBuilders.delete(REST_URL_SLASH + RESTAURANT1_ID + "/menus/1"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}
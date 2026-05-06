package ru.alexeev.mygraduation.restaurant.web;

import org.junit.jupiter.api.Test;
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
import ru.alexeev.mygraduation.restaurant.to.MenuTo;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.alexeev.mygraduation.restaurant.RestaurantTestData.*;
import static ru.alexeev.mygraduation.restaurant.util.RestaurantUtil.*;
import static ru.alexeev.mygraduation.restaurant.web.AdminRestaurantController.REST_URL;

class AdminRestaurantControllerTest extends AbstractControllerTest {

    private static final String REST_URL_SLASH = REST_URL + '/';

    @Autowired
    private RestaurantService restaurantService;

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
        perform(MockMvcRequestBuilders.post(REST_URL_SLASH + RESTAURANT1_ID + "/menu")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(menuTo)))
                .andDo(print())
                .andExpect(status().isCreated());
        Menu created = restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, menuTo.getDate());
        assertThat(created).isNotNull();
        assertThat(created.getDate()).isEqualTo(menuTo.getDate());

        DISH_MATCHER.assertMatch(created.getDishes(), toDishes(menuTo.getDishes()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addMenuAndUpdateExisting() throws Exception {
        MenuTo firstMenu = getNewMenuToForRestaurant1();
        perform(MockMvcRequestBuilders.post(REST_URL_SLASH + RESTAURANT1_ID + "/menu")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(firstMenu)))
                .andDo(print())
                .andExpect(status().isCreated());

        MenuTo updateMenuTo = getUpdatedMenuToForRestaurant1();
        perform(MockMvcRequestBuilders.post(REST_URL_SLASH + RESTAURANT1_ID + "/menu")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(updateMenuTo)))
                .andDo(print())
                .andExpect(status().isCreated());

        Menu created = restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, updateMenuTo.getDate());
        assertThat(created).isNotNull();
        DISH_MATCHER.assertMatch(created.getDishes(), toDishes(updateMenuTo.getDishes()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addMenuWithThePastDate() throws Exception {
        MenuTo pastDateMenu = getMenuWithThePastDate();

        perform(MockMvcRequestBuilders.post(REST_URL_SLASH + RESTAURANT1_ID + "/menu")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(pastDateMenu)))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addMenuWithNullDate() throws Exception {
        MenuTo menuWithNullDate = getMenuWithNullDate();

        perform(MockMvcRequestBuilders.post(REST_URL_SLASH + RESTAURANT1_ID + "/menu")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(menuWithNullDate)))
                .andDo(print())
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addMenuWithEmptyDishes() throws Exception {
        MenuTo menuWithEmptyDishes = getMenuWithEmptyDishes();

        perform(MockMvcRequestBuilders.post(REST_URL_SLASH + RESTAURANT1_ID + "/menu")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(menuWithEmptyDishes)))
                .andDo(print())
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addMenuForNotFoundRestaurant() throws Exception {
        MenuTo menuTo = getNewMenuToForRestaurant1();

        perform(MockMvcRequestBuilders.post(REST_URL_SLASH + NOT_FOUND + "/menu")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(menuTo)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void addMenuWithUserRole() throws Exception {
        MenuTo menuTo = getNewMenuToForRestaurant1();

        perform(MockMvcRequestBuilders.post(REST_URL_SLASH + RESTAURANT1_ID + "/menu")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(menuTo)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void addMenuUnauthorized() throws Exception {
        MenuTo menuTo = getNewMenuToForRestaurant1();

        perform(MockMvcRequestBuilders.post(REST_URL_SLASH + RESTAURANT1_ID + "/menu")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(menuTo)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addMenuWithNewDish() throws Exception {
        Dish newDish = new Dish(null, "Новое блюдо", 999);
        MenuTo menuTo = newMenuTo(LocalDate.now().plusDays(1), List.of(newDish));

        perform(MockMvcRequestBuilders.post(REST_URL_SLASH + RESTAURANT1_ID + "/menu")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(menuTo)))
                .andDo(print())
                .andExpect(status().isCreated());

        Menu created = restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, menuTo.getDate());
        DISH_MATCHER.assertMatch(created.getDishes(), newDish);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addMenuReusesExistingDish() throws Exception {
        MenuTo menuTo = newMenuTo(LocalDate.now().plusDays(1), List.of(dish1));

        perform(MockMvcRequestBuilders.post(REST_URL_SLASH + RESTAURANT1_ID + "/menu")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(menuTo)))
                .andDo(print())
                .andExpect(status().isCreated());

        Menu created = restaurantService.getMenuByRestaurantAndDate(RESTAURANT1_ID, menuTo.getDate());
        Dish usedDish = created.getDishes().getFirst();
        assertThat(usedDish.id()).isEqualTo(dish1.id());
        assertThat(usedDish.getName()).isEqualTo(dish1.getName());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addMenuWithDuplicateDishInMenuShouldFail() throws Exception {
        List<Dish> duplicateDishes = List.of(dish1, dish1);
        MenuTo menuTo = newMenuTo(LocalDate.now().plusDays(1), duplicateDishes);

        perform(MockMvcRequestBuilders.post(REST_URL_SLASH + RESTAURANT1_ID + "/menu")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(menuTo)))
                .andDo(print())
                .andExpect(status().isConflict());
    }
}
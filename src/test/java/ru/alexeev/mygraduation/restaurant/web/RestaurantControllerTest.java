package ru.alexeev.mygraduation.restaurant.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.alexeev.mygraduation.AbstractControllerTest;
import ru.alexeev.mygraduation.restaurant.model.Restaurant;
import ru.alexeev.mygraduation.restaurant.service.RestaurantService;
import ru.alexeev.mygraduation.restaurant.to.RestaurantTo;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.alexeev.mygraduation.restaurant.RestaurantTestData.*;
import static ru.alexeev.mygraduation.restaurant.util.RestaurantUtil.*;
import static ru.alexeev.mygraduation.restaurant.web.RestaurantController.REST_URL;


class RestaurantControllerTest extends AbstractControllerTest {

    private static final String REST_URL_SlASH = REST_URL + '/';

    @Autowired
    private RestaurantService restaurantService;

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void getAllWithTodayMenusAsUser() throws Exception {
        List<Restaurant> restaurants = getAllRestaurants();
        List<RestaurantTo> expected = toRestaurantTosWithTodayMenu(restaurants);


        RESTAURANT_MATCHER.assertMatch(restaurantService.getAllWithTodayMenu(), restaurants);

        perform(MockMvcRequestBuilders.get(REST_URL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(RESTAURANT_TO_MATCHER.contentJson(expected));
    }

    @Test
    void getAllWithTodayMenusUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles =  {"USER", "ADMIN"})
    void get() throws Exception{
        RestaurantTo expected = toRestaurantTo(restaurant1);

        perform(MockMvcRequestBuilders.get(REST_URL_SlASH + RESTAURANT1_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(RESTAURANT_TO_MATCHER.contentJson(expected));
    }

    @Test
    @WithMockUser(roles =  {"USER", "ADMIN"})
    void getNotFound() throws Exception{
        RestaurantTo expected = toRestaurantTo(restaurant1);

        perform(MockMvcRequestBuilders.get(REST_URL_SlASH + NOT_FOUND))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void getUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL_SlASH + RESTAURANT1_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles =  {"USER", "ADMIN"})
    void getTodayMenu() throws Exception{
        perform(MockMvcRequestBuilders.get(REST_URL_SlASH + RESTAURANT1_ID + "/menu"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MENU_TO_MATCHER.contentJson(toMenuTo(menu1)));
    }

    @Test
    @WithMockUser(roles =  {"USER", "ADMIN"})
    void getTodayMenuNotFound() throws Exception{
        perform(MockMvcRequestBuilders.get(REST_URL_SlASH + NOT_FOUND))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles =  {"USER", "ADMIN"})
    void getMenuByDate() throws Exception {
        LocalDate date = LocalDate.now();
        perform(MockMvcRequestBuilders.get(REST_URL_SlASH + RESTAURANT1_ID + "/menu-by-date").param("date", date.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MENU_TO_MATCHER.contentJson(toMenuTo(menu1)));
    }

    @Test
    @WithMockUser(roles =  {"USER", "ADMIN"})
    void getMenuByDateNotFoundDate() throws Exception {
        LocalDate oldDate = LocalDate.of(2000, 1, 1);
        perform(MockMvcRequestBuilders.get(REST_URL_SlASH + RESTAURANT1_ID + "/menu-by-date").param("date", oldDate.toString()))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles =  {"USER", "ADMIN"})
    void getMenuByDateWithMissingParam() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL_SlASH + RESTAURANT1_ID + "/menu-by-date"))
                .andDo(print())
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @WithMockUser(roles =  {"USER", "ADMIN"})
    void getMenuByDateWithInvalidDateFormat() throws Exception {
        LocalDate oldDate = LocalDate.of(2000, 1, 1);
        perform(MockMvcRequestBuilders.get(REST_URL_SlASH + RESTAURANT1_ID + "/menu-by-date").param("date", "invalid date"))
                .andDo(print())
                .andExpect(status().isUnprocessableContent());
    }
}
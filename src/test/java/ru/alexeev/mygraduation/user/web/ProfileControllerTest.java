package ru.alexeev.mygraduation.user.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.alexeev.mygraduation.AbstractControllerTest;
import ru.alexeev.mygraduation.common.util.JsonUtil;
import ru.alexeev.mygraduation.user.model.Role;
import ru.alexeev.mygraduation.user.model.User;
import ru.alexeev.mygraduation.user.service.UserService;
import ru.alexeev.mygraduation.user.to.UserTo;
import ru.alexeev.mygraduation.user.util.UsersUtil;

import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.alexeev.mygraduation.user.UserTestData.*;
import static ru.alexeev.mygraduation.user.web.ProfileController.REST_URL;

class ProfileControllerTest extends AbstractControllerTest {

    @Autowired
    private UserService userService;

    @Test
    @WithUserDetails(value = USER_MAIL)
    void get() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(USER_MATCHER.contentJson(user));
    }

    @Test
    void getUnAuth() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void delete() throws Exception {
        perform(MockMvcRequestBuilders.delete(REST_URL))
                .andExpect(status().isNoContent());
        USER_MATCHER.assertMatch(userService.getAll(), admin, guest);
    }

    @Test
    void register() throws Exception{
        UserTo newTo = new UserTo(null, "newName", "newemail@ya.ru", "newPassword", true, Set.of(Role.USER));
        User newUser = UsersUtil.createNewFromTo(newTo);
        ResultActions action = perform(MockMvcRequestBuilders.post(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(newTo)))
                .andDo(print())
                .andExpect(status().isCreated());

        User created = USER_MATCHER.readFromJson(action);
        int newId = created.id();
        newUser.setId(newId);
        USER_MATCHER.assertMatch(created, newUser);
        USER_MATCHER.assertMatch(userService.get(newId), newUser);
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void update() throws Exception {
        UserTo updatedTo = new UserTo(null, "newName", USER_MAIL, "newPassword", true, Set.of(Role.USER));
        perform(MockMvcRequestBuilders.put(REST_URL).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(updatedTo)))
                .andDo(print())
                .andExpect(status().isNoContent());

        USER_MATCHER.assertMatch(userService.get(USER_ID), UsersUtil.updateFromTo(new User(user), updatedTo));
    }

    @Test
    void registerInvalid() throws Exception {
        UserTo newTo = new UserTo(null, null, null, null, true, null);
        perform(MockMvcRequestBuilders.post(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(newTo)))
                .andDo(print())
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void updateInvalid() throws Exception {
        UserTo updatedTo = new UserTo(null, null, "password", null, true, Set.of(Role.USER));
        perform(MockMvcRequestBuilders.put(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(updatedTo)))
                .andDo(print())
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void updateDuplicate() throws Exception {
        UserTo updatedTo = new UserTo(null, "newName", ADMIN_MAIL, "newPassword", true, Set.of(Role.USER));
        perform(MockMvcRequestBuilders.put(REST_URL).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(updatedTo)))
                .andDo(print())
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().string(containsString(UniqueMailValidator.EXCEPTION_DUPLICATE_EMAIL)));
    }
}
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.alexeev.mygraduation.user.UserTestData.USER_ID;
import static ru.alexeev.mygraduation.user.UserTestData.USER_MAIL;
import static ru.alexeev.mygraduation.user.UserTestData.USER_MATCHER;
import static ru.alexeev.mygraduation.user.UserTestData.admin;
import static ru.alexeev.mygraduation.user.UserTestData.getDuplicateEmail;
import static ru.alexeev.mygraduation.user.UserTestData.getInvalidEmail;
import static ru.alexeev.mygraduation.user.UserTestData.getNew;
import static ru.alexeev.mygraduation.user.UserTestData.getNewWithNullRoles;
import static ru.alexeev.mygraduation.user.UserTestData.getNewWithShortPassword;
import static ru.alexeev.mygraduation.user.UserTestData.getNewWithTooLongName;
import static ru.alexeev.mygraduation.user.UserTestData.getUpdated;
import static ru.alexeev.mygraduation.user.UserTestData.getUserToWithEnabledFalse;
import static ru.alexeev.mygraduation.user.UserTestData.getUserToWithEnabledFalseAndIdNull;
import static ru.alexeev.mygraduation.user.UserTestData.getUserWithMultipleRoles;
import static ru.alexeev.mygraduation.user.UserTestData.getWithAdminDuplicateEmail;
import static ru.alexeev.mygraduation.user.UserTestData.getWithEmptyName;
import static ru.alexeev.mygraduation.user.UserTestData.getWithHtmlUnsafeName;
import static ru.alexeev.mygraduation.user.UserTestData.guest;
import static ru.alexeev.mygraduation.user.UserTestData.user;
import static ru.alexeev.mygraduation.user.util.UsersUtil.*;
import static ru.alexeev.mygraduation.user.web.ProfileController.REST_URL;
import static ru.alexeev.mygraduation.user.web.UniqueMailValidator.EXCEPTION_DUPLICATE_EMAIL;

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
        User newUser = getNew();
        UserTo newTo = createToFromUser(newUser);
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
    void registerWithShortPassword() throws Exception {
        UserTo newTo = createUserToWithPassword(getNewWithShortPassword(), "123");
        perform(MockMvcRequestBuilders.post(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(newTo)))
                .andDo(print())
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void registerWithDuplicateEmail() throws Exception {
        UserTo newTo = createToFromUser(getDuplicateEmail());
        perform(MockMvcRequestBuilders.post(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(newTo)))
                .andDo(print())
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().string(containsString(EXCEPTION_DUPLICATE_EMAIL)));
    }

    @Test
    void registerWithEmptyRoles() throws Exception {
        UserTo newTo = createToFromUser(getNewWithNullRoles());
        perform(MockMvcRequestBuilders.post(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(newTo)))
                .andDo(print())
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void registerWithTooLongName() throws Exception {
        UserTo newTo = createToFromUser(getNewWithTooLongName());
        perform(MockMvcRequestBuilders.post(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(newTo)))
                .andDo(print())
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void registerWithInvalidEmail() throws Exception {
        UserTo newTo = createToFromUser(getInvalidEmail());
        perform(MockMvcRequestBuilders.post(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(newTo)))
                .andDo(print())
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void registerWithEnabledFalse() throws Exception {
        UserTo newTo = createToFromUser(getUserToWithEnabledFalseAndIdNull());
        ResultActions action = perform(MockMvcRequestBuilders.post(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(newTo)))
                .andDo(print())
                .andExpect(status().isCreated());
        User created = USER_MATCHER.readFromJson(action);
        assertThat(created.isEnabled()).isTrue();
    }

    @Test
    void registerWithMultipleRolesAndAdminRoleShouldBeIgnored() throws Exception {
        UserTo newTo = createToFromUser(getUserWithMultipleRoles());
        ResultActions action = perform(MockMvcRequestBuilders.post(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(newTo)))
                .andDo(print())
                .andExpect(status().isCreated());
        User created = USER_MATCHER.readFromJson(action);
        assertThat(created.getRoles()).containsExactly(Role.USER);
        assertThat(created.getRoles()).doesNotContain(Role.ADMIN);
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void update() throws Exception {
        UserTo updatedTo = createToFromUser(getUpdated());
        perform(MockMvcRequestBuilders.put(REST_URL).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(updatedTo)))
                .andDo(print())
                .andExpect(status().isNoContent());

        USER_MATCHER.assertMatch(userService.get(USER_ID), getUpdated());
    }



    @Test
    @WithUserDetails(value = USER_MAIL)
    void updateEnableToFalse() throws Exception {
        User updated = getUserToWithEnabledFalse();
        UserTo updatedTo = createToFromUser(updated);

        perform(MockMvcRequestBuilders.put(REST_URL).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(updatedTo)))
                .andDo(print())
                .andExpect(status().isNoContent());

        USER_MATCHER.assertMatch(userService.get(USER_ID), updated);
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void updateWithShortPassword() throws Exception {
        UserTo updatedTo = createUserToWithPassword(getNewWithShortPassword(), "123");
        perform(MockMvcRequestBuilders.put(REST_URL).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(updatedTo)))
                .andDo(print())
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void updateWithEmptyName() throws Exception {
        UserTo updatedTo = createToFromUser(getWithEmptyName());
        perform(MockMvcRequestBuilders.put(REST_URL).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(updatedTo)))
                .andDo(print())
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void updateWithInvalidEmail() throws Exception {
        UserTo updatedTo = createToFromUser(getInvalidEmail());
        perform(MockMvcRequestBuilders.put(REST_URL).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(updatedTo)))
                .andDo(print())
                .andExpect(status().isUnprocessableContent());
    }



    @Test
    @WithUserDetails(value = USER_MAIL)
    void updateDuplicate() throws Exception {
        UserTo updatedTo =createToFromUser(getWithAdminDuplicateEmail());
        perform(MockMvcRequestBuilders.put(REST_URL).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(updatedTo)))
                .andDo(print())
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().string(containsString(EXCEPTION_DUPLICATE_EMAIL)));
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void updateWithHtmlUnsafe() throws Exception {
        UserTo updatedTo =createToFromUser(getWithHtmlUnsafeName());
        perform(MockMvcRequestBuilders.put(REST_URL).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(updatedTo)))
                .andDo(print())
                .andExpect(status().isUnprocessableContent());
    }
}
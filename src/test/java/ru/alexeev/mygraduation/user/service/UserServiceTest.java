package ru.alexeev.mygraduation.user.service;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.alexeev.mygraduation.common.error.NotFoundException;
import ru.alexeev.mygraduation.user.model.User;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static ru.alexeev.mygraduation.user.UserTestData.*;
import static ru.alexeev.mygraduation.user.util.UsersUtil.createToFromUser;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void get() {
        USER_MATCHER.assertMatch(userService.get(USER_ID), user);
    }

    @Test
    void getNotFound() {
        assertThatThrownBy(() -> userService.get(NOT_FOUND))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Entity with id=" + NOT_FOUND + " not found");
    }

    @Test
    void getAll() {
        USER_MATCHER.assertMatch(userService.getAll(), List.of(admin, guest, user));
    }

    @Test
    void getByEmail() {
        USER_MATCHER.assertMatch(userService.getByEmail(USER_MAIL), user);
    }

    @Test
    void create() {
        User newUser = getNew();
        User created = userService.create(createToFromUser(newUser));
        int newId = created.id();
        newUser.setId(newId);
        USER_MATCHER.assertMatch(created, newUser);
        USER_MATCHER.assertMatch(userService.get(newId), newUser);
    }

    @Test
    void update() {
        User updated = getUpdated();
        userService.update(createToFromUser(updated), USER_ID);
        USER_MATCHER.assertMatch(userService.get(USER_ID), updated);
    }

    @Test
    void delete() {
        userService.delete(USER_ID);
        assertThatThrownBy(() -> userService.get(USER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Entity with id=" + USER_ID + " not found");
    }

    @Test
    void deleteNotFound() {
        assertThatThrownBy(() -> userService.delete(NOT_FOUND))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Entity with id=" + NOT_FOUND + " not found");
    }

    @Test
    void enabled() {
        userService.enable(USER_ID, false);
        User disabled = userService.get(USER_ID);
        assertThat(disabled.isEnabled()).isFalse();

        userService.enable(USER_ID, true);
        User enabled = userService.get(USER_ID);
        assertThat(enabled.isEnabled()).isTrue();
    }

    @Test
    void enabledNotFond() {
        assertThatThrownBy(() -> userService.enable(NOT_FOUND, false))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void createWithInvalidEmail() {
        User invalid = getInvalidEmail();
        assertThatThrownBy(() -> userService.create(createToFromUser(invalid)))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void getWithVotes() {
        User userWithVotes = userService.getWithVotes(USER_ID);
        assertThat(userWithVotes.getVotes()).isNotNull();

    }
}
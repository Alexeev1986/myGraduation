package ru.alexeev.mygraduation.user;

import ru.alexeev.mygraduation.MatcherFactory;
import ru.alexeev.mygraduation.common.util.JsonUtil;
import ru.alexeev.mygraduation.user.model.Role;
import ru.alexeev.mygraduation.user.model.User;

import java.util.Date;
import java.util.List;

public class UserTestData {
    public static final MatcherFactory.Matcher<User> USER_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(User.class, "registered", "votes", "password");

    public static final int USER_ID = 2;
    public static final int ADMIN_ID = 1;
    public static final int GUEST_ID = 3;
    public static final int NOT_FOUND = 100;

    public static final String USER_MAIL = "user@yandex.ru";
    public static final String ADMIN_MAIL = "admin@gmail.com";
    public static final String GUEST_MAIL = "guest@gmail.com";

    public static final User user = new User(USER_ID, "User", USER_MAIL, "{noop}password", true, new Date(), Role.USER);
    public static final User admin = new User(ADMIN_ID, "Admin", ADMIN_MAIL, "{noop}admin", true, new Date(), Role.ADMIN, Role.USER);
    public static final User guest = new User(GUEST_ID, "Guest", GUEST_MAIL, "{noop}guest", true, new Date(), Role.USER);

    public static User getNew() {
        return new User(null, "New", "new@mail.ru", "{noop}newPass", true, new Date(), Role.USER);
    }

    public static User getUpdated() {
        return new User(USER_ID, "UpdatedName", USER_MAIL, "newPass", false, new Date(), List.of(Role.ADMIN));
    }

    public static User getDuplicateEmail() {
        return new User(null, "Duplicate", USER_MAIL, "password", true, new Date(), Role.USER);
    }

    public static User getDuplicateWithAdminEmail() {
        return new User(USER_ID, "Updated", ADMIN_MAIL, "password", true, new Date(), Role.USER);
    }

    public static User getInvalidEmail() {
        return new User(null, "Invalid", "", "password", true, new Date(), Role.USER);
    }

    public static User getWithShortPassword() {
        return new User(null, "Invalid", "invalid@mail.ru", "123", true, new Date(), Role.USER);
    }



    public static String jsonWithPassword(User user, String passw) {
        return JsonUtil.writeAdditionProps(user, "password", passw);
    }

}

package ru.alexeev.mygraduation.user.util;

import lombok.experimental.UtilityClass;
import ru.alexeev.mygraduation.user.model.Role;
import ru.alexeev.mygraduation.user.model.User;
import ru.alexeev.mygraduation.user.to.UserTo;

import java.util.Date;

@UtilityClass
public class UsersUtil {
    public static User createNewFromTo(UserTo userTo) {
        return new User(null, userTo.getName(), userTo.getEmail().toLowerCase(), userTo.getPassword(), true, new Date(), Role.USER);
    }

    public static User updateFromTo(User user, UserTo userTo) {
        user.setName(userTo.getName());
        user.setEmail(userTo.getEmail().toLowerCase());
        user.setPassword(userTo.getPassword());
        user.setEnabled(userTo.isEnabled());
        user.setRoles(userTo.getRoles());
        return user;
    }

    public static UserTo createToFromUser(User user) {
        return new UserTo(user.getId(), user.getName(), user.getEmail(), user.getPassword(), user.isEnabled(), user.getRoles());
    }

    public static UserTo createUserToWithPassword(User user, String password) {
        return new UserTo(user.getId(), user.getName(), user.getEmail(), password, user.isEnabled(), user.getRoles());
    }
}

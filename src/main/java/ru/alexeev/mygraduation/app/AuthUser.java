package ru.alexeev.mygraduation.app;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import ru.alexeev.mygraduation.user.model.Role;
import ru.alexeev.mygraduation.user.model.User;

public class AuthUser extends org.springframework.security.core.userdetails.User {

    @Getter
    private final User user;

    public AuthUser(@NotNull User user) {
        super(user.getEmail(), user.getPassword(), user.getRoles());
        this.user = user;
    }

    public int id() {
        return user.id();
    }

    public boolean hasRole(Role role) {
        return user.hasRole(role);
    }

    @Override
    public String toString() {
        return "AuthUser:" + id() + '[' + user.getEmail() + ']';
    }
}

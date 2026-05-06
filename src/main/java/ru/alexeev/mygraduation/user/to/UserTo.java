package ru.alexeev.mygraduation.user.to;

import jakarta.validation.constraints.*;
import lombok.EqualsAndHashCode;
import lombok.Value;
import ru.alexeev.mygraduation.common.HasIdAndEmail;
import ru.alexeev.mygraduation.common.to.NamedTo;
import ru.alexeev.mygraduation.common.validation.NoHtml;
import ru.alexeev.mygraduation.user.model.Role;

import java.util.*;

@Value
@EqualsAndHashCode(callSuper = true)
public class UserTo extends NamedTo implements HasIdAndEmail {

    @Email
    @NotBlank
    @Size(max = 64)
    @NoHtml
    String email;

    @NotBlank
    @Size(min = 5, max = 128)
    String password;

    @NotNull(message = "enabled must be specified")
    boolean enabled;

    @NotEmpty(message = "At least one role is required")
    Set<Role> roles;

    public UserTo(Integer id, String name, String email, String password, boolean enabled, Set<Role> roles) {
        super(id, name);
        this.email = email;
        this.password = password;
        this.enabled = enabled;
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "UserTo:" + id + "[" + email + "]";
    }
}

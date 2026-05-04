package ru.alexeev.mygraduation.user.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import ru.alexeev.mygraduation.app.AuthUtil;
import ru.alexeev.mygraduation.common.HasIdAndEmail;
import ru.alexeev.mygraduation.user.repository.UserRepository;

@Component
@AllArgsConstructor
public class UniqueMailValidator implements org.springframework.validation.Validator {
    public static final String EXCEPTION_DUPLICATE_EMAIL = "User with this email already exists";

    private final UserRepository repository;
    private final HttpServletRequest request;


    @Override
    public boolean supports(@NotNull Class<?> clazz) {
        return HasIdAndEmail.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        HasIdAndEmail user = ((HasIdAndEmail) target);
        if (StringUtils.hasText(user.getEmail())) {
            repository.findByEmailIgnoreCase(user.getEmail())
                    .ifPresent(dbUser -> {
                        if (request.getMethod().equals("PUT")) {  // UPDATE
                            int dbId = dbUser.id();

                            // it is ok, if update ourselves
                            if (user.getId() != null && dbId == user.id()) return;

                            // Workaround for update with user.id=null in request body
                            // ValidationUtil.assureIdConsistent called after this validation
                            String requestURI = request.getRequestURI();
                            if (requestURI.endsWith("/" + dbId) || (dbId == AuthUtil.get().id() && requestURI.contains("/profile")))
                                return;
                        }
                        errors.rejectValue("email", "", EXCEPTION_DUPLICATE_EMAIL);
                    });
        }
    }
}

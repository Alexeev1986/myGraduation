package ru.alexeev.mygraduation.user.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.alexeev.mygraduation.common.BaseRepository;
import ru.alexeev.mygraduation.common.error.NotFoundException;
import ru.alexeev.mygraduation.user.model.User;

import java.util.Optional;

import static ru.alexeev.mygraduation.app.config.SecurityConfig.PASSWORD_ENCODER;

@Transactional(readOnly = true)
public interface UserRepository extends BaseRepository<User> {
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<User> findByEmailIgnoreCase(String email);

    default User getExistedByEmail(String email) {
        return findByEmailIgnoreCase(email).orElseThrow(
                () -> new NotFoundException("User with email=" + email + " not found")
        );
    }

    @Transactional
    default User prepareAndSave(User user) {
        user.setPassword(PASSWORD_ENCODER.encode(user.getPassword()));
        user.setEmail(user.getEmail().toLowerCase());
        return save(user);
    }
}

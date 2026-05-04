package ru.alexeev.mygraduation.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alexeev.mygraduation.common.error.NotFoundException;
import ru.alexeev.mygraduation.user.model.User;
import ru.alexeev.mygraduation.user.repository.UserRepository;
import ru.alexeev.mygraduation.user.to.UserTo;
import java.util.List;

import static ru.alexeev.mygraduation.common.validation.ValidationUtil.checkIsNew;
import static ru.alexeev.mygraduation.user.util.UsersUtil.createNewFromTo;
import static ru.alexeev.mygraduation.user.util.UsersUtil.updateFromTo;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public User get(int id) {
        log.info("get user {}", id);
        return userRepository.getExisted(id);
    }

    public List<User> getAll() {
        log.info("getAll users");
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "name", "email"));
    }

    public User getByEmail(String email) {
        log.info("get user by email {}", email);
        return userRepository.getExistedByEmail(email);
    }

    @Transactional
    public User create(UserTo userTo) {
        log.info("create user {}", userTo);
        checkIsNew(userTo);
        return userRepository.prepareAndSave(createNewFromTo(userTo));
    }

    @Transactional
    public void update(UserTo userTo, int id) {
        log.info("update user {} with id {}", userTo, id);
        User user = get(id);
        updateFromTo(user, userTo);
        userRepository.save(user);
    }

    @Transactional
    public void delete(int id) {
        log.info("delete user {}", id);
        userRepository.deleteExisted(id);
    }

    @Transactional
    public void enable(int id, boolean enabled) {
        User user = userRepository.getExisted(id);
        log.info((enabled ? "enabled" : "disabled") + " for user {}", id);
        user.setEnabled(enabled);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getWithVotes(int id) {
        log.info("get user {} with votes", id);
        return userRepository.getWithVotes(id)
                .orElseThrow(() -> new NotFoundException("User " + id + "not found"));
    }

}

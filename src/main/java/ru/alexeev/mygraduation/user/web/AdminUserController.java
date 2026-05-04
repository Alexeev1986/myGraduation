package ru.alexeev.mygraduation.user.web;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.alexeev.mygraduation.user.model.User;
import ru.alexeev.mygraduation.user.to.UserTo;
import ru.alexeev.mygraduation.vote.model.Vote;
import ru.alexeev.mygraduation.vote.service.VoteService;
import ru.alexeev.mygraduation.vote.to.VoteTo;

import java.net.URI;
import java.util.List;

import static ru.alexeev.mygraduation.common.validation.ValidationUtil.assureIdConsistent;
import static ru.alexeev.mygraduation.common.validation.ValidationUtil.checkIsNew;
import static ru.alexeev.mygraduation.vote.util.VoteUtil.toVoteTos;

@RestController
@RequestMapping(value = AdminUserController.REST_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
public class AdminUserController extends AbstractUserController{
    static final String REST_URL = "/api/admin/users";

    private final VoteService voteService;

    @GetMapping("/{id}/votes")
    public List<VoteTo> getUserVotes(@PathVariable int id) {
        log.info("get all votes for user {}", id);
        List<Vote> votes = voteService.findByUser(id);
        return toVoteTos(votes);
    }

    @GetMapping
    public List<User> getAll() {
        log.info("getAll users");
        return userService.getAll();
    }

    @GetMapping("/{id}")
    public User get(@PathVariable int id) {
        log.info("get user {}", id);
        return userService.get(id);
    }

    @GetMapping("/by-email")
    public User getByEmail(@RequestParam String email) {
        log.info("get user by email {}", email);
        return userService.getByEmail(email);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> create(@Valid @RequestBody UserTo userTo) {
        log.info("create user {}", userTo);
        checkIsNew(userTo);
        User created = userService.create(userTo);
        URI uriOfNewResource = ServletUriComponentsBuilder.fromCurrentContextPath().
                path(REST_URL + "/{id}")
                .buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(uriOfNewResource).body(created);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@Valid @RequestBody UserTo userTo, @PathVariable int id) {
        log.info("update user {} with {}", userTo, id);
        assureIdConsistent(userTo, id);
        userService.update(userTo, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int id) {
        log.info("delete user {}", id);
        userService.delete(id);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void enable(@PathVariable int id, @RequestParam boolean enabled) {
        log.info(enabled ? "enable {}" : "disable {}", id);
        userService.enable(id, enabled);
    }
}

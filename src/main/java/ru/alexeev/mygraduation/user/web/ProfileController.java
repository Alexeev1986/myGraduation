package ru.alexeev.mygraduation.user.web;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.alexeev.mygraduation.app.AuthUser;
import ru.alexeev.mygraduation.user.model.User;
import ru.alexeev.mygraduation.user.to.UserTo;
import ru.alexeev.mygraduation.vote.model.Vote;
import ru.alexeev.mygraduation.vote.service.VoteService;

import java.net.URI;
import java.util.List;

import static ru.alexeev.mygraduation.common.validation.ValidationUtil.assureIdConsistent;

@RestController
@RequestMapping(value = ProfileController.REST_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
public class ProfileController extends AbstractUserController{
    static final String REST_URL = "/api/profile";
    private final VoteService voteService;

    @GetMapping
    public User get(@AuthenticationPrincipal AuthUser authUser) {
        log.info("get profile for user {}", authUser.id());
        return authUser.getUser();
    }

    @GetMapping("/votes")
    public List<Vote> getMyVotes(@AuthenticationPrincipal AuthUser authUser) {
        log.info("get my votes for user {}", authUser.id());
        return voteService.findByUser(authUser.id());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthUser authUser) {
        log.info("delete profile for user {}", authUser.id());
        userService.delete(authUser.id());
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<User> register(@Valid @RequestBody UserTo userTo) {
        log.info("register user {}", userTo);
        User created = userService.create(userTo);
        URI uriOfNewResource = ServletUriComponentsBuilder.fromCurrentContextPath().path(REST_URL).build().toUri();
        return ResponseEntity.created(uriOfNewResource).body(created);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@Valid @RequestBody UserTo userTo, @AuthenticationPrincipal AuthUser authUser) {
        log.info("update profile for user {} with {}", authUser.id(), userTo);
        assureIdConsistent(userTo, authUser.id());
        userService.update(userTo, authUser.id());
    }
}

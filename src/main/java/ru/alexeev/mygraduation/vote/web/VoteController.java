package ru.alexeev.mygraduation.vote.web;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.alexeev.mygraduation.app.AuthUser;
import ru.alexeev.mygraduation.vote.model.Vote;
import ru.alexeev.mygraduation.vote.service.VoteService;
import ru.alexeev.mygraduation.vote.to.VoteResultTo;
import ru.alexeev.mygraduation.vote.to.VoteTo;

import java.util.List;

import static ru.alexeev.mygraduation.vote.util.VoteUtil.toVoteTo;

@RestController
@RequestMapping(value = VoteController.REST_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
public class VoteController {
    static final String REST_URL = "/api/votes";

    private final VoteService voteService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VoteTo vote(@AuthenticationPrincipal AuthUser authUser, @RequestParam @Min(1) int restaurantId) {
        log.info("user {} votes for restaurant {}", authUser.id(), restaurantId);
        Vote vote = voteService.vote(authUser.id(), restaurantId);
        return toVoteTo(vote);
    }

    @GetMapping("/results/today")
    public List<VoteResultTo> getTodayResults() {
        log.info("get today`s voting results");
        return voteService.getTodayVoteResults();
    }

    @GetMapping("/results/winner")
    public ResponseEntity<VoteResultTo> getTodayWinner() {
        log.info("get today`s winner");
        return voteService.getTodayWinner()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}

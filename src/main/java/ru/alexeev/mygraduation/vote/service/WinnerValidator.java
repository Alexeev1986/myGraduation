package ru.alexeev.mygraduation.vote.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.alexeev.mygraduation.vote.to.VoteResultTo;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class WinnerValidator {
    public Optional<VoteResultTo> determineWinner(List<VoteResultTo> results) {
        if (results == null || results.isEmpty()) {
            log.info("No results to determine winner");
            return Optional.empty();
        }

        int maxVotes = findMaxVotes(results);
        if (maxVotes == 0) {
            log.info("No votes cast, winner not present");
            return Optional.empty();
        }

        List<VoteResultTo> winners = findWinners(results, maxVotes);

        if (winners.size() > 1) {
            log.info("Draw detected between {} restaurants {}", winners.size(), winners.stream().map(VoteResultTo::getRestaurantName).toList());
            return Optional.empty();
        }

        log.info("Winner determinate: {} with {} votes", winners.getFirst().getRestaurantName(), winners.getFirst().getVotesCount());
        return Optional.of(winners.getFirst());
    }

    private int findMaxVotes(List<VoteResultTo> results) {
        return results.stream()
                .mapToInt(VoteResultTo::getVotesCount)
                .max()
                .orElse(0);
    }

    private List<VoteResultTo> findWinners(List<VoteResultTo> result, int maxVotes) {
        return result.stream()
                .filter(r -> r.getVotesCount() == maxVotes)
                .toList();
    }
}

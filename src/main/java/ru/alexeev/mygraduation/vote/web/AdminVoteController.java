package ru.alexeev.mygraduation.vote.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.alexeev.mygraduation.vote.service.VoteService;
import ru.alexeev.mygraduation.vote.to.VoteResultTo;
import ru.alexeev.mygraduation.vote.to.VoteStatsTo;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/admin/votes", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
public class AdminVoteController {

    private final VoteService voteService;

    @GetMapping("/results")
    public List<VoteResultTo> getResultsForDate(@RequestParam LocalDate date) {
        log.info("Admin get voting results for date {}", date);
        return voteService.getVoteResultsForDate(date);
    }

    @GetMapping("/results/range")
    public Map<LocalDate, List<VoteResultTo>> getResultsForDateRange(@RequestParam LocalDate start, @RequestParam LocalDate end) {
        log.info("Admin get voting results from {} to {}", start, end);
        return voteService.getVoteResultsForDateRange(start, end);
    }

    @GetMapping("/stats")
    public VoteStatsTo getGeneralStats() {
        log.info("Admin get general voting statistics");
        return voteService.getGeneralStats();
    }

    @GetMapping("/restaurants/{restaurantId}/today/count")
    public int getVotesCountForRestaurantToday(@PathVariable int restaurantId) {
        log.info("Admin get votes count for restaurant {} today", restaurantId);
        return voteService.getVotesCountForRestaurantToday(restaurantId);
    }
}

package ru.alexeev.mygraduation.vote.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alexeev.mygraduation.common.error.DataConflictException;
import ru.alexeev.mygraduation.restaurant.model.Restaurant;
import ru.alexeev.mygraduation.restaurant.repository.RestaurantRepository;
import ru.alexeev.mygraduation.user.model.User;
import ru.alexeev.mygraduation.user.repository.UserRepository;
import ru.alexeev.mygraduation.vote.model.Vote;
import ru.alexeev.mygraduation.vote.repository.VoteRepository;
import ru.alexeev.mygraduation.vote.to.VoteResultRecord;
import ru.alexeev.mygraduation.vote.to.VoteResultTo;
import ru.alexeev.mygraduation.vote.to.VoteStatsTo;
import ru.alexeev.mygraduation.vote.util.VoteCacheService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class VoteService {

    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final Clock clock;
    private final WinnerDeterminer winnerDeterminer;
    private final VoteCacheService voteCacheService;

    private static final LocalTime DEADLINE = LocalTime.of(11, 0);

    public List<VoteResultTo> getVoteResultsForDate(LocalDate date) {
        log.info("get vote results for date {}", date);
        return voteCacheService.getVoteResultsForDate(date);
    }

    @Transactional(readOnly = true)
    public Optional<VoteResultTo> getTodayWinner() {
        log.info("get today's winner");
        List<VoteResultTo> results = voteCacheService.getVoteResultsForDate(LocalDate.now(clock));
        return winnerDeterminer.determineWinner(results);
    }

    @CacheEvict(value = {"vote_results", "today_winner"}, allEntries = true)
    @Transactional
    public Vote createVote(int userId, int restaurantId) {
        log.info("create vote: user {} votes for restaurant {}", userId, restaurantId);
        LocalDate today = LocalDate.now(clock);
        Optional<Vote> existing = voteRepository.findByUserAndDate(userId, today);
        if (existing.isPresent()) {
            throw new DataConflictException("You have already voted today. Use update method to update your vore");
        }
        User user = userRepository.getExisted(userId);
        Restaurant restaurant = restaurantRepository.getExisted(restaurantId);
        LocalTime now = LocalTime.now(clock);

        Vote vote = new Vote(null, user, restaurant, today, now);
        log.info("Created new vote from user {} to restaurant {}", user.getId(), restaurant.getId());
        return voteRepository.save(vote);
    }

    @CacheEvict(value = {"vote_results", "today_winner"}, allEntries = true)
    @Transactional
    public Vote updateVote(int userId, int voteId, int restaurantId) {
        log.info("update vote: user {} votes for restaurant {}", userId, restaurantId);
        Vote existingVote = voteRepository.getExisted(voteId);
        if (existingVote.getUser().id() != userId) {
            throw new DataConflictException("you can only update your own vote");
        }

        LocalTime now = LocalTime.now(clock);
        if (now.isAfter(DEADLINE)) {
            throw new DataConflictException("Cannot change vote after 11:00");
        }

        Restaurant restaurant = restaurantRepository.getExisted(restaurantId);
        existingVote.setRestaurant(restaurant);
        existingVote.setVoteTime(now);

        log.info("updated vote {} to restaurant {}", existingVote.getId(), restaurant.getId());
        return voteRepository.save(existingVote);
    }

    public int getVotesCountForRestaurantToday(int restaurantId) {
        return (int) voteRepository.countByRestaurantAndDate(restaurantId, LocalDate.now(clock));
    }

    public List<Vote> findByUser(int userId) {
        log.info("Find all votes for user {}", userId);
        userRepository.getExisted(userId);
        return voteRepository.findByUser(userId)
                .orElse(List.of());
    }

    @Transactional(readOnly = true)
    public List<VoteResultTo> getTodayVoteResults() {
        log.info("get today results");
        return voteCacheService.getVoteResultsForDate(LocalDate.now(clock));
    }

    @Transactional(readOnly = true)
    public Map<LocalDate, List<VoteResultTo>> getVoteResultsForDateRange(LocalDate start, LocalDate end) {
        log.info("get vote results from {} to {}", start, end);

        List<VoteResultRecord> records = voteRepository.getVoteResultsForDateRange(start, end);

        Map<LocalDate, List<VoteResultTo>> results = new LinkedHashMap<>();

        for (VoteResultRecord record : records) {
            VoteResultTo resultTo = new VoteResultTo(record.restaurantId(), record.restaurantName(), record.votesCount());
            results.computeIfAbsent(record.voteDate(), k -> new ArrayList<>()).add(resultTo);
        }

        LocalDate current = start;
        while (!current.isAfter(end)) {
            results.putIfAbsent(current, List.of());
            current = current.plusDays(1);
        }
        return results;
    }

    @Transactional(readOnly = true)
    public VoteStatsTo getGeneralStats() {
        log.info("get general voting statistics");
        long totalVotes = voteRepository.count();
        long totalUsersWhoVoted = voteRepository.countDistinctUsers();

        double averageVotesPerUsers = totalUsersWhoVoted > 0 ? (double) totalVotes / totalUsersWhoVoted : 0;
        return new VoteStatsTo(totalVotes, totalUsersWhoVoted, averageVotesPerUsers);
    }
}

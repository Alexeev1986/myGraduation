package ru.alexeev.mygraduation.vote.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alexeev.mygraduation.common.error.DataConflictException;
import ru.alexeev.mygraduation.restaurant.model.Restaurant;
import ru.alexeev.mygraduation.restaurant.repository.RestaurantRepository;
import ru.alexeev.mygraduation.user.model.User;
import ru.alexeev.mygraduation.user.repository.UserRepository;
import ru.alexeev.mygraduation.vote.model.Vote;
import ru.alexeev.mygraduation.vote.repository.VoteRepository;
import ru.alexeev.mygraduation.vote.to.VoteResultTo;
import ru.alexeev.mygraduation.vote.to.VoteStatsTo;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ru.alexeev.mygraduation.vote.util.VoteUtil.convertToVoteResultTos;

@Service
@AllArgsConstructor
@Slf4j
public class VoteService {

    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final Clock clock;
    private final WinnerValidator winnerValidator;
    private final VoteService self;

    private static final LocalTime DEADLINE = LocalTime.of(11, 0);

    @CacheEvict(value = {"vote_results", "today_winner"}, allEntries = true)
    @Transactional
    public Vote vote(int userId, int restaurantId) {
        log.info("vote: user {} votes for restaurant {}", userId, restaurantId);

        User user = userRepository.getExisted(userId);
        Restaurant restaurant = restaurantRepository.getExisted(restaurantId);
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now(clock);

        if (now.isAfter(DEADLINE)) {
            throw new DataConflictException("Cannot vote or change vote after 11:00");
        }

        return voteRepository.findByUserAndDate(userId, today)
                .map(existingVote -> updateExistingVote(existingVote, restaurant, now))
                .orElseGet(() -> createNewVote(user, restaurant, today, now));
    }

    private Vote updateExistingVote(Vote existingVote, Restaurant restaurant, LocalTime now) {
        if (now.isAfter(DEADLINE)) {
            throw new DataConflictException("Cannot change vote after 11:00");
        }
        existingVote.setRestaurant(restaurant);
        existingVote.setVoteTime(now);
        log.info("Update vote {} to restaurant {}", existingVote.getId(), restaurant.getId());
        return voteRepository.save(existingVote);
    }

    private Vote createNewVote(User user, Restaurant restaurant, LocalDate today, LocalTime now) {
        Vote vote = new Vote(null, user, restaurant, today, now);
        log.info("Created new vote from user {} to restaurant {}", user.getId(), restaurant.getId());
        return voteRepository.save(vote);
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
        return self.getVoteResultsForDate(LocalDate.now(clock));
    }

    @Cacheable(value = "vote_results", key = "#date.toString()")
    @Transactional(readOnly = true)
    public List<VoteResultTo> getVoteResultsForDate(LocalDate date) {
        log.info("get vote results for date {}", date);
        List<Object[]> rawResults = voteRepository.getVoteResultsRawForDate(date);
        return convertToVoteResultTos(rawResults);
    }

    @Cacheable(value = "today_winner", key = "#root.methodName")
    @Transactional(readOnly = true)
    public Optional<VoteResultTo> getTodayWinner() {
        List<VoteResultTo> result = getTodayVoteResults();
        return winnerValidator.determineWinner(result);
    }

    @Transactional(readOnly = true)
    public Map<LocalDate, List<VoteResultTo>> getVoteResultsForDateRange(LocalDate start, LocalDate end) {
        log.info("get vote results from {} to {}", start, end);
        Map<LocalDate, List<VoteResultTo>> results = new LinkedHashMap<>();
        LocalDate current = start;
        while (!current.isAfter(end)) {
            results.put(current, self.getVoteResultsForDate(current));
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

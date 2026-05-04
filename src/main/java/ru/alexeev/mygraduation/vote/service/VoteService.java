package ru.alexeev.mygraduation.vote.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alexeev.mygraduation.common.error.DataConflictException;
import ru.alexeev.mygraduation.restaurant.model.Restaurant;
import ru.alexeev.mygraduation.restaurant.repository.RestaurantRepository;
import ru.alexeev.mygraduation.user.model.User;
import ru.alexeev.mygraduation.user.repository.UserRepository;
import ru.alexeev.mygraduation.vote.model.Vote;
import ru.alexeev.mygraduation.vote.repository.VoteRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class VoteService {

    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    private static final LocalTime DEADLINE = LocalTime.of(11, 0);

    @Transactional
    public Vote vote(int userId, int restaurantId) {
        log.info("vote: user {} votes for restaurant {}", userId, restaurantId);

        User user = userRepository.getExisted(userId);
        Restaurant restaurant = restaurantRepository.getExisted(restaurantId);
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

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
        return (int) voteRepository.countByRestaurantAndDate(restaurantId, LocalDate.now());
    }

    public List<Vote> findByUser(int userId) {
        log.info("Find all votes for user {}", userId);
        userRepository.getExisted(userId);
        return voteRepository.findByUser(userId)
                .orElse(List.of());
    }
}

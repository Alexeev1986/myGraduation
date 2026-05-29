package ru.alexeev.mygraduation.vote.util;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alexeev.mygraduation.restaurant.model.Restaurant;
import ru.alexeev.mygraduation.restaurant.repository.RestaurantRepository;
import ru.alexeev.mygraduation.vote.repository.VoteRepository;
import ru.alexeev.mygraduation.vote.to.VoteResultRecord;
import ru.alexeev.mygraduation.vote.to.VoteResultTo;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class VoteCacheService {

    private final VoteRepository voteRepository;

    private final RestaurantRepository restaurantRepository;

    @Cacheable(value = "vote_results", key = "#date.toString()")
    @Transactional
    public List<VoteResultTo> getVoteResultsForDate(LocalDate date) {
        log.info("get vote results for date {}", date);

        List<Restaurant> restaurantsWithMenu = restaurantRepository.findAllWithMenusByDate(date);

        if (restaurantsWithMenu.isEmpty()) {
            return List.of();
        }

        List<VoteResultRecord> records = voteRepository.getVoteResultsForDateRange(date, date);
        Map<Integer, Long> votesMap = records.stream()
                .collect(Collectors.toMap(
                        VoteResultRecord::restaurantId,
                        VoteResultRecord::votesCount
                ));

        return restaurantsWithMenu.stream()
                .map(r -> new VoteResultTo(
                        r.id(),
                        r.getName(),
                        votesMap.getOrDefault(r.getId(), 0L)
                )).toList();
    }
}

package ru.alexeev.mygraduation.vote.util;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alexeev.mygraduation.vote.repository.VoteRepository;
import ru.alexeev.mygraduation.vote.to.VoteResultRecord;
import ru.alexeev.mygraduation.vote.to.VoteResultTo;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class VoteCacheService {

    private final VoteRepository voteRepository;

    @Cacheable(value = "vote_results", key = "#date.toString()")
    @Transactional
    public List<VoteResultTo> getVoteResultsForDate(LocalDate date) {
        log.info("get vote results for date {}", date);
        List<VoteResultRecord> records = voteRepository.getVoteResultsForDateRange(date, date);
        return records.stream()
                .map(r -> new VoteResultTo(r.restaurantId(), r.restaurantName(), r.votesCount()))
                .toList();
    }
}

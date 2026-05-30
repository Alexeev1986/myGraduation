package ru.alexeev.mygraduation.vote.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.alexeev.mygraduation.AbstractServiceTest;
import ru.alexeev.mygraduation.app.config.TimeConfig;
import ru.alexeev.mygraduation.common.error.DataConflictException;
import ru.alexeev.mygraduation.common.error.NotFoundException;
import ru.alexeev.mygraduation.vote.model.Vote;
import ru.alexeev.mygraduation.vote.to.VoteResultTo;
import ru.alexeev.mygraduation.vote.to.VoteStatsTo;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static ru.alexeev.mygraduation.restaurant.RestaurantTestData.*;
import static ru.alexeev.mygraduation.user.UserTestData.*;
import static ru.alexeev.mygraduation.vote.VoteTestData.*;
import static ru.alexeev.mygraduation.vote.VoteTestData.NOT_FOUND;
import static ru.alexeev.mygraduation.vote.VoteTestData.TODAY;

@Import(TimeConfig.class)
class VoteServiceTest extends AbstractServiceTest {

    @Autowired
    private VoteService voteService;

    @MockitoBean
    private Clock clock;

    @BeforeEach
    void setUp() {
        setFixedTime(clock, 10, 30);
    }

    @Test
    void getTodayVoteResults() {
        setFixedDate(clock, TODAY, 10, 30);
        List<VoteResultTo> results = voteService.getTodayVoteResults();
        assertThat(results).isNotNull();
        assertThat(results).isNotEmpty();

        int totalVotes = results.stream().mapToInt(VoteResultTo::getVotesCount).sum();
        assertThat(totalVotes).isEqualTo(2);
    }

    @Test
    void getVotesCountNotFoundRestaurantToday() {
        setFixedTime(clock, 11, 30);
        int count = voteService.getVotesCountForRestaurantToday(NOT_FOUND);
        assertThat(count).isZero();
    }

    @ParameterizedTest
    @CsvSource({
            "10, 30, true",
            "10, 59, true",
            "11, 0, true",
            "11, 1, true",
            "12, 0, true"
    })
    void createNewVoteForNewUserAtDifferentTimes(int hour, int minute, boolean shouldSucceed) {
        setFixedTime(clock, hour, minute);

        if (shouldSucceed) {
            Vote vote = voteService.createVote(VOTER_ID, RESTAURANT3_ID);
            assertThat(vote).isNotNull();
            assertThat(vote.getUser().id()).isEqualTo(VOTER_ID);
            assertThat(vote.getRestaurant().id()).isEqualTo(RESTAURANT3_ID);

            Vote savedVote = voteService.findByUser(VOTER_ID).getFirst();
            assertThat(savedVote.getRestaurant().id()).isEqualTo(RESTAURANT3_ID);
        } else {
            assertThatThrownBy(() -> voteService.createVote(VOTER_ID, RESTAURANT3_ID))
                    .isInstanceOf(DataConflictException.class)
                    .hasMessageContaining("Cannot vote or change vote after 11:00");
        }
    }

    @ParameterizedTest
    @CsvSource({
            "10, 30, true",
            "10, 59, true",
            "11, 0, true",
            "11, 1, false",
            "12, 0, false"
    })
    void updateExistingVoteForNewUserAtDifferentTimes(int hour, int minute, boolean shouldSucceed) {
        setFixedTime(clock, 10, 30);
        Vote firstVote = voteService.createVote(VOTER_ID, RESTAURANT1_ID);

        setFixedTime(clock, hour, minute);

        if (shouldSucceed) {
            Vote updatedVote = voteService.updateVote(VOTER_ID, firstVote.getId(),RESTAURANT3_ID);
            assertThat(updatedVote.getRestaurant().id()).isEqualTo(RESTAURANT3_ID);
            assertThat(updatedVote.id()).isEqualTo(firstVote.id());
            assertThat(updatedVote.getVoteTime()).isEqualTo(LocalTime.of(hour, minute));
        } else {
            assertThatThrownBy(() -> voteService.updateVote(VOTER_ID, firstVote.getId(),RESTAURANT3_ID))
                    .isInstanceOf(DataConflictException.class)
                    .hasMessageContaining("Cannot change vote after 11:00");

            Vote unchangeVote = voteService.findByUser(VOTER_ID).getFirst();
            assertThat(unchangeVote.getRestaurant().id()).isEqualTo(RESTAURANT1_ID);
        }
    }

    @Test
    void updateVoteForOtherUser() {
        setFixedTime(clock, 10, 30);
        Vote vote = voteService.createVote(VOTER_ID, RESTAURANT1_ID);
        assertThatThrownBy(() -> voteService.updateVote(USER_ID, vote.id(), RESTAURANT2_ID))
                .isInstanceOf(DataConflictException.class)
                .hasMessageContaining("You can only update your own vote");
    }

    @Test
    void updateNonExistentVoteShouldFail() {
        setFixedTime(clock, 10, 30);
        assertThatThrownBy(() -> voteService.updateVote(USER_ID, NOT_FOUND, RESTAURANT1_ID))
                .isInstanceOf(NotFoundException.class);
    }


    @Test
    void voteUserNotFound() {
        assertThatThrownBy(() -> voteService.createVote(NOT_FOUND, RESTAURANT1_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Entity with id=" + NOT_FOUND + " not found");
    }

    @Test
    void voteRestaurantNotFound() {
        setFixedTime(clock, 10, 30);
        assertThatThrownBy(() -> voteService.createVote(VOTER_ID, NOT_FOUND))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Entity with id=" + NOT_FOUND + " not found");
    }

    @Test
    void findByUser() {
        List<Vote> votesForUser = voteService.findByUser(USER_ID);
        VOTE_MATCHER.assertMatch(votesForUser, List.of(vote1, vote3, vote6));

        List<Vote> votesForAdmin = voteService.findByUser(ADMIN_ID);
        VOTE_MATCHER.assertMatch(votesForAdmin, List.of(vote2, vote4, vote7));
    }

    @Test
    void voteSameRestaurantTwice() {
        setFixedTime(clock, 10, 30);
        Vote firstVote = voteService.createVote(VOTER_ID, RESTAURANT1_ID);
        assertThatThrownBy(() -> voteService.createVote(VOTER_ID, RESTAURANT1_ID))
                .isInstanceOf(DataConflictException.class)
                .hasMessageContaining("You have already voted today. Use update method to update your vote");
    }

    @Test
    void findByUserNotFound() {
        assertThatThrownBy(() -> voteService.findByUser(NOT_FOUND))
                .isInstanceOf(NotFoundException.class);
    }

    @ParameterizedTest
    @CsvSource({
            "-2, 1, 2",
            "-2, 2, 1",
            "-2, 3, 0",
            "-1, 1, 1",
            "-1, 2, 1",
            "-1, 3, 1",
            "0, 1, 1",
            "0, 2, 1",
            "0, 3, 0"
    })
    void getVoteResultsForDate(int dayOffset, int restaurantId, Integer expectedCount) {
        LocalDate date = LocalDate.now(clock).plusDays(dayOffset);
        setFixedDate(clock, date, 10, 30);

        List<VoteResultTo> results = voteService.getVoteResultsForDate(date);
        assertThat(results).isNotNull();
        Optional<VoteResultTo> result = results.stream()
                .filter(r -> r.getRestaurantId().equals(restaurantId))
                .findFirst();

        if (expectedCount == 0) {
            result.ifPresent(r -> assertThat(r.getVotesCount()).isZero());
        } else {
            assertThat(result).isPresent();
            assertThat(result.get().getVotesCount()).isEqualTo(expectedCount);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "-2, 1, true",
            "-1, 1, false",
            "0, 1, false",
            "10, 0, false"
    })
    void getTodayWinner(int dayOffset, int expectedWinnerId, boolean shouldBePresent) {
        LocalDate date = LocalDate.now().plusDays(dayOffset);
        setFixedDate(clock, date, 10, 30);

        Optional<VoteResultTo> winner = voteService.getTodayWinner();
        if (shouldBePresent) {
            assertThat(winner).isPresent();
            assertThat(winner.get().getRestaurantId()).isEqualTo(expectedWinnerId);
        } else {
            assertThat(winner).isEmpty();
        }
    }



    @ParameterizedTest
    @CsvSource({
            "-2, 3",
            "-1, 3",
            "0, 2",
            "10, 0"
    })
    void getTotalVotesForDate(int dayOffset, int expectedTotalVotes) {
        LocalDate date = LocalDate.now().plusDays(dayOffset);
        setFixedDate(clock, date, 10, 30);

        List<VoteResultTo> results = voteService.getVoteResultsForDate(date);

        int totalVotes = results.stream()
                .mapToInt(VoteResultTo::getVotesCount)
                .sum();
        assertThat(totalVotes).isEqualTo(expectedTotalVotes);
    }

    @ParameterizedTest
    @CsvSource({
            "-2, -1, 2",
            "-2, 0, 3",
            "-1, 0, 2",
            "0, 0, 1",
            "-2, -2, 1"
    })
    void getVoteResultsForDateRange(int startOffset, int endOffset, int expectedDays) {
        LocalDate start = LocalDate.now().plusDays(startOffset);
        LocalDate end = LocalDate.now().plusDays(endOffset);

        Map<LocalDate, List<VoteResultTo>> results = voteService.getVoteResultsForDateRange(start, end);

        assertThat(results).isNotNull();
        assertThat(results.size()).isEqualTo(expectedDays);

        LocalDate current = start;
        while (!current.isAfter(end)) {
            assertThat(results).containsKey(current);
            current = current.plusDays(1);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "1, 3",
            "2, 3",
            "3, 2"
    })
    void getUserTotalVotes(int userId, int expectedVotesCount) {
        List<Vote> votes = voteService.findByUser(userId);
        assertThat(votes).isNotNull();
        assertThat(votes.size()).isEqualTo(expectedVotesCount);
    }

    @ParameterizedTest
    @CsvSource({
            "1, 1",
            "2, 1",
            "3, 0"
    })
    void getVotesCountForRestaurantToday(int restaurantId, int expectedCount) {
        setFixedDate(clock, LocalDate.now(clock), 10, 30);
        int actualCount = voteService.getVotesCountForRestaurantToday(restaurantId);
        assertThat(actualCount).isEqualTo(expectedCount);
    }


    @Test
    void getGeneralStats() {
        VoteStatsTo stats = voteService.getGeneralStats();
        VOTE_STATS_TO_MATCHER.assertMatch(stats, expectedStats);
    }

    @Test
    void getGeneralStatsAfterNewVote() {
        setFixedTime(clock, 10, 30);
        VoteStatsTo beforeStats = voteService.getGeneralStats();
        voteService.createVote(VOTER_ID, RESTAURANT1_ID);

        VoteStatsTo afterStats = voteService.getGeneralStats();

        assertThat(afterStats.getTotalVotes()).isEqualTo(beforeStats.getTotalVotes() + 1);
        assertThat(afterStats.getTotalUserWhoVoted()).isEqualTo(beforeStats.getTotalUserWhoVoted());
    }

    @Test
    void getAllRestaurantsHaveResultsWithZeroVotes() {
        LocalDate futureDate = LocalDate.now().plusDays(10);
        setFixedDate(clock, futureDate, 10, 30);
        List<VoteResultTo> results = voteService.getVoteResultsForDate(futureDate);

        assertThat(results).isEmpty();
    }
}
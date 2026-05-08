package ru.alexeev.mygraduation.vote.service;

import org.junit.jupiter.api.Test;
import ru.alexeev.mygraduation.vote.to.VoteResultTo;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.alexeev.mygraduation.restaurant.RestaurantTestData.RESTAURANT1_ID;
import static ru.alexeev.mygraduation.restaurant.RestaurantTestData.RESTAURANT2_ID;
import static ru.alexeev.mygraduation.vote.VoteTestData.TODAY_EXPECTED_RESULTS;
import static ru.alexeev.mygraduation.vote.VoteTestData.TWO_DAYS_AGO_EXPECTED_RESULTS;
import static ru.alexeev.mygraduation.vote.VoteTestData.YESTERDAY_EXPECTED_RESULTS;
import static ru.alexeev.mygraduation.vote.VoteTestData.twoDaysAgoResultForRestaurant1;
import static ru.alexeev.mygraduation.vote.WinnerValidatorTestData.*;

public class WinnerValidatorTest {

    private final WinnerValidator winnerValidator = new WinnerValidator();

    @Test
    void determineWinner_WithOneWinner() {
        Optional<VoteResultTo> winner = winnerValidator.determineWinner(WINNER_SCENARIO_RESULTS);

        assertThat(winner).isPresent();
        assertThat(winner.get().getRestaurantId()).isEqualTo(RESTAURANT1_ID);
        assertThat(winner.get().getVotesCount()).isEqualTo(5);
    }

    @Test
    void determineWinner_WithDrawBetweenTwo() {
        Optional<VoteResultTo> winner = winnerValidator.determineWinner(DRAW_TWO_SCENARIO_RESULTS);

        assertThat(winner).isEmpty();
    }

    @Test
    void determineWinner_WithDrawBetweenThree() {
        Optional<VoteResultTo> winner = winnerValidator.determineWinner(DRAW_ALL_SCENARIO_RESULTS);

        assertThat(winner).isEmpty();
    }

    @Test
    void determineWinner_WithNotVotes() {
        Optional<VoteResultTo> winner = winnerValidator.determineWinner(NO_VOTES_SCENARIO_RESULTS);

        assertThat(winner).isEmpty();
    }


    @Test
    void determineWinner_WithResultsIsNull() {
        Optional<VoteResultTo> winner = winnerValidator.determineWinner(null);

        assertThat(winner).isEmpty();
    }

    @Test
    void determineWinner_WithSingleRestaurant() {
        Optional<VoteResultTo> winner = winnerValidator.determineWinner(SINGLE_WINNER_SCENARIO_RESULTS);

        assertThat(winner).isPresent();
        assertThat(winner.get().getRestaurantId()).isEqualTo(RESTAURANT1_ID);
        assertThat(winner.get().getVotesCount()).isEqualTo(10);
    }

    @Test
    void determineWinner_WithWinnerHasMoreVotes() {
        Optional<VoteResultTo> winner = winnerValidator.determineWinner(BIG_WINNER_SCENARIO_RESULTS);

        assertThat(winner).isPresent();
        assertThat(winner.get().getRestaurantId()).isEqualTo(RESTAURANT2_ID);
        assertThat(winner.get().getVotesCount()).isEqualTo(7);
    }

    @Test
    void determineWinner_WithOneRestaurantHasVotesAndOthersZero() {
        Optional<VoteResultTo> winner = winnerValidator.determineWinner(ONLY_WINNER_SCENARIO_RESULTS);

        assertThat(winner).isPresent();
        assertThat(winner.get().getRestaurantId()).isEqualTo(RESTAURANT2_ID);
        assertThat(winner.get().getVotesCount()).isEqualTo(5);
    }

    @Test
    void determineWinner_WithTwoDaysAgoData() {
        Optional<VoteResultTo> winner = winnerValidator.determineWinner(TWO_DAYS_AGO_EXPECTED_RESULTS);

        assertThat(winner).isPresent();
        assertThat(winner.get().getRestaurantId()).isEqualTo(twoDaysAgoResultForRestaurant1.getRestaurantId());
        assertThat(winner.get().getVotesCount()).isEqualTo(2);
    }

    @Test
    void determineWinner_WithYesterdayData() {
        Optional<VoteResultTo> winner = winnerValidator.determineWinner(YESTERDAY_EXPECTED_RESULTS);

        assertThat(winner).isEmpty();
    }

    @Test
    void determineWinner_WithTodayData() {
        Optional<VoteResultTo> winner = winnerValidator.determineWinner(TODAY_EXPECTED_RESULTS);

        assertThat(winner).isEmpty();
    }
}

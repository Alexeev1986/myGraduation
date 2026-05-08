package ru.alexeev.mygraduation.vote;

import ru.alexeev.mygraduation.vote.to.VoteResultTo;

import java.util.List;

import static ru.alexeev.mygraduation.restaurant.RestaurantTestData.*;

public class WinnerValidatorTestData {
    public static final VoteResultTo winnerScenarioRestaurant1 = new VoteResultTo(RESTAURANT1_ID, restaurant1.getName(), 5);
    public static final VoteResultTo winnerScenarioRestaurant2 = new VoteResultTo(RESTAURANT2_ID, restaurant2.getName(), 3);
    public static final VoteResultTo winnerScenarioRestaurant3 = new VoteResultTo(RESTAURANT3_ID, restaurant3.getName(), 1);

    public static final List<VoteResultTo> WINNER_SCENARIO_RESULTS = List.of(winnerScenarioRestaurant1, winnerScenarioRestaurant2, winnerScenarioRestaurant3);

    public static final VoteResultTo drawTwoRestaurant1 = new VoteResultTo(RESTAURANT1_ID, restaurant1.getName(), 3);
    public static final VoteResultTo drawTwoRestaurant2 = new VoteResultTo(RESTAURANT2_ID, restaurant2.getName(), 3);
    public static final VoteResultTo drawTwoRestaurant3 = new VoteResultTo(RESTAURANT3_ID, restaurant3.getName(), 1);

    public static final List<VoteResultTo> DRAW_TWO_SCENARIO_RESULTS = List.of(drawTwoRestaurant1, drawTwoRestaurant2, drawTwoRestaurant3);

    public static final VoteResultTo drawAllRestaurant1 = new VoteResultTo(RESTAURANT1_ID, restaurant1.getName(), 2);
    public static final VoteResultTo drawAllRestaurant2 = new VoteResultTo(RESTAURANT2_ID, restaurant2.getName(), 2);
    public static final VoteResultTo drawAllRestaurant3 = new VoteResultTo(RESTAURANT3_ID, restaurant3.getName(), 2);

    public static final List<VoteResultTo> DRAW_ALL_SCENARIO_RESULTS = List.of(drawAllRestaurant1, drawAllRestaurant2, drawAllRestaurant3);

    public static final VoteResultTo singleWinnerRestaurant = new VoteResultTo(RESTAURANT1_ID, restaurant1.getName(), 10);

    public static final List<VoteResultTo> SINGLE_WINNER_SCENARIO_RESULTS = List.of(singleWinnerRestaurant);

    public static final VoteResultTo bigWinnerRestaurant1 = new VoteResultTo(RESTAURANT1_ID, restaurant1.getName(), 2);
    public static final VoteResultTo bigWinnerRestaurant2 = new VoteResultTo(RESTAURANT2_ID, restaurant2.getName(), 7);
    public static final VoteResultTo bigWinnerRestaurant3 = new VoteResultTo(RESTAURANT3_ID, restaurant3.getName(), 4);

    public static final List<VoteResultTo> BIG_WINNER_SCENARIO_RESULTS = List.of(bigWinnerRestaurant1, bigWinnerRestaurant2, bigWinnerRestaurant3);

    public static final VoteResultTo onlyWinnerRestaurant1 = new VoteResultTo(RESTAURANT1_ID, restaurant1.getName(), 0);
    public static final VoteResultTo onlyWinnerRestaurant2 = new VoteResultTo(RESTAURANT2_ID, restaurant2.getName(), 5);
    public static final VoteResultTo onlyWinnerRestaurant3 = new VoteResultTo(RESTAURANT3_ID, restaurant3.getName(), 0);

    public static final List<VoteResultTo> ONLY_WINNER_SCENARIO_RESULTS = List.of(onlyWinnerRestaurant1, onlyWinnerRestaurant2, onlyWinnerRestaurant3);

    public static final VoteResultTo noVotesRestaurant1 = new VoteResultTo(RESTAURANT1_ID, restaurant1.getName(), 0);
    public static final VoteResultTo noVotesRestaurant2 = new VoteResultTo(RESTAURANT2_ID, restaurant2.getName(), 0);
    public static final VoteResultTo noVotesRestaurant3 = new VoteResultTo(RESTAURANT3_ID, restaurant3.getName(), 0);

    public static final List<VoteResultTo> NO_VOTES_SCENARIO_RESULTS = List.of(noVotesRestaurant1, noVotesRestaurant2, noVotesRestaurant3);
}

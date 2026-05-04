package ru.alexeev.mygraduation.vote;

import ru.alexeev.mygraduation.MatcherFactory;
import ru.alexeev.mygraduation.restaurant.model.Restaurant;
import ru.alexeev.mygraduation.user.model.User;
import ru.alexeev.mygraduation.vote.model.Vote;

import java.time.LocalDate;
import java.time.LocalTime;

import static ru.alexeev.mygraduation.restaurant.RestaurantTestData.restaurant1;
import static ru.alexeev.mygraduation.restaurant.RestaurantTestData.restaurant2;
import static ru.alexeev.mygraduation.user.UserTestData.*;

public class VoteTestData {
    public static final MatcherFactory.Matcher<Vote> VOTE_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(Vote.class, "user", "restaurant");

    public static final int VOTE1_ID = 1;
    public static final int VOTE2_ID = 2;
    public static final int NOT_FOUND = 100;

    public static final LocalDate TODAY = LocalDate.now();
    public static final LocalDate YESTERDAY = LocalDate.now().minusDays(1);
    public static final LocalTime USER_VOTE_TIME = LocalTime.of(10, 30);
    public static final LocalTime ADMIN_VOTE_TIME = LocalTime.of(9, 15);
    public static final LocalTime AFTERNOON = LocalTime.of(12, 0);
    public static final LocalTime DEADLINE = LocalTime.of(11, 0);

    public static final Vote vote1 = new Vote(VOTE1_ID, user, restaurant1, TODAY, USER_VOTE_TIME);
    public static final Vote vote2 = new Vote(VOTE2_ID, admin, restaurant2, TODAY, ADMIN_VOTE_TIME);

    public static Vote getNewVote(User user, Restaurant restaurant, LocalDate date, LocalTime time) {
        return new Vote(null, user, restaurant, date, time);
    }
}

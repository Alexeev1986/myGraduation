package ru.alexeev.mygraduation.vote;

import ru.alexeev.mygraduation.MatcherFactory;
import ru.alexeev.mygraduation.vote.model.Vote;
import ru.alexeev.mygraduation.vote.to.VoteResultTo;
import ru.alexeev.mygraduation.vote.to.VoteStatsTo;
import ru.alexeev.mygraduation.vote.to.VoteTo;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static ru.alexeev.mygraduation.restaurant.RestaurantTestData.*;
import static ru.alexeev.mygraduation.user.UserTestData.*;

public class VoteTestData {

    public static final MatcherFactory.Matcher<Vote> VOTE_MATCHER = MatcherFactory.usingIgnoringFieldsComparator(Vote.class, "user", "restaurant");

    public static final MatcherFactory.Matcher<VoteTo> VOTE_TO_MATCHER = MatcherFactory.usingIgnoringFieldsComparator(VoteTo.class);

    public static final MatcherFactory.Matcher<VoteResultTo> VOTE_RESULT_TO_MATCHER = MatcherFactory.usingIgnoringFieldsComparator(VoteResultTo.class);

    public static final MatcherFactory.Matcher<VoteStatsTo> VOTE_STATS_TO_MATCHER = MatcherFactory.usingIgnoringFieldsComparator(VoteStatsTo.class);

    public static final int VOTE1_ID = 7;
    public static final int VOTE2_ID = 8;
    public static final int VOTE3_ID = 4;
    public static final int VOTE4_ID = 5;
    public static final int VOTE5_ID = 6;
    public static final int VOTE6_ID = 1;
    public static final int VOTE7_ID = 2;
    public static final int VOTE8_ID = 3;
    public static final int NOT_FOUND = 100;

    public static final LocalDate TODAY = LocalDate.now();
    public static final LocalDate YESTERDAY = LocalDate.now().minusDays(1);
    public static final LocalDate TWO_DAYS_AGO = LocalDate.now().minusDays(2);

    public static final LocalTime USER_VOTE_TIME_TODAY = LocalTime.of(10, 30);
    public static final LocalTime ADMIN_VOTE_TIME_TODAY = LocalTime.of(9, 15);
    public static final LocalTime USER_VOTE_TIME_YESTERDAY = LocalTime.of(10, 30);
    public static final LocalTime ADMIN_VOTE_TIME_YESTERDAY = LocalTime.of(9, 15);
    public static final LocalTime GUEST_VOTE_TIME_YESTERDAY = LocalTime.of(11, 30);
    public static final LocalTime USER_VOTE_TIME_TWO_DAYS_AGO = LocalTime.of(10, 15);
    public static final LocalTime ADMIN_VOTE_TIME_TWO_DAYS_AGO = LocalTime.of(10, 0);
    public static final LocalTime GUEST_VOTE_TIME_TWO_DAYS_AGO = LocalTime.of(9, 45);

    public static final Vote vote1 = new Vote(VOTE1_ID, user, restaurant1, TODAY, USER_VOTE_TIME_TODAY);
    public static final Vote vote2 = new Vote(VOTE2_ID, admin, restaurant2, TODAY, ADMIN_VOTE_TIME_TODAY);

    public static final Vote vote3 = new Vote(VOTE3_ID, user, restaurant1, YESTERDAY, USER_VOTE_TIME_YESTERDAY);
    public static final Vote vote4 = new Vote(VOTE4_ID, admin, restaurant3, YESTERDAY, ADMIN_VOTE_TIME_YESTERDAY);
    public static final Vote vote5 = new Vote(VOTE5_ID, guest, restaurant2, YESTERDAY, GUEST_VOTE_TIME_YESTERDAY);

    public static final Vote vote6 = new Vote(VOTE6_ID, user, restaurant2, TWO_DAYS_AGO, USER_VOTE_TIME_TWO_DAYS_AGO);
    public static final Vote vote7 = new Vote(VOTE7_ID, admin, restaurant1, TWO_DAYS_AGO, ADMIN_VOTE_TIME_TWO_DAYS_AGO);
    public static final Vote vote8 = new Vote(VOTE8_ID, guest, restaurant1, TWO_DAYS_AGO, GUEST_VOTE_TIME_TWO_DAYS_AGO);

    public static final VoteResultTo todayResultForRestaurant1 = new VoteResultTo(RESTAURANT1_ID, restaurant1.getName(), 1);
    public static final VoteResultTo todayResultForRestaurant2 = new VoteResultTo(RESTAURANT2_ID, restaurant2.getName(), 1);
    public static final VoteResultTo todayResultForRestaurant3 = new VoteResultTo(RESTAURANT3_ID, restaurant3.getName(), 0);

    public static final VoteResultTo yesterdayResultForRestaurant1 = new VoteResultTo(RESTAURANT1_ID, restaurant1.getName(), 1);
    public static final VoteResultTo yesterdayResultForRestaurant2 = new VoteResultTo(RESTAURANT2_ID, restaurant2.getName(), 1);
    public static final VoteResultTo yesterdayResultForRestaurant3 = new VoteResultTo(RESTAURANT3_ID, restaurant3.getName(), 1);

    public static final VoteResultTo twoDaysAgoResultForRestaurant1 = new VoteResultTo(RESTAURANT1_ID, restaurant1.getName(), 2);
    public static final VoteResultTo twoDaysAgoResultForRestaurant2 = new VoteResultTo(RESTAURANT2_ID, restaurant2.getName(), 1);
    public static final VoteResultTo twoDaysAgoResultForRestaurant3 = new VoteResultTo(RESTAURANT3_ID, restaurant3.getName(), 0);

    public static final List<VoteResultTo> TODAY_EXPECTED_RESULTS = List.of(todayResultForRestaurant1, todayResultForRestaurant2, todayResultForRestaurant3);
    public static final List<VoteResultTo> YESTERDAY_EXPECTED_RESULTS = List.of(yesterdayResultForRestaurant1, yesterdayResultForRestaurant2, yesterdayResultForRestaurant3);
    public static final List<VoteResultTo> TWO_DAYS_AGO_EXPECTED_RESULTS = List.of(twoDaysAgoResultForRestaurant1, twoDaysAgoResultForRestaurant2, twoDaysAgoResultForRestaurant3);

    public static final VoteStatsTo expectedStats = new VoteStatsTo(8, 3, 8.0 / 3.0);

    public static void setFixedTime(Clock clock, int hour, int minute) {
        LocalDateTime fixedTime = LocalDateTime.now()
                .withHour(hour)
                .withMinute(minute)
                .withSecond(0)
                .withNano(0);
        when(clock.instant()).thenReturn(fixedTime.atZone(ZoneId.systemDefault()).toInstant());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
    }

    public static void setFixedDate(Clock clock, LocalDate fixedDate, int hour, int minute) {
        LocalDateTime fixedTime = fixedDate.atTime(hour, minute, 0);
        when(clock.instant()).thenReturn(fixedTime.atZone(ZoneId.systemDefault()).toInstant());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
    }

    public static Integer getVotesForRestaurant(List<VoteResultTo> results, int restaurantId) {
        return results.stream()
                .filter(r -> r.getRestaurantId().equals(restaurantId))
                .findFirst()
                .map(VoteResultTo::getVotesCount)
                .orElse(0);
    }

    public static void assertSortedByVotesDesc(List<VoteResultTo> results) {
        for (int i = 0; i < results.size() - 1; i++) {
            assertThat(results.get(i).getVotesCount())
                    .isGreaterThanOrEqualTo(results.get(i + 1).getVotesCount());
        }
    }
}

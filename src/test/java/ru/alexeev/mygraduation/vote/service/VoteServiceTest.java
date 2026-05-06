package ru.alexeev.mygraduation.vote.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.alexeev.mygraduation.app.config.TimeConfig;
import ru.alexeev.mygraduation.common.error.DataConflictException;
import ru.alexeev.mygraduation.common.error.NotFoundException;
import ru.alexeev.mygraduation.user.model.User;
import ru.alexeev.mygraduation.user.service.UserService;
import ru.alexeev.mygraduation.vote.model.Vote;
import java.time.Clock;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static ru.alexeev.mygraduation.restaurant.RestaurantTestData.RESTAURANT1_ID;
import static ru.alexeev.mygraduation.restaurant.RestaurantTestData.RESTAURANT3_ID;
import static ru.alexeev.mygraduation.user.UserTestData.ADMIN_ID;
import static ru.alexeev.mygraduation.user.UserTestData.USER_ID;
import static ru.alexeev.mygraduation.user.UserTestData.getNew;
import static ru.alexeev.mygraduation.user.util.UsersUtil.createToFromUser;
import static ru.alexeev.mygraduation.vote.VoteTestData.NOT_FOUND;
import static ru.alexeev.mygraduation.vote.VoteTestData.VOTE_MATCHER;
import static ru.alexeev.mygraduation.vote.VoteTestData.setFixedTime;
import static ru.alexeev.mygraduation.vote.VoteTestData.vote1;
import static ru.alexeev.mygraduation.vote.VoteTestData.vote2;

@SpringBootTest
@Transactional
@Import(TimeConfig.class)
@ActiveProfiles("test")
class VoteServiceTest {

    @Autowired
    private VoteService voteService;

    @Autowired
    private UserService userService;

    @MockitoBean
    private Clock clock;


    @BeforeEach
    void setUp() {

        setFixedTime(clock, 10, 30);
    }

    @ParameterizedTest
    @CsvSource({
            "1, 1",
            "3, 0"})
    void getVotesCountForRestaurantToday(int restaurantId, int expectedCount) {
        setFixedTime(clock, 11, 30);
        int count = voteService.getVotesCountForRestaurantToday(restaurantId);
        assertThat(count).isEqualTo(expectedCount);
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
            "11, 1, false",
            "12, 0, false"
    })
    void createNewVoteForNewUserAtDifferentTimes(int hour, int minute, boolean shouldSucceed) {
        setFixedTime(clock, hour, minute);
        User savedUser = createNewUser();

        if (shouldSucceed) {
            Vote vote = voteService.vote(savedUser.getId(), RESTAURANT3_ID);
            assertThat(vote).isNotNull();
            assertThat(vote.getUser().id()).isEqualTo(savedUser.id());
            assertThat(vote.getRestaurant().id()).isEqualTo(RESTAURANT3_ID);

            Vote savedVote = voteService.findByUser(savedUser.getId()).getFirst();
            assertThat(savedVote.getRestaurant().id()).isEqualTo(RESTAURANT3_ID);
        } else {
            assertThatThrownBy(() -> voteService.vote(savedUser.getId(), RESTAURANT3_ID))
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
        Vote firstVote = voteService.vote(USER_ID, RESTAURANT1_ID);

        setFixedTime(clock, hour, minute);

        if (shouldSucceed) {
            Vote updatedVote = voteService.vote(USER_ID, RESTAURANT3_ID);
            assertThat(updatedVote.getRestaurant().id()).isEqualTo(RESTAURANT3_ID);
            assertThat(updatedVote.id()).isEqualTo(firstVote.id());
            assertThat(updatedVote.getVoteTime()).isEqualTo(LocalTime.of(hour, minute));
        } else {
            assertThatThrownBy(() -> voteService.vote(USER_ID, RESTAURANT3_ID))
                    .isInstanceOf(DataConflictException.class)
                    .hasMessageContaining("Cannot vote or change vote after 11:00");

            Vote unchangeVote = voteService.findByUser(USER_ID).getFirst();
            assertThat(unchangeVote.getRestaurant().id()).isEqualTo(RESTAURANT1_ID);
        }
    }


    @Test
    void voteUserNotFound() {
        assertThatThrownBy(() -> voteService.vote(NOT_FOUND, RESTAURANT1_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Entity with id=" + NOT_FOUND + " not found");
    }

    @Test
    void voteRestaurantNotFound() {
        assertThatThrownBy(() -> voteService.vote(USER_ID, NOT_FOUND))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Entity with id=" + NOT_FOUND + " not found");
    }

    @Test
    void findByUser() {
        List<Vote> votesForUser = voteService.findByUser(USER_ID);
        VOTE_MATCHER.assertMatch(votesForUser, vote1);

        List<Vote> votesForAdmin = voteService.findByUser(ADMIN_ID);
        VOTE_MATCHER.assertMatch(votesForAdmin, vote2);
    }

    @Test
    void voteSameRestaurantTwice() {
        setFixedTime(clock, 10, 30);

        Vote firstVote = voteService.vote(USER_ID, RESTAURANT1_ID);
        Vote secondVote = voteService.vote(USER_ID, RESTAURANT1_ID);

        VOTE_MATCHER.assertMatch(firstVote, secondVote);

        List<Vote> votes = voteService.findByUser(USER_ID);
        assertThat(votes.size()).isEqualTo(1);
    }

    @Test
    void findByUserNotFound() {
        assertThatThrownBy(() -> voteService.findByUser(NOT_FOUND))
                .isInstanceOf(NotFoundException.class);
    }

    private User createNewUser() {
        User newUser = getNew();
        return userService.create(createToFromUser(newUser));
    }
}
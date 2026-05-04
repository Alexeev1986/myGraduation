package ru.alexeev.mygraduation.vote.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.alexeev.mygraduation.common.error.NotFoundException;
import ru.alexeev.mygraduation.user.model.User;
import ru.alexeev.mygraduation.user.service.UserService;
import ru.alexeev.mygraduation.vote.model.Vote;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static ru.alexeev.mygraduation.restaurant.RestaurantTestData.RESTAURANT1_ID;
import static ru.alexeev.mygraduation.restaurant.RestaurantTestData.RESTAURANT3_ID;
import static ru.alexeev.mygraduation.user.UserTestData.*;
import static ru.alexeev.mygraduation.user.util.UsersUtil.createToFromUser;
import static ru.alexeev.mygraduation.vote.VoteTestData.VOTE_MATCHER;
import static ru.alexeev.mygraduation.vote.VoteTestData.vote1;

@SpringBootTest
@Transactional
class VoteServiceTest {

    @Autowired
    private VoteService voteService;

    @Autowired
    private UserService userService;

    @Test
    void createNewVoteForNewUser() {
        User newUser = getNew();
        User savedUser = userService.create(createToFromUser(newUser));
        Vote vote = voteService.vote(savedUser.getId(), RESTAURANT3_ID);
        assertThat(vote).isNotNull();
        assertThat(vote.getUser().id()).isEqualTo(savedUser.id());
        assertThat(vote.getRestaurant().id()).isEqualTo(RESTAURANT3_ID);

        Vote savedVote = voteService.findByUser(savedUser.getId()).getFirst();
        assertThat(savedVote.getRestaurant().id()).isEqualTo(RESTAURANT3_ID);
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
    void getVotesCountForRestaurantToday() {
        int count = voteService.getVotesCountForRestaurantToday(RESTAURANT1_ID);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void getVotesCountForRestaurantTodayNoVotes() {
        int count = voteService.getVotesCountForRestaurantToday(RESTAURANT3_ID);
        assertThat(count).isZero();
    }

    @Test
    void findByUser() {
        List<Vote> votes = voteService.findByUser(USER_ID);
        VOTE_MATCHER.assertMatch(votes, vote1);
    }

    @Test
    void findByUserNotFound() {
        assertThatThrownBy(() -> voteService.findByUser(NOT_FOUND))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateExistingVote() {
        Vote firstVote = voteService.vote(USER_ID, RESTAURANT1_ID);

        Vote updateVote = voteService.vote(USER_ID, RESTAURANT3_ID);

        assertThat(updateVote.getRestaurant().id()).isEqualTo(RESTAURANT3_ID);
        assertThat(updateVote.id()).isEqualTo(firstVote.id());
    }
}
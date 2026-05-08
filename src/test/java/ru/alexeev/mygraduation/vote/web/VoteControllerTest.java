package ru.alexeev.mygraduation.vote.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.alexeev.mygraduation.vote.model.Vote;
import ru.alexeev.mygraduation.vote.to.VoteResultTo;
import ru.alexeev.mygraduation.vote.to.VoteTo;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.alexeev.mygraduation.restaurant.RestaurantTestData.RESTAURANT1_ID;
import static ru.alexeev.mygraduation.restaurant.RestaurantTestData.RESTAURANT3_ID;
import static ru.alexeev.mygraduation.user.UserTestData.USER_ID;
import static ru.alexeev.mygraduation.vote.VoteTestData.*;
import static ru.alexeev.mygraduation.vote.util.VoteUtil.toVoteTo;
import static ru.alexeev.mygraduation.vote.web.VoteController.REST_URL;

class VoteControllerTest extends AdminVoteControllerTest {

    @Test
    @WithUserDetails(value = "user@yandex.ru")
    void vote() throws Exception {
        setFixedTime(clock, 10, 30);
        ResultActions actions = perform(MockMvcRequestBuilders.post(REST_URL)
                .param("restaurantId", String.valueOf(RESTAURANT1_ID))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated());
        VoteTo actual = VOTE_TO_MATCHER.readFromJson(actions);
        Vote vote =getCurrentUserVote();
        VoteTo expected = toVoteTo(vote);

        VOTE_TO_MATCHER.assertMatch(actual, expected);
    }

    @Test
    @WithUserDetails(value = "user@yandex.ru")
    void voteForNonExistentRestaurant() throws Exception {
        setFixedTime(clock, 10, 30);
        perform(MockMvcRequestBuilders.post(REST_URL)
                .param("restaurantId", String.valueOf(NOT_FOUND))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails(value = "user@yandex.ru")
    void voteWithoutRestaurantId() throws Exception {
        setFixedTime(clock, 10, 30);
        perform(MockMvcRequestBuilders.post(REST_URL)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnprocessableContent());
    }


    @Test
    void voteUnauthorized() throws Exception {
        setFixedTime(clock, 10, 30);
        perform(MockMvcRequestBuilders.post(REST_URL)
                .param("restaurantId", String.valueOf(RESTAURANT1_ID))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithUserDetails(value = "user@yandex.ru")
    void voteUpdateExisting() throws Exception {
        setFixedTime(clock, 10, 30);

        perform(MockMvcRequestBuilders.post(REST_URL)
                .param("restaurantId", String.valueOf(RESTAURANT1_ID))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated());

        ResultActions actions = perform(MockMvcRequestBuilders.post(REST_URL)
                .param("restaurantId", String.valueOf(RESTAURANT3_ID))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated());

        VoteTo actual = VOTE_TO_MATCHER.readFromJson(actions);
        Vote vote = getCurrentUserVote();
        assertThat(vote.getRestaurant().id()).isEqualTo(RESTAURANT3_ID);
        VoteTo expected = toVoteTo(vote);
        VOTE_TO_MATCHER.assertMatch(actual, expected);
    }

    @ParameterizedTest
    @CsvSource({
            "10, 30, true",
            "10, 59, true",
            "11, 0, true",
            "11, 1, false",
            "12, 0, false"
    })
    @WithUserDetails(value = "user@yandex.ru")
    void voteAtDifferentTimes(int hour, int minute, boolean shouldSucceed) throws Exception {
        setFixedTime(clock, hour, minute);
        ResultActions actions = perform(MockMvcRequestBuilders.post(REST_URL)
                .param("restaurantId", String.valueOf(RESTAURANT1_ID))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        if (shouldSucceed) {
            actions.andExpect(status().isCreated());
            VoteTo actual = VOTE_TO_MATCHER.readFromJson(actions);
            Vote vote = voteService.findByUser(USER_ID).getFirst();
            VoteTo expected = toVoteTo(vote);
            VOTE_TO_MATCHER.assertMatch(actual, expected);
        } else {
            actions.andExpect(status().isConflict());
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
    @WithUserDetails(value = "user@yandex.ru")
    void voteUpdateAtDifferentTimes(int hour, int minute, boolean shouldSucceed) throws Exception {
        setFixedTime(clock, 10, 30);
        perform(MockMvcRequestBuilders.post(REST_URL)
                .param("restaurantId", String.valueOf(RESTAURANT1_ID))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        setFixedTime(clock, hour, minute);
        ResultActions actions = perform(MockMvcRequestBuilders.post(REST_URL)
                .param("restaurantId", String.valueOf(RESTAURANT3_ID))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());
        Vote vote = getCurrentUserVote();
        if (shouldSucceed) {
            actions.andExpect(status().isCreated());
            assertThat(vote.getRestaurant().id()).isEqualTo(RESTAURANT3_ID);
            assertThat(vote.getVoteTime()).isEqualTo(LocalTime.of(hour, minute));
        } else {
            actions.andExpect(status().isConflict())
                    .andExpect(content().string(containsString("Cannot vote or change vote after")));
            assertThat(vote.getRestaurant().id()).isEqualTo(RESTAURANT1_ID);
            assertThat(vote.getVoteTime()).isEqualTo(LocalTime.of(10, 30));
        }
    }

    @Test
    @WithUserDetails(value = "user@yandex.ru")
    void voteTwiceSameRestaurant() throws Exception {
        setFixedTime(clock, 10, 30);

        perform(MockMvcRequestBuilders.post(REST_URL)
                .param("restaurantId", String.valueOf(RESTAURANT1_ID))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        perform(MockMvcRequestBuilders.post(REST_URL)
                .param("restaurantId", String.valueOf(RESTAURANT1_ID))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        Vote vote = getCurrentUserVote();
        assertThat(vote.getRestaurant().id()).isEqualTo(RESTAURANT1_ID);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 422",
            "-1, 422",
            "999999, 404",
            "abc, 422"
    })
    @WithUserDetails(value = "user@yandex.ru")
    void voteWithVariousRestaurantIds(String restaurantId, int expectedStatus) throws Exception {
        setFixedTime(clock, 10, 30);

        perform(MockMvcRequestBuilders.post(REST_URL)
                .param("restaurantId", restaurantId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is(expectedStatus));
    }

    @ParameterizedTest
    @CsvSource({
            "-2, 2, 1, 0",
            "-1, 1, 1, 1",
            "0, 1, 1, 0"
    })
    @WithUserDetails(value = "user@yandex.ru")
    void getTodayResults(int dayOffset, int expectedVotes1, int expectedVotes2, int expectedVotes3) throws Exception {
        LocalDate date = LocalDate.now().plusDays(dayOffset);
        setFixedDate(clock, date, 10, 30);

        ResultActions actions = perform(MockMvcRequestBuilders.get(REST_URL + "/results/today")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        List<VoteResultTo> actual = VOTE_RESULT_TO_MATCHER.readListFromJson(actions);

        assertThat(actual).hasSize(3);
        assertThat(actual.get(0).getVotesCount()).isEqualTo(expectedVotes1);
        assertThat(actual.get(1).getVotesCount()).isEqualTo(expectedVotes2);
        assertThat(actual.get(2).getVotesCount()).isEqualTo(expectedVotes3);
    }

    @Test
    @WithUserDetails(value = "user@yandex.ru")
    void getTodayResultsShouldBeSorted() throws Exception {
        setFixedDate(clock, TWO_DAYS_AGO, 10, 30);

        ResultActions actions = perform(MockMvcRequestBuilders.get(REST_URL + "/results/today")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        List<VoteResultTo> actual = VOTE_RESULT_TO_MATCHER.readListFromJson(actions);

        assertSortedByVotesDesc(actual);
    }

    @ParameterizedTest
    @CsvSource({
            "-2, 1, true",
            "-1, 0, false",
            "0, 0, false",
            "10, 0, false"
    })
    @WithUserDetails(value = "user@yandex.ru")
    void getTodayWinner(int dayOffset, int expectedWinnerId, boolean shouldBePresent) throws Exception {
        LocalDate date = LocalDate.now().plusDays(dayOffset);
        setFixedDate(clock, date, 10, 30);

        if (shouldBePresent) {
            ResultActions actions = perform(MockMvcRequestBuilders.get(REST_URL + "/results/winner")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk());
            VoteResultTo actual = VOTE_RESULT_TO_MATCHER.readFromJson(actions);
            assertThat(actual.getRestaurantId()).isEqualTo(expectedWinnerId);
        } else {
            perform(MockMvcRequestBuilders.get(REST_URL + "/results/winner")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }
    }

    @Test
    void getTodayWinnerWithUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL + "/results/winner")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTodayResultWithUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL + "/results/today")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    private Vote getCurrentUserVote() {
        return voteService.findByUser(USER_ID).getFirst();
    }
}
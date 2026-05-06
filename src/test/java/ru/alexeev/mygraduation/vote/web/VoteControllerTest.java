package ru.alexeev.mygraduation.vote.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.alexeev.mygraduation.AbstractControllerTest;
import ru.alexeev.mygraduation.vote.model.Vote;
import ru.alexeev.mygraduation.vote.service.VoteService;
import ru.alexeev.mygraduation.vote.to.VoteTo;
import java.time.Clock;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.alexeev.mygraduation.restaurant.RestaurantTestData.RESTAURANT1_ID;
import static ru.alexeev.mygraduation.restaurant.RestaurantTestData.RESTAURANT3_ID;
import static ru.alexeev.mygraduation.user.UserTestData.USER_ID;
import static ru.alexeev.mygraduation.vote.VoteTestData.NOT_FOUND;
import static ru.alexeev.mygraduation.vote.VoteTestData.VOTE_TO_MATCHER;
import static ru.alexeev.mygraduation.vote.VoteTestData.setFixedTime;
import static ru.alexeev.mygraduation.vote.util.VoteUtil.toVoteTo;
import static ru.alexeev.mygraduation.vote.web.VoteController.REST_URL;

class VoteControllerTest extends AbstractControllerTest {

    @Autowired
    private VoteService voteService;

    @MockitoBean
    private Clock clock;

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

    private Vote getCurrentUserVote() {
        return voteService.findByUser(USER_ID).getFirst();
    }
}
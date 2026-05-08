package ru.alexeev.mygraduation.vote.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.alexeev.mygraduation.vote.AbstractVoteControllerTest;
import ru.alexeev.mygraduation.vote.to.VoteResultTo;
import ru.alexeev.mygraduation.vote.to.VoteStatsTo;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.alexeev.mygraduation.restaurant.RestaurantTestData.RESTAURANT1_ID;
import static ru.alexeev.mygraduation.restaurant.RestaurantTestData.RESTAURANT2_ID;
import static ru.alexeev.mygraduation.restaurant.RestaurantTestData.RESTAURANT3_ID;
import static ru.alexeev.mygraduation.user.UserTestData.ADMIN_MAIL;
import static ru.alexeev.mygraduation.user.UserTestData.USER_MAIL;
import static ru.alexeev.mygraduation.vote.VoteTestData.*;

class AdminVoteControllerTest extends AbstractVoteControllerTest {

    private static final String REST_URL = "/api/admin/votes";

    @Test
    @WithUserDetails(value = ADMIN_MAIL)
    void adminGetResultsForDate() throws Exception {
        setFixedDate(clock, TWO_DAYS_AGO, 10, 30);

        ResultActions actions = perform(MockMvcRequestBuilders.get(REST_URL + "/results")
                .param("date", TWO_DAYS_AGO.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        List<VoteResultTo> actual = VOTE_RESULT_TO_MATCHER.readListFromJson(actions);
        VOTE_RESULT_TO_MATCHER.assertMatch(actual, TWO_DAYS_AGO_EXPECTED_RESULTS);
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void userGetResultsForDate() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL + "/results")
                .param("date", LocalDate.now().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }


    @Test
    void unauthorizedGetResultsForDate() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL + "/results")
                .param("date", LocalDate.now().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @CsvSource({
            "-2, 2, 1, 0",
            "-1, 1, 1, 1",
            "0, 1, 1, 0",
            "10, 0, 0, 0"
    })
    @WithUserDetails(value = ADMIN_MAIL)
    void getRestaurantForeDate(int dayOffSet, int expectedVotes1, int expectedVotes2, int expectedVotes3 ) throws Exception {
        LocalDate date = LocalDate.now().plusDays(dayOffSet);
        setFixedDate(clock, date, 10, 30);

        ResultActions actions = perform(MockMvcRequestBuilders.get(REST_URL + "/results")
                .param("date", date.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        List<VoteResultTo> actual = VOTE_RESULT_TO_MATCHER.readListFromJson(actions);

        assertThat(actual).hasSize(3);
        assertThat(getVotesForRestaurant(actual, RESTAURANT1_ID)).isEqualTo(expectedVotes1);
        assertThat(getVotesForRestaurant(actual, RESTAURANT2_ID)).isEqualTo(expectedVotes2);
        assertThat(getVotesForRestaurant(actual, RESTAURANT3_ID)).isEqualTo(expectedVotes3);
    }

    @Test
    @WithUserDetails(value = ADMIN_MAIL)
    void getResultForDate_WithMissingDataParam() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL + "/results")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnprocessableContent());
    }

    @ParameterizedTest
    @CsvSource({"2024-13-45", "invalid", "not-a-date", "2024/01/01"})
    @WithUserDetails(value = ADMIN_MAIL)
    void getResultForDate_WithInvalidDataParam(String invalidDate) throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL + "/results")
                .param("date", invalidDate)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @WithUserDetails(value = ADMIN_MAIL)
    void getResultForDateShouldBeSortedByVoteDesc() throws Exception {
        setFixedDate(clock, TWO_DAYS_AGO, 10, 30);

        ResultActions actions = perform(MockMvcRequestBuilders.get(REST_URL + "/results")
                .param("date", TWO_DAYS_AGO.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        List<VoteResultTo> actual = VOTE_RESULT_TO_MATCHER.readListFromJson(actions);

        assertSortedByVotesDesc(actual);
    }

    @ParameterizedTest
    @CsvSource({
            "-2, -2, 1",
            "-2, -1, 2",
            "-2, 0, 3",
            "-1, 0, 2",
            "0, 0, 1"
    })
    @WithUserDetails(value = ADMIN_MAIL)
    void getResultsForDateRange(int startOffset, int endOffset, int expectedDays) throws Exception {
        LocalDate start = LocalDate.now().plusDays(startOffset);
        LocalDate end = LocalDate.now().plusDays(endOffset);
        setFixedDate(clock, start, 10, 30);

        ResultActions actions = perform(MockMvcRequestBuilders.get(REST_URL + "/results/range")
                .param("start", start.toString())
                .param("end", end.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        String jsonResponse = actions.andReturn().getResponse().getContentAsString();

        long actualDays = LocalDate.now().plusDays(startOffset).datesUntil(LocalDate.now().plusDays(endOffset).plusDays(1)).count();
        assertThat(actualDays).isEqualTo(expectedDays);

        LocalDate current = start;
        while (!current.isAfter(end)) {
            assertThat(jsonResponse).contains(current.toString());
            current = current.plusDays(1);
        }
    }

    @Test
    @WithUserDetails(value = ADMIN_MAIL)
    void getResultsForDateRangeWithMissingStartParam() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL + "/results/range")
                .param("end", LocalDate.now().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @WithUserDetails(value = ADMIN_MAIL)
    void getResultsForDateRangeWithMissingEndParam() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL + "/results/range")
                .param("start", LocalDate.now().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @WithUserDetails(value = ADMIN_MAIL)
    void getResultsForDateRangeWithStartAfterEnd() throws Exception {
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().minusDays(1);
        ResultActions actions = perform(MockMvcRequestBuilders.get(REST_URL + "/results/range")
                .param("start", start.toString())
                .param("end", end.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
        String jsonResponse = actions.andReturn().getResponse().getContentAsString();
        assertThat(jsonResponse).isEqualTo("{}");
    }

    @Test
    @WithUserDetails(value = ADMIN_MAIL)
    void getResultsForDateRangeWithEmptyRange() throws Exception {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now();
        ResultActions actions = perform(MockMvcRequestBuilders.get(REST_URL + "/results/range")
                .param("start", start.toString())
                .param("end", end.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
        String jsonResponse = actions.andReturn().getResponse().getContentAsString();
        assertThat(jsonResponse).isEqualTo("{}");
    }

    @Test
    @WithUserDetails(value = ADMIN_MAIL)
    void getGeneralStats() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(REST_URL + "/stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        VoteStatsTo actual = VOTE_STATS_TO_MATCHER.readFromJson(actions);
        VOTE_STATS_TO_MATCHER.assertMatch(actual, expectedStats);
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void userGetGeneralStats() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(REST_URL + "/stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({"/results", "/results/range", "/stats"})
    @WithUserDetails(value = USER_MAIL)
    void allAdminEndpointsWithForbiddenForUser(String path) throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL + path)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthorizedGetGeneralStats() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(REST_URL + "/stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}
package ru.alexeev.mygraduation.vote;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.alexeev.mygraduation.AbstractControllerTest;
import ru.alexeev.mygraduation.vote.service.VoteService;

import java.time.Clock;

public class AbstractVoteControllerTest extends AbstractControllerTest {
    @Autowired
    protected VoteService voteService;

    @MockitoBean
    protected Clock clock;
}

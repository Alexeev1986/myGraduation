package ru.alexeev.mygraduation.vote.to;

import lombok.Value;

@Value
public class VoteStatsTo {
    Integer totalVotes;
    Integer totalUserWhoVoted;
    double averageVotesPerUser;

    public VoteStatsTo(long totalVotes, long totalUserWhoVoted, double averageVotesPerUser) {
        this.totalVotes = (int) totalVotes;
        this.totalUserWhoVoted = (int) totalUserWhoVoted;
        this.averageVotesPerUser = averageVotesPerUser;
    }
}

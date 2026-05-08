package ru.alexeev.mygraduation.vote.to;

import lombok.Value;

@Value
public class VoteResultTo {
    Integer restaurantId;
    String restaurantName;
    Integer votesCount;

    public VoteResultTo(Integer restaurantId, String restaurantName, long votesCount) {
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.votesCount = (int) votesCount;
    }
}

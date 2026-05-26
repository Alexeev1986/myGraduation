package ru.alexeev.mygraduation.vote.to;

import java.time.LocalDate;

public record VoteResultRecord(LocalDate voteDate, Integer restaurantId, String restaurantName, Long votesCount) {
}

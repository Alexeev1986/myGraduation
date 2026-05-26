package ru.alexeev.mygraduation.vote.to;

import lombok.Value;

import java.time.LocalDate;
import java.time.LocalTime;

@Value
public class ProfileVoteTo {
    Integer id;
    Integer restaurantId;
    String restaurantName;
    LocalDate voteDate;
    LocalTime voteTime;
}

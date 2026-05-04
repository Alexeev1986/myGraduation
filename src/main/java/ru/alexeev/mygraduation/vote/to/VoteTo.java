package ru.alexeev.mygraduation.vote.to;

import lombok.EqualsAndHashCode;
import lombok.Value;
import ru.alexeev.mygraduation.common.to.BaseTo;

import java.time.LocalDate;
import java.time.LocalTime;

@EqualsAndHashCode(callSuper = true)
@Value
public class VoteTo extends BaseTo {
    Integer restaurantId;
    String restaurantName;
    LocalDate voteDate;
    LocalTime voteTime;

    public VoteTo(Integer id, Integer restaurantId, String restaurantName, LocalDate voteDate, LocalTime voteTime) {
        super(id);
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.voteDate = voteDate;
        this.voteTime = voteTime;
    }
}

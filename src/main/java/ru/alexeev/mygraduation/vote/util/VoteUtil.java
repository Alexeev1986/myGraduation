package ru.alexeev.mygraduation.vote.util;

import lombok.experimental.UtilityClass;
import ru.alexeev.mygraduation.vote.model.Vote;
import ru.alexeev.mygraduation.vote.to.VoteTo;

import java.util.List;

@UtilityClass
public class VoteUtil {

    public static VoteTo toVoteTo(Vote vote) {
        return new VoteTo(
                vote.getId(),
                vote.getRestaurant().getId(),
                vote.getRestaurant().getName(),
                vote.getVoteDate(),
                vote.getVoteTime()
        );
    }

    public static List<VoteTo> toVoteTos(List<Vote> votes) {
        if (votes == null) {
            return List.of();
        }
        return votes.stream()
                .map(VoteUtil::toVoteTo)
                .toList();
    }
}

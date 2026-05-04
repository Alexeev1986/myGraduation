package ru.alexeev.mygraduation.vote.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.alexeev.mygraduation.common.model.BaseEntity;
import ru.alexeev.mygraduation.restaurant.model.Restaurant;
import ru.alexeev.mygraduation.user.model.User;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "vote", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "vote_date"}, name = "uk_vote_user_date")})
@Getter
@Setter
@NoArgsConstructor
public class Vote extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Restaurant restaurant;

    @Column(name = "vote_date", nullable = false)
    @NotNull
    private LocalDate voteDate;

    @Column(name = "vote_time", nullable = false)
    @NotNull
    private LocalTime voteTime;

    public Vote(Integer id, User user, Restaurant restaurant, LocalDate voteDate, LocalTime voteTime) {
        super(id);
        this.user = user;
        this.restaurant = restaurant;
        this.voteDate = voteDate;
        this.voteTime = voteTime;
    }
}

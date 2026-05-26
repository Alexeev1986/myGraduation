package ru.alexeev.mygraduation.vote.repository;

import org.springframework.data.jpa.repository.Query;
import ru.alexeev.mygraduation.common.BaseRepository;
import ru.alexeev.mygraduation.vote.model.Vote;
import ru.alexeev.mygraduation.vote.to.VoteResultRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VoteRepository extends BaseRepository<Vote> {
    @Query("SELECT v FROM Vote v WHERE v.user.id=:userId AND v.voteDate=:date")
    Optional<Vote> findByUserAndDate(int userId, LocalDate date);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.restaurant.id=:restaurantId AND v.voteDate=:date")
    long countByRestaurantAndDate(int restaurantId, LocalDate date);

    @Query("SELECT v FROM Vote v JOIN FETCH v.restaurant WHERE v.user.id=:userId ORDER BY v.voteDate DESC ")
    Optional<List<Vote>> findByUser(int userId);

    @Query("""
            SELECT new ru.alexeev.mygraduation.vote.to.VoteResultRecord(v.voteDate, r.id, r.name, COUNT(v.id))
            FROM Vote v JOIN v.restaurant r
            WHERE  v.voteDate BETWEEN :start AND :end
            GROUP BY v.voteDate, r.id, r.name
            ORDER BY COUNT(v.id) DESC
          """)
    List<VoteResultRecord> getVoteResultsForDateRange(LocalDate start, LocalDate end);

    @Query("SELECT COUNT(DISTINCT v.user.id) FROM Vote v")
    long countDistinctUsers();
}

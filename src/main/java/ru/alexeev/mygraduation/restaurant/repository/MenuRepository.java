package ru.alexeev.mygraduation.restaurant.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.alexeev.mygraduation.common.BaseRepository;
import ru.alexeev.mygraduation.restaurant.model.Menu;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface MenuRepository extends BaseRepository<Menu> {

    @Query("SELECT m FROM Menu m JOIN FETCH m.dishes WHERE m.restaurant.id=:restaurantId AND m.date=:date")
    Optional<Menu> getByRestaurantAndDate(int restaurantId, LocalDate date);

    @Query("SELECT m FROM Menu m JOIN FETCH m.dishes WHERE m.date = CURRENT_DATE")
    List<Menu> getAllTodayMenus();
}

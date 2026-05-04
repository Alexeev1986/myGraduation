package ru.alexeev.mygraduation.restaurant.repository;

import org.springframework.data.jpa.repository.Query;
import ru.alexeev.mygraduation.common.BaseRepository;
import ru.alexeev.mygraduation.restaurant.model.Restaurant;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends BaseRepository<Restaurant> {

    @Query("SELECT r FROM Restaurant r WHERE LOWER(r.name)=LOWER(:name)")
    Optional<Restaurant> findByNameIgnoreCase(String name);

    @Query("SELECT r FROM Restaurant r LEFT JOIN FETCH r.menus m WHERE m.date=CURRENT_DATE")
    List<Restaurant> findAllWithTodayMenus();
}

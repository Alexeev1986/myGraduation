package ru.alexeev.mygraduation.restaurant.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.alexeev.mygraduation.common.BaseRepository;
import ru.alexeev.mygraduation.restaurant.model.Dish;

import java.util.Optional;

@Transactional(readOnly = true)
public interface DishRepository extends BaseRepository<Dish> {

    @Query("SELECT d FROM Dish d WHERE LOWER(d.name) = LOWER(:name)")
    Optional<Dish> findByNameIgnoreCase(String name);
}

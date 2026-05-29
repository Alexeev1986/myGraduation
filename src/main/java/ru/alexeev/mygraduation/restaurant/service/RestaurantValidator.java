package ru.alexeev.mygraduation.restaurant.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.alexeev.mygraduation.common.error.DataConflictException;
import ru.alexeev.mygraduation.restaurant.model.Restaurant;
import ru.alexeev.mygraduation.restaurant.repository.RestaurantRepository;

@Component
@AllArgsConstructor
@Slf4j
public class RestaurantValidator {
    private final RestaurantRepository restaurantRepository;

    public void validateBeforeCreate(Restaurant restaurant) {
        restaurantRepository.findByNameIgnoreCase(restaurant.getName())
                .ifPresent(existing -> {
                    throw new DataConflictException("Restaurant with name " + restaurant.getName() + " already exists");
                });
    }
    public void validateBeforeUpdate(Restaurant existing, Restaurant updated) {
        if (existing.getName().equalsIgnoreCase(updated.getName())) {
            return;
        }

        validateBeforeCreate(updated);
    }
}

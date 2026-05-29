package ru.alexeev.mygraduation.restaurant.service;

import org.springframework.stereotype.Component;
import ru.alexeev.mygraduation.common.error.DataConflictException;
import ru.alexeev.mygraduation.common.error.IllegalRequestDataException;
import ru.alexeev.mygraduation.restaurant.to.MenuTo;

import java.time.LocalDate;

@Component
public class MenuValidator {
    public void validate(MenuTo menuTo) {

        if (menuTo.getDate().isBefore(LocalDate.now())) {
            throw new DataConflictException("Cannot add menu for past date");
        }

        if (menuTo.getDishes() == null || menuTo.getDishes().isEmpty()) {
            throw new IllegalRequestDataException("Menu must contain at least one dish");
        }

        long distinctCount = menuTo.getDishes().stream()
                .map(dishTo -> dishTo.getName().toLowerCase() + "|" + dishTo.getPrice())
                .distinct()
                .count();

        if (distinctCount != menuTo.getDishes().size()) {
            throw new DataConflictException("Menu cannot contain duplicate dishes");
        }
    }
}

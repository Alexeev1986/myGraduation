package ru.alexeev.mygraduation.restaurant.service;

import org.springframework.stereotype.Component;
import ru.alexeev.mygraduation.common.error.DataConflictException;
import ru.alexeev.mygraduation.common.error.IllegalRequestDataException;
import ru.alexeev.mygraduation.restaurant.to.MenuTo;

import java.time.LocalDate;

@Component
public class MenuValidator {
    public void validate(MenuTo menuTo) {
        LocalDate date = menuTo.getDate();
        if (date.isBefore(LocalDate.now())) {
            throw new DataConflictException("Cannot add menu for past date");
        }

        int dishCount = menuTo.getDishes().size();
        if (dishCount < 2 || dishCount > 5) {
            throw new IllegalRequestDataException("Menu must contain between 2 and 5 dishes, but got " + dishCount);
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

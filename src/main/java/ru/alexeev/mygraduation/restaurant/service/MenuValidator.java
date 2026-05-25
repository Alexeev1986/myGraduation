package ru.alexeev.mygraduation.restaurant.service;

import org.springframework.stereotype.Component;
import ru.alexeev.mygraduation.common.error.DataConflictException;
import ru.alexeev.mygraduation.restaurant.to.MenuTo;

@Component
public class MenuValidator {
    public void validate(MenuTo menuTo) {

        long distinctCount = menuTo.getDishes().stream()
                .map(dishTo -> dishTo.getName().toLowerCase() + "|" + dishTo.getPrice())
                .distinct()
                .count();

        if (distinctCount != menuTo.getDishes().size()) {
            throw new DataConflictException("Menu cannot contain duplicate dishes");
        }
    }
}

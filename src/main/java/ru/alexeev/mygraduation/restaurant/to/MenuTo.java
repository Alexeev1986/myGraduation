package ru.alexeev.mygraduation.restaurant.to;

import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Value;
import ru.alexeev.mygraduation.common.to.BaseTo;


import java.time.LocalDate;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Value
public class MenuTo extends BaseTo {
    @NotNull
    LocalDate date;

    List<DishTo> dishes;

    public MenuTo(Integer id, LocalDate date, List<DishTo> dishes) {
        super(id);
        this.date = date;
        this.dishes = dishes;
    }
}

package ru.alexeev.mygraduation.restaurant.to;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.EqualsAndHashCode;
import lombok.Value;
import ru.alexeev.mygraduation.common.to.NamedTo;

@EqualsAndHashCode(callSuper = true)
@Value
public class DishTo extends NamedTo {
    @NotNull
    @Positive
    Integer price;

    public DishTo(Integer id, String name, Integer price) {
        super(id, name);
        this.price = price;
    }
}

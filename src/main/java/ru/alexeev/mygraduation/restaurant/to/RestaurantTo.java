package ru.alexeev.mygraduation.restaurant.to;

import lombok.EqualsAndHashCode;
import lombok.Value;
import ru.alexeev.mygraduation.common.to.NamedTo;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Value
public class RestaurantTo extends NamedTo {
    List<MenuTo> menuTos;

    public RestaurantTo(Integer id, String name, List<MenuTo> menuTos) {
        super(id, name);
        this.menuTos = menuTos;
    }
}

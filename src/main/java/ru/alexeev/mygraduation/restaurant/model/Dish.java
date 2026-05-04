package ru.alexeev.mygraduation.restaurant.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.alexeev.mygraduation.common.model.NamedEntity;

@Entity
@Table(name = "dish", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name", name = "uk_dish_name")})
@Getter
@Setter
@NoArgsConstructor
public class Dish extends NamedEntity {

    @NotNull
    @Positive
    private Integer price;

    public Dish(Integer id, String name, Integer price) {
        super(id, name);
        this.price = price;
    }

    @Override
    public String toString() {
        return "Dish:"+ id + "[" + name + " - " + price + "]";
    }
}

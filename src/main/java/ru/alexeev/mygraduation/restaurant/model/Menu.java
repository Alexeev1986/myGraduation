package ru.alexeev.mygraduation.restaurant.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.alexeev.mygraduation.common.model.BaseEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menu", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"restaurant_id", "date"}, name = "uk_menu_restaurant_date")})
@Getter
@Setter
@NoArgsConstructor
public class Menu extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Restaurant restaurant;

    @Column(name = "date", nullable = false)
    @NotNull
    private LocalDate date;

    @ManyToMany
    @JoinTable(name = "menu_dish", joinColumns = @JoinColumn(name = "menu_id"),
            inverseJoinColumns = @JoinColumn(name = "dish_id"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Dish> dishes = new ArrayList<>();

    public Menu(Integer id, Restaurant restaurant, LocalDate date) {
        super(id);
        this.restaurant = restaurant;
        this.date = date;
    }
}

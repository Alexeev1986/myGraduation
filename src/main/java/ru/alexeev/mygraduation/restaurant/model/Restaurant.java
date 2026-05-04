package ru.alexeev.mygraduation.restaurant.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.alexeev.mygraduation.common.model.NamedEntity;
import ru.alexeev.mygraduation.vote.model.Vote;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "restaurant", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name", name = "uk_restaurant_name")
})
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true, exclude = {"menus", "votes"})
public class Restaurant extends NamedEntity {

    @OneToMany(mappedBy = "restaurant", fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Vote> votes = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Menu> menus = new ArrayList<>();

    public Restaurant(Integer id, String name) {
        super(id, name);
    }
}

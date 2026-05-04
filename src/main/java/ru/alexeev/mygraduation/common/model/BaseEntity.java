package ru.alexeev.mygraduation.common.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import ru.alexeev.mygraduation.common.HasId;

import static ru.alexeev.mygraduation.common.util.HibernateProxyHelper.getClassWithoutInitializingProxy;

@MappedSuperclass
@Access(AccessType.FIELD)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseEntity implements HasId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    protected Integer id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClassWithoutInitializingProxy(this) != getClassWithoutInitializingProxy(o)) return false;
        return getId() != null && getId().equals(((BaseEntity) o).getId());
    }

    @Override
    public int hashCode() {
        return getClassWithoutInitializingProxy(this).hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + id;
    }
}

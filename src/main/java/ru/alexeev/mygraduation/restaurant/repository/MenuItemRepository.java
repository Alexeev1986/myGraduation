package ru.alexeev.mygraduation.restaurant.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.alexeev.mygraduation.common.BaseRepository;
import ru.alexeev.mygraduation.restaurant.model.MenuItem;

public interface MenuItemRepository extends BaseRepository<MenuItem> {

    @Transactional
    @Modifying
    @Query("DELETE FROM MenuItem mi WHERE mi.menu.id=:menuId")
    void deleteByMenuId(int menuId);
}

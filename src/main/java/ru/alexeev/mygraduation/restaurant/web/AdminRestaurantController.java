package ru.alexeev.mygraduation.restaurant.web;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.alexeev.mygraduation.restaurant.model.Restaurant;
import ru.alexeev.mygraduation.restaurant.service.RestaurantService;
import ru.alexeev.mygraduation.restaurant.to.MenuTo;

import java.net.URI;

import static ru.alexeev.mygraduation.common.validation.ValidationUtil.assureIdConsistent;
import static ru.alexeev.mygraduation.common.validation.ValidationUtil.checkIsNew;

@RestController
@RequestMapping(value = AdminRestaurantController.REST_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
public class AdminRestaurantController {
    static final String REST_URL = "/api/admin/restaurants";

    private final RestaurantService restaurantService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Restaurant> create(@Valid @RequestBody Restaurant restaurant) {
        log.info("create restaurant {}", restaurant);
        checkIsNew(restaurant);
        Restaurant created = restaurantService.create(restaurant);
        URI uriOfNewResource = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(REST_URL + "/{id}")
                .buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(uriOfNewResource).body(created);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@Valid @RequestBody Restaurant restaurant, @PathVariable int id) {
        log.info("update restaurant {} with {}", restaurant, id);
        assureIdConsistent(restaurant, id);
        restaurantService.update(restaurant, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int id) {
        log.info("delete restaurant {}", id);
        restaurantService.delete(id);
    }

    @PostMapping(value = "/{id}/menu", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void addMenu(@PathVariable int id, @Valid @RequestBody MenuTo menuTo) {
        log.info("add menu for restaurant {} on {}", id, menuTo.getDate());
        restaurantService.addMenu(id, menuTo);
    }
}

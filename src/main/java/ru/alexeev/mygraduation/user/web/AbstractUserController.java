package ru.alexeev.mygraduation.user.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import ru.alexeev.mygraduation.user.service.UserService;

public abstract class AbstractUserController {

    @Autowired
    protected UserService userService;

    @Autowired
    protected UniqueMailValidator emailValidator;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(emailValidator);
    }
}

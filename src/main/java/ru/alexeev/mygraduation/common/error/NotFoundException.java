package ru.alexeev.mygraduation.common.error;

import static ru.alexeev.mygraduation.common.error.ErrorType.NOT_FOUND;

public class NotFoundException extends AppException{
    public NotFoundException(String msg) {
        super(msg, NOT_FOUND);
    }
}

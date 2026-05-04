package ru.alexeev.mygraduation.common.error;

import static ru.alexeev.mygraduation.common.error.ErrorType.BAD_REQUEST;

public class IllegalRequestDataException extends AppException{
    public IllegalRequestDataException(String msg) {
        super(msg, BAD_REQUEST);
    }
}

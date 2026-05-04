package ru.alexeev.mygraduation.common.error;

import static ru.alexeev.mygraduation.common.error.ErrorType.DATA_CONFLICT;

public class DataConflictException extends AppException {

    public DataConflictException(String msg) {
        super(msg, DATA_CONFLICT);
    }
}

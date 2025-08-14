package com.gudrhs8304.ticketory.exception;

public class DuplicateLoginIdException extends RuntimeException {
    public DuplicateLoginIdException(String message) {
        super(message);
    }
}
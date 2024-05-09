package com.vladlevchik.exception;

public class EntityExistException extends RuntimeException{
    public EntityExistException(String message) {
        super(message);
    }
}

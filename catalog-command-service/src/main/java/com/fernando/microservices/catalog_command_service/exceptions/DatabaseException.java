package com.fernando.microservices.catalog_command_service.exceptions;

public class DatabaseException extends RuntimeException {

    public DatabaseException() {
        super("Database operation failed");
    }

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseException(Throwable cause) {
        super(cause);
    }
}
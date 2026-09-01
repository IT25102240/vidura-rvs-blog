package com.vidurarvs.blog.exception;

/** Thrown for unique-constraint style conflicts (username/email/slug already taken). */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}

package com.vidurarvs.blog.exception;

/** Thrown when a logged-in admin tries to act on something they don't own/control. */
public class ForbiddenActionException extends RuntimeException {
    public ForbiddenActionException(String message) {
        super(message);
    }
}

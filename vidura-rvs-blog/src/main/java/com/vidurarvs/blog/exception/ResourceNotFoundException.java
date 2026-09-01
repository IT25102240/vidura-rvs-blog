package com.vidurarvs.blog.exception;

/** Thrown when a post, category, or user looked up by id/slug does not exist. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

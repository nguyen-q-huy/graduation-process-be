package com.example.graduationprocessbe.exception;

/** A document (e.g. a PDF built from a Word template) could not be produced. */
public class DocumentGenerationException extends RuntimeException {

    public DocumentGenerationException(String message) {
        super(message);
    }

    public DocumentGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}

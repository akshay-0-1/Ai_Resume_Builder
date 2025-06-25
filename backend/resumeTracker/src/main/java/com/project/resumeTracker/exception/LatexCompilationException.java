package com.project.resumeTracker.exception;

public class LatexCompilationException extends RuntimeException {
    public LatexCompilationException(String message) {
        super(message);
    }

    public LatexCompilationException(String message, Throwable cause) {
        super(message, cause);
    }
}

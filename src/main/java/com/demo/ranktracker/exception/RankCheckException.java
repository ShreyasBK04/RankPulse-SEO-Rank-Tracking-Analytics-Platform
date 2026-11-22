package com.demo.ranktracker.exception;

public class RankCheckException extends RuntimeException {

    public RankCheckException(String message) {
        super(message);
    }

    public RankCheckException(String message, Throwable cause) {
        super(message, cause);
    }
}

package io.cicada.common.rate.limiting.api;

public class RateLimitingException extends Exception {

    public RateLimitingException(String message) {
        super(message);
    }
}

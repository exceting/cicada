package io.github.exceting.cicada.common.ratelimiting.api;

public class RateLimitingException extends Exception {

    public RateLimitingException(String message) {
        super(message);
    }
}

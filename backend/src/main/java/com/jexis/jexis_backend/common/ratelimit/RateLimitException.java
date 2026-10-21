package com.jexis.jexis_backend.common.ratelimit;

public class RateLimitException extends RuntimeException {
    private final int status;
    private final long retryAfter;

    public RateLimitException(long retryAfter) {
        super("Too many requests. Please try again later.");
        this.status = 429;
        this.retryAfter = retryAfter;
    }

    public RateLimitException(Throwable cause) {
        super("Request protection is temporarily unavailable. Please try again later.", cause);
        this.status = 503;
        this.retryAfter = 5;
    }

    public int status() { return status; }
    public long retryAfter() { return retryAfter; }
    public String code() { return status == 429 ? "RATE_LIMIT_EXCEEDED" : "RATE_LIMIT_UNAVAILABLE"; }
}

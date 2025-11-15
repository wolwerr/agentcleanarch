package com.poc.infra.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class FixedRetryPolicy implements RetryPolicy {

    private static final Logger LOG = LoggerFactory.getLogger(FixedRetryPolicy.class);

    private final int maxAttempts;
    private final long backoffMillis;

    public FixedRetryPolicy(int maxAttempts, long backoffMillis) {
        this.maxAttempts = maxAttempts;
        this.backoffMillis = backoffMillis;
    }

    @Override
    public <T> T executeWithRetry(Callable<T> action) throws Exception {
        Exception last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return action.call();
            } catch (Exception e) {
                last = e;
                LOG.warn("Tentativa {} de {} falhou. Retentando em {} ms", attempt, maxAttempts, backoffMillis, e);
                Thread.sleep(backoffMillis);
            }
        }
        throw last;
    }
}

package com.poc.infra.util;

import java.util.concurrent.Callable;

public interface RetryPolicy {

    <T> T executeWithRetry(Callable<T> action) throws Exception;
}

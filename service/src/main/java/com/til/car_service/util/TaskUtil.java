package com.til.car_service.util;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.huawei.hmf.tasks.OnSuccessListener;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TaskUtil {

    public static <T> CompletableFuture<T> convert(Task<T> task) {
        CompletableFuture<T> future = new CompletableFuture<>();

        task.addOnCompleteListener(taskResult -> {
            if (task.isSuccessful()) {
                future.complete(taskResult.getResult());
                return;
            }
            Exception exception = task.getException();
            future.completeExceptionally(Objects.requireNonNullElseGet(exception, () -> new RuntimeException("Task failed with unknown error")));
        });

        return future;
    }

    public static <T> CompletableFuture<T> convert(com.huawei.hmf.tasks.Task<T> task) {
        CompletableFuture<T> future = new CompletableFuture<>();

        task.addOnCompleteListener(taskResult -> {
            if (task.isSuccessful()) {
                future.complete(taskResult.getResult());
                return;
            }
            Exception exception = task.getException();
            future.completeExceptionally(exception);
        });

        return future;
    }

    public static <T> T getResultSync(Task<T> task, long timeout, TimeUnit unit) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final Object[] resultHolder = new Object[1];
        final Exception[] exceptionHolder = new Exception[1];

        task.addOnSuccessListener(result -> {
            resultHolder[0] = result;
            latch.countDown();
        }).addOnFailureListener(exception -> {
            exceptionHolder[0] = exception;
            latch.countDown();
        });

        if (!latch.await(timeout, unit)) {
            throw new TimeoutException("Task timed out");
        }

        if (exceptionHolder[0] != null) {
            throw exceptionHolder[0];
        }

        @SuppressWarnings("unchecked")
        T result = (T) resultHolder[0];
        return result;
    }

    public static <T> T getResultSync(com.huawei.hmf.tasks.Task<T> task, long timeout, TimeUnit unit) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final Object[] resultHolder = new Object[1];
        final Exception[] exceptionHolder = new Exception[1];

        task.addOnSuccessListener(result -> {
            resultHolder[0] = result;
            latch.countDown();
        }).addOnFailureListener(exception -> {
            exceptionHolder[0] = exception;
            latch.countDown();
        });

        if (!latch.await(timeout, unit)) {
            throw new TimeoutException("Task timed out");
        }

        if (exceptionHolder[0] != null) {
            throw exceptionHolder[0];
        }

        @SuppressWarnings("unchecked")
        T result = (T) resultHolder[0];
        return result;
    }
}

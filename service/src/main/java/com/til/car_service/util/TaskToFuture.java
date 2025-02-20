package com.til.car_service.util;

import com.google.android.gms.tasks.Task;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class TaskToFuture {

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
}

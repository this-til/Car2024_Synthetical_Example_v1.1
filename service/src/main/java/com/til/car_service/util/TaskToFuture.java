package com.til.car_service.util;

import com.google.android.gms.tasks.Task;

import java.util.concurrent.CompletableFuture;

public class TaskToFuture {

    public static <T> CompletableFuture<T> convert(Task<T> task) {
        CompletableFuture<T> future = new CompletableFuture<>();

        task.addOnCompleteListener(taskResult -> {
            if (task.isSuccessful()) {
                // 任務成功完成，傳遞結果
                future.complete(taskResult.getResult());
            } else {
                // 任務失敗，傳遞異常
                Exception exception = task.getException();
                if (exception != null) {
                    future.completeExceptionally(exception);
                } else {
                    future.completeExceptionally(new RuntimeException("Task failed with unknown error"));
                }
            }
        });

        return future;
    }
}

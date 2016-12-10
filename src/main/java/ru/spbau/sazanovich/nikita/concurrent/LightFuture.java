package ru.spbau.sazanovich.nikita.concurrent;

import java.util.function.Function;

public interface LightFuture<R> {

    /**
     * Checks whether task has been completed.
     *
     * @return true if it's been done
     */
    boolean isReady();

    /**
     * Gets the result of task's execution.
     * If it hasn't been done yet it will block the thread until it's done.
     *
     * @return result if task completed without exception
     * @throws LightExecutionException if an exception was caught during execution
     * @throws InterruptedException if the thread was interrupted while waiting
     */
    R get() throws LightException, InterruptedException;

    /**
     * Waits till the current task is completed and then uses its result and function parameter
     * to run another task.
     *
     * @param transformer function to apply
     * @param <T> result of the function
     * @return task accepted for computation in ThreadPool
     */
    <T> LightFuture<T> thenApply(Function<R, T> transformer);
}

package ru.spbau.sazanovich.nikita.concurrent;

import net.jcip.annotations.GuardedBy;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class ThreadPoolImpl {

    private final ThreadInPool[] threads;
    private final Deque<ThreadPoolLightFuture<?>> tasks = new ArrayDeque<>();
    @GuardedBy("this") private boolean isShutdowned;

    public ThreadPoolImpl(int numberOfThreads) {
        if (numberOfThreads < 1) {
            throw new IllegalArgumentException("number of threads should be positive");
        }
        threads = new ThreadInPool[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++) {
            threads[i] = new ThreadInPool();
            threads[i].start();
        }
    }

    public synchronized void shutdown() {
        if (!isShutdowned) {
            isShutdowned = true;
            for (ThreadInPool thread : threads) {
                thread.interrupt();
            }
            tasks.forEach(ThreadPoolLightFuture::cancel);
        }
    }

    public <R> LightFuture<R> submit(Supplier<R> supplier) {
        if (supplier == null) {
            throw new NullPointerException("null supplier for task");
        }
        final ThreadPoolLightFuture<R> task = new ThreadPoolLightFuture<>(supplier::get);
        return submit(task);
    }

    private <R> ThreadPoolLightFuture<R> submit(ThreadPoolLightFuture<R> task) {
        synchronized (this) {
            if (isShutdowned) {
                return null;
            }
        }
        synchronized (tasks) {
            tasks.add(task);
            tasks.notify();
        }
        return task;
    }

    private class ThreadInPool extends Thread {

        @Override
        public void run() {
            while (!isInterrupted()) {
                ThreadPoolLightFuture<?> nextTask = null;
                synchronized (tasks) {
                    if (!tasks.isEmpty()) {
                        nextTask = tasks.removeFirst();
                    } else {
                        try {
                            tasks.wait();
                        } catch (InterruptedException e) {
                            interrupt();
                        }
                    }
                }

                if (nextTask != null) {
                    nextTask.run();
                }
            }
        }
    }

    private class ThreadPoolLightFuture<R> implements LightFuture<R> {

        @GuardedBy("this") private final List<ThreadPoolLightFuture<?>> dependingTasks = new ArrayList<>();
        private final ThreadSupplier<R> supplier;
        private volatile R result;
        private volatile LightException exception;

        public ThreadPoolLightFuture(ThreadSupplier<R> supplier) {
            this.supplier = supplier;
        }

        @Override
        public boolean isReady() {
            return result != null || exception != null;
        }

        @Override
        public R get() throws LightException, InterruptedException {
            synchronized (this) {
                if (!isReady()) {
                    wait();
                }
            }
            if (result != null) {
                return result;
            } else {
                throw exception;
            }
        }

        @Override
        public <T> LightFuture<T> thenApply(Function<R, T> transformer) {
            final ThreadPoolLightFuture<T> newTask = new ThreadPoolLightFuture<>(() -> transformer.apply(get()));
            synchronized (this) {
                if (isReady()) {
                    submit(newTask);
                } else {
                    dependingTasks.add(newTask);
                }
            }
            return newTask;
        }

        private void run() {
            try {
                result = supplier.get();
            } catch (Exception e) {
                exception = new LightExecutionException(e);
            }
            synchronized (this) {
                notifyAll();
                dependingTasks.forEach(ThreadPoolImpl.this::submit);
            }
        }

        private void cancel() {
            exception = new LightCancellationException();
        }
    }
}

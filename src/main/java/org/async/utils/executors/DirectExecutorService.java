package org.async.utils.executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * User: Barak Bar Orion
 * Date: Jan 30, 2012
 * Time: 8:50:04 AM
 */
public class DirectExecutorService implements ExecutorService {
    private final static Logger logger = LoggerFactory.getLogger(DirectExecutorService.class);

    private volatile boolean stopped;

    @Override
    public void shutdown() {
        stopped = true;
    }

    @Override
    public List<Runnable> shutdownNow() {
        stopped = true;
        return Collections.emptyList();
    }

    @Override
    public boolean isShutdown() {
       return stopped;
    }

    @Override
    public boolean isTerminated() {
        return stopped;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        throw new InterruptedException();
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return new DirectFutureTask<T>(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return new DirectFutureTask<T>(task, result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return new DirectFutureTask<>(task, null);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return Collections.emptyList();
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        throw new InterruptedException();
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        throw new InterruptedException();
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        throw new InterruptedException();
    }

    @Override
    public void execute(Runnable command) {
        command.run();
    }
}

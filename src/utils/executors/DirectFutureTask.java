package utils.executors;

import java.util.concurrent.*;

/**
 * User: Barak Bar Orion
 * Date: Jan 30, 2012
 * Time: 8:50:30 AM
 */
public class DirectFutureTask<V> extends FutureTask<V> {
    public DirectFutureTask(Callable<V> vCallable) {
        super(vCallable);
    }

    public DirectFutureTask(Runnable runnable, V result) {
        super(runnable, result);
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        super.run();
        return super.get();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        throw new IllegalArgumentException("Not implemented");
    }
}
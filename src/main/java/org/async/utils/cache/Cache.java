package org.async.utils.cache;

import org.async.utils.executors.DirectExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * User: Barak Bar Orion
 * Date: Dec 5, 2011
 * Time: 3:01:33 PM
 */
public class Cache<K, V> {
    Logger logger = LoggerFactory.getLogger(Cache.class);

    private final Compute<K, V> compute;
    private final Map<K, SoftValue<K, Future<V>>> map;
    private final ExecutorService executor;
    private final int CACHE_SIZE;
    private ExceptionStrategy<K> exceptionStrategy;
    private final ReferenceQueue<Future<V>> referenceQueue = new ReferenceQueue<Future<V>>();

    @SuppressWarnings({"UnusedDeclaration"})
    public Cache(Compute<K, V> compute, int size) {
        this(compute, new DirectExecutorService(), size);
    }

    /**
     * @param compute  procedure to compute the value
     * @param executor Do not pass direct executor or else you will have deadlock !
     * @param size     the size of the cache.
     */

    public Cache(Compute<K, V> compute, ExecutorService executor, int size) {
        this.CACHE_SIZE = size;
        this.exceptionStrategy = ExceptionStrategies.alwaysRetain();
        this.compute = compute;
        this.map = new LinkedHashMap<K, SoftValue<K, Future<V>>>(CACHE_SIZE, 0.75f, true) {
            private static final long serialVersionUID = -676712291765286574L;

            @Override
            protected boolean removeEldestEntry(Map.Entry<K, SoftValue<K, Future<V>>> eldest) {
                boolean ret = CACHE_SIZE < size();
                if (ret) {
                    logger.debug("Evict " + eldest.getKey());
                }
                return ret;
            }
        };
        this.executor = executor;
    }

    public synchronized SoftValue<K, Future<V>> remove(K key) {
        processQueue();
        return map.remove(key);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public synchronized void clear() {
        processQueue();
        map.clear();
    }

    public V get(final K key) throws Throwable {
        try {
            return getTask(key).get();
        } catch (ExecutionException e) {
            if (exceptionStrategy.removeEntry(key, e.getCause())) {
                logger.debug("removing entry from cache for key " + key + " because of exception " + e.getCause().toString());
                remove(key);
            }
            throw e.getCause();
        }
    }

    private synchronized Future<V> getTask(final K key) {
        processQueue();
        Future<V> ret;
        SoftReference<Future<V>> sr = map.get(key);
        if (sr != null) {
            ret = sr.get();
            if (ret != null) {
                return ret;
            }
        }
        ret = executor.submit(() -> compute.compute(key));
        SoftValue<K, Future<V>> value = new SoftValue<>(ret, referenceQueue, key);
        map.put(key, value);
        return ret;
    }

    private void processQueue() {
        while (true) {
            Reference<? extends Future<V>> o = referenceQueue.poll();
            if (null == o) {
                return;
            }
            SoftValue<K, Future<V>> k = (SoftValue<K, Future<V>>) o;
            K key = k.key;
            map.remove(key);
        }
    }

    public void setExceptionStrategy(ExceptionStrategy<K> exceptionStrategy) {
        this.exceptionStrategy = exceptionStrategy;
    }

    public static class SoftValue<K, V> extends SoftReference<V> {
        final K key;

        public SoftValue(V ref, ReferenceQueue<V> q, K key) {
            super(ref, q);
            this.key = key;
        }
    }
}

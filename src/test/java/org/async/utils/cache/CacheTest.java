package org.async.utils.cache;

import org.async.utils.executors.DirectExecutorService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.equalTo;

/**
 * User: Barak Bar Orion
 * Date: Feb 8, 2012
 * Time: 12:51:49 AM
 */

@RunWith(value = Parameterized.class)
public class CacheTest {
    private final static Logger logger = LoggerFactory.getLogger(CacheTest.class);

    private ExecutorService executorService;

    @BeforeClass
    public static void beforeClass() {
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][]{{new DirectExecutorService()},
                {Executors.newCachedThreadPool()}};
        return Arrays.asList(data);
    }

    public CacheTest(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Test(timeout = 5000)
    public void testGet() throws Throwable {
        final AtomicBoolean fromCache = new AtomicBoolean();
        Cache<String, String> cache = new Cache<>(key -> {
            fromCache.getAndSet(true);
            return key;
        }, executorService, 1);
        Assert.assertEquals("foo", cache.get("foo"));
        Assert.assertTrue(fromCache.getAndSet(false));
        Assert.assertEquals("foo", cache.get("foo"));
        Assert.assertFalse(fromCache.getAndSet(false));
        Assert.assertEquals("bar", cache.get("bar"));
        Assert.assertTrue(fromCache.getAndSet(false));
        Assert.assertEquals("foo", cache.get("foo"));
        Assert.assertTrue(fromCache.getAndSet(false));
    }

    @Test(timeout = 5000)
    public void testGetWaiting() throws Throwable {
        final AtomicInteger nResults = new AtomicInteger(0);
        final AtomicInteger nComputes = new AtomicInteger(0);
        final AtomicBoolean first = new AtomicBoolean(true);
        final CyclicBarrier computeBarrier = new CyclicBarrier(2);
        final Cache<String, String> cache = new Cache<>(key -> {
            if (first.compareAndSet(true, false)) {
                computeBarrier.await();
                computeBarrier.await();
            }
            nComputes.incrementAndGet();
            return key;
        }, executorService, 1);
        final CyclicBarrier threadsBarrier = new CyclicBarrier(11);
        for (int i = 0; i < 10; ++i) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        threadsBarrier.await();
                        String value = cache.get("foo");
                        nResults.incrementAndGet();
                        Assert.assertEquals("foo", value);
                        threadsBarrier.await();
                    } catch (Throwable throwable) {
                        logger.error(throwable.toString(), throwable);
                    }
                }
            }.start();
        }
        threadsBarrier.await();
        computeBarrier.await();
        Assert.assertEquals(0, nResults.get());
        computeBarrier.await();
        threadsBarrier.await();
        Assert.assertEquals(10, nResults.get());
    }

    @Test(timeout = 5000)
    public void testDefaultExceptionStrategy() throws Throwable {
        //default strategy is to cache all exceptions. (alwaysRetain)
        @SuppressWarnings({"ThrowableInstanceNeverThrown"}) final AtomicReference<Object> result = new AtomicReference<>(new IOException("foo"));
        Cache<String, Object> cache = new Cache<>(key -> {
            Object r = result.get();
            if (r instanceof Exception) {
                throw (Exception) r;
            } else {
                return r;
            }
        }, executorService, 1);
        try {
            cache.get("foo");
            Assert.fail("should have thrown IOException");
        } catch (IOException ignored) {
        }
        result.set("foo");
        try {
            cache.get("foo");
            Assert.fail("should have thrown IOException");
        } catch (IOException ignored) {
        }
    }

    @Test(timeout = 5000)
    public void testAlwaysRemoveExceptionStrategy() throws Throwable {
        @SuppressWarnings({"ThrowableInstanceNeverThrown"}) final AtomicReference<Object> result = new AtomicReference<>(new IOException("foo"));
        Cache<String, Object> cache = new Cache<>(key -> {
            Object r = result.get();
            if (r instanceof Exception) {
                throw (Exception) r;
            } else {
                return r;
            }
        }, executorService, 1);
        cache.setExceptionStrategy(ExceptionStrategies.<String>alwaysRemove());
        try {
            cache.get("foo");
            Assert.fail("should have thrown IOException");
        } catch (IOException ignore) {
        }
        result.set("foo");
        Assert.assertThat("foo", equalTo(cache.get("foo")));
    }

    @Test(timeout = 5000)
    public void testCustomExceptionStrategy() throws Throwable {
        @SuppressWarnings({"ThrowableInstanceNeverThrown"}) final AtomicReference<Object> result = new AtomicReference<>(new UnknownHostException("foo"));
        Cache<String, Object> cache = new Cache<>(key -> {
            Object r = result.get();
            if (r instanceof Exception) {
                throw (Exception) r;
            } else {
                return r;
            }
        }, executorService, 1);
        cache.setExceptionStrategy(ExceptionStrategies.<String>removeOn(IOException.class));
        try {
            cache.get("foo");
            Assert.fail("should have thrown UnknownHostException");
        } catch (UnknownHostException ignore) {
        }
        result.set("foo");
        Assert.assertThat("foo", equalTo(cache.get("foo")));
        //noinspection ThrowableInstanceNeverThrown
        result.set(new IllegalArgumentException("bar"));
        try {
            cache.get("bar");
            Assert.fail("should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ignore) {
        }
        result.set("bar");
        try {
            cache.get("bar");
            Assert.fail("should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test(timeout = 5000)
    public void testLockingPolicy() throws Throwable {
        final CyclicBarrier barrier = new CyclicBarrier(2);
        final Cache<String, Object> cache = new Cache<>(key -> {
            if ("foo".equals(key)) {
                try {
                    barrier.await();
                    barrier.await();
                } catch (Exception ignored) {
                }
            }
            return key;
        }, executorService, 3);
        new Thread() {
            @Override
            public void run() {
                try {
                    String value = (String) cache.get("foo");
                    Assert.assertThat(value, equalTo("foo"));
                } catch (Throwable throwable) {
                    logger.error(throwable.toString(), throwable);
                    Assert.fail(throwable.toString());
                }
            }
        }.start();

        barrier.await();
        Assert.assertThat(cache.get("goo"), equalTo("goo"));
        barrier.await();
    }

}
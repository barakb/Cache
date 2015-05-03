package org.async.utils.cache;

/**
 * User: Barak Bar Orion
 * Date: Feb 2, 2012
 * Time: 11:58:04 AM
 */
public class ExceptionStrategies {
    public static <K> ExceptionStrategy<K> alwaysRetain() {
        return new ExceptionStrategy<K>() {
            @Override
            public <T extends Throwable> boolean removeEntry(K key, T throwable) {
                return false;
            }
        };
    }

    public static <K> ExceptionStrategy<K> alwaysRemove() {
        return new ExceptionStrategy<K>() {
            @Override
            public <T extends Throwable> boolean removeEntry(K key, T throwable) {
                return true;
            }
        };
    }

    public static <K> ExceptionStrategy<K> removeOn(final Class<? extends Throwable> cls) {
        return new ExceptionStrategy<K>() {
            @Override
            public <T extends Throwable> boolean removeEntry(K key, T throwable) {
                return cls.isAssignableFrom(throwable.getClass());
            }
        };
    }

    public static <K> ExceptionStrategy<K> not(final ExceptionStrategy<K> strategy) {
        return new ExceptionStrategy<K>() {
            @Override
            public <T extends Throwable> boolean removeEntry(K key, T throwable) {
                return !strategy.removeEntry(key, throwable);
            }
        };
    }

    public static <K> ExceptionStrategy<K> and(final ExceptionStrategy<K>... strategies) {
        return new ExceptionStrategy<K>() {
            @Override
            public <T extends Throwable> boolean removeEntry(K key, T throwable) {
                for (ExceptionStrategy<K> strategy : strategies) {
                    if (!strategy.removeEntry(key, throwable)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    public static <K> ExceptionStrategy<K> or(final ExceptionStrategy<K>... strategies) {
        return new ExceptionStrategy<K>() {
            @Override
            public <T extends Throwable> boolean removeEntry(K key, T throwable) {
                for (ExceptionStrategy<K> strategy : strategies) {
                    if (strategy.removeEntry(key, throwable)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

}

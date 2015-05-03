package org.async.utils.cache;

/**
 * User: Barak Bar Orion
 * Date: Dec 5, 2011
 * Time: 3:03:54 PM
 */
@SuppressWarnings("unused")
public interface EvictionStrategy<E, K, V> {
    public void evict(E event, Cache<K, V> cache);
}

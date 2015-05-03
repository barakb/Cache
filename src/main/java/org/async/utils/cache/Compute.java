package org.async.utils.cache;

/**
 * User: Barak Bar Orion
 * Date: Dec 5, 2011
 * Time: 3:03:24 PM
 */
public interface Compute<K, V> {
    public V compute(K key) throws Exception;
}

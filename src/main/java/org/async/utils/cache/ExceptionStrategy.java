package org.async.utils.cache;

/**
 * User: Barak Bar Orion
 * Date: Feb 2, 2012
 * Time: 11:53:52 AM
 */
public interface ExceptionStrategy<K> {
    /**
     * 
     * @param key the key of the value that throws an exception
     * @param throwable the exception that was thrown
     * @return true iff this &lt;key, throwable&gt; pair should not be cached.
     */
    <T extends Throwable> boolean removeEntry(K key, T throwable);
}

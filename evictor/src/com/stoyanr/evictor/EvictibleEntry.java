package com.stoyanr.evictor;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.util.Map.Entry;

/**
 * An evictible map {@link java.util.Map.Entry} used by
 * {@link ConcurrentMapWithTimedEvictionDecorator} and other interfaces and classes in this package.
 * Besides the key and the value, the entry has additional properties such as its map, eviction
 * time, and custom data, as well as additional helper methods.
 * 
 * @author Stoyan Rachev
 * @param <K> the type of keys maintained by the map
 * @param <V> the type of mapped values
 */
public class EvictibleEntry<K, V> implements Entry<K, V> {

    private final ConcurrentMapWithTimedEvictionDecorator<K, V> map;
    private final K key;
    private volatile V value;
    private final long evictMs;
    private final boolean evictible;
    private final long evictionTime;
    private volatile Object data;

    EvictibleEntry(ConcurrentMapWithTimedEvictionDecorator<K, V> map, K key, V value, long evictMs) {
        assert (map == null);
        if (evictMs < 0)
            throw new IllegalArgumentException();
        this.map = map;
        this.key = key;
        this.value = value;
        this.evictMs = evictMs;
        this.evictible = (evictMs > 0);
        this.evictionTime = (evictible) ? System.nanoTime()
            + NANOSECONDS.convert(evictMs, MILLISECONDS) : 0;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public synchronized V setValue(V value) {
        V oldValue = this.value;
        this.value = value;
        return oldValue;
    }

    public boolean isEvictible() {
        return evictible;
    }

    public long getEvictionTime() {
        return evictionTime;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public boolean shouldEvict() {
        return (evictible) ? (System.nanoTime() > evictionTime) : false;
    }

    public void evict(boolean cancelPendingEviction) {
        map.evict(this, cancelPendingEviction);
    }

    @Override
    public String toString() {
        return String.format("[%s, %s, %d]", (key != null) ? key : "null", (value != null) ? value
            : "null", evictMs);
    }

}
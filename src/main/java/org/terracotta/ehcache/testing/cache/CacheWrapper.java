package org.terracotta.ehcache.testing.cache;

import net.sf.ehcache.store.MemoryStore;
import net.sf.ehcache.store.disk.DiskStore;

import org.terracotta.ehcache.testing.statistics.Stats;

/**
 * CacheWrapper to enable stats collection along with the basic cache
 * operations.
 *
 * @author Himadri Singh
 *
 */
public interface CacheWrapper {

	/**
	 * Get the value for the key
	 * @param key
	 * @return value
	 */
	public Object get(Object key);

	/**
	 * Stores key-value pair in the cache
	 * @param key
	 * @param value
	 */
	public void put(Object key, Object value);

	/**
	 * Get the name of the underlying cache.
	 *
	 * @return name of the cache
	 */
	public String getName();

	/**
	 * Get the size of the cache
	 * @return cache size
	 */
	public Integer getSize();

	/**
	 * enables statistics collection
	 * default: false
	 * @param enabled
	 */
	public void setStatisticsEnabled(boolean enabled);

	/**
	 * get @Stats for read operations
	 * @return read stats
	 */
	public Stats getReadStats();

	/**
	 * get @Stats for write operations
	 * @return write stats
	 */
	public Stats getWriteStats();

	/**
	 * Resets read/write stats
	 * Useful if running test in phases
	 */
	public void resetStats();

	/**
	 * gets the {@link OffHeapStore} in KB
	 * @return offheap in MB
	 */
	public long getOffHeapSize();

	/**
	 * gets the {@link MemoryStore} size in KB
	 * @return size in MB
	 */
	public long getOnHeapSize();

	/**
	 * get the {@link DiskStore} size in KB
	 * @return
	 */
	public long getOnDiskSize();
}

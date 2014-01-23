package org.terracotta.ehcache.testing.cache;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.store.MemoryStore;
import net.sf.ehcache.store.disk.DiskStore;

import org.terracotta.ehcache.testing.statistics.Stats;

/**
 * CacheWrapper to enable stats collection along with the basic cache
 * operations.
 *
 * @author Himadri Singh
 * @author Sanjay Bansal
 */
public interface CacheWrapper {

  /**
   * Get the value for the key
   *
   * @param key
   * @return value
   */
  Object get(Object key);

  /**
   * Stores key-value pair in the cache
   *
   * @param key
   * @param value
   */
  void put(Object key, Object value);

  /**
   * Put if key is absent
   *
   * @param key
   * @param value
   * @return value
   */
  Object putIfAbsent(Object key, Object value);

  /**
   * Remove key from the cache
   *
   * @param key
   */
  boolean remove(Object key);

  boolean removeElement(Object key, final Object value);

  Element replace(Object key, Object value);

  boolean replaceElement(Object oldKey, Object oldValue, final Object newKey, final Object newValue);

  /**
   * Get the name of the underlying cache.
   *
   * @return name of the cache
   */
  String getName();

  /**
   * Get the size of the cache
   *
   * @return cache size
   */
 Long getSize();

  /**
   * enables statistics collection
   * default: false
   *
   * @param enabled
   */
 void setStatisticsEnabled(boolean enabled);

  /**
   * get @Stats for read operations
   *
   * @return read stats
   */
   Stats getReadStats();

  /**
   * get @Stats for write operations
   *
   * @return write stats
   */
  Stats getWriteStats();

  /**
   * get @Stats for remove operations
   *
   * @return remove stats
   */
  Stats getRemoveStats();

  /**
   * Resets read/write stats
   * Useful if running test in phases
   */
  void resetStats();

  /**
   * gets the {@link net.sf.ehcache.store.offheap.OffHeapStore} in KB
   *
   * @return offheap in MB
   */
   long getOffHeapSize();

  /**
   * gets the {@link net.sf.ehcache.store.MemoryStore} size in KB
   *
   * @return size in MB
   */
  long getOnHeapSize();

  /**
   * get the {@link net.sf.ehcache.store.disk.DiskStore} size in KB
   *
   * @return
   */
  public long getOnDiskSize();

  /**
   * get the cache
   *
   * @return
   */
  public Ehcache getCache();

}

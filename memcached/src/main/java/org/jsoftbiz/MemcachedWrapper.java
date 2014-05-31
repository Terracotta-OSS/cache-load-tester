/*
 *  Copyright Terracotta, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jsoftbiz;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.exception.MemcachedException;
import org.slf4j.Logger;
import org.terracotta.ehcache.testing.cache.GenericCacheWrapper;
import org.terracotta.ehcache.testing.operation.CacheOperation;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Memcached class to wrap Memcached operations for the load tester lib module
 * using xmemcached memcached client https://github.com/killme2008/xmemcached
 *
 * @author Aurelien Broszniowski
 */

public class MemcachedWrapper extends GenericCacheWrapper {
  private MemcachedClient client;

  public static GenericCacheWrapper memcached(final MemcachedClient client) {
    return new MemcachedWrapper(client);
  }

  public MemcachedWrapper(final MemcachedClient client) {
    this.client = client;
  }

  @Override
  public String getName() {
    return client.getName();
  }

  @Override
  public Object getCache() {
    return client;
  }

  @Override
  public long getSize() {
    long totalSize = 0;
    try {
      Map<InetSocketAddress, Map<String, String>> stats = client.getStats();
      for (InetSocketAddress address : stats.keySet()) {
        Map<String, String> stringMap = stats.get(address);
        int total_items = Integer.parseInt(stringMap.get("bytes"));   // curr_items
        totalSize += total_items;
      }
    } catch (MemcachedException e) {

    } catch (InterruptedException e) {

    } catch (TimeoutException e) {

    }
    return totalSize;
  }

  @Override
  public void logMemoryInfo(final Logger logger) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public String getDefaultLoaderOperationName() {
    return MemcachedOperation.OPERATIONS.SET.name();
  }

  @Override
  public String getDefaultAccessorOperationName() {
    return MemcachedOperation.OPERATIONS.GET.name();
  }

  @Override
  public CacheOperation getDefaultLoaderOperation(final double ratio) {
    return MemcachedOperation.set(ratio);
  }

  @Override
  public CacheOperation getDefaultAccessorOperation(final double ratio) {
    return MemcachedOperation.get(ratio);
  }
}

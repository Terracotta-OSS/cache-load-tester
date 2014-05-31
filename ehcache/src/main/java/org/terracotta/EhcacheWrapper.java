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
package org.terracotta;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Status;
import net.sf.ehcache.config.MemoryUnit;
import net.sf.ehcache.constructs.nonstop.NonStopCacheException;
import org.slf4j.Logger;
import org.terracotta.ehcache.testing.cache.GenericCacheWrapper;
import org.terracotta.ehcache.testing.operation.CacheOperation;

/**
 * Ehcache class to wrap Ehcache operations for the load tester lib module
 * @author Aurelien Broszniowski
 */

public class EhcacheWrapper extends GenericCacheWrapper {
  private Ehcache ehcache;

  public static GenericCacheWrapper ehcache(final Ehcache ehcache) {
    return new EhcacheWrapper(ehcache);
  }

  public static GenericCacheWrapper[] ehcache(final Ehcache... caches) {
    EhcacheWrapper[] ehcaches = new EhcacheWrapper[caches.length];
    for (int i = 0; i < caches.length; i++) {
      ehcaches[i] = new EhcacheWrapper(caches[i]);
    }
    return ehcaches;
  }


  public EhcacheWrapper(final Ehcache ehcache) {
    this.ehcache = ehcache;
  }

  @Override
  public String getName() {
    return this.ehcache.getName();
  }

  @Override
  public Object getCache() {
    return this.ehcache;
  }

  @Override
  public long getSize() {
    return (long)this.ehcache.getSize();
  }

  @Override
  public void logMemoryInfo(final Logger logger) {
    logger.info("--- mem ---");

    if (Status.STATUS_ALIVE.equals(this.ehcache.getStatus())) {
      logger.info("Cache name = {} \t\t OnHeap={}Kb \t\t OffHeap={}Kb \t\t OnDisk={}Kb", new Object[] {
          getName(),
          getOnHeapSize(),
          getOffHeapSize(),
          getOnDiskSize()
      });
    }
  }

  @Override
  public String getDefaultLoaderOperationName() {
    return EhcacheOperation.OPERATIONS.PUT.name();
  }

  @Override
  public String getDefaultAccessorOperationName() {
    return EhcacheOperation.OPERATIONS.GET.name();
  }

  @Override
  public CacheOperation getDefaultLoaderOperation(final double ratio) {
    return EhcacheOperation.put(ratio);
  }

  @Override
  public CacheOperation getDefaultAccessorOperation(final double ratio) {
    return EhcacheOperation.get(ratio);
  }

  public long getOnHeapSize() {
    try {
      return MemoryUnit.BYTES.toKiloBytes(this.ehcache.getStatistics().getLocalHeapSizeInBytes());
    } catch (NoSuchMethodError e) {
      return -1;
    } catch (NonStopCacheException nsce) {
      return -1;
    } catch (UnsupportedOperationException e) {
      return -1;
    }
  }

  public long getOffHeapSize() {
    try {
      return MemoryUnit.BYTES.toKiloBytes(this.ehcache.getStatistics().getLocalOffHeapSizeInBytes());
    } catch (NonStopCacheException nsce) {
      return -1;
    } catch (NoSuchMethodError e) {
      return -1;
    } catch (UnsupportedOperationException e) {
      return -1;
    }
  }

  public long getOnDiskSize() {
    try {
      return MemoryUnit.BYTES.toKiloBytes(this.ehcache.getStatistics().getLocalDiskSizeInBytes());
    } catch (UnsupportedOperationException e) {
      return -1;
    } catch (NonStopCacheException nsce) {
      return -1;
    } catch (NoSuchMethodError e) {
      return -1;
    }
  }

}

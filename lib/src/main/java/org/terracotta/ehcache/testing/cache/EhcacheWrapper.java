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

package org.terracotta.ehcache.testing.cache;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Status;
import net.sf.ehcache.constructs.nonstop.NonStopCacheException;
import org.slf4j.Logger;

public class EhcacheWrapper extends GenericCacheWrapper {
  private Ehcache ehcache;
  private static final int KB = 1024;

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

  public long getOnHeapSize() {
    try {
      return this.ehcache.getStatistics().getLocalHeapSizeInBytes() / KB;
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
      return this.ehcache.getStatistics().getLocalOffHeapSizeInBytes() / KB;
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
      return this.ehcache.getStatistics().getLocalDiskSizeInBytes() / KB;
    } catch (UnsupportedOperationException e) {
      return -1;
    } catch (NonStopCacheException nsce) {
      return -1;
    } catch (NoSuchMethodError e) {
      return -1;
    }
  }

}

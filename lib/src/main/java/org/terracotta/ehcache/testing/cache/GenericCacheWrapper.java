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

import org.slf4j.Logger;
import org.terracotta.ehcache.testing.operation.CacheOperation;
import org.terracotta.ehcache.testing.statistics.Stats;
import org.terracotta.ehcache.testing.statistics.StatsReporter;

public abstract class GenericCacheWrapper {

  protected final Stats readStats = new Stats();
  protected final Stats writeStats = new Stats();
  protected final Stats removeStats = new Stats();

  private boolean statistics;

  public abstract String getName();

  public abstract Object getCache();

  public abstract long getSize();

  public abstract void logMemoryInfo(final Logger logger);

  /**
   * Enables statistics collection and registers cache to
   * {@link org.terracotta.ehcache.testing.statistics.StatsReporter}
   */
  public void setStatisticsEnabled(boolean statistics) {
    this.statistics = statistics;
    if (statistics)
      StatsReporter.getInstance().register(this);
  }

  public Stats getReadStats() {
    return readStats;
  }

  public Stats getWriteStats() {
    return writeStats;
  }

  public Stats getRemoveStats() {
    return removeStats;
  }

  public void resetStats() {
    readStats.reset();
    writeStats.reset();
    removeStats.reset();
  }

  public boolean isStatisticsEnabled() {
    return statistics;
  }

  abstract public String getDefaultLoaderOperationName();

  abstract public String getDefaultAccessorOperationName();

  abstract public CacheOperation getDefaultLoaderOperation(final double ratio);

  abstract public CacheOperation getDefaultAccessorOperation(final double ratio);

}

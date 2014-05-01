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

/**
 * Returns a GenericCacheWrapper wrapping a specific Cache implementation
 * @TODO : This could be included as part of the EhcacheWrapper, so for different caches impl. we do not need to include
 * all jars, e.g. EhcacheWrapper would be a submodule that gets included if we want to test Ehcache. The submodule would then
 * reference the Ehcache dependencies
 *
 * @author Aurelien Broszniowski
 *
 */
public class CACHES {

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
}

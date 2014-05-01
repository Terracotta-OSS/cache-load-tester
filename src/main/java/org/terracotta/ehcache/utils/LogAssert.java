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
package org.terracotta.ehcache.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to Log assertion results instead of actually asserting them.
 * This helps to debug what actual assertions happen, since some behaviours are sometimes tricky to follow
 *
 * @author Aurelien Broszniowski
 */

public class LogAssert {
  private static Logger logger = LoggerFactory.getLogger("logAssert");

  public static void assertEquals(final long actual, final long expected, final String message) {
    logger.info("--> {} : (actual){} == {}(expected)", new Object[] { message, actual, expected });
  }
}

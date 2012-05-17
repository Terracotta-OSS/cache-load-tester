package org.terracotta.ehcache.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;import java.lang.Object;import java.lang.String;

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

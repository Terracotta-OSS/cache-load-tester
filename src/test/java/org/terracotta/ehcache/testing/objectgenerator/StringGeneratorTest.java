package org.terracotta.ehcache.testing.objectgenerator;

import org.junit.Assert;
import org.junit.Test;

/**
 * Made by aurbrsz / 7/8/11 - 12:45
 */
public class StringGeneratorTest {

  @Test
  public void testIntegers() {
    final ObjectGenerator generator = StringGenerator.integers();
    final Object generate = generator.generate(1);
   Assert.assertEquals(1, Integer.parseInt(generate.toString()));
  }

  @Test
  public void testRandomString() {
    final ObjectGenerator generator = StringGenerator.randomString(10);
    final Object generate = generator.generate(10);
    Assert.assertEquals(10, generate.toString().length());
    Assert.assertNotSame(generate.toString().substring(0, 1) ,
        generate.toString().substring(1, 2));
  }

  @Test
  public void testChars() {
    final ObjectGenerator generator = StringGenerator.chars(10);
    final Object generate = generator.generate(2);
    Assert.assertEquals("2222222222", generate.toString());
  }

}

package org.terracotta.ehcache.testing.objectgenerator;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertArrayEquals;

/**
 * Made by aurbrsz / 7/13/11 - 1:00
 */
public class ByteArrayGeneratorTest {

  @Test
  public void testFixedSizeArray() {
    final ObjectGenerator generator = ByteArrayGenerator.fixedSize(10);
    final byte[] generate = (byte[])generator.generate(1);
    Assert.assertEquals(10, generate.length);
  }

  @Test
  public void testVariableSizearray() {
    int minSize = 5;
    int maxSize = 10;
    final ObjectGenerator generator = ByteArrayGenerator.randomSize(minSize, maxSize);
    int min = Integer.MAX_VALUE;
    int max = Integer.MIN_VALUE;
    for (int i = 0; i < 500; i++) {
      byte[] generate = (byte[])generator.generate(i);
      if (generate.length > max) {
        max = generate.length;
      }
      if (generate.length < min) {
        min = generate.length;
      }
    }
    Assert.assertEquals(minSize, min);
    Assert.assertEquals(maxSize, max);
  }

  @Test
  public void testCollections() {
    final ObjectGenerator generator = ByteArrayGenerator.collections(50, 4);
    final Collection generate = (Collection)generator.generate(3);
//    int depth = assertCollections(generate, 50);
//    Assert.assertEquals("collection depth", 4, depth);

    final Object[] collections = generate.toArray();
    Assert.assertEquals("For seed = 3, we should have 4 collections in the object", 4, collections.length);

    Assert.assertEquals("For seed = 4, collection[0] has 0 collections", 0, ((Collection)collections[0]).size());
    Assert.assertEquals("For seed = 4, collection[1] has 0 collections", 0, ((Collection)collections[1]).size());
    Assert.assertEquals("For seed = 4, collection[2] has 1 collections", 1, ((Collection)collections[2]).size());
    Assert.assertEquals("For seed = 4, collection[3] has 1 collections", 1, ((Collection)collections[3]).size());
  }

/*  private int assertCollections(final Object[] collections, final int size, final int depth) {
    for (int i = 0; i < collections.length; i++) {
      Object collection = collections[i];
      if (collection instanceof Collection) {
        assertCollections(collection, size, depth);
      }
    }
  }*/

   @Test
  public void testGraph() {
    final ObjectGenerator generator = ByteArrayGenerator.objectGraph(50, 3);
    final Triple generate = (Triple)generator.generate(3);
  }

  @Test
  public void testRandomByteGenerator() {
    ObjectGenerator generator = ByteArrayGenerator.fixedSize(128);
    byte[] result1 = (byte[])generator.generate(1L);
    byte[] result2 = (byte[])generator.generate(1L);
    assertArrayEquals(result1, result2);
  }
}

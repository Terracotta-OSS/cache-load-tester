package org.terracotta.ehcache.testing.objectgenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class ByteArrayGenerator implements ObjectGenerator {

  private Holding holding;
  private final int min;
  private final int max;
  private int depth;

  private enum Holding {COLLECTION, GRAPH, NOTHING}

  /**
   * Returns a generator giving a java.lang.Object holding either a java.util.Collection
   * with other instances of Object or Collection, recursing up to a depth
   *
   * @param holding if the Object holds a Collection of objects, just single Object or nothing
   * @param min
   * @param max
   * @param depth   recursing depth
   */
  public ByteArrayGenerator(final Holding holding, final int min, final int max, final int depth) {
    this.holding = holding;
    this.min = min;
    this.max = max;
    this.depth = depth;
  }

  public Object generate(int seed) {
    final Random random = new Random(seed);

    if (Holding.COLLECTION.equals(holding)) {
      return getCollection(depth, random);
    } else if (Holding.GRAPH.equals(holding)) {
      return getGraph(depth, random);
    } else {
      return getObject(random);
    }
  }

  public static ObjectGenerator fixedSize(int size) {
    return new ByteArrayGenerator(Holding.NOTHING, size, size, 0);
  }

  public static ObjectGenerator randomSize(int min, int max) {
    return new ByteArrayGenerator(Holding.NOTHING, min, max, 0);
  }

  public static ObjectGenerator collections(int size, int depth) {
    return new ByteArrayGenerator(Holding.COLLECTION, size, size, depth);
  }

  public static ObjectGenerator objectGraph(int size, int depth) {
    return new ByteArrayGenerator(Holding.GRAPH, size, size, depth);
  }

  private Collection<?> getCollection(final int recursiveCounter, final Random random) {

    List returnList = new ArrayList();
    if (recursiveCounter > 0) {
      final int maxCollections = random.nextInt(depth + 1);
      for (int j = 0; j < maxCollections; j++) {
        returnList.add(getCollection(recursiveCounter - 1, random));
      }
    } else {
      final int maxCollectionObjects = random.nextInt(depth + 1);
      for (int j = 0; j < maxCollectionObjects; j++) {
        returnList.add(getObject(random));
      }
    }
    return returnList;
  }

  private Object getGraph(final int recursiveCounter, final Random random) {
    List<Object> references = new ArrayList<Object>();
    Object graph = recurseGraph(recursiveCounter, random, references);

    for (int i = 0; i < references.size(); i++) {
      Triple triple = (Triple)references.get(i);
      if (random.nextBoolean()) {
        triple.setOne(getObject(random));
        // we keep two
        triple.setThree(references.get(random.nextInt(references.size())));
      }
    }
    return graph;
  }


  private Object recurseGraph(final int recursiveCounter, final Random random, final List<Object> references) {
    if (recursiveCounter > 0) {
      Triple triple = new Triple();
      triple.setOne(recurseGraph(recursiveCounter - 1, random, references));
      triple.setTwo(recurseGraph(recursiveCounter - 1, random, references));
      triple.setThree(recurseGraph(recursiveCounter - 1, random, references));
      references.add(triple);
      return triple;
    } else {
      final Triple triple = new Triple();
      references.add(triple);

      triple.setOne(getObject(random));
      triple.setTwo(getObject(random));
      triple.setThree(getObject(random));
      return triple;
    }

  }

  private Object getObject(final Random random) {
    final int size = min + random.nextInt(max - min + 1);

    byte[] object = new byte[size];
    Arrays.fill(object, (byte)random.nextInt());
    return object;
  }

}

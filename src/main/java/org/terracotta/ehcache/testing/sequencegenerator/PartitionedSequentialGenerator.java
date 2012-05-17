package org.terracotta.ehcache.testing.sequencegenerator;

import java.util.concurrent.atomic.AtomicInteger;

public class PartitionedSequentialGenerator implements SequenceGenerator {

  private final int start;
  private final int stride;

  public PartitionedSequentialGenerator(int i, int count) {
    this.start = i;
    this.stride = count;
  }

  public Sequence createSequence() {
    return new PartitionedSequence(start, stride);
  }

  static class PartitionedSequence implements Sequence {

    private final AtomicInteger next;
    private final int stride;

    public PartitionedSequence(int start, int stride) {
      this.next = new AtomicInteger(start);
      this.stride = stride;
    }

    public int next() {
      return next.getAndAdd(stride);
    }
  }
}

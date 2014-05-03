package org.terracotta.ehcache.testing.sequencegenerator;

import java.util.concurrent.atomic.AtomicLong;

public class PartitionedSequentialGenerator implements SequenceGenerator {

  private final long start;
  private final long stride;

  public PartitionedSequentialGenerator(long i, long count) {
    this.start = i;
    this.stride = count;
  }

  public Sequence createSequence() {
    return new PartitionedSequence(start, stride);
  }

  static class PartitionedSequence implements Sequence {

    private final AtomicLong next;
    private final long stride;

    public PartitionedSequence(long start, long stride) {
      this.next = new AtomicLong(start);
      this.stride = stride;
    }

    public long next() {
      return next.getAndAdd(stride);
    }
  }
}

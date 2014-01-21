package org.terracotta.ehcache.testing.statistics;

import org.junit.Assert;
import org.junit.Test;

/**
 * Made by aurbrsz / 7/25/11 - 20:50
 */
public class StatsTest {

  @Test
  public void testEmptyConstructor() {
    Stats stats = new Stats();
    Assert.assertEquals(0, stats.getTxnCount());
    Assert.assertEquals(Double.NaN, stats.getMaxLatency(), 0);
    Assert.assertEquals(Double.NaN, stats.getMinLatency(), 0);
  }

  @Test
  public void testNullParameterConstructor() {
    Stats stats = new Stats(null);
    Assert.assertEquals(0, stats.getTxnCount());
    Assert.assertEquals(Double.NaN, stats.getMaxLatency(), 0);
    Assert.assertEquals(Double.NaN, stats.getMinLatency(), 0);
  }

  @Test
  public void testNonNullParameterConstructor() {
    Stats stats = new Stats();
    stats.add(5L);
    Assert.assertEquals(1, stats.getTxnCount());
    Assert.assertEquals(5L, stats.getMaxLatency(), 0);
    Assert.assertEquals(5L, stats.getMinLatency(), 0);

    Stats secondStats = new Stats(stats);
    Assert.assertEquals(1, secondStats.getTxnCount());
    Assert.assertEquals(5L, secondStats.getMaxLatency(), 0);
    Assert.assertEquals(5L, secondStats.getMinLatency(), 0);

    secondStats.add(3L);
    Assert.assertEquals(2, secondStats.getTxnCount());
    Assert.assertEquals(5L, secondStats.getMaxLatency(), 0);
    Assert.assertEquals(3L, secondStats.getMinLatency(), 0);
  }

  @Test
  public void testNonNullParameterConstructorAndAdd() {
    Stats stats = new Stats();
    stats.add(3L);
    Assert.assertEquals(1, stats.getTxnCount());
    Assert.assertEquals(3L, stats.getMaxLatency(), 0);
    Assert.assertEquals(3L, stats.getMinLatency(), 0);

    Stats secondStats = new Stats(stats);
    Assert.assertEquals(1, secondStats.getTxnCount());
    Assert.assertEquals(3L, secondStats.getMaxLatency(), 0);
    Assert.assertEquals(3L, secondStats.getMinLatency(), 0);

    secondStats.add(secondStats);
    Assert.assertEquals(2, secondStats.getTxnCount());
    Assert.assertEquals(3L, secondStats.getMaxLatency(), 0);
    Assert.assertEquals(3L, secondStats.getMinLatency(), 0);
  }

  @Test
  public void testPeriodStats() {
    Stats stats = new Stats();
	  stats.add(300);
	  stats.add(700);
	  stats.incrementTotalExceptionCount();
	  stats.incrementTotalExceptionCount();

	  Stats period = stats.getPeriodStats();
	  Assert.assertEquals(2, period.getTxnCount());
	  Assert.assertEquals(300, period.getMinLatency(), 0);
	  Assert.assertEquals(700, period.getMaxLatency(), 0);
	  Assert.assertEquals(500, period.getAvgLatency(), 0);
	  Assert.assertEquals(2, period.getTotalExceptionCount());
//	  Assert.assertEquals(1, period.getHisto().getBUCKET_200_500_COUNT());
//	  Assert.assertEquals(1, period.getHisto().getBUCKET_500_1000_COUNT());

	  Assert.assertEquals(2, stats.getTxnCount());
	  Assert.assertEquals(300, stats.getMinLatency(), 0);
	  Assert.assertEquals(700, stats.getMaxLatency(), 0);
	  Assert.assertEquals(500, stats.getAvgLatency(), 0);
	  Assert.assertEquals(2, stats.getTotalExceptionCount());
//	  Assert.assertEquals(1, stats.getHisto().getBUCKET_200_500_COUNT());
//	  Assert.assertEquals(1, stats.getHisto().getBUCKET_500_1000_COUNT());

	  period = stats.getPeriodStats();
	  Assert.assertEquals(0, period.getTxnCount());
	  Assert.assertEquals(Double.NaN, period.getMinLatency(), 0);
	  Assert.assertEquals(Double.NaN, period.getMaxLatency(), 0);
	  Assert.assertEquals(0, period.getAvgLatency(), 0);
	  Assert.assertEquals(0, period.getTotalExceptionCount());
//	  Assert.assertEquals(0, period.getHisto().getBUCKET_200_500_COUNT());
//	  Assert.assertEquals(0, period.getHisto().getBUCKET_500_1000_COUNT());

	  stats.add(1000);
	  stats.add(4000);
	  stats.incrementTotalExceptionCount();

	  period = stats.getPeriodStats();
	  Assert.assertEquals(2, period.getTxnCount());
	  Assert.assertEquals(1000, period.getMinLatency(), 0);
	  Assert.assertEquals(4000, period.getMaxLatency(), 0);
	  Assert.assertEquals(2500, period.getAvgLatency(), 0);
	  Assert.assertEquals(1, period.getTotalExceptionCount());
//	  Assert.assertEquals(2, stats.getHisto().getBUCKET_500_1000_COUNT());
//	  Assert.assertEquals(1, stats.getHisto().getBUCKET_1000_5000_COUNT());

	  Assert.assertEquals(4, stats.getTxnCount());
	  Assert.assertEquals(300, stats.getMinLatency(), 0);
	  Assert.assertEquals(4000, stats.getMaxLatency(), 0);
	  Assert.assertEquals(1500, stats.getAvgLatency(), 0);
	  Assert.assertEquals(3, stats.getTotalExceptionCount());
//	  Assert.assertEquals(1, stats.getHisto().getBUCKET_200_500_COUNT());
//	  Assert.assertEquals(2, stats.getHisto().getBUCKET_500_1000_COUNT());
//	  Assert.assertEquals(1, stats.getHisto().getBUCKET_1000_5000_COUNT());
  }

  @Test
  public void testStatsFinalise() throws InterruptedException {
    Stats stats = new Stats();
	  stats.add(3000);
	  stats.add(5000);
	  stats.finalise();
	  long tps = stats.getThroughput();
	  Thread.sleep(1000);
	  Assert.assertEquals(tps, stats.getThroughput());
	  Thread.sleep(2000);
	  Assert.assertEquals(tps, stats.getThroughput());
	  Thread.sleep(1000);
	  Assert.assertEquals(tps, stats.getThroughput());
  }

  @Test
  public void testReset() {
    Stats stats = new Stats();
	  stats.add(300);
	  stats.add(700);
	  stats.incrementTotalExceptionCount();
	  stats.incrementTotalExceptionCount();

	  Assert.assertEquals(2, stats.getTxnCount());
	  Assert.assertEquals(300, stats.getMinLatency(), 0);
	  Assert.assertEquals(700, stats.getMaxLatency(), 0);
	  Assert.assertEquals(500, stats.getAvgLatency(), 0);
	  Assert.assertEquals(2, stats.getTotalExceptionCount());
//	  Assert.assertEquals(1, stats.getHisto().getBUCKET_200_500_COUNT());
//	  Assert.assertEquals(1, stats.getHisto().getBUCKET_500_1000_COUNT());

	  stats.reset();

	  Assert.assertEquals(0, stats.getTxnCount());
	  Assert.assertEquals(Double.NaN, stats.getMinLatency(), 0);
	  Assert.assertEquals(Double.NaN, stats.getMaxLatency(), 0);
	  Assert.assertEquals(0, stats.getAvgLatency(), 0);
	  Assert.assertEquals(0, stats.getTotalExceptionCount());
//	  Assert.assertEquals(0, stats.getHisto().getBUCKET_200_500_COUNT());
//	  Assert.assertEquals(0, stats.getHisto().getBUCKET_500_1000_COUNT());
  }

  @Test
  public void testStatsAddition() {
    Stats one = new Stats();
	  one.add(300);
	  one.add(700);
	  one.incrementTotalExceptionCount();
	  one.incrementTotalExceptionCount();

	  Stats two = new Stats();
	  two.add(55);
	  two.add(89);
	  two.add(1234);
	  two.incrementTotalExceptionCount();

	  Assert.assertEquals(2, one.getTxnCount());
	  Assert.assertEquals(300, one.getMinLatency(), 0);
	  Assert.assertEquals(700, one.getMaxLatency(), 0);
	  Assert.assertEquals(500, one.getAvgLatency(), 0);
	  Assert.assertEquals(2, one.getTotalExceptionCount());
//	  Assert.assertEquals(1, one.getHisto().getBUCKET_200_500_COUNT());
//	  Assert.assertEquals(1, one.getHisto().getBUCKET_500_1000_COUNT());

	  one.add(two);

	  Assert.assertEquals(5, one.getTxnCount());
	  Assert.assertEquals(55, one.getMinLatency(), 0);
	  Assert.assertEquals(1234, one.getMaxLatency(), 0);
	  Assert.assertEquals((double)(89 + 55 + 1234 + 300 + 700)/5, one.getAvgLatency(), 0);
	  Assert.assertEquals(3, one.getTotalExceptionCount());
//	  Assert.assertEquals(2, one.getHisto().getBUCKET_50_100_COUNT());
//	  Assert.assertEquals(1, one.getHisto().getBUCKET_200_500_COUNT());
//	  Assert.assertEquals(1, one.getHisto().getBUCKET_500_1000_COUNT());
//	  Assert.assertEquals(1, one.getHisto().getBUCKET_1000_5000_COUNT());
  }
}

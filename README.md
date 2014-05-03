Cache Load Tester
=================

What is it?
-----------
This is a cache loader tester API

Tell me more!
-------------
This API is made of two main components : The CacheLoader and the CacheAccessor

The CacheLoader is mainly for the warmup phase : To load the cache
The CacheAccessor is for the test phase : It reads from the cache and can validate that the content is the one expected, it can also update the cache if wanted.

Both have statistics support, you can enable stats and get various info (TPS, cache sizes etc.) directly on the console or in csv format.

This is a DSL API, aka fluent interface (see http://en.wikipedia.org/wiki/Fluent_interface#Java)
So your IDE autocompletion will help you a lot to know about the various options.

CacheLoader example:
--------------------
The example below will load 3 caches (one, two, three), putting Elements with key is a String, value is a byte array[128]

It will do 100% of put() (default setting)

It will load the caches concurrently with 4 threads sequentially (the keys will be incremented sequentially), putting 10000 Elements.
It will enable the statistics and print them to the console.
```
CacheDriver loader = CacheLoader
  .load(ehcache(one, two, three))
      .using(StringGenerator.integers(), ByteArrayGenerator.fixedSize(128))
      .fillPartitioned(10000, 4)
      .enableStatistics(true).addLogger(new ConsoleStatsLoggerImpl());
```
  
CacheAccessor example:
----------------------
In this example, we access the caches one and two, reading Elements with key is a String, value is a byte array from random size (between 800 and 1200 bytes).
It will read keys randomly, following a gaussian distribution between 0 and 10000, and a width on the gaussian function of 1000 (thats the width of the 'bell' in the gaussian curve).

It will update 0.10, i.e. 10% of the elements, get 75%, putIfAbsent 5% and remove also 10% of the elements while running.
it will stop after 180 seconds.
And stats are enabled. And again we use 4 Threads to access the caches concurrently.

```
CacheAccessor access = CacheAccessor.access(one, two)
    .get(0.75).remove(0.10).update(0.10).putIfAbsent(0.05)
    .using(StringGenerator.integers(), ByteArrayGenerator.randomSize(800, 1200))
    .atRandom(Distribution.GAUSSIAN, 0, 10000, 1000).updateRatio(0.2).removeRatio(0.2)
    .terminateOn(new TimedTerminationCondition(180, TimeUnit.SECONDS)).enableStatistics(true)
    .addLogger(new ConsoleStatsLoggerImpl());
ParallelDriver.inParallel(4, access).run();
```

Version
----
1.1.0

Maven dependency
----------------

```
    <dependency>
      <groupId>org.terracotta</groupId>
      <artifactId>cache-load-tester</artifactId>
      <version>1.1.0</version>
    </dependency>
```
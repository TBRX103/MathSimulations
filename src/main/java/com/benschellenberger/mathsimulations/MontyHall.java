package com.benschellenberger.mathsimulations;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

public class MontyHall {

  private static final int NUM_GAMES = 10000000;
  private static final AtomicLong WINS_NO_SWAP = new AtomicLong(0);
  private static final AtomicLong WINS_SWAP = new AtomicLong(0);
  private static final Random RANDOM = new SecureRandom();
  private static final ExecutorService EXECUTOR_SERVICE = Executors.newVirtualThreadPerTaskExecutor();

  public static void main(String... args) throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(NUM_GAMES);
    IntStream.range(0, NUM_GAMES)
        .forEach(i -> EXECUTOR_SERVICE.submit(new MontyHallRunnable(latch)));

    latch.await();

    System.out.println("Monty Hall");
    System.out.println("Wins With Swap/Loss Without Swap: " + WINS_SWAP.get());
    System.out.println("Wins Without Swap/Loss With Swap: " + WINS_NO_SWAP.get());
  }

  private static class MontyHallRunnable implements Runnable {

    private final CountDownLatch countDownLatch;

    public MontyHallRunnable(CountDownLatch latch) {
      this.countDownLatch = latch;
    }

    private static int getPrizeIndex(int[] doors) {
      for (int i = 0; i < doors.length; i++) {
        if (doors[i] == 1) {
          return i;
        }
      }
      throw new RuntimeException("No prize found");
    }

    public static int[] setupPrize() {
      int[] doors = new int[3];
      int prize = RANDOM.nextInt(0, 3);

      for (int i = 0; i < doors.length; i++) {
        if (prize == i) {
          doors[i] = 1;
        } else {
          doors[i] = 0;
        }
      }
      return doors;
    }

    @Override
    public void run() {
      try {
        int[] doors = setupPrize();
        int pick = RANDOM.nextInt(doors.length);
        int prizeIndex = getPrizeIndex(doors);

        if (prizeIndex == pick) {
          WINS_NO_SWAP.incrementAndGet();
        } else {
          WINS_SWAP.incrementAndGet();
        }
      } finally {
        countDownLatch.countDown();
      }
    }
  }
}


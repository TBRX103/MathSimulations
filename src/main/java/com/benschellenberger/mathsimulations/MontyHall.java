package com.benschellenberger.mathsimulations;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.LongStream;

public class MontyHall {

  private static final long NUM_GAMES = 100;
  private static final long NUM_THREADS = Runtime.getRuntime().availableProcessors();
  private static final AtomicLong GAME_COUNTER = new AtomicLong(0);
  private static final AtomicLong WINS_NO_SWAP = new AtomicLong(0);
  private static final AtomicLong WINS_SWAP = new AtomicLong(0);
  private static final long SLEEP_CHECK_DELAY_MS = 100;
  private static final Random RANDOM = new SecureRandom();

  public static void main(String... args) throws InterruptedException {

    LongStream.range(0, NUM_THREADS).forEach(i -> {
      new MontyHallThread().start();
    });

    while (GAME_COUNTER.get() < NUM_GAMES) {
      Thread.sleep(SLEEP_CHECK_DELAY_MS);
    }

    System.out.println("Monty Hall");
    System.out.println("Wins With Swap/Loss Without Swap: " + WINS_SWAP.get());
    System.out.println("Wins Without Swap/Loss With Swap: " + WINS_NO_SWAP.get());

  }

  private static class MontyHallThread extends Thread {

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

    public static long getNewGameId() {
      long gameId = GAME_COUNTER.incrementAndGet();
      if (gameId > NUM_GAMES) {
        return -1L;
      }
      return gameId;
    }

    @Override
    public void run() {
      long currentGameId = getNewGameId();

      while (currentGameId >= 0) {
        try {
          int[] doors = setupPrize();

          int pick = doors[RANDOM.nextInt(doors.length)];

          int prizeIndex = getPrizeIndex(doors);

          if (prizeIndex == pick) {
            WINS_NO_SWAP.incrementAndGet();
          } else {
            WINS_SWAP.incrementAndGet();
          }
        } finally {
          currentGameId = getNewGameId();
        }
      }
    }
  }
}

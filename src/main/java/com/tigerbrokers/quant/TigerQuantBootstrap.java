package com.tigerbrokers.quant;

import com.tigerbrokers.quant.core.MainEngine;
import com.tigerbrokers.quant.event.EventEngine;
import com.tigerbrokers.quant.gateway.tiger.TigerGateway;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Description: bootstrap
 *
 * @author kevin
 * @date 2019/08/15
 */
public class TigerQuantBootstrap {

  private static final ReentrantLock LOCK = new ReentrantLock();
  private static final Condition STOP = LOCK.newCondition();

  public static void main(String[] args) {
    EventEngine eventEngine = new EventEngine();
    MainEngine mainEngine = new MainEngine(eventEngine);
    mainEngine.addGateway(new TigerGateway(eventEngine));
    mainEngine.start();
    addHook(mainEngine);
    try {
      LOCK.lock();
      STOP.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e.getMessage());
    } finally {
      LOCK.unlock();
    }
  }

  private static void addHook(MainEngine mainEngine) {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      try {
        mainEngine.stop();
      } catch (Exception e) {
        throw new RuntimeException("main stop exception ", e);
      }

      try {
        LOCK.lock();
        STOP.signal();
      } finally {
        LOCK.unlock();
      }
    }, "main-shutdown-hook"));
  }
}

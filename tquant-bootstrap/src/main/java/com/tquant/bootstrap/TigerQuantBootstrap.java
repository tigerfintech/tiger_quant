package com.tquant.bootstrap;

import com.tquant.core.TigerQuantException;
import com.tquant.core.core.MainEngine;
import com.tquant.core.event.EventEngine;
import com.tquant.gateway.tiger.TigerGateway;
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
    LOCK.lock();
    try {
      EventEngine eventEngine = new EventEngine();
      MainEngine mainEngine = new MainEngine(eventEngine);
      mainEngine.addGateway(new TigerGateway(eventEngine));
      mainEngine.start();
      addHook(mainEngine);
      STOP.await();
    } catch (TigerQuantException e1){
      e1.printStackTrace();
      LOCK.unlock();
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

      LOCK.lock();
      try {
        STOP.signal();
      } finally {
        LOCK.unlock();
      }
    }, "main-shutdown-hook"));
  }
}

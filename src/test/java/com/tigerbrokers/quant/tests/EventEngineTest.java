package com.tigerbrokers.quant.tests;

import com.tigerbrokers.quant.event.Event;
import com.tigerbrokers.quant.event.EventEngine;
import com.tigerbrokers.quant.event.EventHandler;
import org.junit.Test;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/08/15
 */
public class EventEngineTest {

  @Test
  public void testRegisterHandler() {
    EventEngine ee = new EventEngine();
    EventHandler handler = new EventHandler() {
      @Override
      public void process(Event event) {
        System.out.println("process");
      }
    };
    ee.registerCommonHandler(handler);
    ee.start();
    ee.stop();
  }
}

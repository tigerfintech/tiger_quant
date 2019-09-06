package com.tigerbrokers.quant.event;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/08/15
 */
public interface EventHandler {

  void process(Event event);
}

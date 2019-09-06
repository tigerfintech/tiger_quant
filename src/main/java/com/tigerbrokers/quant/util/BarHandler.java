package com.tigerbrokers.quant.util;

import com.tigerbrokers.quant.model.data.Bar;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/08/27
 */
@FunctionalInterface
public interface BarHandler {

  /**
   * on bar event
   * @param bar
   */
  void onBar(Bar bar);
}

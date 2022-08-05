package com.tquant.core.util;

import com.tquant.core.model.data.Bar;

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

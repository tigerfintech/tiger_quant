package com.tquant.algorithm.algos;

import com.tquant.core.core.AlgoTemplate;
import com.tquant.core.model.data.Order;
import com.tquant.core.model.data.Tick;
import com.tquant.core.model.data.Trade;
import com.tquant.core.model.enums.Direction;
import com.tquant.core.model.enums.OrderType;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/08/26
 */
public class DmaAlgo extends AlgoTemplate {

  private String orderId;
  private String direction;
  private double price;
  private int volume;

  public DmaAlgo() {
  }

  @Override
  public void onStart() {
    direction = (String) settings.get("direction");
    price = (Double) settings.get("price");
    volume = (Integer) settings.get("volume");
  }

  @Override
  public void onTick(Tick tick) {
    if (orderId == null) {
      if (Direction.BUY.name().equals(direction)) {
        orderId = buy(tick.getSymbol(), price, volume, OrderType.LMT);
      } else {
        orderId = sell(tick.getSymbol(), price, volume, OrderType.LMT);
      }
    }
  }

  @Override
  public void onOrder(Order order) {
    if (!order.isActive()) {
      stop();
    }
  }

  @Override
  public void onTrade(Trade trade) {

  }
}

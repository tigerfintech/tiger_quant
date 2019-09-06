package com.tigerbrokers.quant.algorithm.algos;

import com.tigerbrokers.quant.algorithm.AlgoTemplate;
import com.tigerbrokers.quant.model.data.Order;
import com.tigerbrokers.quant.model.data.Tick;
import com.tigerbrokers.quant.model.data.Trade;
import com.tigerbrokers.quant.model.enums.Direction;
import com.tigerbrokers.quant.model.enums.OrderType;

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

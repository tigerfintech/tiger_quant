package com.tigerbrokers.quant.algorithm.algos;

import com.tigerbrokers.quant.algorithm.AlgoTemplate;
import com.tigerbrokers.quant.model.data.Bar;
import com.tigerbrokers.quant.model.data.Order;
import com.tigerbrokers.quant.model.data.Tick;
import com.tigerbrokers.quant.model.data.Trade;
import com.tigerbrokers.quant.model.enums.BacktestingMode;
import com.tigerbrokers.quant.model.enums.Direction;
import com.tigerbrokers.quant.model.enums.OrderType;
import com.tigerbrokers.quant.util.BarGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Description: This strategy works if you subscribe to only one stock
 *
 * @author kevin
 * @date 2019/08/20
 */
public class BestLimitAlgo extends AlgoTemplate {

  private Tick lastTick;
  private String direction;
  private int volume;
  private String orderId;
  private double orderPrice;
  private int traded = 0;
  private String symbol;
  private BacktestingMode mode;
  private BarGenerator barGenerator;
  private BarGenerator min2BarGenerator;

  public BestLimitAlgo() {
  }

  public BestLimitAlgo(Map<String, Object> settings) {
    super(settings);
  }

  @Override
  public void init() {
    this.direction = (String) settings.get("direction");
    this.volume = (Integer) settings.get("volume");
    this.symbol = (String) settings.get("symbol");
    Object mode = settings.get("mode");
    if (mode != null) {
      this.mode = (BacktestingMode) mode;
    }
  }

  @Override
  public void onStart() {
    barGenerator = new BarGenerator(bar -> onBar(bar));
    List<String> symbols = new ArrayList<>();
    symbols.add("AAPL");
    min2BarGenerator = new BarGenerator(symbols, 2, bar -> on2minBar(bar));
    if (mode == null) {
      subscribe(getAlgoName(), symbol);
    }
  }

  @Override
  public void onTick(Tick tick) {
    this.lastTick = tick;
    if (this.direction.equalsIgnoreCase(Direction.BUY.name())) {
      if (orderId == null) {
        buyBestLimit();
      } else if (this.orderPrice != lastTick.getBidPrice()) {
        cancelAll();
      }
    } else {
      if (orderId == null) {
        sellBestLimit();
      } else if (this.orderPrice != lastTick.getAskPrice()) {
        cancelAll();
      }
    }
    barGenerator.updateTick(tick);
  }

  private void buyBestLimit() {
    int orderVolume = volume - traded;
    orderPrice = lastTick.getBidPrice();
    if (orderPrice > 0) {
      buy(symbol, orderPrice, orderVolume, OrderType.LMT);
    }
  }

  private void sellBestLimit() {
    int orderVolume = volume - traded;
    orderPrice = lastTick.getAskPrice();
    if (orderPrice > 0) {
      sell(symbol, orderPrice, orderVolume, OrderType.LMT);
    }
  }

  @Override
  public void onOrder(Order order) {
    if (!order.isActive()) {
      orderId = "";
      orderPrice = 0;
    }
  }

  @Override
  public void onTrade(Trade trade) {
    this.traded += trade.getVolume();
    if (traded >= trade.getVolume()) {
      log("{} onTrade traded:{},volume:{}", getAlgoName(), traded, trade.getVolume());
      stop();
    }
  }

  @Override
  public void onBar(Bar bar) {
    log("{} onBar {}", getAlgoName(), bar);
    min2BarGenerator.updateBar(bar);
  }

  public void on2minBar(Bar bar) {
    log("{} on2minBar {}", bar);
  }
}

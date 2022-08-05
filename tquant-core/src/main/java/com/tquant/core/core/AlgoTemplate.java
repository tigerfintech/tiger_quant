package com.tquant.core.core;

import com.tquant.core.core.AlgoEngine;
import com.tquant.core.model.data.Asset;
import com.tquant.core.model.data.Bar;
import com.tquant.core.model.data.Contract;
import com.tquant.core.model.data.Order;
import com.tquant.core.model.data.Position;
import com.tquant.core.model.data.Tick;
import com.tquant.core.model.data.Trade;
import com.tquant.core.model.enums.BacktestingMode;
import com.tquant.core.model.enums.BarType;
import com.tquant.core.model.enums.Direction;
import com.tquant.core.model.enums.OrderType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/08/19
 */
public abstract class AlgoTemplate {

  @Setter @Getter
  protected String algoName;
  @Setter @Getter
  protected AlgoEngine algoEngine;
  @Setter @Getter
  protected Map<String, Object> settings;
  @Setter @Getter
  protected BacktestingMode mode;

  private boolean active = false;
  private Map<Long, Order> activeOrders = new HashMap<>();
  private Map<String, Tick> ticks = new HashMap<>();

  public AlgoTemplate() {
    String strategyName = this.getClass().getSimpleName();
    setAlgoName(strategyName);
  }

  public AlgoTemplate(Map<String, Object> settings) {
    setAlgoName(this.getClass().getSimpleName());
    this.settings = settings;
  }

  public void init() {
    if (settings == null) {
      settings = algoEngine.getSettings(this.getClass().getSimpleName());
    }
    Object mode = settings.get("mode");
    if (mode != null) {
      this.mode = (BacktestingMode) mode;
    }
  }

  public void onBar(Bar bar) {
  }

  public void updateTick(Tick tick) {
    if (active) {
      Tick history = ticks.get(tick.getSymbol());
      if (history != null) {
        history.updateTick(tick);
        onTick(history);
      } else {
        ticks.put(tick.getSymbol(), tick);
        onTick(tick);
      }
    }
  }

  public void updateOrder(Order order) {
    if (active) {
      if (order.isActive()) {
        activeOrders.put(order.getId(), order);
      }
      onOrder(order);
    }
  }

  public void updateTrade(Trade trade) {
    if (active) {
      onTrade(trade);
    }
  }

  public void updateTimer() {
    if (active) {
      onTimer();
    }
  }

  public void onStart() {
  }

  public void onStop() {

  }

  public void onTimer() {

  }

  public abstract void onTick(Tick tick);

  public abstract void onOrder(Order order);

  public abstract void onTrade(Trade trade);

  public void start() {
    active = true;
    onStart();
  }

  public void stop() {
    active = false;
    cancelAll();
    onStop();
    log("algo stop");
  }

  public void subscribe(String template, String symbol) {
    if (mode == null) {
      algoEngine.subscribe(template, symbol);
    }
  }

  public void cancelSubscribe(String template, String symbol) {
    algoEngine.cancelSubscribe(template, symbol);
  }

  public String buy(String symbol, Double price, int volume, OrderType orderType) {
    log("buy {}|{}|{}", symbol, price, volume);
    return algoEngine.sendOrder(getAlgoName(), symbol, Direction.BUY, price, volume, orderType);
  }

  public String sell(String symbol, Double price, int volume, OrderType orderType) {
    log("sell {}|{}|{}", symbol, price, volume);
    return algoEngine.sendOrder(getAlgoName(), symbol, Direction.SELL, price, volume, orderType);
  }

  public void cancelOrder(Long orderId) {
    algoEngine.cancelOrder(orderId);
  }

  public void cancelAll() {
    if (activeOrders != null) {
      for (Long orderId : activeOrders.keySet()) {
        cancelOrder(orderId);
      }
    }
  }

  public List<Order> getAllActiveOrders() {
    return algoEngine.getAllActiveOrders();
  }

  public Tick getTick(String symbol) {
    return algoEngine.getTick(symbol);
  }

  public Contract getContract(String symbol) {
    return algoEngine.getContract(symbol);
  }

  public Asset getAsset() {
    return algoEngine.getAsset();
  }

  public Position getPosition(String positionId) {
    return algoEngine.getPosition(positionId);
  }

  public List<Position> getAllPositions() {
    return algoEngine.getAllPositions();
  }

  public List<Bar> getBars(String symbol, BarType barType, int limit) {
    return algoEngine.getBars(symbol, barType, limit);
  }

  public Map<String, List<Bar>> getBars(List<String> symbols, BarType barType, int limit) {
    return algoEngine.getBars(symbols, barType, limit);
  }

  public void log(String format, Object... args) {
    algoEngine.log(format, args);
  }
}

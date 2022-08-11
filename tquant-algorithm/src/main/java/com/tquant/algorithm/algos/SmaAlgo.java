package com.tquant.algorithm.algos;

import com.tquant.core.core.AlgoTemplate;
import com.tquant.core.indicators.Indicators;
import com.tquant.core.model.data.Bar;
import com.tquant.core.model.data.Order;
import com.tquant.core.model.data.Position;
import com.tquant.core.model.data.Tick;
import com.tquant.core.model.data.Trade;
import com.tquant.core.model.enums.BarType;
import com.tquant.core.model.enums.Direction;
import com.tquant.core.model.enums.OrderStatus;
import com.tquant.core.model.enums.OrderType;
import com.tquant.core.util.BarGenerator;
import com.tquant.storage.dao.BarDAO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description:
 *
 * @author kevin
 * @date 2022/08/09
 */
public class SmaAlgo extends AlgoTemplate {

  private BarGenerator barGenerator;
  private List<Bar> bars;
  private volatile int barSize;
  private int buyQuantity = 5;
  private Map<String, List<Order>> symbolFilledOrders = new HashMap<>();
  private static String symbol;
  private BarDAO barDAO = new BarDAO();
  private List<Bar> shortSma;
  private List<Bar> longSma;
  private Indicators indicators;

  public SmaAlgo() {
  }

  public SmaAlgo(Map<String, Object> settings) {
    super(settings);
  }

  @Override
  public void init() {
    if (settings == null) {
      settings = algoEngine.getSettings(this.getClass().getSimpleName());
    }
    symbol = (String) settings.get("symbol");
    barGenerator = new BarGenerator(bar -> onBar(bar));

    bars = barDAO.queryBar(symbol, BarType.day.getValue(), LocalDateTime.of(2018, 1, 1, 0, 0),
        LocalDateTime.of(2018, 1, 15, 0, 0));

    barSize = bars.size();
    indicators = new Indicators();
    shortSma = (List<Bar>)indicators.sma(bars, 5);
    longSma = (List<Bar>)indicators.sma(bars, 10);
  }

  @Override
  public void onStart() {

  }

  @Override
  public void onTick(Tick tick) {
  }

  @Override
  public void onOrder(Order order) {
    if (OrderStatus.Filled.name().equalsIgnoreCase(order.getStatus())) {
      List<Order> orders = symbolFilledOrders.get(order.getSymbol());
      if (orders == null) {
        orders = new ArrayList<>();
        symbolFilledOrders.put(order.getSymbol(), orders);
      }
      orders.add(order);
    }
  }

  @Override
  public void onTrade(Trade trade) {
  }

  @Override
  public void onBar(Bar bar) {
    log("{} onBar {}", getAlgoName(), bar);
    bars.add(bar);
    barSize++;
    shortSma = (List<Bar>)indicators.sma(bars, 5);
    longSma = (List<Bar>)indicators.sma(bars, 10);
    if (shortSma.get(barSize - 1).attr("close_ma5") > longSma.get(barSize - 1).attr("close_ma10")) {
      executeOrderCommand(bar.getSymbol(), bar.getClose(), Direction.BUY);
    } else {
      executeOrderCommand(bar.getSymbol(), bar.getClose(), Direction.SELL);
    }
  }

  private void executeOrderCommand(String symbol, double price, Direction direction) {
    log("Strategy {} entered, price {},position {}", getAlgoName(), price);
    String id = null;
    sendOrder(symbol, direction, round(price, 2),  buyQuantity, false);
    log("Entered {} on {} orderId:{},price:{},amount:{}", getAlgoName(), symbol, id, price, buyQuantity);
  }

  @Override
  public void sendOrder(String symbol, Direction direction, double price, int volume, boolean stop) {
    Position position = getPosition(symbol);
    if (direction == Direction.BUY) {
      buy(symbol, round(price, 2), buyQuantity, OrderType.LMT);
    } else {
      if (position == null || position.getPosition() < 0) {
        log("{} position is null {}", getAlgoName(), position);
        return;
      }
      sell(symbol, round(price, 2), position.getPosition(), OrderType.LMT);
    }
  }

  public static double round(double value, int scales) {
    if (scales < 0) {
      throw new IllegalArgumentException();
    }
    return BigDecimal.valueOf(value).setScale(scales, RoundingMode.HALF_UP).doubleValue();
  }
}

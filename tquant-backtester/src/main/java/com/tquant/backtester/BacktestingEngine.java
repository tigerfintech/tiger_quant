package com.tquant.backtester;

import com.alibaba.fastjson.JSONObject;
import com.tquant.core.core.AlgoTemplate;
import com.tquant.core.model.data.Bar;
import com.tquant.core.model.data.BaseData;
import com.tquant.core.model.data.Order;
import com.tquant.core.model.data.Tick;
import com.tquant.core.model.data.Trade;
import com.tquant.core.model.enums.BacktestingMode;
import com.tquant.core.model.enums.Direction;
import com.tquant.core.model.enums.OrderStatus;
import com.tquant.storage.dao.BarDAO;
import com.tquant.storage.dao.TickDAO;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * Description:
 *
 * @author kevin
 * @date 2022/08/01
 */
public class BacktestingEngine {

  private String symbol;
  private int interval;
  private String period;
  private LocalDateTime start;
  private LocalDateTime end;
  /** 佣金费率 **/
  private double rate = 0;
  /** 每单预计的滑点损失 **/
  private double slippage = 0;
  /** 每手股数 **/
  private int size = 1;
  /** 价格最小变动 **/
  private double priceTick = 0;
  private int capital = 0;
  private BacktestingMode mode;
  private double riskFree = 0;
  private int annualDays = 240;

  private AlgoTemplate strategyTemplate;

  private List<BaseData> historyData = new ArrayList<>();

  private static BarDAO barDAO = new BarDAO();
  private static TickDAO tickDAO = new TickDAO();

  private Bar bar;
  private Tick tick;
  private LocalDateTime datetime;

  private Map<String, Order> activeLimitOrders = new HashMap();
  private Map<String, Order> limitOrders = new HashMap();
  private long limitOrderCount = 0;
  private Map<String, Order> activeStopOrders = new HashMap<>();
  private Map<String, Order> stopOrders = new HashMap<>();
  private long stopOrderCount = 0;

  private long tradeCount = 0;
  private double tradePrice = 0;
  private int posChange = 0;

  private Map<LocalDate, DailyResult> dailyResults = new HashMap<>();
  private Map<String, Trade> trades = new HashMap<>();

  private static final String STOPORDER_PREFIX = "stop";

  public BacktestingEngine(String symbol, int interval, String period, LocalDateTime start, double rate,
      double slippage, int size,
      double priceTick, int capital, LocalDateTime end, BacktestingMode mode, double riskFree, int annualDays) {
    this.symbol = symbol;
    this.interval = interval;
    this.period = period;
    this.start = start;
    this.rate = rate;
    this.slippage = slippage;
    this.size = size;
    this.priceTick = priceTick;
    this.capital = capital;
    this.end = end;
    this.mode = mode;
    this.riskFree = riskFree;
    this.annualDays = annualDays;
  }

  void clearData() {
    historyData = null;
  }

  public void addStrategy(AlgoTemplate strategyTemplate) {
    this.strategyTemplate = strategyTemplate;
  }

  public void loadData() {
    System.out.println("start to load history data");
    if (end == null) {
      end = LocalDateTime.now();
    }
    if (!start.isBefore(end)) {
      throw new RuntimeException("the start date cannot be greater than the end date");
    }
    historyData.clear();
    long durationDays = Math.max(Duration.between(start, end).toDays()/10, 1);
    LocalDateTime deltaEnd = start.plusDays(durationDays);

    while (start.isBefore(end)) {
      if (BacktestingMode.BAR == mode) {
        List<Bar> bars = barDAO.queryBar(symbol, period, start, deltaEnd.isBefore(end) ? deltaEnd : end);
        historyData.addAll(bars);
      } else if (BacktestingMode.TICK == mode) {
        List<Tick> ticks = tickDAO.queryTicks(symbol, start, end);
        historyData.addAll(ticks);
      }
      LocalDateTime plusStart = start.plusDays(durationDays);
      start = plusStart.isAfter(end) ? end : plusStart;
      deltaEnd = start.plusDays(durationDays);
    }
    System.out.println("finish to load history data, mode:" + mode + ", count:" + historyData.size());
  }

  public void runBacktesting() {
    strategyTemplate.init();
    System.out.println("strategy initialed");

    strategyTemplate.onStart();
    System.out.println("strategy started");

    int totalSize = historyData.size();
    int batchSize = Math.max(totalSize / 10, 1);

    for (int i = 0; i < totalSize; i += batchSize) {
      List<BaseData> batchData = historyData.subList(i, i + batchSize >= totalSize ? totalSize : i + batchSize);
      for (BaseData data : batchData) {
        try {
          if (BacktestingMode.BAR == mode) {
            newBar((Bar) data);
          } else {
            newTick((Tick) data);
          }
        } catch (Exception e) {
          e.printStackTrace();
          System.err.println("exception:" + e.getMessage());
        }
      }
      System.out.println("current progress:" + i / totalSize + "%");
    }

    strategyTemplate.onStop();
    System.out.println("strategy stopped");
  }

  private void newBar(Bar bar) {
    this.bar = bar;
    this.datetime = bar.getTime();

    crossLimitOrder();
    crossStopOrder();
    this.strategyTemplate.onBar(bar);
    updateDailyClose(bar.getClose());
  }

  private void crossLimitOrder() {
    double longCrossPrice;
    double shortCrossPrice;
    double longBestPrice;
    double shortBestPrice;
    if (BacktestingMode.BAR == mode) {
      longCrossPrice = bar.getLow();
      shortCrossPrice = bar.getHigh();
      longBestPrice = bar.getOpen();
      shortBestPrice = bar.getOpen();
    } else {
      longCrossPrice = tick.getAskPrice();
      shortCrossPrice = tick.getBidPrice();
      longBestPrice = longCrossPrice;
      shortBestPrice = shortCrossPrice;
    }
    for (Order order : activeLimitOrders.values()) {
      if (order.getStatus().equalsIgnoreCase(OrderStatus.Initial.name())) {
        order.setStatus(OrderStatus.PendingSubmit.name());
        strategyTemplate.onOrder(order);
      }
      boolean longCross = order.getDirection().equalsIgnoreCase(Direction.BUY.name())
          && order.getPrice() >= longCrossPrice
          && longCrossPrice > 0;
      boolean shortCross = order.getDirection().equalsIgnoreCase(Direction.SELL.name())
          && order.getPrice() >= longCrossPrice
          && longCrossPrice > 0;

      if (!longCross && !shortCross) {
        continue;
      }
      order.setFilledVolume(order.getVolume());
      order.setStatus(OrderStatus.Filled.name());
      strategyTemplate.onOrder(order);

      if (activeLimitOrders.containsKey(order.getId())) {
        activeLimitOrders.remove(order.getId());
      }
      tradeCount += 1;
      if (longCross) {
        tradePrice = Math.min(order.getPrice(), longBestPrice);
        posChange = order.getVolume();
      } else {
        tradePrice = Math.max(order.getPrice(), longBestPrice);
        posChange = -order.getVolume();
      }
      Trade trade = new Trade();
      trade.setSymbol(order.getSymbol());
      trade.setOrderId(order.getId());
      trade.setId(tradeCount + "");
      trade.setDirection(order.getDirection());
      trade.setPrice(tradePrice);
      trade.setVolume(order.getVolume());
      trade.setTime(datetime);

      trades.put(trade.getId(), trade);
      strategyTemplate.onTrade(trade);
    }
  }

  private void crossStopOrder() {
    double longCrossPrice;
    double shortCrossPrice;
    double longBestPrice;
    double shortBestPrice;

    if (mode == BacktestingMode.BAR) {
      longCrossPrice = bar.getHigh();
      shortCrossPrice = bar.getLow();
      longBestPrice = bar.getOpen();
      shortBestPrice = bar.getOpen();
    } else {
      longCrossPrice = tick.getLatestPrice();
      shortCrossPrice = tick.getLatestPrice();
      longBestPrice = longCrossPrice;
      shortBestPrice = shortCrossPrice;
    }
    for (Order order : activeStopOrders.values()) {

      boolean longCross = order.getDirection().equalsIgnoreCase(Direction.BUY.name())
          && order.getPrice() <= longCrossPrice;

      boolean shortCross = order.getDirection().equalsIgnoreCase(Direction.SELL.name())
          && order.getPrice() >= shortCrossPrice;

      if (!longCross && !shortCross) {
        continue;
      }

      limitOrderCount += 1;

      Order orderData = new Order();
      orderData.setSymbol(order.getSymbol());
      orderData.setId(limitOrderCount);
      orderData.setDirection(order.getDirection());
      orderData.setPrice(tradePrice);
      orderData.setVolume(order.getVolume());
      orderData.setTime(datetime.toEpochSecond(ZoneOffset.UTC));
      order.setStatus(OrderStatus.Filled.name());
      limitOrders.put(orderData.getId() + "", orderData);

      if (longCross) {
        tradePrice = Math.max(order.getPrice(), longBestPrice);
        posChange = order.getVolume();
      } else {
        tradePrice = Math.min(order.getPrice(), longBestPrice);
        posChange = -order.getVolume();
      }
      tradeCount += 1;

      Trade trade = new Trade();
      trade.setSymbol(order.getSymbol());
      trade.setOrderId(order.getId());
      trade.setId(tradeCount + "");
      trade.setPrice(order.getPrice());
      trade.setDirection(order.getDirection());
      trade.setVolume(order.getVolume());
      trade.setTime(datetime);

      trades.put(trade.getId(), trade);
      strategyTemplate.onOrder(order);
      strategyTemplate.onTrade(trade);
    }
  }

  private void updateDailyClose(double price) {
    LocalDate date = datetime.toLocalDate();

    DailyResult dailyResult = dailyResults.get(date);
    if (dailyResult != null) {
      dailyResult.closePrice = price;
    } else {
      DailyResult newDailyResult = new DailyResult();
      newDailyResult.date = date;
      newDailyResult.closePrice = price;
      dailyResults.put(date, newDailyResult);
    }
  }

  void sendOrder(Direction direction, double price, double volume, boolean stop) {
    if (stop) {
      sendStopOrder(direction, price, volume);
    } else {
      sendLimitOrder(direction, price, volume);
    }
  }

  long sendStopOrder(Direction direction, double price, double volume) {
    stopOrderCount += 1;
    Order order = new Order();
    order.setSymbol(this.symbol);

    activeStopOrders.put(STOPORDER_PREFIX + order.getId(), order);
    stopOrders.put(STOPORDER_PREFIX + order.getId(), order);
    return order.getId();
  }

  long sendLimitOrder(Direction direction, double price, double volume) {
    limitOrderCount += 1;
    Order order = new Order();
    order.setSymbol(this.symbol);

    activeLimitOrders.put(order.getId() + "", order);
    limitOrders.put(order.getId() + "", order);
    return order.getId();
  }

  void cancelOrder(String orderId) {
    if (orderId.startsWith(STOPORDER_PREFIX)) {
      cancelStopOrder(orderId);
    } else {
      cancelLimitOrder(orderId);
    }
  }

  void cancelStopOrder(String orderId) {
    if (!activeStopOrders.containsKey(orderId)) {
      return;
    }
    Order stopOrder = activeStopOrders.remove(orderId);
    stopOrder.setStatus(OrderStatus.Cancelled.name());
    strategyTemplate.onOrder(stopOrder);
  }

  void cancelLimitOrder(String orderId) {
    if (!activeLimitOrders.containsKey(orderId)) {
      return;
    }
    Order order = activeLimitOrders.remove(orderId);
    order.setStatus(OrderStatus.Cancelled.name());
    strategyTemplate.onOrder(order);
  }

  void cancelAll() {
    for (Order order : activeLimitOrders.values()) {
      cancelLimitOrder(order.getId() + "");
    }
    for (Order order : activeStopOrders.values()) {
      cancelStopOrder(STOPORDER_PREFIX + order.getId());
    }
  }

  void newTick(Tick tick) {
    this.tick = tick;
  }

  public void calculateResult() {
    System.out.println("start to calculate result");
    if (trades.isEmpty()) {
      System.out.println("trade record is empty.");
      return;
    }
    for (Trade trade : trades.values()) {
      LocalDate date = trade.getTime().toLocalDate();
      DailyResult dailyResult = dailyResults.get(date);
      if (dailyResult == null) {
        dailyResult = new DailyResult();
        dailyResults.put(date, dailyResult);
      }
      dailyResult.addTrade(trade);
    }

    double preClose = 0;
    double startPos = 0;

    for (DailyResult dailyResult : dailyResults.values()) {
      dailyResult.calculatePnl(preClose, startPos, this.size, this.rate, this.slippage);
      preClose = dailyResult.closePrice;
      startPos = dailyResult.endPos;
    }

    System.out.println("dailyResults: "+JSONObject.toJSONString(dailyResults));
    System.out.println("finish to calculate result");
  }

  public void calculateStatistics() {

  }

  void showChart() {

  }

  public void addTrade(Trade trade) {
    trades.put(trade.getId(), trade);
  }

}

@Data
class DailyResult {

  LocalDate date;
  double closePrice;
  double preClose;
  int tradeCount = 0;
  List<Trade> trades = new ArrayList<>();
  double startPos = 0;
  double endPos = 0;
  double turnover = 0;
  double commission = 0;
  double slippage = 0;
  double tradingPnl = 0;
  double holdingPnl = 0;
  double totalPnl = 0;
  double netPnl = 0;

  void addTrade(Trade trade) {
    trades.add(trade);
  }

  void calculatePnl(double preClose, double startPos, int size, double rate, double slippage) {
    if (preClose > 0) {
      this.preClose = preClose;
    } else {
      preClose = 1;
    }
    this.startPos = startPos;
    this.endPos = startPos;
    this.holdingPnl = startPos * (closePrice - preClose) * size;
    this.tradeCount = trades.size();

    double posChange = 0;
    for (Trade trade : trades) {
      if (trade.getDirection().equalsIgnoreCase(Direction.BUY.name())) {
        posChange = trade.getVolume();
      }
      this.endPos += posChange;

      this.turnover = trade.getVolume() * size * trade.getPrice();
      this.tradingPnl += posChange * (closePrice - trade.getPrice()) * size;
      this.slippage += trade.getVolume() * size * slippage;
      this.turnover += turnover;
      this.commission += turnover * rate;
    }
    this.totalPnl = tradingPnl + holdingPnl;
    this.netPnl = totalPnl - commission - slippage;
  }

}

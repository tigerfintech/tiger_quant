package com.tquant.algorithm.algos;

import com.tquant.core.TigerQuantException;
import com.tquant.core.core.AlgoTemplate;
import com.tquant.core.model.data.Bar;
import com.tquant.core.model.data.Order;
import com.tquant.core.model.data.Position;
import com.tquant.core.model.data.Tick;
import com.tquant.core.model.data.Trade;
import com.tquant.core.model.enums.BarType;
import com.tquant.core.model.enums.OrderType;
import com.tquant.storage.dao.BarDAO;
import com.tquant.core.util.BarGenerator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/08/26
 */
public class MacdAlgo extends AlgoTemplate {

  private BarGenerator barGenerator;
  private BarGenerator xminBarGenerator;
  private Map<String, TimeSeries> symbolSeries = new HashMap<>();
  private Map<String, Strategy> symbolStrategy = new HashMap<>();
  private Map<String, TradingRecord> symbolTradingRecord = new HashMap<>();
  private Map<String, List<Order>> symbolFilledOrders = new HashMap<>();
  private Map<String, List<Order>> symbolOpenOrders = new HashMap<>();

  private static final List<String> symbols = new ArrayList<>();
  private static int barSize = 0;
  private static final int FILLED_ORDER_QUANTITY_THRESHOLD = 50;
  private static final double PRICE_TICK_THRESHOLD = 0.05;
  private static final long TIME_RANGE_THRESHOLD = 10000L;
  private double lastSellPrice = 0;
  private double lastTickPrice = 0;
  private long lastOpenOrderTime = 0;
  private long lastSellOrderTime = 0;

  BarDAO barDAO = new BarDAO();

  public MacdAlgo() {
    //init();
  }

  public MacdAlgo(Map<String, Object> settings) {
    super(settings);
    //init();
  }

  @Override
  public void init() {
    if (settings == null) {
      settings = algoEngine.getSettings(this.getClass().getSimpleName());
    }
    barSize = (Integer) settings.get("bars");
    symbols.add((String) settings.get("symbol"));
    barGenerator = new BarGenerator(bar -> onBar(bar));
    xminBarGenerator = new BarGenerator(symbols, 5, bar -> on5minBar(bar));
  }

  @Override
  public void onStart() {

    for (String symbol : symbols) {
      TimeSeries series = new BaseTimeSeries.SeriesBuilder().withName("MacdAlgo").build();
      series.setMaximumBarCount(400);
      symbolSeries.put(symbol, series);
      TradingRecord tradingRecord = new BaseTradingRecord();
      symbolTradingRecord.put(symbol, tradingRecord);
      Strategy strategy = newMacdStrategy(series);
      symbolStrategy.put(symbol, strategy);
    }

    List<Bar> bars =
        barDAO.queryBar(symbols.get(0), BarType.min1.getValue(), LocalDateTime.of(2022, 8, 1, 0, 0),
            LocalDateTime.of(2022, 8, 2, 0, 0));
    if (bars == null) {
      throw new TigerQuantException("get bars is null,symbols:" + symbols);
    }
    int i=0;
    for (Bar bar : bars) {
      symbolSeries.get(symbols.get(0)).addBar(bar.toBaseBar());
      if (i++ == 100) {
        break;
      }
    }

  }

  @Override
  public void onTick(Tick tick) {
    barGenerator.updateTick(tick);
    symbolSeries.get(tick.getSymbol()).addPrice(tick.getLatestPrice());
  }

  @Override
  public void onOrder(Order order) {
    if ("Filled".equalsIgnoreCase(order.getStatus())) {
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
    symbolSeries.get(trade.getSymbol()).addPrice(DoubleNum.valueOf(trade.getPrice()));
  }

  @Override
  public void onBar(Bar bar) {
    log("{} onBar {}", getAlgoName(), bar);
    addToSerie(bar);
    xminBarGenerator.updateBar(bar);
    makeTradingChoice(bar.getSymbol(),bar.getClose());
  }

  public void on5minBar(Bar bar) {
    log("{} on5minBar {}", getAlgoName(), bar);
  }

  private void addToSerie(Bar bar) {
    TimeSeries timeSeries = symbolSeries.get(bar.getSymbol());
    BaseBar baseBar = bar.toBaseBar();
    if (baseBar.getEndTime().isAfter(timeSeries.getBar(timeSeries.getEndIndex()).getEndTime())) {
      timeSeries.addBar(baseBar);
    }
  }


  public void makeTradingChoice(String symbol,double price) {
    TimeSeries series = symbolSeries.get(symbol);
    Strategy strategy = symbolStrategy.get(symbol);
    TradingRecord tradingRecord = symbolTradingRecord.get(symbol);

    if (strategy.shouldEnter(series.getEndIndex(), tradingRecord)) {
      buyStrategy(symbol,price);
    } else if (strategy.shouldExit(series.getEndIndex(), tradingRecord)) {
      sellStrategy(symbol,price);
    }
  }

  private void buyStrategy(String symbol,double price) {
    if (price == lastTickPrice) {
      return;
    }
    lastTickPrice = price;
    log("Strategy {} entered, price {}", getAlgoName(), lastTickPrice);

    if (meetNonOpeningConditions(symbol,price)) {
      return;
    }

    Position position = getPosition(symbol);
    log("{} position {}", getAlgoName(), position);


    int volume = 5;
    String id = buy(symbol, round(price - 0.01, 2), volume, OrderType.LMT);
    lastOpenOrderTime = System.currentTimeMillis();
    log("Entered {} on {} orderId:{},price:{},amount:{}", getAlgoName(), symbol, id, price, volume);
  }

  private void sellStrategy(String symbol,double price) {
    if (price == lastTickPrice) {
      return;
    }
    lastTickPrice = price;
    log("Strategy {} exited,price {}", getAlgoName(), lastTickPrice);

    if (!shouldSold(price)) {
      return;
    }

    Position position = getPosition(symbol);
    if (position == null || position.getPosition() < 0) {
      return;
    }
    log("{} position {}", getAlgoName(), position);

    String id = sell(symbol, round(price + 0.01, 2), position.getPosition(), OrderType.LMT);
    lastSellPrice = price;
    lastSellOrderTime = System.currentTimeMillis();
    log("Exited {} on {} orderId:{},price:{},amount:{}", getAlgoName(), symbol, id, price,
        position.getPosition());
  }

  private Strategy newMacdStrategy(TimeSeries series) {
    ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
    RSIIndicator rsi = new RSIIndicator(closePrice, 250);
    MACDIndicator macd = new MACDIndicator(rsi, 12, 26);
    EMAIndicator emaMacd = new EMAIndicator(macd, 9);

    Rule entryRule = new CrossedUpIndicatorRule(macd, emaMacd);
    Rule exitRule = new CrossedDownIndicatorRule(macd, emaMacd);
    return new BaseStrategy(entryRule, exitRule);
  }

  private boolean meetNonOpeningConditions(String symbol,double price) {
    double avgPrice = 0;
    double totalPrice = 0;
    int totalVolume = 0;
    if (System.currentTimeMillis() - lastOpenOrderTime <= TIME_RANGE_THRESHOLD) {
      log("meet openTime threshold: {}", TIME_RANGE_THRESHOLD);
      return true;
    }
    List<Order> orders = symbolFilledOrders.get(symbol);
    if (orders != null) {
      for (Order order : orders) {
        if ("BUY".equalsIgnoreCase(order.getDirection()) && order.getVolume() > 0) {
          totalVolume += order.getVolume();
          totalPrice = order.getVolume() * order.getAverageFilledPrice();
        }
      }
      if (totalVolume >= FILLED_ORDER_QUANTITY_THRESHOLD) {
        log("volume meet threshold: {}", FILLED_ORDER_QUANTITY_THRESHOLD);
        return true;
      }
    }
    if (totalVolume != 0) {
      avgPrice = totalPrice / totalVolume;
    }
    if (meetPriceThresholdCondition(price, avgPrice)) {
      log("avgPrice meet threshold: {},latestPrice: {}", avgPrice, price);
      return true;
    }

    return false;
  }

  private boolean shouldSold(double price) {
    if (System.currentTimeMillis() - lastSellOrderTime <= TIME_RANGE_THRESHOLD) {
      return false;
    }
    if (Math.abs(price - lastSellPrice) >= PRICE_TICK_THRESHOLD) {
      return true;
    }
    return false;
  }

  private boolean meetPriceThresholdCondition(double latestPrice, double avgPrice) {
    if (avgPrice == 0) {
      return false;
    }
    if (latestPrice < avgPrice && latestPrice + PRICE_TICK_THRESHOLD > avgPrice) {
      return true;
    }
    if (latestPrice > avgPrice && latestPrice - PRICE_TICK_THRESHOLD < avgPrice) {
      return true;
    }

    return false;
  }

  public static double round(double value, int places) {
    if (places < 0) {
      throw new IllegalArgumentException();
    }

    BigDecimal bd = BigDecimal.valueOf(value);
    bd = bd.setScale(places, RoundingMode.HALF_UP);
    return bd.doubleValue();
  }
}

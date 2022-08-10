package com.tquant.algorithm.algos;

import com.tquant.core.TigerQuantException;
import com.tquant.core.core.AlgoTemplate;
import com.tquant.core.model.data.Bar;
import com.tquant.core.model.data.Order;
import com.tquant.core.model.data.Position;
import com.tquant.core.model.data.Tick;
import com.tquant.core.model.data.Trade;
import com.tquant.core.model.enums.BarType;
import com.tquant.core.model.enums.Direction;
import com.tquant.core.model.enums.OrderStatus;
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
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/08/26
 */
public class MacdAlgo extends AlgoTemplate {

  private BarGenerator barGenerator;
  private BarSeries barSeries = new BaseBarSeries();;
  private int barSize;
  private int buyQuantity = 5;
  private Map<String, Strategy> symbolStrategy = new HashMap<>();
  private Map<String, TradingRecord> symbolTradingRecord = new HashMap<>();
  private Map<String, List<Order>> symbolFilledOrders = new HashMap<>();
  private static String symbol;
  private BarDAO barDAO = new BarDAO();
  private static final String STRATEGY_NAME = "MACD Algorithm";

  public MacdAlgo() {
  }

  public MacdAlgo(Map<String, Object> settings) {
    super(settings);
  }

  @Override
  public void init() {
    if (settings == null) {
      settings = algoEngine.getSettings(this.getClass().getSimpleName());
    }
    barSize = (Integer) settings.get("bars");
    symbol = (String) settings.get("symbol");
    barGenerator = new BarGenerator(bar -> onBar(bar));

    BarSeries series = new BaseBarSeriesBuilder().withName(STRATEGY_NAME).build();
    series.setMaximumBarCount(10);
    TradingRecord tradingRecord = new BaseTradingRecord();
    symbolTradingRecord.put(symbol, tradingRecord);
    Strategy strategy = createStrategy(series);
    symbolStrategy.put(symbol, strategy);
  }

  @Override
  public void onStart() {
    List<Bar> bars = loadBars();
    for (int i = 0; i <= barSize; i++) {
      barSeries.addBar(bars.get(i).toBaseBar());
    }
  }

  protected List<Bar> loadBars() {
    List<Bar> bars = barDAO.queryBar(symbol, BarType.min1.getValue(), LocalDateTime.of(2022, 8, 1, 0, 0),
        LocalDateTime.of(2022, 8, 2, 0, 0));
    if (bars == null) {
      throw new TigerQuantException("get bars is null,symbol: " + symbol);
    }
    return bars;
  }

  @Override
  public void onTick(Tick tick) {
    barGenerator.updateTick(tick);
    barSeries.addPrice(tick.getLatestPrice());
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

    BaseBar baseBar = bar.toBaseBar();
    if (baseBar.getEndTime().isAfter(barSeries.getBar(barSeries.getEndIndex()).getEndTime())) {
      barSeries.addBar(baseBar);
    }
    executeStrategy(bar.getSymbol(), bar.getClose());
  }

  public void executeStrategy(String symbol, double price) {
    if (price < 120) {
      executeCommand(symbol, price, Direction.BUY);
    } else if(price>150){
      executeCommand(symbol, price, Direction.SELL);
    }
  }

  private void executeCommand(String symbol, double price, Direction direction) {
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

  private Strategy createStrategy(BarSeries series) {
    ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
    RSIIndicator rsi = new RSIIndicator(closePrice, 250);
    MACDIndicator macd = new MACDIndicator(rsi, 12, 26);
    EMAIndicator emaMacd = new EMAIndicator(macd, 9);

    return new BaseStrategy(new CrossedUpIndicatorRule(macd, emaMacd), new CrossedDownIndicatorRule(macd, emaMacd));
  }

  public static double round(double value, int scales) {
    if (scales < 0) {
      throw new IllegalArgumentException();
    }
    return BigDecimal.valueOf(value).setScale(scales, RoundingMode.HALF_UP).doubleValue();
  }
}

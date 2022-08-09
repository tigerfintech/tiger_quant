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
import com.tquant.core.util.BarGenerator;
import com.tquant.storage.dao.BarDAO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ta4j.core.AnalysisCriterion;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BarSeriesManager;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.criteria.pnl.GrossReturnCriterion;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

/**
 * Description:
 *
 * @author kevin
 * @date 2022/08/09
 */
public class SmaAlgo extends AlgoTemplate {

  private BarGenerator barGenerator;
  private BarSeries barSeries = new BaseBarSeries();;
  private int barSize;
  private int buyQuantity = 5;
  private Map<String, Strategy> symbolStrategy = new HashMap<>();
  private Map<String, TradingRecord> symbolTradingRecord = new HashMap<>();
  private Map<String, List<Order>> symbolFilledOrders = new HashMap<>();
  private static String symbol;
  private BarDAO barDAO = new BarDAO();
  private static final String STRATEGY_NAME = "SMA Algorithm";

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
    BarSeries series = createBarSeries();
    Strategy strategy3DaySma = create3DaySmaStrategy(series);

    BarSeriesManager seriesManager = new BarSeriesManager(series);
    TradingRecord tradingRecord3DaySma = seriesManager.run(strategy3DaySma, org.ta4j.core.Trade.TradeType.BUY,
        DecimalNum.valueOf(50));
    System.out.println(tradingRecord3DaySma);

    Strategy strategy2DaySma = create2DaySmaStrategy(series);
    TradingRecord tradingRecord2DaySma = seriesManager.run(strategy2DaySma, org.ta4j.core.Trade.TradeType.BUY,
        DecimalNum.valueOf(50));
    System.out.println(tradingRecord2DaySma);

    AnalysisCriterion criterion = new GrossReturnCriterion();
    Num calculate3DaySma = criterion.calculate(series, tradingRecord3DaySma);
    Num calculate2DaySma = criterion.calculate(series, tradingRecord2DaySma);

    System.out.println(calculate3DaySma);
    System.out.println(calculate2DaySma);
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
    barSeries.addPrice(DoubleNum.valueOf(trade.getPrice()));
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
    Strategy strategy = symbolStrategy.get(symbol);
    TradingRecord tradingRecord = symbolTradingRecord.get(symbol);
    if (strategy.shouldEnter(barSeries.getEndIndex(), tradingRecord)) {
      executeCommand(symbol, price, Direction.BUY);
    } else if (strategy.shouldExit(barSeries.getEndIndex(), tradingRecord)) {
      executeCommand(symbol, price, Direction.SELL);
    }
  }

  private void executeCommand(String symbol, double price, Direction direction) {
    Position position = getPosition(symbol);
    log("Strategy {} entered, price {},position {}", getAlgoName(), price, position);
    String id;
    if (direction == Direction.BUY) {
      id = buy(symbol, round(price, 2), buyQuantity, OrderType.LMT);
    } else {
      if (position == null || position.getPosition() < 0) {
        log("{} position is null {}", getAlgoName(), position);
        return;
      }
      id = sell(symbol, round(price, 2), position.getPosition(), OrderType.LMT);
    }
    log("Entered {} on {} orderId:{},price:{},amount:{}", getAlgoName(), symbol, id, price, buyQuantity);
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


  private static BarSeries createBarSeries() {
    BarSeries series = new BaseBarSeries();
    series.addBar(createBar(createDay(1), 100.0, 100.0, 100.0, 100.0, 1060));
    series.addBar(createBar(createDay(2), 110.0, 110.0, 110.0, 110.0, 1070));
    series.addBar(createBar(createDay(3), 140.0, 140.0, 140.0, 140.0, 1080));
    series.addBar(createBar(createDay(4), 119.0, 119.0, 119.0, 119.0, 1090));
    series.addBar(createBar(createDay(5), 100.0, 100.0, 100.0, 100.0, 1100));
    series.addBar(createBar(createDay(6), 110.0, 110.0, 110.0, 110.0, 1110));
    series.addBar(createBar(createDay(7), 120.0, 120.0, 120.0, 120.0, 1120));
    series.addBar(createBar(createDay(8), 130.0, 130.0, 130.0, 130.0, 1130));
    return series;
  }

  private static BaseBar createBar(ZonedDateTime endTime, Number openPrice, Number highPrice, Number lowPrice,
      Number closePrice, Number volume) {
    return BaseBar.builder(DecimalNum::valueOf, Number.class).timePeriod(Duration.ofDays(1)).endTime(endTime)
        .openPrice(openPrice).highPrice(highPrice).lowPrice(lowPrice).closePrice(closePrice).volume(volume)
        .build();
  }


  private static ZonedDateTime createDay(int day) {
    return ZonedDateTime.of(2018, 01, day, 12, 0, 0, 0, ZoneId.systemDefault());
  }

  private static Strategy create3DaySmaStrategy(BarSeries series) {
    ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
    SMAIndicator sma = new SMAIndicator(closePrice, 3);
    return new BaseStrategy(new UnderIndicatorRule(sma, closePrice), new OverIndicatorRule(sma, closePrice));
  }

  private static Strategy create2DaySmaStrategy(BarSeries series) {
    ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
    SMAIndicator sma = new SMAIndicator(closePrice, 2);
    return new BaseStrategy(new UnderIndicatorRule(sma, closePrice), new OverIndicatorRule(sma, closePrice));
  }


}

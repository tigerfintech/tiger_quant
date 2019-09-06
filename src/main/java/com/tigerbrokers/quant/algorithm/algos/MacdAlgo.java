package com.tigerbrokers.quant.algorithm.algos;

import com.tigerbrokers.quant.algorithm.AlgoTemplate;
import com.tigerbrokers.quant.model.data.Asset;
import com.tigerbrokers.quant.model.data.Bar;
import com.tigerbrokers.quant.model.data.Order;
import com.tigerbrokers.quant.model.data.Position;
import com.tigerbrokers.quant.model.data.Tick;
import com.tigerbrokers.quant.model.data.Trade;
import com.tigerbrokers.quant.model.enums.BarType;
import com.tigerbrokers.quant.model.enums.OrderType;
import com.tigerbrokers.quant.util.BarGenerator;
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

  private static final List<String> symbols = new ArrayList<>();

  static {
    symbols.add("CN1909");
  }

  public MacdAlgo() {
    init();
  }

  private void init() {
    barGenerator = new BarGenerator(bar -> onBar(bar));
    xminBarGenerator = new BarGenerator(symbols, 5, bar -> on5minBar(bar));
  }

  @Override
  public void onStart() {
    Integer barSize = (Integer) settings.get("bars");

    for (String symbol : symbols) {
      TimeSeries series = new BaseTimeSeries.SeriesBuilder().withName("MacdSeries").build();
      series.setMaximumBarCount(400);
      symbolSeries.put(symbol, series);
      TradingRecord tradingRecord = new BaseTradingRecord();
      symbolTradingRecord.put(symbol, tradingRecord);
      Strategy strategy = newMacdStrategy(series);
      symbolStrategy.put(symbol, strategy);
    }

    Map<String, List<Bar>> bars = getBars(symbols, BarType.min1, barSize);
    for (String symbol : bars.keySet()) {
      List<Bar> barList = bars.get(symbol);
      for (Bar bar : barList) {
        symbolSeries.get(symbol).addBar(bar.toBaseBar());
      }
    }
    for (String symbol : symbols) {
      subscribe(getAlgoName(), symbol);
    }
    Asset asset = getAsset();
    log("initial asset:{}", asset);
    List<Position> positions = getAllPositions();
    log("initial positions:{}", positions);
  }

  @Override
  public void onTick(Tick tick) {
    barGenerator.updateTick(tick);
  }

  @Override
  public void onOrder(Order order) {

  }

  @Override
  public void onTrade(Trade trade) {
    symbolSeries.get(trade.getSymbol()).addPrice(DoubleNum.valueOf(trade.getPrice()));
  }

  public void onBar(Bar bar) {
    log("{} onBar {}", getAlgoName(), bar);
    addToSerie(bar);
    makeTradingChoice(bar);
    xminBarGenerator.updateBar(bar);
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

  public void makeTradingChoice(Bar bar) {
    TimeSeries series = symbolSeries.get(bar.getSymbol());
    Strategy strategy = symbolStrategy.get(bar.getSymbol());
    TradingRecord tradingRecord = symbolTradingRecord.get(bar.getSymbol());

    int endIndex = series.getEndIndex();
    if (strategy.shouldEnter(endIndex, tradingRecord)) {
      log("Strategy {} entered", getAlgoName());
      double price = bar.getLow();
      int volume = 1;
      String id = buy(bar.getSymbol(), bar.getLow(), volume, OrderType.LMT);
      log("Entered {} on {} orderId:{},price:{},amount:{}", getAlgoName(), bar.getSymbol(), id, price, volume);
    }
    if (strategy.shouldExit(endIndex, tradingRecord)) {
      log("Strategy {} exit", getAlgoName());
      double price = bar.getHigh();
      int volume = 1;
      String id = sell(bar.getSymbol(), price, volume, OrderType.LMT);
      log("Exited {} on {} orderId:{},price:{},amount:{}", getAlgoName(), bar.getSymbol(), id, price, volume);
    }
  }

  private Strategy newMacdStrategy(TimeSeries series) {
    ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
    MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
    EMAIndicator emaMacd = new EMAIndicator(macd, 9);
    Rule entryRule = new CrossedUpIndicatorRule(macd, emaMacd);
    Rule exitRule = new CrossedDownIndicatorRule(macd, emaMacd);
    return new BaseStrategy(entryRule, exitRule);
  }
}

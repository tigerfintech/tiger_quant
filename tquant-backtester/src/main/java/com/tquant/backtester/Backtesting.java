package com.tquant.backtester;

import com.tquant.core.core.AlgoEngine;
import com.tquant.core.core.AlgoTemplate;
import com.tquant.algorithm.algos.MacdAlgo;
import com.tquant.core.core.MainEngine;
import com.tquant.core.event.EventEngine;
import com.tquant.gateway.tiger.TigerGateway;
import com.tquant.core.model.data.Trade;
import com.tquant.core.model.enums.BacktestingMode;
import com.tquant.core.model.enums.OrderType;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 *
 * @author kevin
 * @date 2022/08/02
 */
public class Backtesting {

  public static void main(String[] args) {
    Backtesting backtesting = new Backtesting();
    backtesting.testCalculateResult();
    //backtesting.testBacktesting();
  }

  private BacktestingEngine initEngine() {
    LocalDateTime start = LocalDateTime.of(2021, 1, 1, 0, 0, 0);
    LocalDateTime end = LocalDateTime.of(2021, 12, 31, 23, 59, 59);
    return new BacktestingEngine("AAPL", 60, "day", start, 3 / 10000,
        0.2, 1, 0.1, 1000000, end, BacktestingMode.BAR, 0.1, 240);
  }

  private Map<String, Object> initSettings() {
    Map<String, Object> settings = new HashMap<>();
    settings.put("symbol", "AAPL");
    settings.put("bars", 10);
    settings.put("mode", BacktestingMode.BAR);
    return settings;
  }

  private void testBacktesting() {
    BacktestingEngine backtestingEngine = initEngine();
    Map<String, Object> settings = initSettings();

    AlgoTemplate algoTemplate = new MacdAlgo(settings);
    EventEngine eventEngine = new EventEngine();
    MainEngine mainEngine = new MainEngine(eventEngine);
    mainEngine.addGateway(new TigerGateway(eventEngine));
    AlgoEngine algoEngine = new AlgoEngine(mainEngine, eventEngine);
    algoTemplate.setAlgoEngine(algoEngine);

    backtestingEngine.addStrategy(algoTemplate);
    backtestingEngine.loadData();
    backtestingEngine.runBacktesting();
    backtestingEngine.calculateResult();
    backtestingEngine.calculateStatistics();
  }

  private void testCalculateResult() {
    BacktestingEngine backtestingEngine = initEngine();
    for (int i = 0; i < 10; i++) {
      Trade trade = new Trade();
      trade.setId(i + "");
      trade.setTime(LocalDateTime.now());
      trade.setVolume(100);
      trade.setDirection(i % 2 == 0 ? "BUY" : "SELL");
      trade.setPrice(10+i);
      trade.setOrderId(1000L+i);
      trade.setSymbol("AAPL");
      trade.setOrderType(OrderType.LMT.getType());
      backtestingEngine.addTrade(trade);
    }
    backtestingEngine.calculateResult();
    backtestingEngine.calculateStatistics();
  }
}

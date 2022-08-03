package com.tigerbrokers.quant.backtest;

import com.tigerbrokers.quant.algorithm.algos.BestLimitAlgo;
import com.tigerbrokers.quant.model.enums.BacktestingMode;
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
    LocalDateTime start = LocalDateTime.of(2021, 1, 1, 0, 0, 0);
    LocalDateTime end = LocalDateTime.of(2021, 12, 31, 23, 59, 59);

    BacktestingEngine backtestingEngine = new BacktestingEngine("AAPL", 60, "day", start, 0.3 / 10000,
        0.2, 300, 0.1, 1000000, end, BacktestingMode.BAR, 0, 0);

    Map<String, Object> settings = new HashMap<>();
    settings.put("direction", "BUY");
    settings.put("volume", 10);
    settings.put("symbol", "AAPL");
    settings.put("mode", BacktestingMode.BAR);
    backtestingEngine.addStrategy(new BestLimitAlgo(settings));
    backtestingEngine.loadData();
    backtestingEngine.runBacktesting();
    backtestingEngine.calculateResult();
    backtestingEngine.calculateStatistics();
  }
}

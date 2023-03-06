package com.tquant.backtester;

import com.tquant.algorithm.algos.SmaAlgo;
import com.tquant.bootstrap.command.CliRunner;
import com.tquant.bootstrap.command.CommandExecuteTemplate;
import com.tquant.core.core.AlgoEngine;
import com.tquant.core.core.MainEngine;
import com.tquant.core.event.EventEngine;
import com.tquant.core.model.enums.Direction;
import com.tquant.gateway.tiger.TigerGateway;
import com.tquant.core.model.data.Trade;
import com.tquant.core.model.enums.BacktestingMode;
import com.tquant.core.model.enums.OrderType;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import static com.tquant.core.util.QuantConstants.ALGO_CONFIG_PATH;
import static com.tquant.core.util.QuantConstants.ALGO_CONFIG_PATH_PROP;
import static com.tquant.core.util.QuantConstants.GATEWAY_CONFIG_PATH;
import static com.tquant.core.util.QuantConstants.TIGER_CONFIG_PATH_PROP;

/**
 * Description:
 *
 * @author kevin
 * @date 2022/08/02
 */
public class Backtesting implements CliRunner {

  public static void main(String[] args) {
    CommandExecuteTemplate.execute(args, "-[a][g]", new Backtesting());
  }

  private void initProperties(String algoConfigPath, String gatewayConfigPath) {
    System.setProperty(ALGO_CONFIG_PATH_PROP, algoConfigPath);
    System.setProperty(TIGER_CONFIG_PATH_PROP, gatewayConfigPath);
  }

  private BacktestingEngine initEngine() {
    LocalDateTime start = LocalDateTime.of(2021, 1, 1, 0, 0, 0);
    LocalDateTime end = LocalDateTime.of(2021, 12, 31, 23, 59, 59);
    return new BacktestingEngine("AAPL", 60, "day", start, 3D / 10000,
        0.2, 1, 0.1, 1000000, end, BacktestingMode.BAR, 0.1, 240, 10);
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

    BacktestingAlgo algoTemplate = new BacktestingAlgo(settings);
    EventEngine eventEngine = new EventEngine();
    MainEngine mainEngine = new MainEngine(eventEngine);
    mainEngine.addGateway(new TigerGateway(eventEngine));
    AlgoEngine algoEngine = new AlgoEngine(mainEngine, eventEngine);
    algoTemplate.setAlgoEngine(algoEngine);
    algoTemplate.setBacktestingEngine(backtestingEngine);

    backtestingEngine.addStrategy(algoTemplate);
    backtestingEngine.loadData();
    backtestingEngine.runBacktesting();
    backtestingEngine.calculateResult();
    backtestingEngine.calculateStatistics();
  }

  private void testCalculateResult() {
    BacktestingEngine backtestingEngine = initEngine();
    LocalDateTime start = backtestingEngine.getStart();
    for (int i = 0; i < 10; i++) {
      Trade trade = new Trade();
      trade.setId(i + "");
      trade.setTime(start);
      trade.setVolume(100);
      trade.setDirection(i % 2 == 0 ? "BUY" : "SELL");
      trade.setPrice(10+i);
      trade.setOrderId(1000L+i);
      trade.setSymbol("AAPL");
      trade.setOrderType(OrderType.LMT.getType());
      backtestingEngine.addTrade(trade);
      start = start.plusDays(1);
    }
    backtestingEngine.calculateResult();
    backtestingEngine.calculateStatistics();
  }

  @Override
  public Options initOptions() {
    Options options = new Options();
    options.addOption(ALGO_CONFIG_PATH, true, "Algorithm strategy configuration path, cannot be empty");
    options.addOption(GATEWAY_CONFIG_PATH, true, "Gateway config path, cannot be empty");
    return options;
  }

  @Override
  public boolean validateOptions(CommandLine cmdLine) {
    return (cmdLine.hasOption(ALGO_CONFIG_PATH) && cmdLine.hasOption(GATEWAY_CONFIG_PATH));
  }

  @Override
  public void start(CommandLine cmdLine) {
    String algoConfigPath = cmdLine.getOptionValue(ALGO_CONFIG_PATH);
    String gatewayConfigPath = cmdLine.getOptionValue(GATEWAY_CONFIG_PATH);
    initProperties(algoConfigPath, gatewayConfigPath);
    startService();
  }

  private void startService() {
    Backtesting backtesting = new Backtesting();
    //backtesting.testCalculateResult();
    backtesting.testBacktesting();
  }
}

class BacktestingAlgo extends SmaAlgo {
  private BacktestingEngine backtestingEngine;

  public BacktestingAlgo(Map<String, Object> settings) {
    super(settings);
  }

  public void setBacktestingEngine(BacktestingEngine backtestingEngine) {
    this.backtestingEngine = backtestingEngine;
  }

  @Override
  public void sendOrder(String symbol, Direction direction, double price, int volume, boolean stop) {
    backtestingEngine.sendOrder(direction, price, volume, stop);
  }
}

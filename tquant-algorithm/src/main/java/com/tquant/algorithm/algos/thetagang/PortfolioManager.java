package com.tquant.algorithm.algos.thetagang;

import com.google.common.collect.Lists;
import com.tigerbrokers.stock.openapi.client.https.domain.option.item.OptionBriefItem;
import com.tigerbrokers.stock.openapi.client.https.domain.option.item.OptionRealTimeQuoteGroup;
import com.tigerbrokers.stock.openapi.client.struct.enums.Right;
import com.tquant.algorithm.algos.utils.Utils;
import com.tquant.core.event.EventEngine;
import com.tquant.core.model.data.Asset;
import com.tquant.core.model.data.Contract;
import com.tquant.core.model.data.Order;
import com.tquant.core.model.data.Position;
import com.tquant.core.model.data.RealtimeQuote;
import com.tquant.core.model.data.Tick;
import com.tquant.core.model.enums.Direction;
import com.tquant.core.model.enums.OrderStatus;
import com.tquant.core.model.enums.SecType;
import com.tquant.core.model.request.OptionChainFilter;
import com.tquant.gateway.api.OptionApi;
import com.tquant.gateway.api.QuoteApi;
import com.tquant.gateway.api.TradeApi;
import com.tquant.gateway.tiger.TigerGateway;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import static com.tquant.algorithm.algos.utils.Utils.getStrikeLimit;

/**
 * Description: 期权策略只能在盘中运行，如果盘前操作会有问题
 * 1.因为盘前无法获取到最新期权价格，导致下单不符合预期
 * 2.同时获取期权最新价接口只有在盘前阶段才会返回盘口价格和期权最新价
 *
 * @author kevin
 * @date 2023/02/09
 */
public class PortfolioManager {

  private List<Order> orders = new ArrayList<>();
  private RollingSellPutConfig config;
  private EventEngine eventEngine = new EventEngine();
  private TigerGateway gateway = new TigerGateway(eventEngine);
  private TradeApi tradeApi = gateway.getTradeApi();
  private QuoteApi quoteApi = gateway.getQuoteApi();
  private OptionApi optionApi = gateway.getOptionApi();
  private Logger log;

  public PortfolioManager(RollingSellPutConfig config,Logger logger) {
    this.config = config;
    //是否要调用mock 交易接口，方便测试期权订单，实现下单时可以改成false
    this.tradeApi.setMock(false);
    this.log = logger;
  }

  /**
   * @param order
   */
  public void orderStatusEvent(Order order) {
    log.info("Receive order status={}, symbol={}", order.getStatus(), order.getSymbol());
  }

  public List<Position> getCalls(Map<String, List<Position>> portfolioPositions) {
    return getOptions(portfolioPositions, Right.CALL);
  }

  public List<Position> getPuts(Map<String, List<Position>> portfolioPositions) {
    return getOptions(portfolioPositions, Right.PUT);
  }

  private List<Position> getOptions(Map<String, List<Position>> portfolioPositions, Right right) {
    List<Position> result = new ArrayList<>();

    for (String symbol : portfolioPositions.keySet()) {
      result.addAll(
          portfolioPositions.get(symbol).stream()
              .filter(p -> p.isOption(right.name()))
              .collect(Collectors.toList())
      );
    }
    return result;
  }

  private Tick getTickerForStock(Contract contract) {
    RealtimeQuote realTimeQuotes = quoteApi
        .getRealTimeQuotes(Lists.newArrayList(contract.getSymbol()))
        .get(contract.getSymbol())
        .get(0);
    return new Tick(realTimeQuotes, contract);
  }

  private Tick getTickerForOption(Contract contract) {
    List<OptionBriefItem> optionBrief = optionApi
        .getOptionBrief(contract.getSymbol(), Right.valueOf(contract.getRight()), contract.getStrike().toString(), contract.getExpiry());
    return new Tick(Utils.convertToRealtimeQuote(optionBrief.get(0)), contract);
  }

  private Tick getTickerFor(Contract contract) {
    if (contract != null && contract.isOption()) {
      return getTickerForOption(contract);
    } else {
      return getTickerForStock(contract);
    }
  }

  public boolean putIsItm(Contract contract) {
    Tick ticker = getTickerForStock(contract);
    return contract.getStrike() >= ticker.getLatestPrice();
  }

  /**
   * 判断平仓条件，如果持仓盈亏大于阈值（50%），返回true
   * @param position
   * @return
   */
  public boolean positionCanBeClosed(Position position) {
    Double closeAtPnl = config.getRollWhen().getCloseAtPnl();
    if (closeAtPnl != null && closeAtPnl > 0) {
      double pnl = Utils.positionPnl(position);
      if (pnl > closeAtPnl) {
        log.info(String.format("  %s will be closed because P&L of %.1f%% is > %.1f%%", position.getSymbol(), pnl * 100,
            closeAtPnl * 100));
        return true;
      }
    }
    return false;
  }

  public boolean putCanBeClosed(Position put) {
    return positionCanBeClosed(put);
  }

  public boolean putCanBeRolled(Position put) {
    // Ignore long positions, we only roll shorts
    if (put.getDirection().equalsIgnoreCase(Direction.BUY.name())) {
      return false;
    }

    // Check if this put is ITM, and if it's o.k. to roll
    if (!config.getRollWhen().getPuts().isItm() && !putIsItm(put.getContract())) {
      return false;
    }

    int dte = Utils.optionDte(put.getContract().getExpiry());
    double pnl = Utils.positionPnl(put);

    int rollWhenDte = config.getRollWhen().getDte();
    double rollWhenPnl = config.getRollWhen().getPnl();
    double rollWhenMinPnl = config.getRollWhen().getMinPnl();

    if (config.getRollWhen().getMaxDte() != null && dte > config.getRollWhen().getMaxDte()) {
      return false;
    }

    if (dte <= rollWhenDte) {
      if (pnl >= rollWhenMinPnl) {
        log.info(String.format("  %s can be rolled because DTE of %d is <= %d and P&L of %.1f%% is >= %.1f%%",
            put.getSymbol(), dte, config.getRollWhen().getDte(), pnl * 100,
            rollWhenMinPnl * 100));
        return true;
      } else {
        log.info(String.format("  %s can't be rolled because P&L of %.1f%% is < %.1f%%",
            put.getSymbol(), pnl * 100, rollWhenMinPnl * 100));
      }
    }

    if (pnl >= rollWhenPnl) {
      log.info(String.format("  %s can be rolled because P&L of %.1f%% is >= %.1f%%",
          put.getContract().getSymbol(), pnl * 100, rollWhenPnl * 100));
      return true;
    }

    return false;
  }

  public boolean callIsItm(Contract contract) {
    Tick ticker = getTickerForStock(contract);
    return contract.getStrike() <= ticker.getLatestPrice();
  }

  public boolean callCanBeClosed(Position call) {
    return positionCanBeClosed(call);
  }

  public boolean callCanBeRolled(Position call) {
    if (call.getPosition() > 0) {
      return false;
    }

    if (!config.getRollWhen().getCalls().isItm() && callIsItm(call.getContract())) {
      return false;
    }

    int dte = Utils.optionDte(call.getContract().getExpiry());
    double pnl = Utils.positionPnl(call);

    int rollWhenDte = config.getRollWhen().getDte();
    double rollWhenPnl = config.getRollWhen().getPnl();
    double rollWhenMinPnl = config.getRollWhen().getMinPnl();

    if (config.getRollWhen().getMaxDte() != null && dte > config.getRollWhen().getMaxDte()) {
      return false;
    }

    if (dte <= rollWhenDte) {
      if (pnl >= rollWhenMinPnl) {
        log.info(String.format(
            "%s can be rolled because DTE of %d is <= %f and P&L of %.1f%% is >= %.1f%%",
            call.getContract().getSymbol(), dte, rollWhenDte, pnl * 100, rollWhenMinPnl * 100
        ));
        return true;
      } else {
        log.info(String.format(
            "%s can't be rolled because P&L of %.1f%% is < %.1f%%",
            call.getContract().getSymbol(), pnl * 100, rollWhenMinPnl * 100
        ));
      }
    }

    if (pnl >= rollWhenPnl) {
      log.info(String.format(
          "%s can be rolled because P&L of %.1f%% is >= %.1f%%",
          call.getContract().getSymbol(), pnl * 100, rollWhenPnl * 100
      ));
      return true;
    }

    return false;
  }

  public List<String> getSymbols() {
    List<String> symbols = new ArrayList<>();
    for (String key : config.getSymbols().keySet()) {
      symbols.add(key);
    }
    return symbols;
  }

  public List<Position> filterPositions(List<Position> portfolioPositions) {
    List<String> symbols = getSymbols();
    List<Position> filteredPositions = new ArrayList<>();
    for (Position position : portfolioPositions) {
      if (position.getAccount().equals(config.getAccount().getAccountId())
          && symbols.contains(position.getContract().getSymbol())
          && position.getPosition() != 0
          && position.getAverageCost() != 0) {
        filteredPositions.add(position);
      }
    }
    return filteredPositions;
  }

  public Map<String, List<Position>> getPortfolioPositions() {
    List<Position> portfolioPositions = tradeApi
        .getPositions().values().stream()
        .flatMap(List::stream)
        .collect(Collectors.toList());

    return Utils.portfolioPositionsToDict(filterPositions(portfolioPositions));
  }

  public void initializeAccount() {
    if (config.getAccount().getCancelOrders()) {
      List<Order> openTrades = tradeApi.getOpenOrders();
      if (openTrades == null) {
        log.info("No open orders to be closed");
        return;
      }
      for (Order trade : openTrades) {
        if (!trade.getStatus().equalsIgnoreCase(OrderStatus.Filled.name()) && getSymbols().contains(trade.getSymbol())) {
          log.info("Cancelling order={}", trade.getId());
          tradeApi.cancelOrder(trade.getId());
        }
      }
    }
  }

  public static String printCol(String c) {
    return String.format("%10s", c);
  }

  private void printAccountSummary(Asset accountSummary) {
    Map<String, String> justifiedValues = new HashMap<>();
    justifiedValues.put("NetLiquidation", String.format("%,.0f", accountSummary.getNetLiquidation()));
    justifiedValues.put("FullMaintMarginReq",
        String.format("%,.0f", accountSummary.getMaintenanceMarginReq()));
    justifiedValues.put("BuyingPower", String.format("%,.0f", accountSummary.getBuyingPower()));
    justifiedValues.put("TotalCashValue", String.format("%,.0f", accountSummary.getAvailableFunds()));

    for (Map.Entry<String, String> entry : justifiedValues.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      log.info("  {} = {}", key, String.format("%1$10s", value));
    }
  }

  private void printPositionDetail(Map<String, List<Position>> portfolioPositions) {
    log.info("Portfolio positions:");

    log.info(
        String.format(
            "   %s  %s  %s  %s  %s  %s  %s  %s  %s  %s  %s",
            printCol("Symbol"),
            printCol("Qty"),
            printCol("MktPrice"),
            printCol("AvgPrice"),
            printCol("Value"),
            printCol("Cost"),
            printCol("P&L"),
            printCol("Strike"),
            printCol("DTE"),
            printCol("Exp"),
            printCol("ITM?")
        )
    );

    DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
    for (String symbol : portfolioPositions.keySet()) {
      List<Position> positions = portfolioPositions.get(symbol);
      for (Position p : positions) {
        Contract contract = p.getContract();
        boolean isOpt = contract != null && contract.getSecType().equalsIgnoreCase(SecType.OPT.name());
        log.info(
            String.format(
                "   %s  %s  %s  %s  %s  %s  %s  %s  %s  %s  %s",
                printCol(p.getSymbol()),
                printCol(String.valueOf(p.getPosition())),
                printCol(decimalFormat.format(p.getMarketPrice())),
                printCol(decimalFormat.format(p.getAverageCost())),
                printCol(decimalFormat.format(p.getMarketValue())),
                printCol(decimalFormat.format(
                    p.getAverageCost() * p.getPosition() * (p.getContract().getMultiplier() != null ? p.getContract()
                        .getMultiplier() : 1))),
                printCol(decimalFormat.format(Utils.positionPnl(p) * 100) + "%"),
                printCol(isOpt ? decimalFormat.format(contract.getStrike()) : null),
                printCol(isOpt ? contract.getExpiry() : null),
                printCol(isOpt ? Integer.toString(Utils.optionDte(contract.getExpiry())) : null),
                printCol(String.valueOf(isItm(p)))
            )
        );
      }
    }
  }

  private Pair<Asset, Map<String, List<Position>>> summarizeAccount() {
    Asset accountSummary = tradeApi.getAsset();
    log.info("Account summary:");

    if (accountSummary.getNetLiquidation() <= 0D) {
      throw new RuntimeException("Account number "
          + config.getAccount().getAccountId()
          + " appears invalid (no account data returned)");
    }

    Map<String, List<Position>> portfolioPositions = getPortfolioPositions();
    printAccountSummary(accountSummary);
    printPositionDetail(portfolioPositions);

    return Pair.of(accountSummary, portfolioPositions);
  }

  public boolean isItm(Position p) {
    if (p.getContract().getRight() == null) {
      return false;
    }
    if (p.getContract().getRight().equalsIgnoreCase(Right.CALL.name()) && callIsItm(p.getContract())) {
      return true;
    } else if (p.getContract().getRight().equalsIgnoreCase(Right.PUT.name()) && putIsItm(p.getContract())) {
      return true;
    }
    return false;
  }

  public void manage() {
    try {
      initializeAccount();
      Pair<Asset, Map<String, List<Position>>> accountSummaryPair = summarizeAccount();
      Map<String, List<Position>> portfolioPositions = accountSummaryPair.getRight();

      log.info("Checking positions...");

      checkPuts(accountSummaryPair.getLeft(), accountSummaryPair.getRight());
      checkCalls(accountSummaryPair.getLeft(), accountSummaryPair.getRight());

      //Look for lots of stock that don't have covered calls
      checkForUncoveredPositions(accountSummaryPair.getLeft(), portfolioPositions);

      //Refresh positions, in case anything changed from the ordering above
      portfolioPositions = getPortfolioPositions();

      //Check if we have enough buying power to write some puts
      checkIfCanWritePuts(accountSummaryPair.getLeft(), portfolioPositions);

      log.info("ThetaGang is done, shutting down! Cya next time.");
    } catch (Exception e) {
      log.info("An exception was raised, exiting, {}", e.getMessage());
      throw e;
    }

    System.exit(1);
  }

  public void checkPuts(Asset accountSummary, Map<String, List<Position>> portfolioPositions) {
    // Check for puts which may be rolled to the next expiration or a better price
    List<Position> puts = getPuts(portfolioPositions);

    // find puts eligible to be rolled or closed
    List<Position> rollablePuts = new ArrayList<>();
    List<Position> closeablePuts = new ArrayList<>();

    for (Position p : puts) {
      if (putCanBeRolled(p)) {
        rollablePuts.add(p);
      } else if (putCanBeClosed(p)) {
        closeablePuts.add(p);
      }
    }

    int totalRollablePuts = (int) Math.floor(rollablePuts.stream().mapToDouble(p -> Math.abs(p.getPosition())).sum());
    int totalCloseablePuts =
        (int) Math.floor(closeablePuts.stream().mapToDouble(p -> Math.abs(p.getPosition())).sum());

    log.info("{} puts can be rolled", totalRollablePuts);
    log.info("{} puts can be closed", totalCloseablePuts);

    rollPuts(rollablePuts, accountSummary);
    closePuts(closeablePuts);
  }

  private void rollPuts(List<Position> puts, Asset accountSummary) {
    rollPositions(puts, Right.PUT.name(), accountSummary, null);
  }

  private void closePuts(List<Position> closeablePuts) {
    closePositions(closeablePuts);
  }

  public void checkCalls(Asset accountSummary, Map<String, List<Position>> portfolioPositions) {
    // Check for calls which may be rolled to the next expiration or a better price
    List<Position> calls = getCalls(portfolioPositions);

    // find calls eligible to be rolled
    List<Position> rollableCalls = new ArrayList<>();
    List<Position> closeableCalls = new ArrayList<>();

    for (Position c : calls) {
      if (callCanBeRolled(c)) {
        rollableCalls.add(c);
      } else if (callCanBeClosed(c)) {
        closeableCalls.add(c);
      }
    }

    int totalRollableCalls = (int) Math.floor(
        rollableCalls.stream().map(p -> Math.abs(p.getPosition())).reduce(0, (a, b) -> a + b)
    );
    int totalCloseableCalls = (int) Math.floor(
        closeableCalls.stream().map(p -> Math.abs(p.getPosition())).reduce(0, (a, b) -> a + b)
    );

    log.info(String.format("%d calls can be rolled", totalRollableCalls));
    log.info(String.format("%d calls can be closed", totalCloseableCalls));

    rollCalls(rollableCalls, accountSummary, portfolioPositions);
    closeCalls(closeableCalls);
  }

  private void rollCalls(List<Position> rollableCalls, Asset accountSummary,
      Map<String, List<Position>> portfolioPositions) {
    rollPositions(rollableCalls, Right.CALL.name(), accountSummary, portfolioPositions);
  }

  private void closeCalls(List<Position> closeableCalls) {
    closePositions(closeableCalls);
  }

  /**
   * 获取待交易合约数量
   * @param contract
   * @param accountSummary
   * @return
   */
  public int getMaximumNewContractsFor(Contract contract, Asset accountSummary) {

    double totalBuyingPower = getBuyingPower(accountSummary.getBuyingPower());
    double maxBuyingPower = (config.getTarget().getMaximumNewContractsPercent() * totalBuyingPower);

    Tick ticker = getTickerForStock(contract);
    double price = Utils.midpointOrMarketPrice(ticker);

    return Math.max(1, (int) Math.round(maxBuyingPower / price / 100));
  }

  public Contract newContract(String symbol, SecType secType, Right right) {
    return newContract(symbol, secType, right, null, null);
  }

  public Contract newContract(String symbol, SecType secType, Right right, String expiry, Double strike) {
    Contract contract = new Contract();
    contract.setSymbol(symbol);
    contract.setSecType(secType.name());
    if (right != null) {
      contract.setRight(right.name());
    }
    if (expiry != null) {
      contract.setExpiry(expiry);
    }
    if (strike != null) {
      contract.setStrike(strike);
    }
    return contract;
  }

  public void checkForUncoveredPositions(Asset
      accountSummary, Map<String, List<Position>> portfolioPositions) {
    for (String symbol : portfolioPositions.keySet()) {
      int callCount = Math.max(0, Utils.countShortOptionPositions(symbol, portfolioPositions, Right.CALL.name()));
      int stockCount = (int) Math.floor(
          portfolioPositions.get(symbol)
              .stream()
              .filter(p -> p.isStock())
              .mapToInt(p -> p.getPosition())
              .sum()
      );

      Double strikeLimitDouble = getStrikeLimit(config, symbol, Right.CALL.name());
      double strikeLimit = (int) Math.ceil(
          Math.max(Math.max(strikeLimitDouble != null ? strikeLimitDouble : 0, 0),
              portfolioPositions.get(symbol)
                  .stream()
                  .filter(p -> p.isStock())
                  .mapToDouble(p -> p.getAverageCost())
                  .max()
                  .orElse(0)
          )
      );

      int targetCalls = Math.max(0, (int) Math.floor((stockCount * Utils.getCallCap(config)) / 100.0));
      int newContractsNeeded = targetCalls - callCount;
      int excessCalls = callCount - targetCalls;

      if (excessCalls > 0) {
        log.info("Warning: {} has excess_calls={} stock_count={}, call_count={}, target_calls={}", symbol, excessCalls,
            stockCount, callCount, targetCalls);
      }

      Contract contract = newContract(symbol, SecType.STK, null);
      int maximumNewContracts = getMaximumNewContractsFor(contract, accountSummary);
      int callsToWrite = Math.max(0, Math.min(newContractsNeeded, maximumNewContracts));

      boolean writeOnlyWhenGreen = config.getWriteWhen().getCalls().getGreen();
      Tick ticker = writeOnlyWhenGreen ? getTickerForStock(contract) : null;

      boolean okToWrite = !writeOnlyWhenGreen || ticker != null && ticker.getLatestPrice() > ticker.getClose();

      if (callsToWrite > 0 && okToWrite) {
        log.info(
            "Will write {} calls, {} needed for {}, capped at {}, at or above strike ${} (target_calls={}, call_count={})",
            callsToWrite, newContractsNeeded, symbol, maximumNewContracts, strikeLimit, targetCalls, callCount);
        try {
          writeCalls(symbol, callsToWrite, strikeLimit);
        } catch (RuntimeException e) {
          log.info("Failed to write calls for {}. Continuing anyway...", symbol);
          log.error("writeCalls error {}", e.getMessage());
          throw e;
        }
      } else if (callsToWrite > 0) {
        log.info("Need to write {} calls for {}, but skipping because underlying is red", callsToWrite, symbol);
      }
    }
  }

  private void writeCalls(String symbol, int quantity, double strikeLimit) {
    try {
      Tick sellTicker = findEligibleContracts(symbol, Right.CALL.name(), strikeLimit, null, null, null);
      if (sellTicker == null) {
        log.info("writeCalls error , cannot find suitable contract for {}", symbol);
        return;
      }
      Contract contract = sellTicker.getContract();
      // Submit order
      long orderId = tradeApi.placeLimitOrder(contract, Direction.SELL.name(), Utils.midpointOrAskPrice(sellTicker), quantity);
      Order order = tradeApi.getOrder(orderId);
      orders.add(order);

      log.info("Order submitted {}", order);
    } catch (RuntimeException e) {
      log.info("Order trade submission seems to have failed, or a response wasn't received in time. Continuing anyway...");
      log.error("writeCalls error {}", e.getMessage());
    }
  }

  public void writePuts(String symbol, int quantity, double strikeLimit) {
    Tick sellTicker;
    try {
      sellTicker = findEligibleContracts(symbol, Right.PUT.name(), strikeLimit, null, null, null);
    } catch (RuntimeException e) {
      log.error("Finding eligible contracts for {} failed. Continuing anyway...", symbol);
      log.error("writePuts error {}", e.getMessage());
      return;
    }

    Contract contract = newContract(symbol, SecType.OPT, Right.PUT, sellTicker.getContract().getExpiry(),
        sellTicker.getContract().getStrike());

    // Submit order
    long orderId = tradeApi.placeLimitOrder(contract, Direction.SELL.name(), Utils.midpointOrAskPrice(sellTicker), quantity);
    Order order = tradeApi.getOrder(orderId);
    orders.add(order);

    log.info("Order submitted {}", order);
  }

  public double getBuyingPower(double buyingPower) {
    return (int) Math.floor(buyingPower * config.getAccount().getMarginUsage());
  }

  public void checkIfCanWritePuts(Asset accountSummary, Map<String, List<Position>> portfolioPositions) {
    List<Position> stockPositions = portfolioPositions.values().stream()
        .flatMap(Collection::stream)
        .filter(Position::isStock)
        .collect(Collectors.toList());

    double totalBuyingPower = getBuyingPower(accountSummary.getBuyingPower());
    log.info("Total buying power: ${} at {} % margin usage", (int) totalBuyingPower,(int) (config.getAccount().getMarginUsage() * 100));

    Map<String, Position> stockSymbols =
        stockPositions.stream().collect(Collectors.toMap(Position::getSymbol, Function.identity()));

    Map<String, Double> targets = new HashMap<>();
    Map<String, Integer[]> targetAdditionalQuantity = new HashMap<>();

    for (String symbol : config.getSymbols().keySet()) {
      log.info("  {}", symbol);

      Contract contract = newContract(symbol, SecType.STK, null);
      Tick ticker = getTickerForStock(contract);

      int currentPosition = (int) Math.floor(
          stockSymbols.containsKey(symbol) ? stockSymbols.get(symbol).getPosition() : 0
      );
      log.info("    Current position quantity: {} shares", currentPosition);

      targets.put(symbol, Math.round(
          config.getSymbols().get(symbol).getWeight() * totalBuyingPower * 100.0) / 100.0
      );
      log.info("    Target value: ${}", targets.get(symbol));
      int targetQuantity = (int) Math.floor(targets.get(symbol) / ticker.getLatestPrice());
      log.info("    Target share quantity: {}", targetQuantity);

      int putCount = Utils.countShortOptionPositions(symbol, portfolioPositions, Right.PUT.name());

      boolean writeOnlyWhenRed = config.getWriteWhen().getPuts().isRed();

      boolean okToWrite = !writeOnlyWhenRed || ticker.getLatestPrice() < ticker.getClose();

      targetAdditionalQuantity.put(symbol, new Integer[] {
          (int) Math.floor(targetQuantity - currentPosition - 100 * putCount),
          okToWrite ? 1 : 0
      });

      log.info("    Net quantity: {} shares, {} contracts", targetAdditionalQuantity.get(symbol)[0],
          targetAdditionalQuantity.get(symbol)[0] / 100);
    }

    for (String symbol : targetAdditionalQuantity.keySet()) {
      int okToWrite = targetAdditionalQuantity.get(symbol)[1];
      int additionalQuantity = targetAdditionalQuantity.get(symbol)[0] / 100;
      if (additionalQuantity >= 1 && okToWrite > 0) {
        Contract contract = newContract(symbol, SecType.STK, null);
        int maximumNewContracts = getMaximumNewContractsFor(contract, accountSummary);
        int putsToWrite = Math.min(additionalQuantity, maximumNewContracts);
        if (putsToWrite > 0) {
          Double strikeLimitDouble = getStrikeLimit(config, symbol, Right.PUT.name());
          double strikeLimit = strikeLimitDouble != null ? strikeLimitDouble : 0D;
          if (strikeLimit > 0) {
            log.info("Will write {} puts, {} needed for {}, capped at {}, at or below strike ${}", putsToWrite,
                additionalQuantity, symbol, maximumNewContracts, strikeLimit);
          } else {
            log.info("Will write {} puts, {} needed for {}, capped at {}", putsToWrite, additionalQuantity, symbol,
                maximumNewContracts);
          }
          writePuts(symbol, putsToWrite, strikeLimit);
        }
      } else if (additionalQuantity >= 1) {
        log.info("Need {} additional for {}, but won't write because {} is green", additionalQuantity, symbol, symbol);
      }
    }
  }

  private void closePositions(List<Position> positions) {
    for (Position position : positions) {
      try {
        Tick buyTicker = getTickerFor(position.getContract());
        double price = buyTicker.getBidPrice();
        if (price <= 0D) {
          log.info("Unable to close {} because market price data unavailable, skipping",
              position.getContract().getSymbol());
          continue;
        }

        long orderId = tradeApi.placeLimitOrder(buyTicker.getContract(), Direction.BUY.name(), price,
            Math.abs(position.getPosition()));

        Order order = tradeApi.getOrder(orderId);
        orders.add(order);

        log.info(
            "Order submitted, current position={}, price={}, order={}", Math.abs(position.getPosition()),
            Math.round(price), order);
      } catch (RuntimeException e) {
        log.info("Error occurred when trying to close position. Continuing anyway...");
        log.error("closePositions error {}",e.getMessage());
        throw e;
      }
    }
  }

  public void rollPositions(List<Position> positions, String right, Asset accountSummary,
      Map<String, List<Position>> portfolioPositions) {
    for (Position position : positions) {
      try {
        Tick buyTicker = getTickerFor(position.getContract());
        String symbol = position.getSymbol();
        Double strikeLimit = Utils.getStrikeLimit(config, symbol, right);
        boolean isCall = right.equalsIgnoreCase(Right.CALL.name());
        List<Double> strikeLimits = new ArrayList<>();
        if (strikeLimit != null) {
          strikeLimits.add(strikeLimit);
        }
        if (isCall) {
          for (Position p : portfolioPositions.get(symbol)) {
            if (p.isStock()) {
              strikeLimits.add(p.getAverageCost());
            }
          }
          strikeLimit = Math.round(Collections.max(strikeLimits)) * 100.0 / 100.0;
        } else {
          strikeLimits.add(
              position.getContract().getStrike() + position.getAverageCost() - Utils.midpointOrMarketPrice(buyTicker));
          strikeLimit = Math.round(strikeLimit + position.getContract().getStrike() + position.getAverageCost()
              - Utils.midpointOrMarketPrice(buyTicker)) * 100.0 / 100.0;
        }

        boolean isCreditOnly =
            isCall ? config.getRollWhen().getCalls().isCreditOnly() : config.getRollWhen().getPuts().isCreditOnly();

        double minimumPrice = isCreditOnly ? Utils.midpointOrMarketPrice(buyTicker) : 0.0;

        Tick sellTicker = findEligibleContracts(
            symbol,
            right,
            strikeLimit,
            position.getContract().getExpiry(),
            position.getContract().getStrike(),
            minimumPrice
        );

        int qtyToRoll = Math.abs(position.getPosition());
        Contract contract = newContract(symbol, SecType.STK, null);
        int maximumNewContracts = getMaximumNewContractsFor(contract, accountSummary);
        int fromDte = Utils.optionDte(position.getContract().getExpiry());
        int rollWhenDte = config.getRollWhen().getDte();
        if (fromDte > rollWhenDte) {
          qtyToRoll = Math.min(qtyToRoll, maximumNewContracts);
        }

        double price = Utils.midpointOrMarketPrice(buyTicker) - Utils.midpointOrMarketPrice(sellTicker);

        //平仓原来的期权持仓
        long orderId1 = tradeApi.placeLimitOrder(position.getContract(), Direction.BUY.name(), price, qtyToRoll);

        //卖出新的期权
        int toDte = Utils.optionDte(sellTicker.getContract().getExpiry());
        Double fromStrike = position.getContract().getStrike();
        Double toStrike = sellTicker.getContract().getStrike();
        Contract contract2 = newContract(symbol,SecType.OPT,Right.valueOf(right),sellTicker.getContract().getExpiry(),strikeLimit);

        long orderId2 = tradeApi.placeLimitOrder(contract2, Direction.SELL.name(), price, qtyToRoll);

        // Submit order
        Order order1 = tradeApi.getOrder(orderId1);
        Order order2 = tradeApi.getOrder(orderId2);
        this.orders.add(order1);
        this.orders.add(order2);

        log.info(
            "Order submitted, current position={}, qty_to_roll={}, from_dte={}, to_dte={}, from_strike={}, to_strike={}, price={}, order1={},order2={}",
            Math.abs(position.getPosition()), qtyToRoll, fromDte, toDte, fromStrike, toStrike, price, order1, order2);
      } catch (Exception e) {
        log.error("rollPositions error {}", e.getMessage());
        return;
      }
    }
  }

  public Tick findEligibleContracts(String symbol, String right, Double strikeLimit, String excludeExpirationsBefore,
      Double excludeFirstExpStrike, Double minimumPrice) {
    log.info(
        "Searching option chain for symbol={} right={}, strike_limit={}, minimum_price={} this can take a while...",
        symbol, right, strikeLimit, minimumPrice);

    Contract stock = newContract(symbol, SecType.STK, Right.valueOf(right));
    Tick ticker = getTickerForStock(stock);

    List<String> rights = Collections.singletonList(right);

    int minDte = 0;
    if (excludeExpirationsBefore != null) {
      minDte = Utils.optionDte(excludeExpirationsBefore);
    }

    List<String> allExpirations = optionApi.getOptionExpiration(symbol).getDates();
    List<String> expirations = new ArrayList<>();
    for (String exp : allExpirations) {
      int optionDTE = Utils.optionDte(exp);
      if (optionDTE >= config.getTarget().getDte() && optionDTE >= minDte) {
        expirations.add(exp);
      }
    }
    if (expirations.isEmpty()) {
      throw new RuntimeException("No valid contracts found for " + symbol + ". Aborting.");
    }
    Collections.sort(expirations);
    int chainExpirations = Math.min(config.getOptionChains().getExpirations(), expirations.size());
    expirations = expirations.subList(0, chainExpirations);


    stock.setExpiry(expirations.get(0));
    List<OptionRealTimeQuoteGroup> chains = getChainsForStock(stock);

    List<Double> strikes = new ArrayList<>();
    for (OptionRealTimeQuoteGroup quoteGroup : chains) {
      double chainStrike;
      if (Utils.isCall(right)) {
        if (quoteGroup.getCall() == null) {
          continue;
        }
        chainStrike = Double.parseDouble(quoteGroup.getCall().getStrike());
      }else {
        if (quoteGroup.getPut() == null) {
          continue;
        }
        chainStrike = Double.parseDouble(quoteGroup.getPut().getStrike());
      }
      if (Utils.isValidStrike(right, chainStrike,  ticker.getLatestPrice(), strikeLimit)) {
        strikes.add(chainStrike);
      }
    }
    if (strikes.isEmpty()) {
      throw new RuntimeException("No valid contracts found for " + symbol + ". Aborting.");
    }
    Collections.sort(strikes);

    List<Contract> contracts = new ArrayList<>();
    for (String loopRight : rights) {
      for (String expiration : expirations) {
        List<Double> nearestStrikes = getNearestStrikes(strikes, loopRight);
        for (Double strike : nearestStrikes) {
          Contract contract = newContract(symbol, SecType.OPT, Right.valueOf(loopRight), expiration, strike);
          contracts.add(contract);
        }
      }
    }

    if (excludeFirstExpStrike != null && excludeFirstExpStrike != 0) {
      List<Contract> contractsToRemove = new ArrayList<>();
      for (Contract contract : contracts) {
        if (contract.getExpiry().equals(expirations.get(0))
            && contract.getStrike().equals(excludeFirstExpStrike)) {
          contractsToRemove.add(contract);
        }
      }
      contracts.removeAll(contractsToRemove);
    }

    for (Contract contract : contracts) {
      Tick tickerForOption = getTickerForOption(contract);
      if (priceIsValid(tickerForOption, minimumPrice)) {
        return tickerForOption;
      }
    }
    throw new RuntimeException("cannot get suitable ticker");
  }

  private List<OptionRealTimeQuoteGroup> getChainsForStock(Contract stock) {
    // openInterest 和 delta 要满足配置要求
    return optionApi
        .getOptionChain(stock.getSymbol(), stock.getExpiry(), OptionChainFilter.builder()
            .minOpenInterest(config.getTarget().getMinimumOpenInterest())
            .maxDelta(Utils.getTargetDelta(config, stock.getSymbol(), stock.getRight()))
            .build());
  }

  public List<Double> getNearestStrikes(List<Double> strikes, String right) {
    int chainStrikes = this.config.getOptionChains().getStrikes();
    if (right.equalsIgnoreCase(Right.PUT.name())) {
      if (strikes.size() < chainStrikes) {
        return strikes;
      }
      return strikes.subList(strikes.size() - chainStrikes, strikes.size());
    } else {
      return strikes.subList(0, Math.min(chainStrikes, strikes.size()));
    }
  }

  public boolean priceIsValid(Tick ticker,Double minimumPrice) {
    if (minimumPrice == null) {
      minimumPrice = 0D;
    }
    if (Utils.midpointOrMarketPrice(ticker) > minimumPrice) {
      return true;
    } else {
      return false;
    }
  }
}
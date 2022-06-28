package com.tigerbrokers.quant.core;

import com.tigerbrokers.quant.TigerQuantException;
import com.tigerbrokers.quant.event.EventEngine;
import com.tigerbrokers.quant.event.EventHandler;
import com.tigerbrokers.quant.event.EventType;
import com.tigerbrokers.quant.model.data.Account;
import com.tigerbrokers.quant.model.data.Asset;
import com.tigerbrokers.quant.model.data.Contract;
import com.tigerbrokers.quant.model.data.Order;
import com.tigerbrokers.quant.model.data.Position;
import com.tigerbrokers.quant.model.data.Tick;
import com.tigerbrokers.quant.model.data.Trade;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/08/16
 */
public class OrderEngine extends Engine {

  public static final String ENGINE_NAME = "OrderEngine";

  private Map<String, Tick> ticks = new HashMap<>();
  private Map<Long, Order> orders = new HashMap<>();
  private Map<Long, Order> activeOrders = new HashMap<>();
  private Map<Long, Trade> trades = new HashMap<>();
  private Map<String, Position> positions = new HashMap<>();
  private Asset asset = null;
  private Map<String, Contract> contracts = new HashMap<>();

  private EventHandler tickHandler = event -> {
    Tick tick = (Tick) event.getEventData();
    ticks.put(tick.getSymbol(), tick);
  };
  private EventHandler orderHandler = event -> {
    Order order = (Order) event.getEventData();
    orders.put(order.getId(), order);
    if (order.isActive()) {
      activeOrders.put(order.getId(), order);
    } else {
      if (activeOrders.containsKey(order.getId())) {
        activeOrders.remove(order.getId());
      }
    }
  };
  private EventHandler tradeHandler = event -> {
    Trade trade = (Trade) event.getEventData();
    trades.put(trade.getOrderId(), trade);
  };
  private EventHandler positionHandler = event -> {
    Position position = (Position) event.getEventData();
    positions.put(position.getIdentifier(), position);
    positions.put(position.getSymbol(), position);
  };
  private EventHandler assetHandler = event -> {
    Asset asset = (Asset) event.getEventData();
    this.asset = asset;
  };
  private EventHandler contractHandler = event -> {
    Contract contract = (Contract) event.getEventData();
    contracts.put(contract.getIdentifier(), contract);
  };

  public OrderEngine(MainEngine mainEngine, EventEngine eventEngine) {
    this(ENGINE_NAME, mainEngine, eventEngine);
  }

  public OrderEngine(String engineName, MainEngine mainEngine, EventEngine eventEngine) {
    super(engineName, mainEngine, eventEngine);
    registerEvent();
  }

  private void registerEvent() {
    eventEngine.register(EventType.EVENT_TICK, tickHandler);
    eventEngine.register(EventType.EVENT_ORDER, orderHandler);
    eventEngine.register(EventType.EVENT_TRADE, tradeHandler);
    eventEngine.register(EventType.EVENT_POSITION, positionHandler);
    eventEngine.register(EventType.EVENT_ASSET, assetHandler);
    eventEngine.register(EventType.EVENT_CONTRACT, contractHandler);
  }

  public Tick getTick(String symbol) {
    return ticks.get(symbol);
  }

  public Order getOrder(Long orderId) {
    return orders.get(orderId);
  }

  public Trade getTrade(Long orderId) {
    return trades.get(orderId);
  }

  public Position getPosition(String positionId) {
    return positions.get(positionId);
  }

  public Account getAccount(String account) {
    return null;
  }

  public Asset getAsset() {
    if (asset == null) {
      throw new TigerQuantException("get asset is null");
    }
    return asset;
  }

  public Contract getContract(String identifier) {
    return contracts.get(identifier);
  }

  public List<Tick> getAllTicks() {
    return new ArrayList<>(ticks.values());
  }

  public List<Order> getAllOrders() {
    return new ArrayList<>(orders.values());
  }

  public List<Trade> getAllTrades() {
    return new ArrayList<>(trades.values());
  }

  public List<Position> getAllPositions() {
    return new ArrayList<>(positions.values());
  }

  public List<Account> getAllAccounts() {
    return null;
  }

  public List<Contract> getAllContracts() {
    return new ArrayList<>(contracts.values());
  }

  public List<Order> getAllActiveOrders(String symbol) {
    if (symbol == null) {
      return new ArrayList<>(activeOrders.values());
    }
    List<Order> activeOrderList = new ArrayList<>();
    for (Order order : activeOrders.values()) {
      if (symbol.equals(order.getSymbol())) {
        activeOrderList.add(order);
      }
    }
    return activeOrderList;
  }
}

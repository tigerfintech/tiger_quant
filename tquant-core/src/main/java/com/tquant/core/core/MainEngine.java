package com.tquant.core.core;

import com.tquant.core.TigerQuantException;
import com.tquant.core.core.AlgoEngine;
import com.tquant.core.event.Event;
import com.tquant.core.event.EventEngine;
import com.tquant.core.event.EventType;
import com.tquant.core.core.Gateway;
import com.tquant.core.model.data.Account;
import com.tquant.core.model.data.Asset;
import com.tquant.core.model.data.Bar;
import com.tquant.core.model.data.Contract;
import com.tquant.core.model.data.Order;
import com.tquant.core.model.data.Position;
import com.tquant.core.model.data.Tick;
import com.tquant.core.model.data.Trade;
import com.tquant.core.model.enums.BarType;
import com.tquant.core.model.request.ModifyRequest;
import com.tquant.core.model.request.OrderRequest;
import com.tquant.core.model.request.SubscribeRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/08/16
 */
public class MainEngine {

  private String todayDate;
  private Map<String, Gateway> gateways = new HashMap<>();
  private Map<String, Engine> engines = new HashMap<>();
  private EventEngine eventEngine;
  private LogEngine logEngine;

  public MainEngine(EventEngine eventEngine) {
    this.todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    this.eventEngine = eventEngine;
    log("MainEngine init at:" + todayDate);
    this.eventEngine.start();
    initEngines();
  }

  private void initEngines() {
    logEngine = new LogEngine(this, eventEngine);
    addEngine(logEngine);
    addEngine(new OrderEngine(this, eventEngine));
    addEngine(new AlgoEngine(this, eventEngine));
  }

  public Logger getLogger() {
    return logEngine.getLogger();
  }

  public Engine addEngine(Engine engine) {
    return this.engines.putIfAbsent(engine.getEngineName(), engine);
  }

  public Engine getEngine(String engineName) {
    Engine engine = engines.get(engineName);
    if (engine == null) {
      log("cannot find engine:" + engineName);
    }
    return engine;
  }

  public List<Engine> getAllEngines() {
    return new ArrayList<>(engines.values());
  }

  public Gateway addGateway(Gateway gateway) {
    return this.gateways.putIfAbsent(gateway.getGatewayName(), gateway);
  }

  public Gateway getGateway(String gatewayName) {
    Gateway gateway = gateways.get(gatewayName);
    if (gateway == null) {
      log("cannot find gateway:" + gatewayName);
    }
    return gateway;
  }

  public List<String> getAllGatewayNames() {
    return new ArrayList<>(gateways.keySet());
  }

  public void subscribe(String gatewayName, SubscribeRequest request) {
    Gateway gateway = getGateway(gatewayName);
    if (gateway == null) {
      throw new TigerQuantException("gateway " + gatewayName + " not found");
    }
    gateway.subscribe(request);
  }

  public void cancelSubscribe(String gatewayName, SubscribeRequest request) {
    Gateway gateway = getGateway(gatewayName);
    if (gateway == null) {
      throw new TigerQuantException("gateway " + gatewayName + " not found");
    }
    gateway.cancelSubscribe(request);
  }

  public long sendOrder(String gatewayName, OrderRequest request) {
    Gateway gateway = getGateway(gatewayName);
    if (gateway == null) {
      throw new TigerQuantException("gateway " + gatewayName + " not found");
    }
    return gateway.sendOrder(request);
  }

  public void cancelOrder(String gatewayName, ModifyRequest request) {
    Gateway gateway = getGateway(gatewayName);
    if (gateway != null) {
      gateway.cancelOrder(request);
    }
  }

  public Map<String, List<Bar>> getBars(String gatewayName, List<String> symbols, BarType barType, int limit) {
    Gateway gateway = getGateway(gatewayName);
    if (gateway == null) {
      throw new TigerQuantException("gateway " + gatewayName + " not found");
    }
    return gateway.getBars(symbols, barType, limit);
  }

  public OrderEngine getOrderEngine() {
    OrderEngine engine = (OrderEngine) getEngine(OrderEngine.ENGINE_NAME);
    if (engine == null) {
      throw new TigerQuantException("order engine is null");
    }
    return engine;
  }

  public Tick getTick(String symbol) {
    return getOrderEngine().getTick(symbol);
  }

  public Order getOrder(Long orderId) {
    return getOrderEngine().getOrder(orderId);
  }

  public Trade getTrade(Long orderId) {
    return getOrderEngine().getTrade(orderId);
  }

  public Position getPosition(String positionId) {
    return getOrderEngine().getPosition(positionId);
  }

  public Account getAccount(String account) {
    return getOrderEngine().getAccount(account);
  }

  public Asset getAsset() {
    return getOrderEngine().getAsset();
  }

  public Contract getContract(String identifier) {
    return getOrderEngine().getContract(identifier);
  }

  public List<Tick> getAllTicks() {
    return getOrderEngine().getAllTicks();
  }

  public List<Order> getAllOrders() {
    return getOrderEngine().getAllOrders();
  }

  public List<Trade> getAllTrades() {
    return getOrderEngine().getAllTrades();
  }

  public List<Position> getAllPositions() {
    return getOrderEngine().getAllPositions();
  }

  public List<Account> getAllAccounts() {
    return getOrderEngine().getAllAccounts();
  }

  public List<Contract> getAllContracts() {
    return getOrderEngine().getAllContracts();
  }

  public List<Order> getAllActiveOrders(String symbol) {
    return getOrderEngine().getAllActiveOrders(symbol);
  }

  public void start() {
    for (String gatewayName : getAllGatewayNames()) {
      Gateway gateway = getGateway(gatewayName);
      if (gateway != null) {
        gateway.connect();
      }
    }
    for (Engine engine : getAllEngines()) {
      engine.start();
    }
  }

  public void stop() {
    eventEngine.stop();
    for (Engine engine : engines.values()) {
      engine.stop();
    }

    for (Gateway gateway : gateways.values()) {
      gateway.stop();
    }
    log("main engine stopped.");
  }

  public void log(String format, Object... args) {
    if (format == null || format.isEmpty()) {
      return;
    }
    FormattingTuple formatting = MessageFormatter.arrayFormat(format, args);
    eventEngine.put(new Event(EventType.EVENT_LOG, formatting.getMessage()));
  }
}

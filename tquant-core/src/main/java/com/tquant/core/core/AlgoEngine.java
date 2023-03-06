package com.tquant.core.core;

import com.tquant.core.TigerQuantException;
import com.tquant.core.config.*;
import com.tquant.core.event.EventEngine;
import com.tquant.core.event.EventHandler;
import com.tquant.core.event.EventType;
import com.tquant.core.model.data.Asset;
import com.tquant.core.model.data.Bar;
import com.tquant.core.model.data.Contract;
import com.tquant.core.model.data.Order;
import com.tquant.core.model.data.Position;
import com.tquant.core.model.data.Tick;
import com.tquant.core.model.data.Trade;
import com.tquant.core.model.enums.BarType;
import com.tquant.core.model.enums.Direction;
import com.tquant.core.model.enums.OrderType;
import com.tquant.core.model.request.ModifyRequest;
import com.tquant.core.model.request.OrderRequest;
import com.tquant.core.model.request.SubscribeRequest;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.tquant.core.util.QuantConstants.ALGO_CONFIG_PATH_PROP;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/08/19
 */
public class AlgoEngine extends Engine {

  public static final String ENGINE_NAME = "AlgoEngine";
  private static final String DEFAULT_GATEWAY = "TigerGateway";

  private Map<String, Map<String, Object>> algoSettings;
  private Map<String, AlgoTemplate> algos = new HashMap<>();
  private Map<String, List<String>> symbolAlgoTemplateMap = new HashMap<>();
  private Map<Long, String> orderIdAlgoTemplateMap = new HashMap<>();

  public AlgoEngine(MainEngine mainEngine, EventEngine eventEngine) {
    this(ENGINE_NAME, mainEngine, eventEngine);
  }

  public AlgoEngine(String engineName, MainEngine mainEngine, EventEngine eventEngine) {
    super(engineName, mainEngine, eventEngine);
    log("===============Tiger Quantitative Algorithm Engine Started===============");
    initEngine();
    registerEvent();
  }

  private void initEngine() {
    loadSetting();
  }

  private void loadSetting() {
    String algoConfigPath = System.getProperty(ALGO_CONFIG_PATH_PROP);
    if (algoConfigPath == null) {
      throw new TigerQuantException("algo strategy config path cannot be empty");
    }
    algoSettings = ConfigLoader.loadConfigs(algoConfigPath);
  }

  private void registerEvent() {
    eventEngine.register(EventType.EVENT_TICK, tickHandler);
    eventEngine.register(EventType.EVENT_TIMER, timerHandler);
    eventEngine.register(EventType.EVENT_TRADE, tradeHandler);
    eventEngine.register(EventType.EVENT_ORDER, orderHandler);
  }

  private EventHandler tickHandler = event -> {
    Tick tick = (Tick) event.getEventData();
    List<String> algoTemplates = symbolAlgoTemplateMap.get(tick.getSymbol());
    if (algoTemplates != null) {
      for (String template : algoTemplates) {
        algos.get(template).updateTick(tick);
      }
    }
  };

  private EventHandler orderHandler = event -> {
    Order order = (Order) event.getEventData();
    String template = orderIdAlgoTemplateMap.get(order.getId());
    if (template != null) {
      algos.get(template).updateOrder(order);
    }
  };

  private EventHandler timerHandler = event -> {
    for (AlgoTemplate algoTemplate : algos.values()) {
      algoTemplate.updateTimer();
    }
  };

  private EventHandler tradeHandler = event -> {
    Trade order = (Trade) event.getEventData();
    String template = orderIdAlgoTemplateMap.get(order.getOrderId());
    if (template != null) {
      algos.get(template).updateTrade(order);
    }
  };

  @Override
  public void start() {
    for (String algoName : algoSettings.keySet()) {
      Map<String, Object> settings = algoSettings.get(algoName);
      if (settings == null) {
        continue;
      }
      Boolean enable = (Boolean) settings.get("enable");
      if (enable == null || enable == false) {
        continue;
      }
      String className = (String) settings.get("class");
      if (className == null || className.isEmpty()) {
        continue;
      }
      try {
        Class<?> aClass = Class.forName(className);
        Constructor<?> constructor = aClass.getConstructor();
        AlgoTemplate algoTemplate = (AlgoTemplate) constructor.newInstance();
        algoTemplate.setSettings(settings);
        algoTemplate.setAlgoEngine(this);
        algoTemplate.init();
        algos.put(algoName, algoTemplate);
        algoTemplate.start();
      } catch (ClassNotFoundException e) {
        throw new TigerQuantException("algo ClassNotFoundException:" + e.getMessage());
      } catch (NoSuchMethodException e) {
        throw new TigerQuantException("algo NoSuchMethodException:" + e.getMessage());
      } catch (IllegalAccessException e) {
        throw new TigerQuantException("algo IllegalAccessException:" + e.getMessage());
      } catch (InstantiationException e) {
        throw new TigerQuantException("algo InstantiationException:" + e.getMessage());
      } catch (InvocationTargetException e) {
        throw new TigerQuantException("algo InvocationTargetException:" + e.getMessage());
      }
    }
  }

  @Override
  public void stop() {
    for (String templateName : algos.keySet()) {
      stopAlgo(templateName);
    }
  }

  public void stopAlgo(String algoName) {
    AlgoTemplate algoTemplate = algos.get(algoName);
    if (algoTemplate != null) {
      algoTemplate.stop();
      algos.remove(algoName);
    }
  }

  public void subscribe(String templateName, String symbol) {
    if (templateName == null) {
      throw new TigerQuantException("subscribe templateName is null");
    }
    if (symbol == null) {
      throw new TigerQuantException("subscribe symbol is null");
    }

    Contract contract = mainEngine.getContract(symbol);
    if (contract == null) {
      log("subscribe error,cannot find contract symbol:{}", symbol);
      return;
    }

    List<String> algoTemplates = symbolAlgoTemplateMap.get(symbol);
    if (algoTemplates == null) {
      algoTemplates = new ArrayList<>();
      symbolAlgoTemplateMap.put(symbol, algoTemplates);
    }
    if (algoTemplates != null) {
      Set<String> symbols = new HashSet<>();
      symbols.add(symbol);
      SubscribeRequest req = new SubscribeRequest(symbols);
      mainEngine.subscribe(DEFAULT_GATEWAY, req);
    }
    algoTemplates.add(templateName);
  }

  public void cancelSubscribe(String templateName, String symbol) {
    if (templateName == null) {
      throw new TigerQuantException("cancel subscribe templateName is null");
    }
    if (symbol == null) {
      throw new TigerQuantException("cancel subscribe symbol is null");
    }

    Contract contract = mainEngine.getContract(symbol);
    if (contract == null) {
      log("cancel subscribe error,cannot find contract symbol:{}", symbol);
      return;
    }
    Set<String> symbols = new HashSet<>();
    symbols.add(symbol);
    SubscribeRequest request = new SubscribeRequest(symbols);
    mainEngine.cancelSubscribe(DEFAULT_GATEWAY, request);
  }

  public long sendOrder(String algoTemplate, String symbol, Direction direction,
      Double price, int volume, OrderType orderType) {
    Contract contract = mainEngine.getContract(symbol);
    if (contract == null) {
      log("send order error，cannot find contract symbol:{}", symbol);
      return -1L;
    }
    if (symbol == null) {
      throw new TigerQuantException("send order symbol is null");
    }
    if (direction == null) {
      throw new TigerQuantException("send order direction is null");
    }
    if (price!=null && price <= 0) {
      throw new TigerQuantException("send order price is less than zero");
    }
    if (volume <= 0) {
      throw new TigerQuantException("send order volume is less than zero");
    }
    if (orderType == null) {
      throw new TigerQuantException("send order orderType is null");
    }

    if (contract.getMinTick() != null) {
      Integer lotSize = contract.getLotSize() != null ? contract.getLotSize() : 1;
      if (volume % lotSize != 0) {
        log("send order error,volume:{} is not multiple of lotSize:{}", volume, contract.getLotSize());
        throw new TigerQuantException("send order error,volume:" + volume + " is not multiple of lotSize :" + lotSize);
      }
      double minTick = contract.getMinTick() != null ? contract.getMinTick() : 1;
      if (price - Math.floor(price / minTick) * minTick >= 0.000001) {
        log("send order error,price:{} is not multiple of minTick:{}", volume, contract.getMinTick());
        throw new TigerQuantException("send order error,");
      }
    }

    OrderRequest request =
        new OrderRequest(symbol, contract.getExchange(), direction.name(), orderType.name(), volume, price);

    long orderId = mainEngine.sendOrder(DEFAULT_GATEWAY, request);
    orderIdAlgoTemplateMap.put(orderId, algoTemplate);
    return orderId;
  }

  public void cancelOrder(Long orderId) {
    if (orderId == null) {
      throw new TigerQuantException("cancel order orderId is null");
    }
    Order order = mainEngine.getOrder(orderId);
    if (order == null) {
      log("cancel order error，cannot find orderId：{}", orderId);
      return;
    }
    ModifyRequest req = order.createCancelRequest();
    mainEngine.cancelOrder(order.getGatewayName(), req);
  }

  public List<Bar> getBars(String symbol, BarType barType, int limit) {
    List<String> symbols = new ArrayList<>();
    symbols.add(symbol);
    Map<String, List<Bar>> bars = mainEngine.getBars(DEFAULT_GATEWAY, symbols, barType, limit);
    if (bars == null || bars.isEmpty()) {
      return null;
    }
    return bars.get(symbol);
  }

  public Map<String, List<Bar>> getBars(List<String> symbols, BarType barType, int limit) {
    Map<String, List<Bar>> bars = mainEngine.getBars(DEFAULT_GATEWAY, symbols, barType, limit);
    if (bars == null || bars.isEmpty()) {
      return null;
    }
    return bars;
  }

  public Tick getTick(String symbol) {
    if (symbol == null || symbol.isEmpty()) {
      throw new TigerQuantException("get tick symbol is null");
    }
    Tick tick = mainEngine.getTick(symbol);
    if (tick == null) {
      log("get tick error，cannot find symbol：{}", symbol);
    }
    return tick;
  }

  public Contract getContract(String symbol) {
    if (symbol == null || symbol.isEmpty()) {
      throw new TigerQuantException("get contract symbol is null");
    }
    Contract contract = mainEngine.getContract(symbol);
    if (contract == null) {
      log("get contract error，cannot find contract symbol：{}", symbol);
    }
    return contract;
  }

  public Asset getAsset() {
    return mainEngine.getAsset();
  }

  public Position getPosition(String identifier) {
    return mainEngine.getPosition(identifier);
  }

  public List<Position> getAllPositions() {
    return mainEngine.getAllPositions();
  }

  public List<Order> getAllActiveOrders() {
    return mainEngine.getAllActiveOrders(null);
  }

  public Map<String, Object> getSettings(String strategyName) {
    return algoSettings.get(strategyName);
  }

  public void log(String format, Object... args) {
    mainEngine.log(format, args);
  }
}

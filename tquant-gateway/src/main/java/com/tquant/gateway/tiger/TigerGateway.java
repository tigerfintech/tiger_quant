package com.tquant.gateway.tiger;

import com.tquant.core.TigerQuantException;
import com.tquant.core.config.ConfigLoader;
import com.tquant.gateway.api.OptionApi;
import com.tquant.gateway.api.QuoteApi;
import com.tquant.gateway.api.TradeApi;
import com.tquant.core.event.EventEngine;
import com.tquant.core.core.Gateway;
import com.tquant.core.model.data.Asset;
import com.tquant.core.model.data.Bar;
import com.tquant.core.model.data.Contract;
import com.tquant.core.model.data.Order;
import com.tquant.core.model.data.Position;
import com.tquant.core.model.enums.BarType;
import com.tquant.core.model.request.ModifyRequest;
import com.tquant.core.model.request.OrderRequest;
import com.tquant.core.model.request.SubscribeRequest;
import com.tquant.storage.dao.ContractDAO;
import com.tquant.core.util.QuantUtils;
import com.tigerbrokers.stock.openapi.client.config.ClientConfig;
import com.tigerbrokers.stock.openapi.client.https.client.TigerHttpClient;
import com.tigerbrokers.stock.openapi.client.socket.WebSocketClient;
import com.tigerbrokers.stock.openapi.client.struct.enums.RightOption;
import com.tigerbrokers.stock.openapi.client.struct.enums.SecType;
import com.tigerbrokers.stock.openapi.client.struct.enums.Subject;
import com.tigerbrokers.stock.openapi.client.util.ApiLogger;
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
public class TigerGateway extends Gateway {

  public static final String GATEWAY_NAME = "TigerGateway";

  private TigerHttpClient serverClient;
  private WebSocketClient socketClient;
  private TigerConfig config;
  private static Boolean contractLoadEnabled = (Boolean) ConfigLoader.GLOBAL_SETTINGS.get("contract.load.enable");
  private static Boolean subscribeEnabled = (Boolean) ConfigLoader.GLOBAL_SETTINGS.get("subscribe.enable");
  private TradeApi tradeApi;
  private QuoteApi quoteApi;
  private OptionApi optionApi;
  private TigerSubscribeApi subscribeApi;

  private Map<String, Contract> contractDict = new HashMap<>();
  private Map<Long, Order> orderDict = new HashMap<>();
  private Map<Long, Order> openOrderDict = new HashMap<>();
  private Map<String, Position> positionDict = new HashMap<>();
  private Map<String, Asset> assetDict = new HashMap<>();

  private ContractDAO contractDAO = new ContractDAO();

  public TigerGateway(EventEngine eventEngine) {
    super(eventEngine, GATEWAY_NAME);
    init();
  }

  private void init() {
    this.config = TigerConfigLoader.loadTigerConfig();
    if (this.config == null) {
      throw new TigerQuantException("config is null");
    }
    ApiLogger.setEnabled(config.isApiLogEnable(), config.getApiLogPath());
    ClientConfig clientConfig = ClientConfig.DEFAULT_CONFIG;
    clientConfig.privateKey = config.getPrivateKey();
    clientConfig.tigerId = config.getTigerId();
    clientConfig.defaultAccount = config.getAccount();
    serverClient =  TigerHttpClient.getInstance().clientConfig(clientConfig);
    try {
      subscribeApi = new TigerSubscribeApi(this);
      socketClient = WebSocketClient.getInstance().clientConfig(clientConfig).apiComposeCallback(subscribeApi);
    } catch (Exception e) {
      throw new TigerQuantException("build socket client exception:" + e.getMessage());
    }
    tradeApi = new TigerTradeApi(serverClient);
    quoteApi = new TigerQuoteApi(serverClient);
    optionApi = new TigerOptionApi(serverClient);
  }

  public TradeApi getTradeApi() {
    return tradeApi;
  }

  public QuoteApi getQuoteApi() {
    return quoteApi;
  }

  public OptionApi getOptionApi() {
    return optionApi;
  }

  @Override
  public void connect() {
    SecType[] secTypes = new SecType[] {SecType.STK, SecType.FUT};
    queryContract();
    for (SecType secType : secTypes) {
      queryAsset(secType);
      queryOrder(secType);
      queryPosition(secType);
      queryAccount();
    }
    quoteApi.grabQuotePermission();

    if (!socketClient.isConnected()) {
      socketClient.connect();
    }
  }

  @Override
  public void stop() {
    cancelAllActiveOrders();
    socketClient.disconnect();
  }

  private void cancelAllActiveOrders() {
    List<Order> activeOrders = tradeApi.getOpenOrders(SecType.FUT.name());
    if (activeOrders != null) {
      activeOrders.forEach(order -> {
        log("cancel active order: {}", order);
        tradeApi.cancelOrder(order.getId());
      });
    }
  }

  private void queryOrder(SecType secType) {
    List<Order> orders = tradeApi.getOrders(secType.name());
    if (orders == null) {
      return;
    }
    for (Order order : orders) {
      this.orderDict.put(order.getId(), order);
      if (order.isActive()) {
        this.openOrderDict.put(order.getId(), order);
      }
      onOrder(order);
    }
  }

  private void queryPosition(SecType secType) {
    this.positionDict = tradeApi.getPositions(secType.name());
    for (Position position : positionDict.values()) {
      onPosition(position);
    }
  }

  private void queryAccount() {
  }

  private void queryAsset(SecType secType) {
    Asset asset = tradeApi.getAsset();
    this.assetDict.put(asset.getAccount(), asset);
    onAsset(asset);
  }

  private void queryContract() {
    if (contractLoadEnabled == null || !contractLoadEnabled) {
      return;
    }
    log("contract loading......");
    List<Contract> contracts = contractDAO.queryContracts();
    for (Contract contract : contracts) {
      contractDict.put(contract.getIdentifier(), contract);
      onContract(contract);
    }
    log("contract loaded......");
  }

  @Override
  public void subscribe(SubscribeRequest request) {
    if (subscribeEnabled == null || !subscribeEnabled) {
      return;
    }
    socketClient.subscribe(Subject.OrderStatus);
    socketClient.subscribe(Subject.Asset);
    socketClient.subscribe(Subject.Position);
    socketClient.subscribeQuote(request.getSymbols());
  }

  @Override
  public void cancelSubscribe(SubscribeRequest request) {
    socketClient.cancelSubscribeQuote(request.getSymbols());
  }

  @Override
  public long sendOrder(OrderRequest request) {
    Contract contract = contractDict.get(request.getSymbol());
    if (request.getPrice() == null) {
      return tradeApi.placeMarketOrder(contract, request.getDirection(), request.getQuantity());
    } else {
      return tradeApi.placeLimitOrder(contract, request.getDirection(), request.getPrice(), request.getQuantity());
    }
  }

  @Override
  public void cancelOrder(ModifyRequest request) {
    if (request == null) {
      throw new TigerQuantException("cancel order request is null");
    }
    if (request.getOrderId() <= 0) {
      throw new TigerQuantException("cancel order request orderId <= 0");
    }
    tradeApi.cancelOrder(request.getOrderId());
  }

  @Override
  public void modifyOrder(ModifyRequest request) {
    if (request == null) {
      throw new TigerQuantException("modify order request is null");
    }
    if (request.getLimitPrice() == null || request.getLimitPrice() <= 0) {
      throw new TigerQuantException("modify param error,price " + request.getLimitPrice() + " illegal");
    }
    if (request.getQuantity() == null || request.getQuantity() <= 0) {
      throw new TigerQuantException("modify param error, quantity " + request.getQuantity() + " illegal");
    }
    tradeApi.modifyOrder(request.getOrderId(), request.getLimitPrice(), request.getQuantity());
  }

  @Override
  public Map<String, List<Bar>> getBars(List<String> symbols, BarType barType, int limit) {
    if (symbols == null || symbols.isEmpty()) {
      throw new TigerQuantException("get bars symbols is null");
    }
    if (barType == null) {
      throw new TigerQuantException("get bars barType is null");
    }
    if (limit <= 0) {
      throw new TigerQuantException("get bars limit <= 0");
    }
    List<String> stockSymbols = new ArrayList<>();
    List<String> futureSymbols = new ArrayList<>();
    for (String symbol : symbols) {
      if (QuantUtils.isFuturesSymbol(symbol)) {
        futureSymbols.add(symbol);
      } else {
        stockSymbols.add(symbol);
      }
    }
    Map<String, List<Bar>> bars = new HashMap<>();
    if (!stockSymbols.isEmpty()) {
      bars.putAll(quoteApi.getBars(stockSymbols, barType, RightOption.br, limit));
    }
    if (!futureSymbols.isEmpty()) {
      bars.putAll(quoteApi.getFuturesBars(futureSymbols, barType, limit));
    }
    return bars;
  }
}

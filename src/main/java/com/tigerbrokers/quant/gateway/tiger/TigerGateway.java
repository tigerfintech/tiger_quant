package com.tigerbrokers.quant.gateway.tiger;

import com.tigerbrokers.quant.TigerQuantException;
import com.tigerbrokers.quant.api.OptionApi;
import com.tigerbrokers.quant.api.QuoteApi;
import com.tigerbrokers.quant.api.TradeApi;
import com.tigerbrokers.quant.config.ConfigLoader;
import com.tigerbrokers.quant.config.TigerConfig;
import com.tigerbrokers.quant.event.EventEngine;
import com.tigerbrokers.quant.gateway.Gateway;
import com.tigerbrokers.quant.model.data.Asset;
import com.tigerbrokers.quant.model.data.Bar;
import com.tigerbrokers.quant.model.data.Contract;
import com.tigerbrokers.quant.model.data.Order;
import com.tigerbrokers.quant.model.data.Position;
import com.tigerbrokers.quant.model.enums.BarType;
import com.tigerbrokers.quant.model.request.ModifyRequest;
import com.tigerbrokers.quant.model.request.OrderRequest;
import com.tigerbrokers.quant.model.request.SubscribeRequest;
import com.tigerbrokers.quant.storage.dao.ContractDAO;
import com.tigerbrokers.quant.util.QuantUtils;
import com.tigerbrokers.stock.openapi.client.https.client.TigerHttpClient;
import com.tigerbrokers.stock.openapi.client.socket.ApiAuthentication;
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
    this.config = ConfigLoader.loadTigerConfig();
    if (this.config == null) {
      throw new TigerQuantException("config is null");
    }
    ApiLogger.setEnabled(config.isApiLogEnable(), config.getApiLogPath());
    serverClient = new TigerHttpClient(config.getServerUrl(), config.getTigerId(), config.getPrivateKey());
    try {
      subscribeApi = new TigerSubscribeApi(this);
      socketClient = WebSocketClient.getInstance().url(config.getSocketUrl()).
          authentication(ApiAuthentication.build(config.getTigerId(), config.getPrivateKey()))
          .apiComposeCallback(subscribeApi);
    } catch (Exception e) {
      throw new TigerQuantException("build socket client exception:" + e.getMessage());
    }
    tradeApi = new TigerTradeApi(serverClient, config.getAccount());
    quoteApi = new TigerQuoteApi(serverClient);
    optionApi = new TigerOptionApi(serverClient);
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
    String grabQuoteResult = quoteApi.grabQuotePermission();
    log("grabQuotePermission: {}", grabQuoteResult);
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
    List<Order> activeOrders = tradeApi.getOpenOrders(SecType.FUT);
    if (activeOrders != null) {
      activeOrders.forEach(order -> {
        log("cancel active order: {}", order);
        tradeApi.cancelOrder(order.getId());
      });
    }
  }

  private void queryOrder(SecType secType) {
    List<Order> orders = tradeApi.getOrders(secType);
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
    this.positionDict = tradeApi.getPositions(secType);
    for (Position position : positionDict.values()) {
      onPosition(position);
    }
  }

  private void queryAccount() {
  }

  private void queryAsset(SecType secType) {
    Asset asset = tradeApi.getAsset(secType);
    this.assetDict.put(asset.getAccount(), asset);
    onAsset(asset);
  }

  private void queryContract() {
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
  public String sendOrder(OrderRequest request) {
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

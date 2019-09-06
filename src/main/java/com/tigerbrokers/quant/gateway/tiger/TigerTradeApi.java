package com.tigerbrokers.quant.gateway.tiger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tigerbrokers.quant.TigerQuantException;
import com.tigerbrokers.quant.api.TradeApi;
import com.tigerbrokers.quant.model.data.Asset;
import com.tigerbrokers.quant.model.data.Contract;
import com.tigerbrokers.quant.model.data.Order;
import com.tigerbrokers.quant.model.data.Position;
import com.tigerbrokers.stock.openapi.client.constant.ApiServiceType;
import com.tigerbrokers.stock.openapi.client.https.client.TigerHttpClient;
import com.tigerbrokers.stock.openapi.client.https.domain.contract.item.ContractItem;
import com.tigerbrokers.stock.openapi.client.https.domain.contract.model.ContractsModel;
import com.tigerbrokers.stock.openapi.client.https.domain.future.item.FutureExchangeItem;
import com.tigerbrokers.stock.openapi.client.https.request.TigerHttpRequest;
import com.tigerbrokers.stock.openapi.client.https.request.contract.ContractsRequest;
import com.tigerbrokers.stock.openapi.client.https.request.future.FutureContractByExchCodeRequest;
import com.tigerbrokers.stock.openapi.client.https.request.future.FutureExchangeRequest;
import com.tigerbrokers.stock.openapi.client.https.request.quote.QuoteSymbolRequest;
import com.tigerbrokers.stock.openapi.client.https.response.TigerHttpResponse;
import com.tigerbrokers.stock.openapi.client.https.response.contract.ContractResponse;
import com.tigerbrokers.stock.openapi.client.https.response.future.FutureBatchContractResponse;
import com.tigerbrokers.stock.openapi.client.https.response.future.FutureExchangeResponse;
import com.tigerbrokers.stock.openapi.client.https.response.quote.QuoteSymbolResponse;
import com.tigerbrokers.stock.openapi.client.struct.enums.ActionType;
import com.tigerbrokers.stock.openapi.client.struct.enums.Currency;
import com.tigerbrokers.stock.openapi.client.struct.enums.Market;
import com.tigerbrokers.stock.openapi.client.struct.enums.OrderType;
import com.tigerbrokers.stock.openapi.client.struct.enums.SecType;
import com.tigerbrokers.stock.openapi.client.struct.enums.TimeInForce;
import com.tigerbrokers.stock.openapi.client.util.builder.AccountParamBuilder;
import com.tigerbrokers.stock.openapi.client.util.builder.TradeParamBuilder;
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
public class TigerTradeApi implements TradeApi {

  private TigerHttpClient client;
  private String account;

  public TigerTradeApi(TigerHttpClient client, String account) {
    this.client = client;
    this.account = account;
  }

  @Override
  public int getOrderId() {
    TigerHttpRequest request = new TigerHttpRequest(ApiServiceType.ORDER_NO);

    String bizContent = TradeParamBuilder.instance().account(account).buildJson();
    request.setBizContent(bizContent);

    TigerHttpResponse response = client.execute(request);
    if (!response.isSuccess()) {
      throw new RuntimeException("response error,code:" + response.getCode() + ",message:" + response.getMessage());
    }
    return JSONObject.parseObject(response.getData()).getIntValue("orderId");
  }

  @Override
  public String placeLimitOrder(Contract contract, String actionType, Double price, int quantity) {
    TigerHttpRequest request = new TigerHttpRequest(ApiServiceType.PLACE_ORDER);

    String bizContent = TradeParamBuilder.instance()
        .account(account)
        .orderId(getOrderId())
        .symbol(contract.getSymbol())
        .secType(SecType.valueOf(contract.getSecType()))
        .market(contract.getMarket() != null ? Market.valueOf(contract.getMarket()) : null)
        .currency(contract.getCurrency() != null ? Currency.valueOf(contract.getCurrency()) : null)
        .strike(contract.getStrike() != null ? contract.getStrike() + "" : null)
        .expiry(contract.getExpiry())
        .multiplier(contract.getMultiplier() != null ? Float.parseFloat(contract.getMultiplier() + "") : null)
        .exchange(contract.getExchange())
        .right(contract.getRight())
        .action(ActionType.valueOf(actionType))
        .orderType(OrderType.LMT)
        .limitPrice(price)
        .totalQuantity(quantity)
        .outsideRth(true)
        .timeInForce(TimeInForce.DAY)
        .buildJson();

    request.setBizContent(bizContent);

    TigerHttpResponse response = client.execute(request);
    if (!response.isSuccess()) {
      throw new RuntimeException("response error,code:" + response.getCode() + ",message:" + response.getMessage());
    }
    return JSONObject.parseObject(response.getData()).getString("id");
  }

  @Override
  public String placeMarketOrder(Contract contract, String actionType, int quantity) {
    TigerHttpRequest request = new TigerHttpRequest(ApiServiceType.PLACE_ORDER);

    String bizContent = TradeParamBuilder.instance()
        .account(account)
        .orderId(getOrderId())
        .symbol(contract.getSymbol())
        .secType(SecType.valueOf(contract.getSecType()))
        .market(contract.getMarket() != null ? Market.valueOf(contract.getMarket()) : null)
        .currency(contract.getCurrency() != null ? Currency.valueOf(contract.getCurrency()) : null)
        .strike(contract.getStrike() != null ? contract.getStrike() + "" : null)
        .expiry(contract.getExpiry())
        .multiplier(contract.getMultiplier() != null ? Float.parseFloat(contract.getMultiplier() + "") : null)
        .exchange(contract.getExchange())
        .right(contract.getRight())
        .action(ActionType.valueOf(actionType))
        .orderType(OrderType.MKT)
        .totalQuantity(quantity)
        .outsideRth(true)
        .timeInForce(TimeInForce.DAY)
        .buildJson();

    request.setBizContent(bizContent);

    TigerHttpResponse response = client.execute(request);
    if (!response.isSuccess()) {
      throw new RuntimeException("response error,code:" + response.getCode() + ",message:" + response.getMessage());
    }
    return JSONObject.parseObject(response.getData()).getString("id");
  }

  @Override
  public String cancelOrder(long id) {
    TigerHttpRequest request = new TigerHttpRequest(ApiServiceType.CANCEL_ORDER);
    String bizContent = TradeParamBuilder.instance()
        .account(account)
        .id(id)
        .buildJson();
    request.setBizContent(bizContent);

    TigerHttpResponse response = client.execute(request);
    if (!response.isSuccess()) {
      throw new RuntimeException("response error,code:" + response.getCode() + ",message:" + response.getMessage());
    }

    return response.getData();
  }

  @Override
  public String modifyOrder(long id, Double limitPrice, int quantity) {
    TigerHttpRequest request = new TigerHttpRequest(ApiServiceType.MODIFY_ORDER);
    String bizContent = TradeParamBuilder.instance()
        .account(account)
        .id(id)
        .totalQuantity(quantity)
        .limitPrice(limitPrice)
        .outsideRth(false)
        .buildJson();
    request.setBizContent(bizContent);

    TigerHttpResponse response = client.execute(request);
    if (!response.isSuccess()) {
      throw new RuntimeException("response error,code:" + response.getCode() + ",message:" + response.getMessage());
    }

    return response.getData();
  }

  @Override
  public Order getOrder(long id) {
    TigerHttpRequest request = new TigerHttpRequest(ApiServiceType.ORDERS);

    String bizContent = AccountParamBuilder.instance()
        .account(account)
        .id(id)
        .isBrief(false)
        .buildJson();
    request.setBizContent(bizContent);

    TigerHttpResponse response = client.execute(request);
    if (!response.isSuccess()) {
      throw new RuntimeException("response error,code:" + response.getCode() + ",message:" + response.getMessage());
    }
    if (response.getData() == null || response.getData().isEmpty()) {
      throw new RuntimeException("response data is empty");
    }
    if (!response.getData().startsWith("{")) {
      throw new RuntimeException("response data is not in json format");
    }
    JSONObject jsonObject = JSON.parseObject(response.getData());
    Order order = jsonObject.toJavaObject(Order.class);
    order.setGatewayName(TigerGateway.GATEWAY_NAME);
    return order;
  }

  @Override
  public List<Order> getOrders() {
    return getOrderByServiceType(ApiServiceType.ORDERS);
  }

  @Override
  public List<Order> getCancelledOrders() {
    return getOrderByServiceType(ApiServiceType.INACTIVE_ORDERS);
  }

  @Override
  public List<Order> getOpenOrders() {
    return getOrderByServiceType(ApiServiceType.ACTIVE_ORDERS);
  }

  @Override
  public List<Order> getFilledOrders() {
    return getOrderByServiceType(ApiServiceType.FILLED_ORDERS);
  }

  private List<Order> getOrderByServiceType(String serviceType) {
    TigerHttpRequest request = new TigerHttpRequest(serviceType);
    String bizContent = AccountParamBuilder.instance()
        .account(account)
        .isBrief(false)
        .buildJson();
    request.setBizContent(bizContent);

    TigerHttpResponse response = client.execute(request);
    if (!response.isSuccess()) {
      throw new RuntimeException("response error,code:" + response.getCode() + ",message:" + response.getMessage());
    }
    if (response.getData() == null || response.getData().isEmpty()) {
      throw new RuntimeException("response data is empty");
    }
    if (!response.getData().startsWith("{")) {
      throw new RuntimeException("response data is not in json format");
    }
    JSONObject jsonObject = JSONObject.parseObject(response.getData());
    if (jsonObject.isEmpty()) {
      return null;
    }
    JSONArray items = jsonObject.getJSONArray("items");
    if (items.isEmpty()) {
      return null;
    }
    List<Order> orders = items.toJavaList(Order.class);
    for (Order order : orders) {
      order.setGatewayName(TigerGateway.GATEWAY_NAME);
    }
    return orders;
  }

  @Override
  public List<String> getAccounts() {
    TigerHttpRequest request = new TigerHttpRequest(ApiServiceType.ACCOUNTS);
    String bizContent = AccountParamBuilder.instance()
        .account(account)
        .buildJson();
    request.setBizContent(bizContent);

    TigerHttpResponse response = client.execute(request);
    if (!response.isSuccess()) {
      throw new RuntimeException("response error,code:" + response.getCode() + ",message:" + response.getMessage());
    }

    List<String> accounts = new ArrayList<>();
    accounts.add(response.getData());

    return accounts;
  }

  @Override
  public Asset getAsset(SecType secType) {
    if (secType == null || (secType != SecType.FUT && secType != SecType.STK)) {
      throw new TigerQuantException("get asset secType is null");
    }
    TigerHttpRequest request = new TigerHttpRequest(ApiServiceType.ASSETS);
    String bizContent = AccountParamBuilder.instance()
        .account(account)
        .buildJson();
    request.setBizContent(bizContent);

    TigerHttpResponse response = client.execute(request);
    if (!response.isSuccess()) {
      throw new RuntimeException("response error,code:" + response.getCode() + ",message:" + response.getMessage());
    }
    if (response.getData() == null || response.getData().isEmpty()) {
      throw new RuntimeException("response asset is empty");
    }
    JSONObject jsonObject = JSONObject.parseObject(response.getData());
    if (jsonObject == null) {
      throw new TigerQuantException("get assets is null");
    }
    JSONArray assets = jsonObject.getJSONArray("items");
    if (assets == null) {
      throw new TigerQuantException("get assets is null");
    }
    String category = "S";
    if (secType == SecType.FUT) {
      category = "C";
    }

    JSONObject assetObject = assets.getJSONObject(0);
    JSONArray segments = assetObject.getJSONArray("segments");
    for (int i = 0; i < segments.size(); i++) {
      JSONObject segmentObject = segments.getJSONObject(i);
      if (segmentObject.getString("category").equals(category)) {
        assetObject.putAll(segmentObject);
        break;
      }
    }

    Asset asset = assetObject.toJavaObject(Asset.class);
    asset.setMaintenanceMarginReq(assetObject.getDoubleValue("maintMarginReq"));
    return asset;
  }

  @Override
  public Map<String, Position> getPositions() {
    TigerHttpRequest request = new TigerHttpRequest(ApiServiceType.POSITIONS);
    String bizContent = AccountParamBuilder.instance()
        .account(account)
        .currency(Currency.USD)
        .market(Market.US)
        .secType(SecType.STK)
        .buildJson();

    request.setBizContent(bizContent);

    TigerHttpResponse response = client.execute(request);
    if (!response.isSuccess()) {
      throw new RuntimeException("response error,code:" + response.getCode() + ",message:" + response.getMessage());
    }
    Map<String, Position> positions = new HashMap<>();
    if (response.getData() != null && response.getData().startsWith("{")) {
      JSONObject jsonObject = JSON.parseObject(response.getData());
      if (jsonObject != null) {
        JSONArray items = jsonObject.getJSONArray("items");
        if (items != null) {
          for (int i = 0; i < items.size(); i++) {
            Position position = items.getJSONObject(i).toJavaObject(Position.class);
            if (position.getPosition() > 0) {
              position.setDirection("BUY");
            } else {
              position.setPosition(-position.getPosition());
              position.setDirection("SELL");
            }
            positions.put(position.getSymbol(), position);
          }
        }
      }
    }
    return positions;
  }

  @Override
  public List<Contract> getContracts(SecType secType) {
    if (secType == null) {
      throw new TigerQuantException("getContracts secType is null");
    }
    if (secType != SecType.STK && secType != SecType.FUT) {
      throw new TigerQuantException("getContracts secType not supported");
    }

    List<Contract> contracts = new ArrayList<>();
    if (secType == SecType.STK) {
      QuoteSymbolResponse symbolResponse = client.execute(QuoteSymbolRequest.newRequest(Market.US));
      if (!symbolResponse.isSuccess()) {
        throw new TigerQuantException("get symbols is null");
      }
      List<String> symbols = symbolResponse.getSymbols();
      int end = symbols.size();
      int index = 0;
      while (index < end) {
        List<String> subList = symbols.subList(index, index + 50 > end ? end : index + 50);
        ContractResponse response =
            client.execute(ContractsRequest.newRequest(new ContractsModel(account, subList, secType.name())));
        if (!response.isSuccess()) {
          throw new TigerQuantException("get contracts error:" + response.getMessage());
        }
        List<ContractItem> items = response.getItems();
        if (items != null) {
          contracts.addAll(Contract.toContracts(items));
        }
        index += 50;
      }
    } else {
      FutureExchangeResponse exchangeResponse = client.execute(FutureExchangeRequest.newRequest(secType.name()));
      for (FutureExchangeItem item : exchangeResponse.getFutureExchangeItems()) {
        FutureBatchContractResponse contractResponse =
            client.execute(FutureContractByExchCodeRequest.newRequest(item.getCode()));
        contracts.addAll(Contract.toFuturesContracts(contractResponse.getFutureContractItems()));
      }
    }

    if (contracts.isEmpty()) {
      throw new TigerQuantException("get contracts is null");
    }

    return contracts;
  }

  @Override
  public List<Contract> getContracts(List<String> symbols, SecType secType) {
    if (symbols == null || symbols.size() > 50) {
      throw new TigerQuantException("get contracts symbol error");
    }
    if (secType == null) {
      throw new TigerQuantException("get contracts secType error");
    }
    ContractResponse response =
        client.execute(ContractsRequest.newRequest(new ContractsModel(account, symbols, secType.name())));
    if (!response.isSuccess()) {
      throw new TigerQuantException("get contracts error:" + response.getMessage());
    }
    List<ContractItem> items = response.getItems();
    if (items == null || items.isEmpty()) {
      throw new TigerQuantException("get contracts is null");
    }
    List<Contract> contracts = new ArrayList<>();
    contracts.addAll(Contract.toContracts(response.getItems()));
    return contracts;
  }
}

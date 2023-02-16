package com.tquant.gateway.api;

import com.tquant.core.model.data.Asset;
import com.tquant.core.model.data.Contract;
import com.tquant.core.model.data.Order;
import com.tquant.core.model.data.Position;
import java.util.List;
import java.util.Map;

/**
 * Description: trade api
 *
 * @author kevin
 * @date 2019/08/15
 */
public interface TradeApi {

  void setMock(boolean isMock);

  int getOrderId();

  long placeLimitOrder(Contract contract, String direction, Double price, int quantity);

  long placeMarketOrder(Contract contract, String direction, int quantity);

  String cancelOrder(long id);

  String modifyOrder(long id, Double limitPrice, int quantity);

  Order getOrder(long id);

  List<Order> getOpenOrders();

  List<Order> getOpenOrders(String secType);

  List<Order> getFilledOrders(String secType);

  List<Order> getCancelledOrders(String secType);

  List<Order> getOrders(String secType);

  List<String> getAccounts();

  Asset getAsset();

  Asset getAsset(String secType);

  Map<String, Position> getPositions(String secType);

  List<Contract> getContracts(String secType);

  List<Contract> getContracts(List<String> symbols, String secType);
}

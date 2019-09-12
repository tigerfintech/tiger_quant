package com.tigerbrokers.quant.api;

import com.tigerbrokers.quant.model.data.Asset;
import com.tigerbrokers.quant.model.data.Contract;
import com.tigerbrokers.quant.model.data.Order;
import com.tigerbrokers.quant.model.data.Position;
import com.tigerbrokers.stock.openapi.client.struct.enums.SecType;
import java.util.List;
import java.util.Map;

/**
 * Description: trade api
 *
 * @author kevin
 * @date 2019/08/15
 */
public interface TradeApi {

  int getOrderId();

  String placeLimitOrder(Contract contract, String direction, Double price, int quantity);

  String placeMarketOrder(Contract contract, String direction, int quantity);

  String cancelOrder(long id);

  String modifyOrder(long id, Double limitPrice, int quantity);

  Order getOrder(long id);

  List<Order> getOpenOrders(SecType secType);

  List<Order> getFilledOrders(SecType secType);

  List<Order> getCancelledOrders(SecType secType);

  List<Order> getOrders(SecType secType);

  List<String> getAccounts();

  Asset getAsset(SecType secType);

  Map<String, Position> getPositions(SecType secType);

  List<Contract> getContracts(SecType secType);

  List<Contract> getContracts(List<String> symbols, SecType secType);
}

package com.tquant.core.model.data;

import com.alibaba.fastjson.JSONObject;
import com.tquant.core.model.request.ModifyRequest;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/08/15
 */
@Data
public class Order {

  private long id;
  private String gatewayName;
  private String name;
  private String account;
  private String symbol;
  private String identifier;
  private String direction;
  private String orderType;
  private double price;
  private long volume;
  private double averageFilledPrice;
  private long filledVolume;
  private String status;
  private String remark;
  private long time;

  private static final Set<String> activeStatus = new HashSet<>();

  {
    activeStatus.add("Initial");
    activeStatus.add("PendingSubmit");
    activeStatus.add("Submitted");
    activeStatus.add("PendingCancel");
  }

  public Order() {
  }

  public Order(long id, String symbol, double price, long volume, String direction, String orderType) {
    this.id = id;
    this.symbol = symbol;
    this.price = price;
    this.volume = volume;
    this.direction = direction;
    this.orderType = orderType;
  }

  public ModifyRequest createCancelRequest() {
    ModifyRequest request = new ModifyRequest();
    request.setOrderId(id);
    return request;
  }

  public boolean isActive() {
    return status != null && activeStatus.contains(status);
  }

  public void jsonToOrder(JSONObject jsonObject) {
    this.gatewayName = "TigerGateway";
    this.id = jsonObject.getLongValue("id");
    this.identifier = jsonObject.getString("identifier");
    this.name = jsonObject.getString("name");
    this.account = jsonObject.getString("account");
    this.symbol = jsonObject.getString("symbol");
    this.direction = jsonObject.getString("action");
    this.orderType = jsonObject.getString("orderType");
    this.price = jsonObject.getDoubleValue("limitPrice");
    this.volume = jsonObject.getLongValue("totalQuantity");
    this.averageFilledPrice = jsonObject.getDoubleValue("avgFillPrice");
    this.filledVolume = jsonObject.getLongValue("filledQuantity");
    this.remark = jsonObject.getString("remark");
    this.status = jsonObject.getString("status");
    this.time = jsonObject.getLongValue("openTime");
  }
}

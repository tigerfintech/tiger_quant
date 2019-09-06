package com.tigerbrokers.quant.model.data;

import lombok.Data;

/**
 * Description:Trade detail contains information of a fill of an order.
 * One order can have several trade fills.
 *
 * @author kevin
 * @date 2019/08/16
 */
@Data
public class Trade {

  private Long orderId;
  private String account;
  private String symbol;
  private String exchange;
  private String direction;
  private String orderType;
  private double price;
  private int volume;
  private String status;
  private String time;
}

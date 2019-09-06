package com.tigerbrokers.quant.model.data;

import lombok.Data;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/08/15
 */
@Data
public class Position {

  private String account;
  private String symbol;
  private String identifier;
  private String exchange;
  private String direction;
  private int position;
  private double averageCost;
  private double marketPrice;
  private double marketValue;
  private double realizedPnl;
  private double unrealizedPnl;

  public void updatePosition(Position position) {
    if (position.getExchange() != null) {
      this.exchange = position.getExchange();
    }
    if (position.getDirection() != null) {
      this.direction = position.getDirection();
    }
    if (position.getPosition() > 0) {
      this.position = position.getPosition();
    }
    if (position.getAverageCost() > 0) {
      this.averageCost = position.getAverageCost();
    }
    if (position.getMarketPrice() > 0) {
      this.marketPrice = position.getMarketPrice();
    }
    if (position.getMarketValue() > 0) {
      this.marketValue = position.getMarketValue();
    }
    if (position.getRealizedPnl() > 0) {
      this.realizedPnl = position.getRealizedPnl();
    }
    if (position.getUnrealizedPnl() > 0) {
      this.unrealizedPnl = position.getUnrealizedPnl();
    }
  }
}

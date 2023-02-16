package com.tquant.core.model.data;

import com.tquant.core.model.enums.SecType;
import lombok.Data;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/08/15
 */
@Data
public class Position {

  private Contract contract;
  private String account;
  private String symbol;
  private String secType;
  private String right;
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
    if (position.getSecType() != null) {
      this.secType = position.getSecType();
    }
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

  public boolean isStock() {
    return secType != null && secType.equalsIgnoreCase(SecType.STK.name());
  }

  public boolean isOption() {
    return secType != null && secType.equalsIgnoreCase(SecType.OPT.name());
  }

  public boolean isOption(String right) {
    return secType != null && secType.equalsIgnoreCase(SecType.OPT.name()) && getRight().equalsIgnoreCase(right);
  }

  public boolean isOptionCall() {
    return isOption("CALL");
  }

  public boolean isOptionPut() {
    return isOption("PUT");
  }

}

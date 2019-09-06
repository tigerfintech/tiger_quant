package com.tigerbrokers.quant.model.data;

import lombok.Data;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/08/15
 */
@Data
public class Asset {

  private String account;
  private String capability;
  private double netLiquidation;
  private double availableFunds;
  private double initMarginReq;
  private double maintenanceMarginReq;
  private double buyingPower;
  private double grossPositionValue;
  private double realizedPnL;
  private double unrealizedPnL;

  public void updateAsset(Asset asset) {
    if (asset.getNetLiquidation() > 0) {
      this.netLiquidation = asset.getNetLiquidation();
    }
    if (asset.getAvailableFunds() > 0) {
      this.availableFunds = asset.getAvailableFunds();
    }
    if (asset.getInitMarginReq() > 0) {
      this.initMarginReq = asset.getInitMarginReq();
    }
    if (asset.getMaintenanceMarginReq() > 0) {
      this.maintenanceMarginReq = asset.getMaintenanceMarginReq();
    }
    if (asset.getBuyingPower() > 0) {
      this.buyingPower = asset.getBuyingPower();
    }
    if (asset.getGrossPositionValue() > 0) {
      this.grossPositionValue = asset.getGrossPositionValue();
    }
    if (asset.getRealizedPnL() > 0) {
      this.realizedPnL = asset.getRealizedPnL();
    }
    if (asset.getUnrealizedPnL() > 0) {
      this.unrealizedPnL = asset.getUnrealizedPnL();
    }
  }
}

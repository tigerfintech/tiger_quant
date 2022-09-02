package com.tquant.core.model.enums;

/**
 * Description:
 * Created by lijiawen on 2018/12/27.
 */
public enum StockStatus {

  UNKNOWN(-1D, "未知"), NORMAL(0D, "正常"), HALTED(3.0D, "停牌"), DELIST(4.0D, "退市"), NEW(7.0D, "新股"), ALTER(8.0D, "变更");

  private Double value;

  private String desc;

  StockStatus(Double value, String desc) {
    this.value = value;
    this.desc = desc;
  }

  public Double getValue() {
    return value;
  }

  public String getDesc() {
    return desc;
  }
}

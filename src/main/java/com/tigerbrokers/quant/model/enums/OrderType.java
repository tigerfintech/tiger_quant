package com.tigerbrokers.quant.model.enums;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/09/05
 */
public enum OrderType {
  /**
   * market order
   */
  MKT("MKT", "市价"),
  /**
   * limit order
   */
  LMT("LMT", "限价");

  private String type;
  private String desc;

  OrderType(String type, String desc) {
    this.type = type;
    this.desc = desc;
  }

  public String getType() {
    return type;
  }

  public String getDesc() {
    return desc;
  }
}

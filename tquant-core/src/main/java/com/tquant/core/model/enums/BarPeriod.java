package com.tquant.core.model.enums;

/**
 * Description:
 *
 * @author kevin
 * @date 2022/08/02
 */
public enum BarPeriod {
  day("day"), week("week"), month("month"), year("year"), min1("1min"), min5("5min"),
  min15("15min"), min30("30min"), min60("60min");

  private String value;

  BarPeriod(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}

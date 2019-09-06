package com.tigerbrokers.quant.model.enums;

import com.tigerbrokers.stock.openapi.client.struct.enums.FutureKType;
import com.tigerbrokers.stock.openapi.client.struct.enums.KType;
import java.util.HashSet;
import java.util.Set;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/09/05
 */
public enum BarType {
  min1("1min"), min3("3min"), min5("5min"), min10("10min"),
  min15("15min"), min30("30min"), min45("45min"), min60("60min"),
  hour2("2hour"), hour3("3hour"), hour4("4hour"), hour6("6hour"),
  day("day"), week("week"), month("month"), year("year");

  private static Set<BarType> stockBarType = new HashSet<>();

  static {
    stockBarType.add(min1);
    stockBarType.add(min5);
    stockBarType.add(min15);
    stockBarType.add(min30);
    stockBarType.add(min60);
    stockBarType.add(day);
    stockBarType.add(week);
    stockBarType.add(month);
    stockBarType.add(year);
  }

  private String value;

  BarType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public KType toKType() {
    return KType.valueOf(this.name());
  }

  public FutureKType toFutureKType() {
    return FutureKType.valueOf(this.name());
  }

  public boolean isStockBarType() {
    return stockBarType.contains(this);
  }
}

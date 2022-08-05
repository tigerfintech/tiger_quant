package com.tquant.core.model.data;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Description:
 *
 * @author kevin
 * @date 2022/08/05
 */
public class StockData extends LinkedHashMap<String, Double> implements Comparable<StockData> {

  /**
   * 股票代码
   */
  public String symbol;

  /**
   * 股票名称
   */
  public String name;

  /**
   * 数据时间点
   */
  public Date date;

  /**
   * 属性值
   */
  public Map<String, String> attribute = Maps.newHashMap();

  public StockData() {
  }

  public StockData(String symbol) {
    this.symbol = symbol;
  }

  public StockData(Map<String, Double> map) {
    this.putAll(map);
  }

  public void attr(String key, String val) {
    attribute.put(key, val);
  }

  public String attr(String key) {
    return attribute.get(key);
  }

  @Override
  public String toString() {
    return "StockData{" +
        "symbol='" + symbol + '\'' +
        ", name='" + name + '\'' +
        ", date=" + date +
        ", stockData=" + JSON.toJSONString(this) +
        ", stockAttribute= " + JSON.toJSONString(attribute) +
        '}';
  }

  @Override
  public int compareTo(StockData stockData) {
    int compare = this.symbol.compareTo(stockData.symbol);
    if (compare != 0) {
      return compare;
    }
    return this.date.compareTo(stockData.date);
  }
}

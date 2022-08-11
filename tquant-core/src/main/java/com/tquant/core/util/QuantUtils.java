package com.tquant.core.util;

import com.tquant.core.model.data.StockData;
import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/09/05
 */
public class QuantUtils {

  private static final Pattern FUTURE_SYMBOL_PATTERN = Pattern.compile("[A-Z]{2,4}([0-9]{4}|main){1}");

  public static boolean isFuturesSymbol(String symbol) {
    if (symbol == null || symbol.isEmpty()) {
      return false;
    }
    if (FUTURE_SYMBOL_PATTERN.matcher(symbol).matches()) {
      return true;
    }
    return false;
  }

  /**
   * 将StockDatas的一列值转化成double[]
   * @param stockDatas stock data list
   * @param column column
   * @return return
   */
  public static double[] toDoubleArray(List<? extends StockData> stockDatas,String column){
    List<Double> values = stockDatas.parallelStream().map(stockData -> stockData.attr(column)).collect(Collectors.toList());
    double[] doubles = QuantUtils.toDoubleArray(values);
    return doubles;
  }

  public static boolean addDoubleArrayToList(double[] values,List<? extends StockData> stockDatas,String column){
    if(values.length == stockDatas.size()){
      for(int i =0 ;i<values.length;i++){
        stockDatas.get(i).attr(column,new BigDecimal(values[i]).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
      }
      return true;
    }else{
      return false;
    }
  }

  public static double[] toDoubleArray(List<Double> values){
    double[] array = new double[values.size()];
    for(int i=0;i<values.size();i++){
      array[i] = values.get(i);
    }
    return array;
  }
}

package com.tigerbrokers.quant.util;

import java.util.regex.Pattern;

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
}

package com.tquant.algorithm.algo;

import com.tquant.core.indicators.Indicators;
import com.tquant.core.model.data.Bar;
import com.tquant.core.model.enums.BarType;
import com.tquant.storage.dao.BarDAO;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Description:
 *
 * @author kevin
 * @date 2022/08/10
 */
public class SmaIndicatorTest {

  public static void main(String[] args) {
    BarDAO barDAO = new BarDAO();
    List<Bar> bars = barDAO.queryBar("AAPL", BarType.day.getValue(), LocalDateTime.of(2018, 1, 1, 0, 0),
        LocalDateTime.of(2018, 1, 31, 0, 0));
    System.out.println(bars.size());

    Indicators indicators = new Indicators();
    double[] closes = bars.stream().mapToDouble(bar -> bar.getClose()).toArray();
    double[] shortSma = indicators.sma(closes, 5);
    double[] longSma = indicators.sma(closes, 10);

    System.out.println(closes[0] + "," + closes[1]);
    System.out.println(shortSma[20] + "," + longSma[20]);
  }
}

package com.tigerbrokers.quant.util;

import com.tigerbrokers.quant.TigerQuantException;
import com.tigerbrokers.quant.model.data.Bar;
import com.tigerbrokers.quant.model.data.Tick;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description:
 * 1. generating 1 minute bar data from tick data
 * 2. generating x minute bar/x hour bar data from 1 minute data
 * Notice:
 * 1. for x minute bar, x must be able to divide 60: 2, 3, 5, 6, 10, 15, 20, 30
 * 2. for x hour bar, x can be any number
 *
 * @author kevin
 * @date 2019/08/16
 */
public class BarGenerator {

  class BarData {

    private Bar bar;
    private Bar xminBar;
    private int xmin = 0;
    private double baseAmount = 0;
    private long baseVolume = 0;
  }

  private Map<String, BarData> symbolBars = new HashMap<>();

  private BarHandler barHandler;

  public BarGenerator(BarHandler barHandler) {
    this.barHandler = barHandler;
  }

  public BarGenerator(List<String> symbols, int xmin, BarHandler barHandler) {
    for (String symbol : symbols) {
      BarData barData = symbolBars.get(symbol);
      if (barData == null) {
        barData = new BarData();
        symbolBars.put(symbol, barData);
      }
      barData.xmin = xmin;
    }
    this.barHandler = barHandler;
  }

  public void updateTick(Tick tick) {
    boolean newMinute = false;

    if (tick.getLatestPrice() < 0) {
      return;
    }

    BarData barData = symbolBars.get(tick.getSymbol());
    if (barData == null) {
      barData = new BarData();
      symbolBars.put(tick.getSymbol(), barData);
    }
    Bar bar = barData.bar;
    double baseAmount = barData.baseAmount;
    long baseVolume = barData.baseVolume;

    if (bar == null) {
      newMinute = true;
    } else if (bar.getTime() != null && bar.getTime().getMinute() != tick.getLatestTime().getMinute()) {
      bar.setTime(bar.getTime().withSecond(0).withNano(0));
      barHandler.onBar(bar);
      newMinute = true;
    }
    if (newMinute) {
      bar = new Bar();
      bar.setSymbol(tick.getSymbol());
      bar.setDuration(Duration.ofSeconds(60));
      bar.setOpen(tick.getLatestPrice());
      bar.setClose(tick.getLatestPrice());
      bar.setHigh(tick.getLatestPrice());
      bar.setLow(tick.getLatestPrice());
      if (tick.getAmount() > 0) {
        baseAmount = tick.getAmount();
        barData.baseAmount = baseAmount;
      }
      if (tick.getVolume() > 0) {
        baseVolume = tick.getVolume();
        barData.baseVolume = baseVolume;
      }
      bar.setAmount(0);
      bar.setVolume(0);
      barData.bar = bar;
    } else {
      if (tick.getLatestPrice() > 0) {
        bar.setHigh(Math.max(bar.getHigh(), tick.getLatestPrice()));
        if (bar.getLow() == 0) {
          bar.setLow(tick.getLatestPrice());
        } else {
          bar.setLow(Math.min(bar.getLow(), tick.getLatestPrice()));
        }
        bar.setClose(tick.getLatestPrice());
        if (bar.getOpen() == 0) {
          bar.setOpen(tick.getLatestPrice());
        }
      }
      if (tick.getAmount() > 0) {
        if (baseAmount == 0) {
          baseAmount = tick.getAmount();
          barData.baseAmount = baseAmount;
        } else {
          bar.setAmount(tick.getAmount() - baseAmount);
        }
      }
      if (tick.getVolume() > 0) {
        if (baseVolume == 0) {
          baseVolume = tick.getVolume();
          barData.baseVolume = baseVolume;
        } else {
          bar.setVolume(tick.getVolume() - baseVolume);
        }
      }
      bar.setTime(tick.getLatestTime());
    }
  }

  public void updateBar(Bar bar) {
    BarData barData = symbolBars.get(bar.getSymbol());
    int xmin = barData.xmin;
    Bar xminBar = barData.xminBar;
    if (xmin <= 0) {
      throw new TigerQuantException("xmin must be greater than zero");
    }
    if (xminBar == null) {
      xminBar = new Bar();
      xminBar.setSymbol(bar.getSymbol());
      xminBar.setTime(bar.getTime());
      xminBar.setOpen(bar.getOpen());
      xminBar.setHigh(bar.getHigh());
      xminBar.setLow(bar.getLow());
      barData.xminBar = xminBar;
    } else {
      xminBar.setHigh(Math.max(xminBar.getHigh(), bar.getHigh()));
      xminBar.setLow(Math.min(xminBar.getLow(), bar.getLow()));
    }
    xminBar.setClose(bar.getClose());
    xminBar.setVolume(xminBar.getVolume() + bar.getVolume());
    xminBar.setAmount(xminBar.getAmount() + bar.getAmount());
    if ((bar.getTime().getMinute() + 1) % xmin == 0) {
      xminBar.setDuration(Duration.ofSeconds(60 * xmin));
      xminBar.setTime(xminBar.getTime().withSecond(0).withNano(0));
      barHandler.onBar(xminBar);
      barData.xminBar = null;
    }
  }
}

package com.tquant.core.model.data;

import com.tquant.core.model.enums.BarType;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.ta4j.core.BaseBar;
import org.ta4j.core.num.DecimalNum;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/08/16
 */
public class Bar extends StockData implements BaseData {

  private String symbol;
  private String period;
  private Duration duration;
  private double open;
  private double close;
  private double high;
  private double low;
  private LocalDateTime time;
  private long volume;
  private double amount;


  public BaseBar toBaseBar() {
    return BaseBar.builder(DecimalNum::valueOf, Number.class).timePeriod(duration).endTime(time.atZone(ZoneId.systemDefault()))
        .openPrice(open).highPrice(high).lowPrice(low).closePrice(close).volume(volume)
        .build();
  }

  public static Duration getDurationByKType(BarType barType) {
    if (barType == null) {
      return Duration.ofDays(1);
    }
    switch (barType) {
      case day:
        return Duration.ofDays(1);
      case min1:
        return Duration.ofMinutes(1);
      case min3:
        return Duration.ofMinutes(3);
      case min10:
        return Duration.ofMinutes(10);
      case min45:
        return Duration.ofMinutes(45);
      case hour2:
        return Duration.ofHours(2);
      case hour3:
        return Duration.ofHours(3);
      case hour4:
        return Duration.ofHours(4);
      case hour6:
        return Duration.ofHours(6);
      case min5:
        return Duration.ofMinutes(5);
      case min15:
        return Duration.ofMinutes(15);
      case min30:
        return Duration.ofMinutes(30);
      case min60:
        return Duration.ofMinutes(60);
      case week:
        return Duration.ofDays(7);
      case month:
        return Duration.ofDays(30);
      case year:
        return Duration.ofDays(365);
      default:
        return Duration.ofDays(1);
    }
  }

  public String getSymbol() {
    return symbol;
  }

  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }

  public String getPeriod() {
    return period;
  }

  public void setPeriod(String period) {
    this.period = period;
  }

  public Duration getDuration() {
    return duration;
  }

  public void setDuration(Duration duration) {
    this.duration = duration;
  }

  public double getOpen() {
    return open;
  }

  public void setOpen(double open) {
    this.open = open;
    attr("open", this.open);
  }

  public double getClose() {
    return close;
  }

  public void setClose(double close) {
    this.close = close;
    attr("close", this.close);
  }

  public double getHigh() {
    return high;
  }

  public void setHigh(double high) {
    this.high = high;
    attr("high", this.high);
  }

  public double getLow() {
    return low;
  }

  public void setLow(double low) {
    this.low = low;
    attr("low", this.low);
  }

  public LocalDateTime getTime() {
    return time;
  }

  public void setTime(LocalDateTime time) {
    this.time = time;
  }

  public long getVolume() {
    return volume;
  }

  public void setVolume(long volume) {
    this.volume = volume;
  }

  public double getAmount() {
    return amount;
  }

  public void setAmount(double amount) {
    this.amount = amount;
  }

}

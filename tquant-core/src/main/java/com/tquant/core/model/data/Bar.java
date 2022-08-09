package com.tquant.core.model.data;

import com.tquant.core.model.enums.BarType;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.Data;
import org.ta4j.core.BaseBar;
import org.ta4j.core.num.DecimalNum;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/08/16
 */
@Data
public class Bar implements BaseData {

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
    //return new BaseBar(duration, time.atZone(ZoneId.systemDefault()), open, high, low, close, volume, amount);
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
}

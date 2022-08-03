package com.tigerbrokers.quant.model.data;

import com.tigerbrokers.quant.model.enums.BarType;
import com.tigerbrokers.stock.openapi.client.https.domain.future.item.FutureKlineItem;
import com.tigerbrokers.stock.openapi.client.https.domain.quote.item.KlinePoint;
import com.tigerbrokers.stock.openapi.client.util.SymbolUtil;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.ta4j.core.BaseBar;
import org.ta4j.core.num.PrecisionNum;

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
    return new BaseBar(duration, time.atZone(ZoneId.systemDefault()),
        PrecisionNum.valueOf(open),
        PrecisionNum.valueOf(high),
        PrecisionNum.valueOf(low),
        PrecisionNum.valueOf(close),
        PrecisionNum.valueOf(volume),
        PrecisionNum.valueOf(amount)
    );
  }

  public static List<Bar> toBars(String symbol, BarType barType, List<KlinePoint> klinePoints) {
    List<Bar> bars = new ArrayList<>();
    for (KlinePoint point : klinePoints) {
      Bar bar = new Bar();
      bar.setSymbol(symbol);
      bar.setPeriod(barType.getValue());
      bar.setDuration(getDurationByKType(barType));
      bar.setOpen(point.getOpen());
      bar.setClose(point.getClose());
      bar.setHigh(point.getHigh());
      bar.setLow(point.getLow());
      bar.setVolume(point.getVolume());
      bar.setTime(Instant.ofEpochMilli(point.getTime()).atZone(ZoneId.of(getZoneId(symbol))).toLocalDateTime());
      bars.add(bar);
    }
    return bars;
  }

  private static String getZoneId(String symbol) {
    if(SymbolUtil.isUsStockSymbol(symbol)){
      return  "America/New_York";
    } else {
      return  "Asia/Shanghai";
    }
  }

  public static List<Bar> toFuturesBars(String symbol, BarType barType, List<FutureKlineItem> klineItems) {
    List<Bar> bars = new ArrayList<>();
    for (FutureKlineItem point : klineItems) {
      Bar bar = new Bar();
      bar.setSymbol(symbol);
      bar.setDuration(getDurationByKType(barType));
      bar.setOpen(point.getOpen().doubleValue());
      bar.setClose(point.getClose().doubleValue());
      bar.setHigh(point.getHigh().doubleValue());
      bar.setLow(point.getLow().doubleValue());
      bar.setVolume(point.getVolume());
      bar.setTime(Instant.ofEpochMilli(point.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime());
      bars.add(bar);
    }
    return bars;
  }

  private static Duration getDurationByKType(BarType barType) {
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

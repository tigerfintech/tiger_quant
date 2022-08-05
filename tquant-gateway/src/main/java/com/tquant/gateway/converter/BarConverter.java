package com.tquant.gateway.converter;

import com.tigerbrokers.stock.openapi.client.https.domain.future.item.FutureKlineItem;
import com.tigerbrokers.stock.openapi.client.https.domain.quote.item.KlinePoint;
import com.tigerbrokers.stock.openapi.client.struct.enums.FutureKType;
import com.tigerbrokers.stock.openapi.client.struct.enums.KType;
import com.tigerbrokers.stock.openapi.client.util.SymbolUtil;
import com.tquant.core.model.data.Bar;
import com.tquant.core.model.enums.BarType;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 *
 * @author kevin
 * @date 2022/08/05
 */
public class BarConverter {
  public static List<Bar> toBars(String symbol, BarType barType, List<KlinePoint> klinePoints) {
    List<Bar> bars = new ArrayList<>();
    for (KlinePoint point : klinePoints) {
      Bar bar = new Bar();
      bar.setSymbol(symbol);
      bar.setPeriod(barType.getValue());
      bar.setDuration(Bar.getDurationByKType(barType));
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
      bar.setDuration(Bar.getDurationByKType(barType));
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

  public static KType toKType(BarType barType) {
    return KType.valueOf(barType.name());
  }

  public static FutureKType toFutureKType(BarType barType) {
    return FutureKType.valueOf(barType.name());
  }

}

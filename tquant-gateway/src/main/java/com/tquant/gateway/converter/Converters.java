package com.tquant.gateway.converter;

import com.tigerbrokers.stock.openapi.client.https.domain.contract.item.ContractItem;
import com.tigerbrokers.stock.openapi.client.https.domain.future.item.FutureContractItem;
import com.tigerbrokers.stock.openapi.client.https.domain.future.item.FutureKlineItem;
import com.tigerbrokers.stock.openapi.client.https.domain.quote.item.KlinePoint;
import com.tigerbrokers.stock.openapi.client.https.domain.quote.item.MarketItem;
import com.tigerbrokers.stock.openapi.client.https.domain.quote.item.RealTimeQuoteItem;
import com.tigerbrokers.stock.openapi.client.https.domain.quote.item.SymbolNameItem;
import com.tigerbrokers.stock.openapi.client.https.domain.quote.item.TimelineItem;
import com.tigerbrokers.stock.openapi.client.struct.enums.FutureKType;
import com.tigerbrokers.stock.openapi.client.struct.enums.KType;
import com.tigerbrokers.stock.openapi.client.struct.enums.SecType;
import com.tigerbrokers.stock.openapi.client.util.SymbolUtil;
import com.tquant.core.model.data.Bar;
import com.tquant.core.model.data.Contract;
import com.tquant.core.model.data.HourTrading;
import com.tquant.core.model.data.MarketStatus;
import com.tquant.core.model.data.RealtimeQuote;
import com.tquant.core.model.data.SymbolName;
import com.tquant.core.model.data.TimelinePoint;
import com.tquant.core.model.data.TimelineQuote;
import com.tquant.core.model.data.TimelineRange;
import com.tquant.core.model.data.TradeCalendar;
import com.tquant.core.model.enums.BarType;
import com.tquant.core.model.enums.StockStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 *
 * @author kevin
 * @date 2022/09/02
 */
public class Converters {

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


  public static List<Contract> toContracts(List<ContractItem> contractItems) {
    List<Contract> contracts = new ArrayList<>();
    for (ContractItem item : contractItems) {
      contracts.add(toContract(item));
    }
    return contracts;
  }

  public static List<Contract> toFuturesContracts(List<FutureContractItem> contractItems) {
    List<Contract> contracts = new ArrayList<>();
    for (FutureContractItem item : contractItems) {
      if (item.getContractMonth() == null || item.getContractMonth().isEmpty()) {
        continue;
      }
      contracts.add(toContract(item));
    }
    return contracts;
  }

  public static Contract toContract(FutureContractItem contractItem) {
    Contract contract = new Contract();
    contract.setIdentifier(contractItem.getContractCode());
    contract.setName(contractItem.getName());
    int contractMonthLen = contractItem.getContractCode().length() - 4;
    contract.setSymbol(contractItem.getContractCode().substring(0, contractMonthLen));
    contract.setSecType(SecType.FUT.name());
    contract.setExchange(contractItem.getExchangeCode());
    contract.setContractMonth(contractItem.getContractMonth());
    contract.setExpiry(contractItem.getLastTradingDate());
    BigDecimal multiplier = contractItem.getMultiplier();
    if (multiplier == null) {
      contract.setMultiplier(0D);
    } else {
      contract.setMultiplier(multiplier.doubleValue());
    }
    return contract;
  }

  public static Contract toContract(ContractItem contractItem) {
    Contract contract = new Contract();
    contract.setIdentifier(contractItem.getSymbol());
    contract.setName(contractItem.getName());
    contract.setSymbol(contractItem.getSymbol());
    contract.setSecType(contractItem.getSecType());
    contract.setCurrency(contractItem.getCurrency());
    contract.setExchange(contractItem.getExchange());
    contract.setMarket(contractItem.getMarket());
    contract.setExpiry(contractItem.getExpiry());
    contract.setContractMonth(contractItem.getContractMonth());
    contract.setStrike(contractItem.getStrike());
    contract.setMultiplier(contractItem.getMultiplier());
    contract.setRight(contractItem.getRight());
    return contract;
  }

  public static List<MarketStatus> toMarketStatuses(List<MarketItem> marketItems) {
    List<MarketStatus> marketStatusList = new ArrayList<>();
    for (MarketItem marketItem : marketItems) {
      marketStatusList.add(
          new MarketStatus(marketItem.getMarket(), marketItem.getStatus(), marketItem.getMarketStatus(),
              marketItem.getOpenTime()));
    }
    return marketStatusList;
  }

  public static List<TradeCalendar> toTradeCalendars(
      List<com.tigerbrokers.stock.openapi.client.https.domain.quote.item.TradeCalendar> tradeCalendars) {
    List<TradeCalendar> tradeCalendarList = new ArrayList<>();
    tradeCalendars.stream()
        .forEach(
            calendar -> tradeCalendarList.add(new TradeCalendar(calendar.getDate(), calendar.getType())));
    return tradeCalendarList;
  }

  public static List<SymbolName> toSymbolNames(List<SymbolNameItem> symbolNameItems) {
    List<SymbolName> symbolNameList = new ArrayList<>();
    symbolNameItems.stream().forEach(item -> symbolNameList.add(new SymbolName(item.getSymbol(), item.getName())));
    return symbolNameList;
  }

  public static List<RealtimeQuote> toRealtimeQuotes(List<RealTimeQuoteItem> realTimeQuoteItems) {
    List<RealtimeQuote> realtimeQuotes = new ArrayList<>();
    realTimeQuoteItems.stream().forEach(item ->{
      com.tigerbrokers.stock.openapi.client.https.domain.quote.item.HourTrading origHourTrading = item.getHourTrading();
      HourTrading hourTrading = null;
      if (origHourTrading != null) {
        hourTrading =
            new HourTrading(origHourTrading.getTag(), origHourTrading.getLatestPrice(), origHourTrading.getPreClose(),
                origHourTrading.getLatestTime(), origHourTrading.getVolume(), origHourTrading.getTimestamp());
      }
      RealtimeQuote realtimeQuote = RealtimeQuote.builder()
          .symbol(item.getSymbol())
          .askPrice(item.getAskPrice())
          .askSize(item.getAskSize())
          .bidPrice(item.getBidPrice())
          .bidSize(item.getBidSize())
          .close(item.getClose())
          .high(item.getHigh())
          .low(item.getLow())
          .open(item.getOpen())
          .preClose(item.getPreClose())
          .status(StockStatus.valueOf(item.getStatus().name()))
          .latestPrice(item.getLatestPrice())
          .latestTime(item.getLatestTime())
          .volume(item.getVolume())
          .hourTrading(hourTrading)
          .build();
      realtimeQuotes.add(realtimeQuote);
    });
    return realtimeQuotes;
  }

  public static List<TimelineQuote> toTimelineQuotes(List<TimelineItem> timelineItems) {
    List<TimelineQuote> timelineQuotes = new ArrayList<>();
    timelineItems.stream().forEach(item->{
      com.tigerbrokers.stock.openapi.client.https.domain.quote.item.TimelineRange intraday = item.getIntraday();
      com.tigerbrokers.stock.openapi.client.https.domain.quote.item.TimelineRange preMarket = item.getPreMarket();
      com.tigerbrokers.stock.openapi.client.https.domain.quote.item.TimelineRange afterHours = item.getAfterHours();
      TimelineQuote timelineQuote = TimelineQuote.builder()
          .symbol(item.getSymbol())
          .period(item.getPeriod())
          .preClose(item.getPreClose())
          .intraday(TimelineRange.builder()
              .items(toTimelinePoints(intraday.getItems()))
              .beginTime(intraday.getBeginTime())
              .endTime(intraday.getEndTime())
              .build())
          .preMarket(TimelineRange.builder()
              .items(toTimelinePoints(preMarket.getItems()))
              .beginTime(preMarket.getBeginTime())
              .endTime(preMarket.getEndTime())
              .build())
          .afterHours(TimelineRange.builder()
              .items(toTimelinePoints(afterHours.getItems()))
              .beginTime(afterHours.getBeginTime())
              .endTime(afterHours.getEndTime())
              .build())
          .build();
      timelineQuotes.add(timelineQuote);
    });
    return timelineQuotes;
  }

  public static List<TimelinePoint> toTimelinePoints(
      List<com.tigerbrokers.stock.openapi.client.https.domain.quote.item.TimelinePoint> timelinePoints) {
    List<TimelinePoint> timelinePointList = new ArrayList<>();
    timelinePoints.stream()
        .forEach(item -> timelinePointList.add(
            new TimelinePoint(item.getPrice(), item.getAvgPrice(), item.getTime(), item.getVolume())));
    return timelinePointList;
  }

}

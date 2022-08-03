package com.tigerbrokers.quant.gateway.tiger;

import com.tigerbrokers.quant.TigerQuantException;
import com.tigerbrokers.quant.api.QuoteApi;
import com.tigerbrokers.quant.model.data.Bar;
import com.tigerbrokers.quant.model.enums.BarType;
import com.tigerbrokers.stock.openapi.client.constant.ApiServiceType;
import com.tigerbrokers.stock.openapi.client.https.client.TigerHttpClient;
import com.tigerbrokers.stock.openapi.client.https.domain.financial.item.CorporateDividendItem;
import com.tigerbrokers.stock.openapi.client.https.domain.financial.item.CorporateSplitItem;
import com.tigerbrokers.stock.openapi.client.https.domain.financial.item.FinancialDailyItem;
import com.tigerbrokers.stock.openapi.client.https.domain.financial.item.FinancialReportItem;
import com.tigerbrokers.stock.openapi.client.https.domain.future.item.FutureKlineBatchItem;
import com.tigerbrokers.stock.openapi.client.https.domain.future.item.FutureKlineItem;
import com.tigerbrokers.stock.openapi.client.https.domain.quote.item.*;
import com.tigerbrokers.stock.openapi.client.https.request.TigerHttpRequest;
import com.tigerbrokers.stock.openapi.client.https.request.financial.CorporateDividendRequest;
import com.tigerbrokers.stock.openapi.client.https.request.financial.CorporateSplitRequest;
import com.tigerbrokers.stock.openapi.client.https.request.financial.FinancialDailyRequest;
import com.tigerbrokers.stock.openapi.client.https.request.financial.FinancialReportRequest;
import com.tigerbrokers.stock.openapi.client.https.request.future.FutureKlineRequest;
import com.tigerbrokers.stock.openapi.client.https.request.quote.*;
import com.tigerbrokers.stock.openapi.client.https.response.TigerHttpResponse;
import com.tigerbrokers.stock.openapi.client.https.response.financial.CorporateDividendResponse;
import com.tigerbrokers.stock.openapi.client.https.response.financial.CorporateSplitResponse;
import com.tigerbrokers.stock.openapi.client.https.response.financial.FinancialDailyResponse;
import com.tigerbrokers.stock.openapi.client.https.response.financial.FinancialReportResponse;
import com.tigerbrokers.stock.openapi.client.https.response.future.FutureKlineResponse;
import com.tigerbrokers.stock.openapi.client.https.response.quote.*;
import com.tigerbrokers.stock.openapi.client.struct.enums.FinancialPeriodType;
import com.tigerbrokers.stock.openapi.client.struct.enums.Market;
import com.tigerbrokers.stock.openapi.client.struct.enums.RightOption;
import com.tigerbrokers.stock.openapi.client.struct.enums.TimeLineType;
import com.tigerbrokers.stock.openapi.client.util.builder.AccountParamBuilder;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/08/16
 */
public class TigerQuoteApi implements QuoteApi {

  private TigerHttpClient client;

  public TigerQuoteApi(TigerHttpClient client) {
    this.client = client;
  }

  @Override
  public String grabQuotePermission() {
    TigerHttpRequest request = new TigerHttpRequest(ApiServiceType.GRAB_QUOTE_PERMISSION);
    String bizContent = AccountParamBuilder.instance()
            .buildJson();
    request.setBizContent(bizContent);
    TigerHttpResponse response = client.execute(request);
    if (!response.isSuccess()) {
      throw new RuntimeException("grab quote permission error:"+response.getMessage());
    }
    return response.getData();
  }

  @Override
  public List<MarketItem> getMarketState(Market market) {
    QuoteMarketResponse response = client.execute(QuoteMarketRequest.newRequest(market));
    if (!response.isSuccess()) {
      throw new TigerQuantException("get market state error:" + response.getMessage());
    }
    return response.getMarketItems();
  }

  @Override
  public List<String> getSymbols(Market market) {
    QuoteSymbolResponse response = client.execute(QuoteSymbolRequest.newRequest(market));
    if (!response.isSuccess()) {
      throw new TigerQuantException("get symbols error:" + response.getMessage());
    }
    return response.getSymbols();
  }

  @Override
  public List<SymbolNameItem> getSymbolNames(Market market) {
    QuoteSymbolNameResponse response = client.execute(QuoteSymbolNameRequest.newRequest(market));
    if (!response.isSuccess()) {
      throw new TigerQuantException("get symbol names error:" + response.getMessage());
    }
    return response.getSymbolNameItems();
  }

  @Override
  public Map<String, List<Bar>> getBars(List<String> symbols, BarType barType, RightOption rightOption,
      int limit) {
    if (barType == null || !barType.isStockBarType()) {
      throw new TigerQuantException("stock bar type [" + barType + "] not support");
    }
    QuoteKlineResponse response =
        client.execute(
            QuoteKlineRequest.newRequest(symbols, barType.toKType()).withLimit(limit).withRight(rightOption));
    if (!response.isSuccess()) {
      throw new TigerQuantException("get bars error:" + response.getMessage());
    }
    Map<String, List<Bar>> result = new HashMap<>();
    List<KlineItem> klineItems = response.getKlineItems();
    if (klineItems != null) {
      for (KlineItem item : klineItems) {
        List<KlinePoint> klinePoints = item.getItems();
        if (klinePoints == null || klinePoints.isEmpty()) {
          continue;
        }
        result.put(item.getSymbol(), Bar.toBars(item.getSymbol(), barType, klinePoints));
      }
    }
    return result;
  }

  @Override
  public Map<String, List<Bar>> getBars(List<String> symbols, BarType barType, LocalDate start, LocalDate end,
      RightOption rightOption) {
    if (barType == null || !barType.isStockBarType()) {
      throw new TigerQuantException("stock bar type [" + barType + "] not support");
    }
    long startTime = Timestamp.valueOf(start.atTime(LocalTime.MIDNIGHT)).getTime();
    long endTime = Timestamp.valueOf(end.atTime(LocalTime.MIDNIGHT)).getTime();
    System.out.println(startTime+","+endTime);
    QuoteKlineResponse response =
        client.execute(
            QuoteKlineRequest.newRequest(symbols, barType.toKType(),startTime,endTime).withLimit(365).withRight(rightOption));
    if (!response.isSuccess()) {
      throw new TigerQuantException("get bars error:" + response.getMessage());
    }
    Map<String, List<Bar>> result = new HashMap<>();
    List<KlineItem> klineItems = response.getKlineItems();
    if (klineItems != null) {
      for (KlineItem item : klineItems) {
        List<KlinePoint> klinePoints = item.getItems();
        if (klinePoints == null || klinePoints.isEmpty()) {
          continue;
        }
        result.put(item.getSymbol(), Bar.toBars(item.getSymbol(), barType, klinePoints));
      }
    }
    return result;
  }

  @Override
  public Map<String, List<Bar>> getFuturesBars(List<String> symbols, BarType barType, int limit) {
    if (barType == null || barType == BarType.year) {
      throw new TigerQuantException("futures bar type [" + barType + "] not support");
    }
    FutureKlineResponse response =
        client.execute(FutureKlineRequest.newRequest(symbols, barType.toFutureKType(), limit));
    if (!response.isSuccess()) {
      throw new TigerQuantException("get futures bars error:" + response.getMessage());
    }
    Map<String, List<Bar>> result = new HashMap<>();
    List<FutureKlineBatchItem> klineItems = response.getFutureKlineItems();
    if (klineItems != null) {
      for (FutureKlineBatchItem item : klineItems) {
        List<FutureKlineItem> klinePoints = item.getItems();
        if (klinePoints == null || klinePoints.isEmpty()) {
          continue;
        }
        List<Bar> bars = Bar.toFuturesBars(item.getContractCode(), barType, klinePoints);
        Collections.reverse(bars);
        result.put(item.getContractCode(), bars);
      }
    }
    return result;
  }

  @Override
  public Map<String, List<RealTimeQuoteItem>> getRealTimeQuotes(List<String> symbols) {
    QuoteRealTimeQuoteResponse response = client.execute(QuoteRealTimeQuoteRequest.newRequest(symbols));
    if (!response.isSuccess()) {
      throw new TigerQuantException("get real-time quotes error:" + response.getMessage());
    }
    return response.getRealTimeQuoteItems().stream().collect(Collectors.groupingBy(RealTimeQuoteItem::getSymbol));
  }

  @Override
  public Map<String, List<TradeTickItem>> getTradeTicks(List<String> symbols) {
    QuoteTradeTickResponse response = client.execute(QuoteTradeTickRequest.newRequest(symbols));
    if (!response.isSuccess()) {
      throw new TigerQuantException("get trade ticks error:" + response.getMessage());
    }

    return response.getTradeTickItems().stream().collect(Collectors.groupingBy(TradeTickItem::getSymbol));
  }

  @Override
  public Map<String, List<TimelineItem>> getTimeShareQuotes(List<String> symbols, Long beginTime) {
    QuoteTimelineResponse response =
        client.execute(QuoteTimelineRequest.newRequest(symbols, beginTime, true, TimeLineType.day));
    if (!response.isSuccess()) {
      throw new TigerQuantException("get time-share quotes error:" + response.getMessage());
    }
    return response.getTimelineItems().stream().collect(Collectors.groupingBy(TimelineItem::getSymbol));
  }

  @Override
  public Map<String, List<FinancialDailyItem>> getFinancialDaily(List<String> symbols, List<String> fields,
      LocalDate beginDate, LocalDate endDate) {

    FinancialDailyResponse response =
        client.execute(FinancialDailyRequest.newRequest(symbols, fields, "2016-08-18", "2019-10-17"));
    if (!response.isSuccess()) {
      throw new TigerQuantException("get financial daily error:" + response.getMessage());
    }
    return response.getFinancialDailyItems().stream().collect(Collectors.groupingBy(FinancialDailyItem::getSymbol));
  }

  @Override
  public Map<String, List<FinancialReportItem>> getFinancialReport(List<String> symbols, List<String> fields,
      FinancialPeriodType periodType) {
    FinancialReportResponse response =
        client.execute(FinancialReportRequest.newRequest(symbols, Market.US, fields, FinancialPeriodType.LTM));
    if (!response.isSuccess()) {
      throw new TigerQuantException("get financial report error:" + response.getMessage());
    }
    return response.getFinancialReportItems().stream().collect(Collectors.groupingBy(FinancialReportItem::getSymbol));
  }

  @Override
  public Map<String, List<CorporateSplitItem>> getCorporateSplit(List<String> symbols, LocalDate beginDate,
      LocalDate endDate) {
    CorporateSplitResponse response = client.execute(
        CorporateSplitRequest.newRequest(symbols, Market.US,
            Date.from(beginDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()),
            Date.from(endDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())));
    if (!response.isSuccess()) {
      throw new TigerQuantException("get corporate split error:" + response.getMessage());
    }
    return response.getItems();
  }

  @Override
  public Map<String, List<CorporateDividendItem>> getCorporateDividend(List<String> symbols,
      LocalDate beginDate, LocalDate endDate) {
    CorporateDividendResponse response = client.execute(
        CorporateDividendRequest.newRequest(symbols, Market.US,
            Date.from(beginDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()),
            Date.from(endDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())));
    if (!response.isSuccess()) {
      throw new TigerQuantException("get corporate split error:" + response.getMessage());
    }
    return response.getItems();
  }
}

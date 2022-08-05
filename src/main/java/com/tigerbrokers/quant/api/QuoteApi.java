package com.tigerbrokers.quant.api;

import com.tigerbrokers.quant.model.data.Bar;
import com.tigerbrokers.quant.model.data.Tick;
import com.tigerbrokers.quant.model.enums.BarType;
import com.tigerbrokers.stock.openapi.client.https.domain.financial.item.CorporateDividendItem;
import com.tigerbrokers.stock.openapi.client.https.domain.financial.item.CorporateSplitItem;
import com.tigerbrokers.stock.openapi.client.https.domain.financial.item.FinancialDailyItem;
import com.tigerbrokers.stock.openapi.client.https.domain.financial.item.FinancialReportItem;
import com.tigerbrokers.stock.openapi.client.https.domain.quote.item.MarketItem;
import com.tigerbrokers.stock.openapi.client.https.domain.quote.item.RealTimeQuoteItem;
import com.tigerbrokers.stock.openapi.client.https.domain.quote.item.SymbolNameItem;
import com.tigerbrokers.stock.openapi.client.https.domain.quote.item.TimelineItem;
import com.tigerbrokers.stock.openapi.client.https.domain.quote.item.TradeTickItem;
import com.tigerbrokers.stock.openapi.client.struct.enums.FinancialPeriodType;
import com.tigerbrokers.stock.openapi.client.struct.enums.Market;
import com.tigerbrokers.stock.openapi.client.struct.enums.RightOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Description: quote api
 *
 * @author kevin
 * @date 2019/08/15
 */
public interface QuoteApi {

  String grabQuotePermission();

  List<MarketItem> getMarketState(Market market);

  List<String> getSymbols(Market market);

  List<SymbolNameItem> getSymbolNames(Market market);

  Map<String, List<Bar>> getBars(List<String> symbols, BarType barType, RightOption rightOption, int limit);

  Map<String, List<Bar>> getBars(List<String> symbols, BarType barType, LocalDate start, LocalDate end,
      RightOption rightOption);

  Map<String, List<Bar>> getFuturesBars(List<String> symbols, BarType barType, int limit);

  Map<String, List<RealTimeQuoteItem>> getRealTimeQuotes(List<String> symbols);

  Map<String, List<Tick>> getTradeTicks(List<String> symbols);

  Map<String, List<TimelineItem>> getTimeShareQuotes(List<String> symbols, Long beginTime);

  Map<String, List<FinancialDailyItem>> getFinancialDaily(List<String> symbols, List<String> fields,
      LocalDate beginDate,
      LocalDate endDate);

  Map<String, List<FinancialReportItem>> getFinancialReport(List<String> symbols, List<String> fields,
      FinancialPeriodType periodType);

  Map<String, List<CorporateSplitItem>> getCorporateSplit(List<String> symbols, LocalDate beginDate, LocalDate endDate);

  Map<String, List<CorporateDividendItem>> getCorporateDividend(List<String> symbols, LocalDate beginDate,
      LocalDate endDate);
}

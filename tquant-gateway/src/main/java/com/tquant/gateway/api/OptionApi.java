package com.tquant.gateway.api;

import com.tigerbrokers.stock.openapi.client.https.domain.option.item.OptionBriefItem;
import com.tigerbrokers.stock.openapi.client.https.domain.option.item.OptionExpirationItem;
import com.tigerbrokers.stock.openapi.client.https.domain.option.item.OptionKlineItem;
import com.tigerbrokers.stock.openapi.client.https.domain.option.item.OptionRealTimeQuoteGroup;
import com.tigerbrokers.stock.openapi.client.https.domain.option.item.OptionTradeTickItem;
import com.tigerbrokers.stock.openapi.client.struct.enums.Right;
import com.tquant.core.model.request.OptionChainFilter;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Description: option api
 *
 * @author kevin
 * @date 2019/08/15
 */
public interface OptionApi {

  OptionExpirationItem getOptionExpiration(String symbol) ;

  List<OptionBriefItem> getOptionBrief(String symbol, Right right, String strike, String expiry);

  List<OptionRealTimeQuoteGroup> getOptionChain(String symbol, String expiry, OptionChainFilter optionChainFilter);

  List<OptionKlineItem> getOptionKline(String symbol, Right right, String strike, String expiry, LocalDate beginDate,
      LocalDate endDate);

  List<OptionTradeTickItem> getOptionTradeTick(String symbol, Right right, String strike, String expiry);
}

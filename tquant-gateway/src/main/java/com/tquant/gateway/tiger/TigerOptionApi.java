package com.tquant.gateway.tiger;

import com.google.common.collect.Lists;
import com.tigerbrokers.stock.openapi.client.https.domain.option.item.OptionRealTimeQuoteGroup;
import com.tigerbrokers.stock.openapi.client.https.domain.option.model.OptionChainFilterModel;
import com.tigerbrokers.stock.openapi.client.https.request.option.OptionChainQueryV3Request;
import com.tquant.core.TigerQuantException;
import com.tquant.core.model.request.OptionChainFilter;
import com.tquant.gateway.api.OptionApi;
import com.tigerbrokers.stock.openapi.client.https.client.TigerHttpClient;
import com.tigerbrokers.stock.openapi.client.https.domain.option.item.OptionBriefItem;
import com.tigerbrokers.stock.openapi.client.https.domain.option.item.OptionChainItem;
import com.tigerbrokers.stock.openapi.client.https.domain.option.item.OptionExpirationItem;
import com.tigerbrokers.stock.openapi.client.https.domain.option.item.OptionKlineItem;
import com.tigerbrokers.stock.openapi.client.https.domain.option.item.OptionTradeTickItem;
import com.tigerbrokers.stock.openapi.client.https.domain.option.model.OptionChainModel;
import com.tigerbrokers.stock.openapi.client.https.domain.option.model.OptionCommonModel;
import com.tigerbrokers.stock.openapi.client.https.domain.option.model.OptionKlineModel;
import com.tigerbrokers.stock.openapi.client.https.request.option.OptionBriefQueryRequest;
import com.tigerbrokers.stock.openapi.client.https.request.option.OptionChainQueryRequest;
import com.tigerbrokers.stock.openapi.client.https.request.option.OptionExpirationQueryRequest;
import com.tigerbrokers.stock.openapi.client.https.request.option.OptionKlineQueryRequest;
import com.tigerbrokers.stock.openapi.client.https.request.option.OptionTradeTickQueryRequest;
import com.tigerbrokers.stock.openapi.client.https.response.option.OptionBriefResponse;
import com.tigerbrokers.stock.openapi.client.https.response.option.OptionChainResponse;
import com.tigerbrokers.stock.openapi.client.https.response.option.OptionExpirationResponse;
import com.tigerbrokers.stock.openapi.client.https.response.option.OptionKlineResponse;
import com.tigerbrokers.stock.openapi.client.https.response.option.OptionTradeTickResponse;
import com.tigerbrokers.stock.openapi.client.struct.enums.Right;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/08/16
 */
public class TigerOptionApi implements OptionApi {

  private TigerHttpClient client;

  public TigerOptionApi(TigerHttpClient client) {
    this.client = client;
  }

  @Override
  public OptionExpirationItem getOptionExpiration(String symbol) {
    OptionExpirationResponse response = client.execute(OptionExpirationQueryRequest.of(Lists.newArrayList(symbol)));
    if (!response.isSuccess()) {
      throw new TigerQuantException("get option expiration error:" + response.getMessage());
    }
    return response.getOptionExpirationItems().get(0);
  }

  @Override
  public List<OptionBriefItem> getOptionBrief(String symbol, Right right, String strike, String expiry) {
    OptionCommonModel optionCommonModel = new OptionCommonModel();
    optionCommonModel.setSymbol(symbol);
    optionCommonModel.setRight(right.name());
    optionCommonModel.setStrike(strike);
    if (expiry != null && !expiry.contains("-")) {
      optionCommonModel.setExpiry(expiry.substring(0, 4) + "-" + expiry.substring(4, 6) + "-" + expiry.substring(6));
    } else {
      optionCommonModel.setExpiry(expiry);
    }
    OptionBriefResponse response = client.execute(OptionBriefQueryRequest.of(optionCommonModel));
    if (!response.isSuccess()) {
      throw new TigerQuantException("get option brief error:" + response.getMessage());
    }
    return response.getOptionBriefItems();
  }

  @Override
  public List<OptionRealTimeQuoteGroup> getOptionChain(String symbol, String expiry, OptionChainFilter filter) {
    OptionChainModel basicModel = new OptionChainModel(symbol, expiry);
    OptionChainFilterModel filterModel = null;
    if (filter != null) {
      filterModel = new OptionChainFilterModel().inTheMoney(filter.getInTheMoney())
          .impliedVolatility(filter.getMinImpliedVolatility(), filter.getMaxImpliedVolatility())
          .openInterest(filter.getMinOpenInterest(), filter.getMaxOpenInterest())
          .greeks(new OptionChainFilterModel.Greeks()
              .delta(filter.getMinDelta(), filter.getMaxDelta())
              .gamma(filter.getMinGamma(), filter.getMaxGamma())
              .vega(filter.getMinVega(), filter.getMaxVega())
              .theta(filter.getMinTheta(), filter.getMaxTheta())
              .rho(filter.getMinRho(), filter.getMaxRho()));
    }

    OptionChainResponse response = client.execute(OptionChainQueryV3Request.of(basicModel, filterModel));
    if (!response.isSuccess()) {
      throw new TigerQuantException("get option chain error:" + response.getMessage());
    }
    List<OptionChainItem> optionChainItems = response.getOptionChainItems();
    if (optionChainItems == null || optionChainItems.isEmpty()) {
      throw new TigerQuantException("get option chain result empty");
    }
    return optionChainItems.get(0).getItems();
  }

  @Override
  public List<OptionKlineItem> getOptionKline(String symbol, Right right, String strike, String expiry,
      LocalDate beginDate, LocalDate endDate) {
    OptionKlineModel model = new OptionKlineModel();
    model.setSymbol(symbol);
    model.setRight(right.name());
    model.setStrike(strike);
    model.setExpiry(expiry);
    model.setBeginTime(beginDate.toEpochDay());
    model.setEndTime(endDate.toEpochDay());
    OptionKlineResponse response = client.execute(OptionKlineQueryRequest.of(model));
    if (!response.isSuccess()) {
      throw new TigerQuantException("get option kline error:" + response.getMessage());
    }
    return response.getKlineItems();
  }

  @Override
  public List<OptionTradeTickItem> getOptionTradeTick(String symbol, Right right, String strike,
      String expiry) {
    OptionTradeTickResponse response = client.execute(
        OptionTradeTickQueryRequest.of(new OptionCommonModel(symbol, right.name(), strike, Long.parseLong(expiry))));
    if (!response.isSuccess()) {
      throw new TigerQuantException("get option trade tick error:" + response.getMessage());
    }
    return response.getOptionTradeTickItems();
  }
}

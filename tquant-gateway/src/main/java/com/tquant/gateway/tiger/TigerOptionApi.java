package com.tquant.gateway.tiger;

import com.tquant.core.TigerQuantException;
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
  public Map<String, List<OptionExpirationItem>> getOptionExpiration(List<String> symbols) {
    OptionExpirationResponse response = client.execute(OptionExpirationQueryRequest.of(symbols));
    if (!response.isSuccess()) {
      throw new TigerQuantException("get option expiration error:" + response.getMessage());
    }
    return response.getOptionExpirationItems().stream().collect(Collectors.groupingBy(OptionExpirationItem::getSymbol));
  }

  @Override
  public List<OptionBriefItem> getOptionBrief(String symbol, Right right, String strike, String expiry) {
    OptionBriefResponse response = client.execute(
        OptionBriefQueryRequest.of(new OptionCommonModel(symbol, right.name(), strike, Long.parseLong(expiry))));
    if (!response.isSuccess()) {
      throw new TigerQuantException("get option brief error:" + response.getMessage());
    }
    return response.getOptionBriefItems();
  }

  @Override
  public List<OptionChainItem> getOptionChain(String symbol, String expiry) {
    OptionChainModel model = new OptionChainModel();
    model.setSymbol(symbol);
    model.setExpiry(expiry);
    OptionChainResponse response = client.execute(OptionChainQueryRequest.of(model));
    if (!response.isSuccess()) {
      throw new TigerQuantException("get option chain error:" + response.getMessage());
    }
    return response.getOptionChainItems();
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

package com.tquant.algorithm.algos;

import com.tigerbrokers.stock.openapi.client.https.domain.option.item.OptionRealTimeQuote;
import com.tigerbrokers.stock.openapi.client.https.domain.option.item.OptionRealTimeQuoteGroup;
import com.tquant.core.core.AlgoTemplate;
import com.tquant.core.model.data.Order;
import com.tquant.core.model.data.Tick;
import com.tquant.core.model.data.Trade;
import com.tquant.core.model.enums.OrderType;
import com.tquant.gateway.api.OptionApi;
import com.tquant.gateway.tiger.TigerClient;
import com.tquant.gateway.tiger.TigerOptionApi;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Description:
 *
 * @author kevin
 * @date 2022/08/30
 */
public class OptionCoveredCallAlgo extends AlgoTemplate {

  private String symbol;
  private String expiryDateStr;
  private LocalDate expiryDate;
  private String optionSymbol;
  private Double maxPrice = 180.0;
  private volatile boolean isOpen = false;
  private volatile boolean isClose = false;
  private DateTimeFormatter formatter =  DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private OptionApi optionApi = new TigerOptionApi(TigerClient.getInstance());

  @Override
  public void onStart() {
    symbol = (String) settings.get("symbol");
    //以下周五的期权为到期日
    expiryDate = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.FRIDAY));
    expiryDateStr = expiryDate.format(formatter);

    //根据配置的 symbol 和 expiry 获取期权链
    List<OptionRealTimeQuoteGroup> optionChain = optionApi.getOptionChain(symbol, expiryDateStr, null);

    //只过滤call 期权
    List<OptionRealTimeQuote> calls = new ArrayList<>();
    optionChain.stream().forEach(chain -> calls.add(chain.getCall()));

    //获取成交量最大的期权行情
    OptionRealTimeQuote maxVolumeCallQuote =
        calls.stream().max(Comparator.comparingInt(OptionRealTimeQuote::getVolume)).get();

    optionSymbol = maxVolumeCallQuote.getIdentifier();
    //订阅正股行情，订阅成交量最大的期权行情
    subscribe(symbol);
    subscribe(optionSymbol);
  }

  @Override
  public void onTick(Tick tick) {
    //价格达到最大限制时，买入正股，并卖出call，只进行一次买入操作
    if (!isOpen && tick.getSymbol().equals(symbol) && tick.getLatestPrice() >= maxPrice) {
      isOpen = true;
      log("match option covered call open position tick: {}", tick);
      buy(symbol, null, 100, OrderType.MKT);
      sell(optionSymbol, null, 1, OrderType.MKT);
    }

    //到达到期日时，平仓
    if (!isClose && expiryDate.equals(LocalDate.now())) {
      isClose = true;
      log("match option covered call close position tick: {}", tick);
      sell(symbol, null, 100, OrderType.MKT);
      buy(optionSymbol, null, 1, OrderType.MKT);
    }
  }

  @Override
  public void onOrder(Order order) {

  }

  @Override
  public void onTrade(Trade trade) {

  }

  @Override
  public void onStop() {
    //取消行情订阅
    cancelSubscribe(symbol);
    cancelSubscribe(optionSymbol);
  }
}

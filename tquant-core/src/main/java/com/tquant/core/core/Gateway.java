package com.tquant.core.core;

import com.tquant.core.event.Event;
import com.tquant.core.event.EventEngine;
import com.tquant.core.event.EventType;
import com.tquant.core.model.data.Account;
import com.tquant.core.model.data.Asset;
import com.tquant.core.model.data.Bar;
import com.tquant.core.model.data.Contract;
import com.tquant.core.model.data.Order;
import com.tquant.core.model.data.Position;
import com.tquant.core.model.data.Tick;
import com.tquant.core.model.data.Trade;
import com.tquant.core.model.enums.BarType;
import com.tquant.core.model.request.ModifyRequest;
import com.tquant.core.model.request.OrderRequest;
import com.tquant.core.model.request.SubscribeRequest;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/08/15
 */
@Data
public abstract class Gateway {

  private EventEngine eventEngine;
  private String gatewayName;

  public Gateway(EventEngine eventEngine, String gatewayName) {
    this.eventEngine = eventEngine;
    this.gatewayName = gatewayName;
  }

  public void onEvent(EventType eventType, Object data) {
    eventEngine.put(new Event(eventType, data));
  }

  public void onTick(Tick tick) {
    onEvent(EventType.EVENT_TICK, tick);
  }

  public void onTrade(Trade trade) {
    onEvent(EventType.EVENT_TRADE, trade);
  }

  public void onOrder(Order order) {
    onEvent(EventType.EVENT_ORDER, order);
  }

  public void onPosition(Position position) {
    onEvent(EventType.EVENT_POSITION, position);
  }

  public void onAccount(Account account) {
    onEvent(EventType.EVENT_ACCOUNT, account);
  }

  public void onContract(Contract contract) {
    onEvent(EventType.EVENT_CONTRACT, contract);
  }

  public void onAsset(Asset asset) {
    onEvent(EventType.EVENT_ASSET, asset);
  }

  public void log(String format, Object... args) {
    if (format == null || format.isEmpty()) {
      return;
    }
    FormattingTuple formatting = MessageFormatter.arrayFormat(format, args);
    onEvent(EventType.EVENT_LOG, formatting.getMessage());
  }

  public void stop() {

  }

  public abstract void connect();

  public abstract void subscribe(SubscribeRequest request);

  public abstract void cancelSubscribe(SubscribeRequest request);

  public abstract String sendOrder(OrderRequest request);

  public abstract void cancelOrder(ModifyRequest request);

  public abstract void modifyOrder(ModifyRequest request);

  public abstract Map<String, List<Bar>> getBars(List<String> symbols, BarType barType, int limit);
}

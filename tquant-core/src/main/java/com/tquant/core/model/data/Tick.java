package com.tquant.core.model.data;

import com.alibaba.fastjson.JSONObject;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimeZone;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/08/16
 */
@Data
@NoArgsConstructor
public class Tick extends StockData implements BaseData {

  private Contract contract;
  private String identifier;
  private String symbol;
  private String name;
  private String type;
  private long volume;
  private double amount;
  private double latestPrice;
  private double latestVolume;
  private LocalDateTime latestTime;
  private long time;
  private Integer openInterest;

  private double open;
  private double high;
  private double low;
  private double close;
  private double preClose;

  private double bidPrice;
  private int bidSize;
  private double askPrice;
  private int askSize;
  private double midpoint;

  public Tick(RealtimeQuote realtimeQuote,Contract contract) {
    this.symbol = realtimeQuote.getSymbol();
    if (this.contract == null) {
      this.contract = contract;
    }
    if (realtimeQuote.getOpen() != null) {
      this.open = realtimeQuote.getOpen();
    }
    if (realtimeQuote.getHigh() != null) {
      this.high = realtimeQuote.getHigh();
    }
    if (realtimeQuote.getLow() != null) {
      this.low = realtimeQuote.getLow();
    }
    if (realtimeQuote.getClose() != null) {
      this.close = realtimeQuote.getClose();
    }
    if (realtimeQuote.getPreClose() != null) {
      this.preClose = realtimeQuote.getPreClose();
    }
    if (realtimeQuote.getLatestPrice() != null) {
      this.latestPrice = realtimeQuote.getLatestPrice();
    }
    if (realtimeQuote.getLatestTime() != null) {
      this.latestTime = Instant.ofEpochMilli(realtimeQuote.getLatestTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
    if (realtimeQuote.getAskPrice() != null) {
      this.askPrice = realtimeQuote.getAskPrice();
    }
    if (realtimeQuote.getAskSize() != null) {
      this.askSize = realtimeQuote.getAskSize().intValue();
    }
    if (realtimeQuote.getBidPrice() != null) {
      this.bidPrice = realtimeQuote.getBidPrice();
    }
    if (realtimeQuote.getBidSize() != null) {
      this.bidSize = realtimeQuote.getBidSize().intValue();
    }
    if (realtimeQuote.getVolume() != null) {
      this.volume = realtimeQuote.getVolume();
    }
    if (realtimeQuote.getOpenInterest() != null) {
      this.openInterest = realtimeQuote.getOpenInterest();
    }
  }

  public void updateTick(Tick tick) {
    if (tick.getVolume() > 0) {
      this.volume = tick.getVolume();
    }
    if (tick.getLatestPrice() > 0) {
      this.latestPrice = tick.getLatestPrice();
    }
    if (tick.getLatestVolume() > 0) {
      this.latestVolume = tick.getLatestVolume();
    }
    if (tick.getLatestTime() != null) {
      this.latestTime = tick.getLatestTime();
    }
    if (tick.getOpen() > 0) {
      this.open = tick.getOpen();
    }
    if (tick.getHigh() > 0) {
      this.high = tick.getHigh();
    }
    if (tick.getLow() > 0) {
      this.low = tick.getLow();
    }
    if (tick.getClose() > 0) {
      this.close = tick.getClose();
    }
    if (tick.getPreClose() > 0) {
      this.preClose = tick.getPreClose();
    }
    if (tick.getAskPrice() > 0) {
      this.askPrice = tick.getAskPrice();
    }
    if (this.getAskSize() > 0) {
      this.askSize = tick.getAskSize();
    }
    if (tick.getBidPrice() > 0) {
      this.bidPrice = tick.getBidPrice();
    }
    if (tick.getBidSize() > 0) {
      this.bidSize = tick.getBidSize();
    }
    if (tick.getAmount() > 0) {
      this.amount = tick.getAmount();
    }
  }

  public void jsonToTick(JSONObject jsonObject) {
    this.symbol = jsonObject.getString("symbol");
    this.identifier = jsonObject.getString("symbol");
    this.volume = jsonObject.getLongValue("volume");
    this.latestPrice = jsonObject.getDoubleValue("latestPrice");
    this.amount = jsonObject.getDoubleValue("amount");
    this.latestVolume = jsonObject.getIntValue("latestVolume");
    long timestamp = jsonObject.getLongValue("timestamp");
    if ( timestamp > 0) {
      this.latestTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
          TimeZone.getDefault().toZoneId());
      this.time = timestamp;
    } else {
      this.latestTime = LocalDateTime.now();
      this.time = System.currentTimeMillis();
    }

    this.open = jsonObject.getDoubleValue("open");
    this.high = jsonObject.getDoubleValue("high");
    this.low = jsonObject.getDoubleValue("low");
    this.close = jsonObject.getDoubleValue("close");
    this.preClose = jsonObject.getDoubleValue("preClose");

    this.askPrice = jsonObject.getDoubleValue("askPrice");
    this.askSize = jsonObject.getIntValue("askSize");
    this.bidPrice = jsonObject.getDoubleValue("bidPrice");
    this.bidSize = jsonObject.getIntValue("bidSize");

    String hourTradingTag = jsonObject.getString("hourTradingTag");
    if (hourTradingTag != null && hourTradingTag.equals("盘前")) {
      Double latestPrice = jsonObject.getDouble("hourTradingLatestPrice");
      if (latestPrice != null) {
        this.latestPrice = latestPrice;
      }
      Double preClose = jsonObject.getDouble("hourTradingPreClose");
      if (preClose != null) {
        this.preClose = preClose;
      }
      Long volume = jsonObject.getLong("hourTradingVolume");
      if (volume != null) {
        this.volume = volume;
      }
    }
  }

  public double getMidpoint() {
    if (this.askPrice > 0 && this.bidPrice > 0) {
      this.midpoint =
          new BigDecimal((this.askPrice + this.bidPrice) / 2).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
    return this.midpoint;
  }
}

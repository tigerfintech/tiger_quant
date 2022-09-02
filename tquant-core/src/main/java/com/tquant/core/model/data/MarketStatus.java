package com.tquant.core.model.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description:
 *
 * @author kevin
 * @date 2022/09/02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketStatus {
  /**
   * US:美股，CN:沪深，HK:港股
   **/
  private String market;

  /**
   * 市场状态，包括：NOT_YET_OPEN:未开盘,PRE_HOUR_TRADING:盘前交易,TRADING:交易中,MIDDLE_CLOSE:午间休市,POST_HOUR_TRADING:盘后交易,CLOSING:已收盘,EARLY_CLOSED:提前休市,MARKET_CLOSED:休市
   **/
  private String status;

  /**
   * 市场状态描述(未开盘，交易中，休市等）
   **/
  private String marketStatus;

  /**
   * 最近开盘、交易时间  MM-dd HH:mm:ss z
   **/
  private String openTime;
}

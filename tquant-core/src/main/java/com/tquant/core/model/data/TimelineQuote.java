package com.tquant.core.model.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description:
 * Created by lijiawen on 2018/12/25.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimelineQuote {

  /**
   * 股票代号
   */
  private String symbol;

  /**
   * 分时 day ; 5日分时 5day
   */
  private String period;

  /**
   * 昨日收盘价
   */
  private Double preClose;

  /**
   * 分时数据,当天分时数据没有endTime
   */
  private TimelineRange intraday;

  /**
   * 盘前交易分时数据（仅美股）
   */
  private TimelineRange preMarket;

  /**
   * 盘后交易分时数据（仅美股）
   */
  private TimelineRange afterHours;
}

package com.tquant.core.model.data;

import com.tquant.core.model.enums.StockStatus;
import lombok.Builder;
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
@Builder
public class RealtimeQuote {

  /**
   * 股票代码
   */
  private String symbol;

  /**
   * 开盘价
   */
  private Double open;

  /**
   * 最高价
   */
  private Double high;

  /**
   * 最低价
   */
  private Double low;

  /**
   * 收盘价
   */
  private Double close;

  /**
   * 昨日收盘价
   */
  private Double preClose;

  /**
   * 最新价
   */
  private Double latestPrice;

  /**
   * 最新成交时间
   */
  private Long latestTime;

  /**
   * 卖盘价
   */
  private Double askPrice;

  /**
   * 卖盘数量
   */
  private Long askSize;

  /**
   * 买盘价
   */
  private Double bidPrice;

  /**
   * 买盘数量
   */
  private Long bidSize;

  /**
   * 当日成交量
   */
  private Long volume;

  /**
   * 个股状态：
   * 0: 正常 3: 停牌 4: 退市 7: 新股 8: 变更
   */
  private StockStatus status;

  private HourTrading hourTrading;

}

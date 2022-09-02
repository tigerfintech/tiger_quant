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
public class TradeCalendar {
  /**
   * trading day date，yyyy-MM-dd
   */
  private String date;

  /**
   * trading day type:NORMAL/EARLY_CLOSE
   */
  private String type;
}

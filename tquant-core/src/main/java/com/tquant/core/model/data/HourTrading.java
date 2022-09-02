package com.tquant.core.model.data;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description:
 * Created by lijiawen on 2018/12/28.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HourTrading implements Serializable {

  /**
   * 盘前、盘后
   */
  private String tag;

  protected Double latestPrice;

  protected Double preClose;

  private String latestTime;

  private Long volume;

  private Long timestamp;
}

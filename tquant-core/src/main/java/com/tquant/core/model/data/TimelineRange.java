package com.tquant.core.model.data;

import java.io.Serializable;
import java.util.List;
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
public class TimelineRange implements Serializable {

  /**
   * 分时数据数组
   */
  private List<TimelinePoint> items;

  private Long beginTime;

  private Long endTime;
}

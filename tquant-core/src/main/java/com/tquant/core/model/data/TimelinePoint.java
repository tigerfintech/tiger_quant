package com.tquant.core.model.data;

import java.io.Serializable;
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
public class TimelinePoint implements Serializable {

  private Double price;

  private Double avgPrice;

  private Long time;

  private Long volume;
}

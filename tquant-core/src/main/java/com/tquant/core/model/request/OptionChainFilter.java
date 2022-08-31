package com.tquant.core.model.request;

import lombok.Builder;
import lombok.Data;

/**
 * Description:
 *
 * @author kevin
 * @date 2022/08/30
 */
@Data
@Builder
public class OptionChainFilter {

  private Boolean inTheMoney;
  private Double minImpliedVolatility;
  private Double maxImpliedVolatility;
  private Integer minOpenInterest;
  private Integer maxOpenInterest;
  private Double minDelta;
  private Double maxDelta;
  private Double minGamma;
  private Double maxGamma;
  private Double minVega;
  private Double maxVega;
  private Double minTheta;
  private Double maxTheta;
  private Double minRho;
  private Double maxRho;
}

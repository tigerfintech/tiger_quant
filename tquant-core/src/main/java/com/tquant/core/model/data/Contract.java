package com.tquant.core.model.data;

import com.tquant.core.model.enums.SecType;
import lombok.Data;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/08/15
 */
@Data
public class Contract {

  protected String identifier;
  protected String name;
  protected String gatewayName;
  private String symbol;
  private String secType;
  private String currency;
  private String exchange;
  private String market;
  private String expiry;
  private String contractMonth;
  private Double strike;
  private Double multiplier;
  private String right;
  private Double minTick;
  private Integer lotSize;

  public boolean isOption() {
    return secType != null && secType.equalsIgnoreCase(SecType.OPT.name());
  }
}

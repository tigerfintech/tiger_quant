package com.tigerbrokers.quant.model.request;

import lombok.Data;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/08/16
 */
@Data
public class ModifyRequest {

  private long orderId;
  private String symbol;
  private Double limitPrice;
  private Integer quantity;
  private String exchange;
}

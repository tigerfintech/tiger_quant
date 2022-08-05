package com.tquant.core.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/08/16
 */
@Data
@AllArgsConstructor
public class OrderRequest {

  private String symbol;
  private String exchange;
  private String direction;
  private String orderType;
  private int quantity;
  private Double price;
}

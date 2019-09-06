package com.tigerbrokers.quant.model.data;

import com.tigerbrokers.quant.model.request.ModifyRequest;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/08/15
 */
@Data
public class Order {

  private long id;
  private String gatewayName;
  private String account;
  private String symbol;
  private String exchange;
  private String direction;
  private String orderType;
  private double price;
  private int volume;
  private String status;
  private String time;

  private static final Set<String> activeStatus = new HashSet<>();

  {
    activeStatus.add("Initial");
    activeStatus.add("PendingSubmit");
    activeStatus.add("Submitted");
    activeStatus.add("PendingCancel");
  }

  public ModifyRequest createCancelRequest() {
    ModifyRequest request = new ModifyRequest();
    request.setOrderId(id);
    return request;
  }

  public boolean isActive() {
    return status != null && activeStatus.contains(status);
  }
}

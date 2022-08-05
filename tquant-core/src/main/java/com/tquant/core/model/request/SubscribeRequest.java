package com.tquant.core.model.request;

import java.util.Set;
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
public class SubscribeRequest {

  private Set<String> symbols;
}

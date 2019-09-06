package com.tigerbrokers.quant.config;

import lombok.Data;

/**
 * Description: tiger config
 *
 * @author kevin
 * @date 2019/08/16
 */
@Data
public class TigerConfig implements Config {

  private String tigerId;
  private String account;
  private boolean apiLogEnable;
  private String apiLogPath;
  private String privateKey;
  private String serverUrl;
  private String socketUrl;
  private String apiCallbackClassName;
  private Class<?> apiCallbackClass;
}

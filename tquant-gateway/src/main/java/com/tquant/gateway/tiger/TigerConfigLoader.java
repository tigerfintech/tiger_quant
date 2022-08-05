package com.tquant.gateway.tiger;

import com.tquant.core.TigerQuantException;
import com.tquant.core.config.*;

/**
 * Description:
 *
 * @author kevin
 * @date 2022/08/05
 */
public class TigerConfigLoader {
  private static final String TIGER_CONFIG_FILE = "tiger_gateway_setting.json";

  public static TigerConfig loadTigerConfig() {
    TigerConfig config = ConfigLoader.loadConfig(TIGER_CONFIG_FILE, TigerConfig.class);

    if (config.getTigerId() == null) {
      throw new TigerQuantException("tigerId is null");
    }
    if (config.getAccount() == null) {
      throw new TigerQuantException("account is null");
    }
    if (config.getPrivateKey() == null) {
      throw new TigerQuantException("privateKey is null");
    }
    return config;
  }
}

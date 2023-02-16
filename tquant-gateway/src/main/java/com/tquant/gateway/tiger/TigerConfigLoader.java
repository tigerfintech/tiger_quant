package com.tquant.gateway.tiger;

import com.tquant.core.TigerQuantException;
import com.tquant.core.config.*;

import static com.tquant.core.util.QuantUtils.TIGER_CONFIG_PATH_PROP;

/**
 * Description:
 *
 * @author kevin
 * @date 2022/08/05
 */
public class TigerConfigLoader {

  public static TigerConfig loadTigerConfig() {
    String gatewayConfigPath = System.getProperty(TIGER_CONFIG_PATH_PROP);
    if (gatewayConfigPath == null) {
      throw new TigerQuantException("tiger gateway config path cannot be empty");
    }
    TigerConfig config = ConfigLoader.loadConfig(gatewayConfigPath, TigerConfig.class);

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

package com.tigerbrokers.quant.util;

import com.tigerbrokers.quant.config.ConfigLoader;
import com.tigerbrokers.quant.config.TigerConfig;
import com.tigerbrokers.stock.openapi.client.config.ClientConfig;
import com.tigerbrokers.stock.openapi.client.https.client.TigerHttpClient;

/**
 * Description:
 *
 * @author kevin
 * @date 2022/08/03
 */
public class TigerClient {

  public static TigerHttpClient getInstance() {
    TigerConfig config = ConfigLoader.loadTigerConfig();
    ClientConfig clientConfig = ClientConfig.DEFAULT_CONFIG;
    clientConfig.tigerId = config.getTigerId();
    clientConfig.privateKey = config.getPrivateKey();
    clientConfig.defaultAccount = config.getAccount();
    return TigerHttpClient.getInstance().clientConfig(clientConfig);
  }
}

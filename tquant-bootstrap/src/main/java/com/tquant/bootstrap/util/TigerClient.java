package com.tquant.bootstrap.util;

import com.tigerbrokers.stock.openapi.client.config.ClientConfig;
import com.tigerbrokers.stock.openapi.client.https.client.TigerHttpClient;
import com.tquant.gateway.tiger.TigerConfig;
import com.tquant.gateway.tiger.TigerConfigLoader;

/**
 * Description:
 *
 * @author kevin
 * @date 2022/08/03
 */
public class TigerClient {

  public static TigerHttpClient getInstance() {
    TigerConfig config = TigerConfigLoader.loadTigerConfig();
    ClientConfig clientConfig = ClientConfig.DEFAULT_CONFIG;
    clientConfig.tigerId = config.getTigerId();
    clientConfig.privateKey = config.getPrivateKey();
    clientConfig.defaultAccount = config.getAccount();
    return TigerHttpClient.getInstance().clientConfig(clientConfig);
  }
}

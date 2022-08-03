package com.tigerbrokers.quant.config;

import com.alibaba.fastjson.JSON;
import com.tigerbrokers.quant.TigerQuantException;
import com.tigerbrokers.stock.openapi.client.config.ClientConfig;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Description: gateway config loader
 *
 * @author kevin
 * @date 2019/08/15
 */
public class ConfigLoader {

  private static final String TIGER_CONFIG_FILE = "tiger_gateway_setting.json";
  public static final String GLOBAL_FILENAME = "global_setting.json";
  public static final Map<String, Object> GLOBAL_SETTINGS = loadConfig(GLOBAL_FILENAME);

  /**
   * @param fileName config fileName
   * @param clazz config class
   * @param <T> config generic class
   */
  public static <T extends Config> T loadConfig(String fileName, Class clazz) {
    try (BufferedInputStream inputStream =
             new BufferedInputStream(
                 Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName))) {
      return JSON.parseObject(inputStream, clazz);
    } catch (IOException e) {
      throw new TigerQuantException("parse config exception:" + e.getMessage());
    }
  }

  /**
   * single config
   *
   * @param fileName config fileName
   */
  public static Map<String, Object> loadConfig(String fileName) {
    try (BufferedInputStream inputStream =
             new BufferedInputStream(
                 Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName))) {
      return JSON.parseObject(inputStream, Map.class);
    } catch (IOException e) {
      throw new TigerQuantException("parse config exception:" + e.getMessage());
    }
  }

  /**
   * all algorithm configs
   *
   * @param fileName algorithm config fileName
   */
  public static Map<String, Map<String, Object>> loadConfigs(String fileName) {
    try (BufferedInputStream inputStream =
             new BufferedInputStream(
                 Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName))) {
      return JSON.parseObject(inputStream, Map.class);
    } catch (IOException e) {
      throw new TigerQuantException("parse config exception:" + e.getMessage());
    }
  }

  public static TigerConfig loadTigerConfig() {
    TigerConfig config = com.tigerbrokers.quant.config.ConfigLoader.loadConfig(TIGER_CONFIG_FILE, TigerConfig.class);

    if (config.getTigerId() == null) {
      throw new TigerQuantException("tigerId is null");
    }
    if (config.getAccount() == null) {
      throw new TigerQuantException("account is null");
    }
    //if (config.getServerUrl() == null) {
    //  if (clientConfig.serverUrl != null) {
    //    config.setServerUrl(clientConfig.serverUrl);
    //  } else {
    //    throw new TigerQuantException("serverUrl is null");
    //  }
    //}
    //if (config.getSocketUrl() == null) {
    //  if (clientConfig.socketServerUrl != null) {
    //    config.setSocketUrl(clientConfig.socketServerUrl);
    //  }
    //}
    if (config.getPrivateKey() == null) {
      throw new TigerQuantException("privateKey is null");
    }
    return config;
  }
}

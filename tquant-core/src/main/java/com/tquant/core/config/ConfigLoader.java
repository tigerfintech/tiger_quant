package com.tquant.core.config;

import com.alibaba.fastjson.JSON;
import com.tquant.core.TigerQuantException;
import com.tquant.core.model.Config;
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
}

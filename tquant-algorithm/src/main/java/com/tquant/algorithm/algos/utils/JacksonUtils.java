package com.tquant.algorithm.algos.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Description:
 *
 * @author kevin
 * @date 2023/02/13
 */
public class JacksonUtils {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  static {
    JavaTimeModule javaTimeModule = new JavaTimeModule();
    //处理LocalDate
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
    objectMapper.registerModule(javaTimeModule);
    objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
  }

  public static ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  public static <T> T convertValue(Map map, Class<T> clazz) {
    try {
      return getObjectMapper().convertValue(map, clazz);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}

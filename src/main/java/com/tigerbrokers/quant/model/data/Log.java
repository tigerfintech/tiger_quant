package com.tigerbrokers.quant.model.data;

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
public class Log {

  enum LogLevel {
    /**
     * debug level
     */
    DEBUG,
    /**
     * info level
     */
    INFO,
    /**
     * warn level
     */
    WARN,
    /**
     * error level
     */
    ERROR
  }

  private LogLevel level = LogLevel.INFO;
  private String gatewayName;
  private String message;

  public Log(String message) {
    this.message = message;
  }

  public Log(String gatewayName, String message) {
    this.gatewayName = gatewayName;
    this.message = message;
  }
}

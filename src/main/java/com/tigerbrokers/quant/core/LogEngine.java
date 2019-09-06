package com.tigerbrokers.quant.core;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import com.tigerbrokers.quant.TigerQuantException;
import com.tigerbrokers.quant.config.ConfigLoader;
import com.tigerbrokers.quant.event.EventEngine;
import com.tigerbrokers.quant.event.EventHandler;
import com.tigerbrokers.quant.event.EventType;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.slf4j.LoggerFactory;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/08/16
 */
public class LogEngine extends Engine {

  private Logger logger;
  private Level logLevel = Level.INFO;
  private EventHandler logHandler;

  public LogEngine(MainEngine mainEngine, EventEngine eventEngine) {
    this("LogEngine", mainEngine, eventEngine);
  }

  public LogEngine(String engineName, MainEngine mainEngine, EventEngine eventEngine) {
    super(engineName, mainEngine, eventEngine);

    Map<String, Object> settings = ConfigLoader.GLOBAL_SETTINGS;
    if (!(boolean) settings.get("log.enable")) {
      return;
    }
    setLogLevel((String) settings.get("log.level"));

    if ((boolean) settings.get("log.console")) {
      addConsoleHandler();
    }
    if ((boolean) settings.get("log.file")) {
      addFileHandler((String) settings.get("log.path"));
    }
    registerEvent();
  }

  private void setLogLevel(String level) {
    if ("info".equalsIgnoreCase(level)) {
      this.logLevel = Level.INFO;
    } else if ("debug".equalsIgnoreCase(level)) {
      this.logLevel = Level.DEBUG;
    } else if ("warn".equalsIgnoreCase(level)) {
      this.logLevel = Level.WARN;
    } else if ("error".equalsIgnoreCase(level)) {
      this.logLevel = Level.ERROR;
    } else {
      this.logLevel = Level.INFO;
    }
  }

  public void addConsoleHandler() {
    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

    PatternLayoutEncoder encoder = new PatternLayoutEncoder();
    encoder.setContext(loggerContext);
    encoder.setCharset(Charset.forName("UTF-8"));
    encoder.setPattern("%d %level - %msg%n");
    encoder.start();

    ConsoleAppender appender = new ConsoleAppender();
    appender.setEncoder(encoder);
    appender.setContext(loggerContext);
    appender.setName("TigerQuant");
    appender.start();

    logger = loggerContext.getLogger("LogEngine");
    logger.addAppender(appender);
  }

  public void addFileHandler(String logPathName) {
    String filename = "tiger_quant_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".log";
    if (logPathName == null) {
      logPathName = "log/";
    }
    if (!logPathName.endsWith("/")) {
      logPathName += "/";
    }

    File logPath = new File(logPathName);
    if (!logPath.exists()) {
      logPath.mkdir();
    }
    String filepath = logPathName + filename;
    File logFile = new File(filepath);
    if (!logPath.exists()) {
      try {
        logFile.createNewFile();
      } catch (IOException e) {
        throw new TigerQuantException("create log error!");
      }
    }
    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

    PatternLayoutEncoder encoder = new PatternLayoutEncoder();
    encoder.setContext(loggerContext);
    encoder.setCharset(Charset.forName("UTF-8"));
    encoder.setPattern("%d %level - %msg%n");
    encoder.start();

    FileAppender fileAppender = new FileAppender();
    fileAppender.setName("TigerQuant");
    fileAppender.setContext(loggerContext);
    fileAppender.setFile(filepath);
    fileAppender.setEncoder(encoder);
    fileAppender.start();

    logger = loggerContext.getLogger("LogEngine");
    logger.addAppender(fileAppender);

    if (this.logLevel == Level.DEBUG) {
      this.logHandler = event -> {
        if (logger != null) {
          this.logger.debug("{}", event.getEventData());
        }
      };
    } else if (this.logLevel == Level.INFO) {
      this.logHandler = event -> {
        if (logger != null) {
          this.logger.info("{}", event.getEventData());
        }
      };
    } else if (this.logLevel == Level.WARN) {
      this.logHandler = event -> {
        if (logger != null) {
          this.logger.warn("{}", event.getEventData());
        }
      };
    } else if (this.logLevel == Level.ERROR) {
      this.logHandler = event -> {
        if (logger != null) {
          this.logger.error("{}", event.getEventData());
        }
      };
    }
  }

  public void registerEvent() {
    eventEngine.register(EventType.EVENT_LOG, logHandler);
  }
}

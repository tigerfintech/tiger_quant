package com.tquant.bootstrap;

import com.tquant.bootstrap.command.CliRunner;
import com.tquant.bootstrap.command.CommandExecuteTemplate;
import com.tquant.core.TigerQuantException;
import com.tquant.core.core.MainEngine;
import com.tquant.core.event.EventEngine;
import com.tquant.gateway.tiger.TigerGateway;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import static com.tquant.core.util.QuantUtils.ALGO_CONFIG_PATH_PROP;
import static com.tquant.core.util.QuantUtils.TIGER_CONFIG_PATH_PROP;

/**
 * Description: bootstrap
 *
 * @author kevin
 * @date 2019/08/15
 */
public class TigerQuantBootstrap implements CliRunner {

  private static final String GATEWAY_CONFIG_PATH = "g";
  private static final String ALGO_CONFIG_PATH = "a";

  private static final ReentrantLock LOCK = new ReentrantLock();
  private static final Condition STOP = LOCK.newCondition();

  public static void main(String[] args) {
    LOCK.lock();
    try {
      CommandExecuteTemplate.execute(args, "-[a][g]", new TigerQuantBootstrap());

      STOP.await();
    } catch (TigerQuantException e1){
      e1.printStackTrace();
      LOCK.unlock();
    } catch (InterruptedException e) {
      throw new RuntimeException(e.getMessage());
    } finally {
      LOCK.unlock();
    }
  }

  public void startService() {

    EventEngine eventEngine = new EventEngine();
    MainEngine mainEngine = new MainEngine(eventEngine);
    mainEngine.addGateway(new TigerGateway(eventEngine));
    mainEngine.start();
    addHook(mainEngine);
  }

  private void initProperties(String algoConfigPath, String gatewayConfigPath) {
    System.setProperty(ALGO_CONFIG_PATH_PROP, algoConfigPath);
    System.setProperty(TIGER_CONFIG_PATH_PROP, gatewayConfigPath);
  }

  private static void addHook(MainEngine mainEngine) {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      try {
        mainEngine.stop();
      } catch (Exception e) {
        throw new RuntimeException("main stop exception ", e);
      }

      LOCK.lock();
      try {
        STOP.signal();
      } finally {
        LOCK.unlock();
      }
    }, "main-shutdown-hook"));
  }

  @Override
  public Options initOptions() {
    Options options = new Options();
    options.addOption(ALGO_CONFIG_PATH, true, "Algorithm strategy configuration path, cannot be empty");
    options.addOption(GATEWAY_CONFIG_PATH, true, "Gateway config path, cannot be empty");
    return options;
  }

  @Override
  public boolean validateOptions(CommandLine cmdLine) {
    return (cmdLine.hasOption(ALGO_CONFIG_PATH) && cmdLine.hasOption(GATEWAY_CONFIG_PATH));
  }

  @Override
  public void start(CommandLine cmdLine) {
    String algoConfigPath = cmdLine.getOptionValue(ALGO_CONFIG_PATH);
    String gatewayConfigPath = cmdLine.getOptionValue(GATEWAY_CONFIG_PATH);
    initProperties(algoConfigPath, gatewayConfigPath);
    startService();
  }
}

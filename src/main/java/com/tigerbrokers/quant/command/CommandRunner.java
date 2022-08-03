package com.tigerbrokers.quant.command;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Description:
 *
 * @author kevin
 * @date 2022/08/03
 */

public class CommandRunner {
  public static final String PARAM_START = "s";
  public static final String PARAM_END = "e";
  public static final String PARAM_SYMBOL = "sy";
  public static final String CLI_PARAM_F = "f";
  public static final String CLI_PARAM_L = "l";
  public static final String CLI_PARAM_H = "h";
  private static HelpFormatter formatter = new HelpFormatter();

  public CommandRunner() {
  }

  public static void execute(String[] args, String cmdName, CliRunner runner) {
    CommandLineParser parser = new DefaultParser();
    Options options = runner.initOptions();

    try {
      CommandLine cmdLine = parser.parse(options, args);
      if (!runner.validateOptions(cmdLine) || cmdLine.hasOption("help")) {
        formatter.printHelp(cmdName, options);
        return;
      }

      runner.start(cmdLine);
    } catch (ParseException var6) {
      System.out.println("Unexpected exception:" + var6.getMessage());
      formatter.printHelp(cmdName, options);
    }
  }

}

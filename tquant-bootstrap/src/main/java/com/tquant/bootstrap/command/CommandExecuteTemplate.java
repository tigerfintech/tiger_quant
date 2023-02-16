package com.tquant.bootstrap.command;

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

public class CommandExecuteTemplate {

  private static HelpFormatter formatter = new HelpFormatter();

  public CommandExecuteTemplate() {
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

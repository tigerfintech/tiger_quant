package com.tigerbrokers.quant.command;

/**
 * Description:
 *
 * @author kevin
 * @date 2022/08/03
 */
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

public interface CliRunner {
  Options initOptions();

  boolean validateOptions(CommandLine var1);

  void start(CommandLine var1);
}

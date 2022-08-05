package com.tigerbrokers.quant.loader;

import com.tigerbrokers.quant.api.QuoteApi;
import com.tigerbrokers.quant.command.CommandLineRunner;
import com.tigerbrokers.quant.command.CommandExecuteTemplate;
import com.tigerbrokers.quant.gateway.tiger.TigerQuoteApi;
import com.tigerbrokers.quant.model.data.Tick;
import com.tigerbrokers.quant.storage.dao.TickDAO;
import com.tigerbrokers.quant.util.TigerClient;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

/**
 * Description:
 *
 * @author kevin
 * @date 2022/08/04
 */
public class TickLoader implements CommandLineRunner {

  private static final String SYMBOL = "m";
  private static final String START = "s";
  private static final String END = "e";

  private static TickDAO tickDAO = new TickDAO();

  public static void main(String[] args) {
    CommandExecuteTemplate.execute(args, "-[m][s][e]", new TickLoader());
  }

  @Override
  public Options initOptions() {
    Options options = new Options();
    options.addOption(SYMBOL, true, "股票代码");
    options.addOption(START, true, "开始时间，格式:yyyyMMdd");
    options.addOption(END, true, "结束时间，格式:yyyyMMdd");
    return options;
  }

  @Override
  public boolean validateOptions(CommandLine cmdLine) {
    return (cmdLine.hasOption(SYMBOL) && cmdLine.hasOption(START) && cmdLine.hasOption(END)) ;
  }

  @Override
  public void start(CommandLine cmdLine) {
    String symbol;
    String start;
    String end;
    if (cmdLine.hasOption(SYMBOL) && cmdLine.hasOption(START) && cmdLine.hasOption(END)) {
      symbol = cmdLine.getOptionValue(SYMBOL);
      start = cmdLine.getOptionValue(START);
      end = cmdLine.getOptionValue(END);
    } else {
      throw new RuntimeException("symbol, start_date, end_date must not be null");
    }

    List<String> symbols = new ArrayList<>();
    symbols.add(symbol);

    queryAndSaveTick(symbols, parseLocalDate(start), parseLocalDate(end));
  }

  public void queryAndSaveTick(List<String> symbols, LocalDate startDate, LocalDate endDate) {
    QuoteApi quoteApi = new TigerQuoteApi(TigerClient.getInstance());
    Map<String, List<Tick>> tradeTicks = quoteApi.getTradeTicks(symbols);
    for (List<Tick> ticks : tradeTicks.values()) {
      ticks.stream().forEach(tick -> tickDAO.saveTick(tick));
    }
  }

  private static LocalDate parseLocalDate(String date) {
    return LocalDate.of(Integer.parseInt(date.substring(0, 4)), Integer.parseInt(date.substring(4, 6)),
        Integer.parseInt(date.substring(6, 8)));
  }
}

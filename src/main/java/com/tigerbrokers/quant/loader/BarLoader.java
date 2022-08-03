package com.tigerbrokers.quant.loader;

import com.tigerbrokers.quant.api.QuoteApi;
import com.tigerbrokers.quant.command.CommandRunner;
import com.tigerbrokers.quant.command.CliRunner;
import com.tigerbrokers.quant.gateway.tiger.TigerQuoteApi;
import com.tigerbrokers.quant.model.data.Bar;
import com.tigerbrokers.quant.model.enums.BarType;

import com.tigerbrokers.quant.storage.dao.BarDAO;
import com.tigerbrokers.quant.util.TigerClient;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.ibatis.session.SqlSession;

/**
 * Description:
 *
 * @author kevin
 * @date 2022/08/02
 */
public class BarLoader implements CliRunner {

  private static final String SYMBOL = "m";
  private static final String START = "s";
  private static final String END = "e";

  private static BarDAO barDAO = new BarDAO();

  public static void main(String[] args) {
    CommandRunner.execute(args, "-[m][s][e]", new BarLoader());
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

    BarLoader barLoader = new BarLoader();
    List<String> symbols = new ArrayList<>();
    symbols.add(symbol);

    barLoader.queryAndSaveBar(symbols, BarType.day, parseLocalDate(start), parseLocalDate(end));
  }

  public void queryAndSaveBar(List<String> symbols, BarType barType, LocalDate startDate, LocalDate endDate) {
    QuoteApi quoteApi = new TigerQuoteApi(TigerClient.getInstance());
    Map<String, List<Bar>> symbolBars = quoteApi.getBars(symbols, barType, startDate, endDate, null);
    SqlSession sqlSession = barDAO.openSession();
    for (List<Bar> bars : symbolBars.values()) {
      for (Bar bar : bars) {
        barDAO.saveBar(sqlSession,bar);
      }
    }
    barDAO.closeSession(sqlSession);
  }

  private static LocalDate parseLocalDate(String date) {
    return LocalDate.of(Integer.parseInt(date.substring(0, 4)), Integer.parseInt(date.substring(4, 6)),
        Integer.parseInt(date.substring(6, 8)));
  }
}

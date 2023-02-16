package com.tquant.algorithm.algos.utils;

import com.tigerbrokers.stock.openapi.client.https.domain.option.item.OptionBriefItem;
import com.tigerbrokers.stock.openapi.client.struct.enums.Right;
import com.tquant.algorithm.algos.thetagang.RollingSellPutConfig;
import com.tquant.core.model.data.Position;
import com.tquant.core.model.data.RealtimeQuote;
import com.tquant.core.model.data.Tick;
import java.time.LocalDate;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description:
 *
 * @author kevin
 * @date 2023/02/09
 */
public class Utils {
  private static DateTimeFormatter YYYY_MM_DD_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
  private static DateTimeFormatter YYYY_MM_FORMAT = DateTimeFormatter.ofPattern("yyyyMM");

  public static LocalDate contractDateToDatetime(String expiration) {
    if (expiration.length() == 8) {
      return LocalDate.parse(expiration, YYYY_MM_DD_FORMAT);
    } else {
      return LocalDate.parse(expiration, YYYY_MM_FORMAT);
    }
  }

  public static int optionDte(String expiration) {
    if (expiration == null) {
      return 0;
    }
    LocalDate today = LocalDate.now();
    LocalDate contractDate = convertStringToLocalDate(expiration);
    return (int) java.time.Duration.between(today.atStartOfDay(), contractDate.atStartOfDay()).toDays();
  }

  public static LocalDate convertStringToLocalDate(String date) {
    return LocalDate.parse(date, YYYY_MM_DD_FORMAT);
  }

  public static Map<String, List<Position>> portfolioPositionsToDict(List<Position> portfolioPositions) {
    Map<String, List<Position>> d = new HashMap<>();
    for (Position p : portfolioPositions) {
      String symbol = p.getContract().getSymbol();
      if (!d.containsKey(symbol)) {
        d.put(symbol, new ArrayList<>());
      }
      d.get(symbol).add(p);
    }
    return d;
  }

  public static double positionPnl(Position p) {
    return p.getUnrealizedPnl() / Math.abs(p.getAverageCost() * p.getPosition() * (p.getContract().getMultiplier() != null ? p.getContract()
                            .getMultiplier() : 1));
  }

  /**
   * @param ticker
   * @return
   */
  public static double midpointOrMarketPrice(Tick ticker) {
    if (ticker.getMidpoint() <= 0) {
      return ticker.getLatestPrice();
    } else {
      return ticker.getMidpoint();
    }
  }

  public static double midpointOrAskPrice(Tick ticker) {
    if (ticker.getMidpoint() <= 0D) {
      return ticker.getAskPrice();
    } else {
      return ticker.getMidpoint();
    }
  }

  public static int countShortOptionPositions(String symbol, Map<String, List<Position>> portfolioPositions, String right) {
    if (portfolioPositions.containsKey(symbol)) {
      return (int) Math.floor(
          -portfolioPositions.get(symbol)
              .stream()
              .filter(p -> p.isOption(right) && p.getPosition() < 0)
              .mapToDouble(Position::getPosition)
              .sum()
      );
    }

    return 0;
  }

  public static double getStrikeLimit(RollingSellPutConfig config, String symbol, String right) {
    String pOrC = "calls";
    if (right.equalsIgnoreCase(Right.PUT.name())) {
      pOrC = "puts";
    }
    Map<String, RollingSellPutConfig.Symbol> symbols = config.getSymbols();
    if (symbols != null && symbols.containsKey(symbol)) {
      if (pOrC.equalsIgnoreCase("calls")) {
        RollingSellPutConfig.Symbol.Calls calls = symbols.get(symbol).getCalls();
        if (calls != null) {
          return calls.getStrikeLimit();
        } else {
          return 0D;
        }
      } else {
        RollingSellPutConfig.Symbol.Puts puts = symbols.get(symbol).getPuts();
        if (puts != null) {
          return puts.getStrikeLimit();
        } else {
          return 0D;
        }
      }
    }
    return 0D;
  }

  public static double getCallCap(RollingSellPutConfig config) {
    RollingSellPutConfig.WriteWhen writeWhen = config.getWriteWhen();
    if (writeWhen != null) {
      RollingSellPutConfig.WriteWhen.Calls calls = writeWhen.getCalls();
      if (calls != null) {
        Double capFactor = calls.getCapFactor();
        if (capFactor != null) {
          return Math.max(0, Math.min(1.0, (double) capFactor));
        }
      }
    }
    return 1.0;
  }

  public static Double getTargetDelta(RollingSellPutConfig config, String symbol, String right) {
    RollingSellPutConfig.Symbol configSymbol = config.getSymbols().get(symbol);
    if (isCall(right)) {
      return configSymbol.getCalls().getDelta();
    } else if (isPut(right)) {
      return configSymbol.getPuts().getDelta();
    }
    return null;
  }

  public static Double getLowestPrice(Tick tick) {
    return Math.min(tick.getMidpoint(), tick.getLatestPrice());
  }

  public static Double getHighestPrice(Tick tick) {
    return Math.max(tick.getMidpoint(), tick.getLatestPrice());
  }

  public static RealtimeQuote convertToRealtimeQuote(OptionBriefItem optionBriefItem) {
    RealtimeQuote realtimeQuote = new RealtimeQuote();
    realtimeQuote.setSymbol(optionBriefItem.getSymbol());
    realtimeQuote.setOpen(optionBriefItem.getOpen());
    realtimeQuote.setHigh(optionBriefItem.getHigh());
    realtimeQuote.setLow(optionBriefItem.getLow());
    realtimeQuote.setClose(optionBriefItem.getLatestPrice());
    realtimeQuote.setPreClose(optionBriefItem.getPreClose());
    realtimeQuote.setLatestPrice(optionBriefItem.getLatestPrice());
    realtimeQuote.setLatestTime(optionBriefItem.getTimestamp());
    if (optionBriefItem.getAskPrice() != null) {
      realtimeQuote.setAskPrice(optionBriefItem.getAskPrice());
    }
    if (optionBriefItem.getAskSize() != null) {
      realtimeQuote.setAskSize(Long.valueOf(optionBriefItem.getAskSize()));
    }
    if (optionBriefItem.getBidPrice() != null) {
      realtimeQuote.setBidPrice(optionBriefItem.getBidPrice());
    }
    if (optionBriefItem.getBidSize() != null) {
      realtimeQuote.setBidSize(Long.valueOf(optionBriefItem.getBidSize()));
    }
    realtimeQuote.setVolume(Long.valueOf(optionBriefItem.getVolume()));
    realtimeQuote.setOpenInterest(optionBriefItem.getOpenInterest());
    return realtimeQuote;
  }

  public static double roundToNearest(double x) {
    double firstDecimal = (x % 1) * 10;

    if (firstDecimal < 5) {
      return Math.round(x * 10) / 10;
    } else if (firstDecimal > 5) {
      return x - firstDecimal / 10 + 0.5;
    } else {
      return Math.floor(x * 10) / 10;
    }
  }

  public static boolean isPut(String right) {
    return right != null && right.equalsIgnoreCase(Right.PUT.name());
  }

  public static boolean isCall(String right) {
    return right != null && right.equalsIgnoreCase(Right.CALL.name());
  }

  public static boolean isValidStrike(String right, double strike, double marketPrice, Double strikeLimit) {
    if (Utils.isPut(right)) {
      if (strikeLimit != null) {
        // put 要尽量低的价格
        return strike <= marketPrice && strike <= strikeLimit;
      } else {
        return strike <= marketPrice;
      }
    } else if (Utils.isCall(right)) {
      // call 要尽量高的价格
      if (strikeLimit != null) {
        return strike >= marketPrice && strike >= strikeLimit;
      } else {
        return strike >= marketPrice;
      }
    }
    return false;
  }
}

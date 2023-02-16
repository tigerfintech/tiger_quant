package com.tquant.algorithm.algos.thetagang;

import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description:
 *
 * @author kevin
 * @date 2023/02/09
 */
@Data
@NoArgsConstructor
public class RollingSellPutConfig {

  private boolean enable;
  private Account account;
  private boolean cancelOrders;
  private int marketDataType;
  private Orders orders;
  private OptionChains optionChains;
  private RollWhen rollWhen;
  private WriteWhen writeWhen;
  private Target target;
  private Map<String, Symbol> symbols;

  @Data
  @NoArgsConstructor
  public static class Account {
    private String accountId;
    private Boolean cancelOrders;
    private Double marginUsage;
  }

  @Data
  @NoArgsConstructor
  public static class Orders {

    private String exchange;
    private Algo algo;

    @Data
    @NoArgsConstructor
    public static class Algo {

      private String strategy;
      private List<String[]> params;
    }
  }

  @Data
  @NoArgsConstructor
  public static class OptionChains {

    private int expirations;
    private int strikes;
  }

  @Data
  @NoArgsConstructor
  public static class RollWhen {

    private Double pnl;
    private Integer dte;
    private Integer minDte;
    private Integer maxDte;
    private Double closeAtPnl;
    private Double minPnl;
    private Calls calls;
    private Puts puts;

    @Data
    @NoArgsConstructor
    public static class Calls {

      private boolean itm;
      private boolean creditOnly;
    }

    @Data
    @NoArgsConstructor
    public static class Puts {

      private boolean itm;
      private boolean creditOnly;
    }
  }

  @Data
  @NoArgsConstructor
  public static class WriteWhen {

    private Calls calls;
    private Puts puts;

    @Data
    @NoArgsConstructor
    public static class Calls {

      private Boolean green;
      private Double capFactor;
    }

    @Data
    @NoArgsConstructor
    public static class Puts {

      private boolean red;
    }
  }

  @Data
  @NoArgsConstructor
  public static class Target {

    private int dte;
    private double delta;
    private double maximumNewContractsPercent;
    private int minimumOpenInterest;
  }

  @Data
  @NoArgsConstructor
  public static class Symbol {

    private double weight;
    private Puts puts;
    private Calls calls;
    private String primaryExchange;
    private double delta;

    @Data
    @NoArgsConstructor
    public static class Puts {

      private double delta;
      private double strikeLimit;
    }

    @Data
    @NoArgsConstructor
    public static class Calls {

      private double delta;
      private double strikeLimit;
    }
  }
}

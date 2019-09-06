package com.tigerbrokers.quant.model.data;

import com.tigerbrokers.quant.gateway.tiger.TigerGateway;
import com.tigerbrokers.stock.openapi.client.https.domain.contract.item.ContractItem;
import com.tigerbrokers.stock.openapi.client.https.domain.future.item.FutureContractItem;
import com.tigerbrokers.stock.openapi.client.struct.enums.SecType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/08/15
 */
@Data
public class Contract {

  protected String identifier;
  protected String name;
  protected String gatewayName;
  private String symbol;
  private String secType;
  private String currency;
  private String exchange;
  private String market;
  private String expiry;
  private String contractMonth;
  private Double strike;
  private Double multiplier;
  private String right;
  private Double minTick;
  private Integer lotSize;

  public static List<Contract> toContracts(List<ContractItem> contractItems) {
    List<Contract> contracts = new ArrayList<>();
    for (ContractItem item : contractItems) {
      contracts.add(toContract(item));
    }
    return contracts;
  }

  public static List<Contract> toFuturesContracts(List<FutureContractItem> contractItems) {
    List<Contract> contracts = new ArrayList<>();
    for (FutureContractItem item : contractItems) {
      if (item.getContractMonth() == null || item.getContractMonth().isEmpty()) {
        continue;
      }
      contracts.add(toContract(item));
    }
    return contracts;
  }

  public static Contract toContract(FutureContractItem contractItem) {
    Contract contract = new Contract();
    contract.setIdentifier(contractItem.getContractCode());
    contract.setName(contractItem.getName());
    int contractMonthLen = contractItem.getContractCode().length() - 4;
    contract.setSymbol(contractItem.getContractCode().substring(0, contractMonthLen));
    contract.setSecType(SecType.FUT.name());
    contract.setExchange(contractItem.getExchangeCode());
    contract.setContractMonth(contractItem.getContractMonth());
    contract.setExpiry(contractItem.getLastTradingDate());
    BigDecimal multiplier = contractItem.getMultiplier();
    if (multiplier == null) {
      contract.setMultiplier(0D);
    } else {
      contract.setMultiplier(multiplier.doubleValue());
    }
    contract.setGatewayName(TigerGateway.GATEWAY_NAME);
    return contract;
  }

  public static Contract toContract(ContractItem contractItem) {
    Contract contract = new Contract();
    contract.setIdentifier(contractItem.getSymbol());
    contract.setName(contractItem.getName());
    contract.setSymbol(contractItem.getSymbol());
    contract.setSecType(contractItem.getSecType());
    contract.setCurrency(contractItem.getCurrency());
    contract.setExchange(contractItem.getExchange());
    contract.setMarket(contractItem.getMarket());
    contract.setExpiry(contractItem.getExpiry());
    contract.setContractMonth(contractItem.getContractMonth());
    contract.setStrike(contractItem.getStrike());
    contract.setMultiplier(contractItem.getMultiplier());
    contract.setRight(contractItem.getRight());
    contract.setGatewayName(TigerGateway.GATEWAY_NAME);
    return contract;
  }
}

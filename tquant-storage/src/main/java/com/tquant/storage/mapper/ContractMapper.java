package com.tquant.storage.mapper;

import com.tquant.core.model.data.Contract;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Description:
 *
 * @author kevin
 * @date 2022/03/04
 */
@Mapper
public interface ContractMapper {

  String SAVE_CONTRACT_SQL = "insert into contract(identifier,symbol,sec_type,currency,exchange,market,expiry,"
      + "contract_month,strike,multiplier,`right`,min_tick,lot_size,create_time) "
      + "values (#{identifier},#{symbol},#{secType},#{currency},#{exchange},#{market},#{expiry},"
      + "#{contractMonth},#{strike},#{multiplier},#{right},#{minTick},#{lotSize},now())"
      + "ON DUPLICATE KEY UPDATE "
      + " identifier=VALUES(identifier),symbol=VALUES(symbol)";

  String QUERY_CONTRACTS_SQL =
      "select identifier,symbol,sec_type,currency,exchange,market,expiry,contract_month,strike,"
          + "multiplier,`right`,min_tick,lot_size,create_time from contract";

  String QUERY_CONTRACT_SQL = "select identifier,symbol,sec_type,currency,exchange,market,expiry,contract_month,strike,"
      + "multiplier,`right`,min_tick,lot_size,create_time from contract where identifier=#{identifier} limit 1";

  @Insert(SAVE_CONTRACT_SQL)
  void saveContract(Contract bar);

  @Select(QUERY_CONTRACTS_SQL)
  List<Contract> queryContracts();

  @Select(QUERY_CONTRACT_SQL)
  Contract queryContract(@Param("identifier") String identifier);
}

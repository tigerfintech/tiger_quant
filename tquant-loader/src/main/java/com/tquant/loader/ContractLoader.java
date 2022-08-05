package com.tquant.loader;

import com.tquant.loader.command.CommandExecuteTemplate;
import com.tquant.loader.command.CliRunner;
import com.tquant.core.model.data.Contract;

import com.tigerbrokers.stock.openapi.client.config.ClientConfig;
import com.tigerbrokers.stock.openapi.client.https.client.TigerHttpClient;
import com.tigerbrokers.stock.openapi.client.struct.enums.SecType;
import com.tquant.storage.dao.ContractDAO;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.ibatis.session.SqlSession;

/**
 * Description: update contract once per day
 *
 * @author kevin
 * @date 2022/03/04
 */
public class ContractLoader implements CliRunner {
  private static final String SEC_TYPE = "t";

  private static ContractDAO contractDAO = new ContractDAO();

  public static void main(String[] args) {
    CommandExecuteTemplate.execute(args, "-[t]", new ContractLoader());
  }

  @Override
  public Options initOptions() {
    Options options = new Options();
    options.addOption(SEC_TYPE, true, "交易品种，包括 SEC/FUT");
    return options;
  }

  @Override
  public boolean validateOptions(CommandLine cmdLine) {
    return cmdLine.hasOption(SEC_TYPE);
  }

  @Override
  public void start(CommandLine cmdLine) {
    queryAndSaveContracts(SecType.valueOf(cmdLine.getOptionValue(SEC_TYPE)));
  }

  private static void queryAndSaveContracts(SecType secType) {
    //TigerConfig config = ConfigLoader.loadTigerConfig();
    //ClientConfig clientConfig = ClientConfig.DEFAULT_CONFIG;
    //clientConfig.tigerId = config.getTigerId();
    //clientConfig.privateKey = config.getPrivateKey();
    //clientConfig.defaultAccount = config.getAccount();
    //TigerHttpClient serverClient =
    //    TigerHttpClient.getInstance().clientConfig(clientConfig);
    //TradeApi tradeApi = new TigerTradeApi(serverClient, config.getAccount());
    //
    //SqlSession sqlSession = contractDAO.openSession();
    //List<Contract> contracts = tradeApi.getContracts(secType);
    //for (Contract contract : contracts) {
    //  contractDAO.saveContract(sqlSession, contract);
    //}
    //contractDAO.closeSession(sqlSession);
  }
}

package com.tigerbrokers.quant.loader;

import com.tigerbrokers.quant.api.TradeApi;
import com.tigerbrokers.quant.config.ConfigLoader;
import com.tigerbrokers.quant.config.TigerConfig;
import com.tigerbrokers.quant.gateway.tiger.TigerTradeApi;
import com.tigerbrokers.quant.model.data.Contract;
import com.tigerbrokers.quant.storage.dao.ContractDAO;
import com.tigerbrokers.stock.openapi.client.config.ClientConfig;
import com.tigerbrokers.stock.openapi.client.https.client.TigerHttpClient;
import com.tigerbrokers.stock.openapi.client.struct.enums.SecType;
import java.util.List;
import org.apache.ibatis.session.SqlSession;

/**
 * Description: update contract once per day
 *
 * @author kevin
 * @date 2022/03/04
 */
public class ContractLoader {

  private static ContractDAO contractDAO = new ContractDAO();

  public static void main(String[] args) {
    queryAndSaveContracts();
  }

  private static void queryAndSaveContracts() {
    TigerConfig config = ConfigLoader.loadTigerConfig();
    ClientConfig clientConfig = ClientConfig.DEFAULT_CONFIG;
    clientConfig.tigerId = config.getTigerId();
    clientConfig.privateKey = config.getPrivateKey();
    clientConfig.defaultAccount = config.getAccount();
    TigerHttpClient serverClient =
        TigerHttpClient.getInstance().clientConfig(clientConfig);
    TradeApi tradeApi = new TigerTradeApi(serverClient, config.getAccount());

    SqlSession sqlSession = contractDAO.openSession();
    List<Contract> contracts = tradeApi.getContracts(SecType.STK);
    for (Contract contract : contracts) {
      contractDAO.saveContract(sqlSession, contract);
    }
    contractDAO.closeSession(sqlSession);
  }
}

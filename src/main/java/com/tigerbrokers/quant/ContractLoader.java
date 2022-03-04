package com.tigerbrokers.quant;

import com.tigerbrokers.quant.api.TradeApi;
import com.tigerbrokers.quant.config.ConfigLoader;
import com.tigerbrokers.quant.config.TigerConfig;
import com.tigerbrokers.quant.gateway.tiger.TigerTradeApi;
import com.tigerbrokers.quant.model.data.Contract;
import com.tigerbrokers.quant.storage.dao.ContractDAO;
import com.tigerbrokers.stock.openapi.client.https.client.TigerHttpClient;
import com.tigerbrokers.stock.openapi.client.struct.enums.SecType;
import java.util.List;
import org.apache.ibatis.session.SqlSession;

/**
 * Description:
 *
 * @author kevin
 * @date 2022/03/04
 */
public class ContractLoader {

  public static void main(String[] args) {
    queryAndSaveContracts();
  }

  private static void queryAndSaveContracts() {
    TigerConfig config = ConfigLoader.loadTigerConfig();
    TigerHttpClient serverClient =
        new TigerHttpClient(config.getServerUrl(), config.getTigerId(), config.getPrivateKey());
    TradeApi tradeApi = new TigerTradeApi(serverClient, config.getAccount());
    ContractDAO contractDAO = new ContractDAO();
    SqlSession sqlSession = contractDAO.getOpenSession();
    List<Contract> contracts = tradeApi.getContracts(SecType.STK);
    for (Contract contract : contracts) {
      contractDAO.saveContract(sqlSession, contract);
    }
    contractDAO.closeSession(sqlSession);
  }
}

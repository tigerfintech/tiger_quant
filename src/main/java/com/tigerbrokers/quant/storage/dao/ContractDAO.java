package com.tigerbrokers.quant.storage.dao;

import com.tigerbrokers.quant.model.data.Contract;
import com.tigerbrokers.quant.storage.mapper.ContractMapper;
import java.util.List;
import org.apache.ibatis.session.SqlSession;

/**
 * Description:
 *
 * @author kevin
 * @date 2022/03/04
 */
public class ContractDAO extends BaseDAO {

  public SqlSession getOpenSession() {
    return super.openSession();
  }

  public void closeSession(SqlSession sqlSession) {
    if (sqlSession != null) {
      sqlSession.close();
    }
  }

  public void saveContract(SqlSession sqlSession, Contract contract) {
    sqlSession.getMapper(ContractMapper.class).saveContract(contract);
    sqlSession.commit();
  }

  public List<Contract> queryContracts() {
    SqlSession sqlSession = null;
    try {
      sqlSession = openSession();
      List<Contract> contracts = sqlSession.getMapper(ContractMapper.class).queryContracts();
      sqlSession.commit();
      return contracts;
    } finally {
      sqlSession.close();
    }
  }

  public Contract queryContract(String identifier) {
    SqlSession sqlSession = null;
    try {
      sqlSession = openSession();
      Contract contract = sqlSession.getMapper(ContractMapper.class).queryContract(identifier);
      sqlSession.commit();
      return contract;
    } finally {
      sqlSession.close();
    }
  }
}

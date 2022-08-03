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

  public void saveContract(SqlSession sqlSession, Contract contract) {
    sqlSession.getMapper(ContractMapper.class).saveContract(contract);
    sqlSession.commit();
  }

  public List<Contract> queryContracts(SqlSession sqlSession) {
    return sqlSession.getMapper(ContractMapper.class).queryContracts();
  }

  public Contract queryContract(SqlSession sqlSession, String identifier) {
    return sqlSession.getMapper(ContractMapper.class).queryContract(identifier);
  }
}

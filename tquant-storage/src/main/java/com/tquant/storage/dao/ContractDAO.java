package com.tquant.storage.dao;

import com.tquant.core.model.data.Contract;
import com.tquant.storage.mapper.ContractMapper;
import java.util.List;
import org.apache.ibatis.session.SqlSession;

/**
 * Description:
 *
 * @author kevin
 * @date 2022/03/04
 */
public class ContractDAO extends BaseDAO {

  private static SqlSession sqlSession = new BaseDAO().openSession();

  public void saveContract(Contract contract) {
    sqlSession.getMapper(ContractMapper.class).saveContract(contract);
    sqlSession.commit();
  }

  public List<Contract> queryContracts() {
    return sqlSession.getMapper(ContractMapper.class).queryContracts();
  }

  public Contract queryContract(String identifier) {
    return sqlSession.getMapper(ContractMapper.class).queryContract(identifier);
  }
}

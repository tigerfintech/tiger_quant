package com.tquant.algorithm.algos;

import com.tquant.algorithm.algos.thetagang.PortfolioManager;
import com.tquant.algorithm.algos.thetagang.RollingSellPutConfig;
import com.tquant.algorithm.algos.utils.JacksonUtils;
import com.tquant.core.core.AlgoTemplate;
import com.tquant.core.model.data.Order;
import com.tquant.core.model.data.Tick;
import com.tquant.core.model.data.Trade;

/**
 * Description:
 *
 * @author kevin
 * @date 2023/02/09
 */
public class ThetaGangAlgo extends AlgoTemplate {

  private PortfolioManager portfolioManager;

  @Override
  public void onStart() {
    portfolioManager = new PortfolioManager(JacksonUtils.convertValue(settings, RollingSellPutConfig.class),
        getAlgoEngine().getMainEngine().getLogger());
    portfolioManager.manage();
  }

  @Override
  public void onTick(Tick tick) {

  }

  @Override
  public void onOrder(Order order) {
    portfolioManager.orderStatusEvent(order);
  }

  @Override
  public void onTrade(Trade trade) {

  }


}

package com.tigerbrokers.quant.core;

import com.tigerbrokers.quant.event.EventEngine;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/08/16
 */
@NoArgsConstructor
@Data
public abstract class Engine {

  protected String engineName;
  protected MainEngine mainEngine;
  protected EventEngine eventEngine;

  public Engine(String engineName, MainEngine mainEngine, EventEngine eventEngine) {
    this.engineName = engineName;
    this.mainEngine = mainEngine;
    this.eventEngine = eventEngine;
  }

  public void start() {

  }

  public void stop() {

  }
}

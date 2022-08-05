package com.tquant.core.event;

/**
 * Description:Event Type
 *
 * @author kevin
 * @date 2019/08/15
 */
public enum EventType {
  /**
   * Timer event, sent every 1 second
   */
  EVENT_TIMER,
  /**
   * Log event
   */
  EVENT_LOG,

  /**
   * TICK market return
   */
  EVENT_TICK,

  /**
   * Bar event
   */
  EVENT_BAR,

  /**
   * Trade event
   */
  EVENT_TRADE,
  /**
   * Order return
   */
  EVENT_ORDER,
  /**
   * Assets return
   */
  EVENT_ASSET,
  /**
   * Position return
   */
  EVENT_POSITION,

  /**
   * Contract return
   */
  EVENT_CONTRACT,

  /**
   * Account return
   */
  EVENT_ACCOUNT,

  /**
   * Algo event
   */
  EVENT_ALGO,
  /**
   * Error return
   */
  EVENT_ERROR,

}

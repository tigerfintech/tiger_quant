package com.tquant.core.event;

import lombok.Data;

/**
 * Description: Event
 *
 * @author kevin
 * @date 2019/08/15
 */
@Data
public class Event {

  /**
   * event type
   */
  private EventType eventType;
  /**
   * event data
   */
  private Object eventData;

  public Event(EventType eventType) {
    this.eventType = eventType;
  }

  public Event(EventType eventType, Object eventData) {
    this.eventType = eventType;
    this.eventData = eventData;
  }
}

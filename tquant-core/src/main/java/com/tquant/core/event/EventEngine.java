package com.tquant.core.event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/08/15
 */
public class EventEngine {

  /**
   * event queue
   */
  private LinkedBlockingQueue<Event> queue;
  /**
   * event engine switch
   */
  private boolean active;

  /**
   * event process thread
   */
  private Thread thread;
  /**
   * trigger timer event thread
   */
  private Thread timer;
  /**
   * timer working state
   */
  private boolean timerActive;
  /**
   * timer trigger duration,default 1 second
   */
  private int timerDuration;

  /**
   * event handlers
   */
  private Map<EventType, List<EventHandler>> handlers;

  /**
   * common handlers to process all kinds of event
   */
  private List<EventHandler> commonHandlers;

  public EventEngine() {
    this.queue = new LinkedBlockingQueue<>(8192);
    this.active = false;
    this.thread = new Thread(() -> run());
    this.timer = new Thread(() -> runTimer());
    this.timerActive = false;
    this.timerDuration = 1000;
    this.handlers = new ConcurrentHashMap<>();
    this.commonHandlers = new CopyOnWriteArrayList<>();
  }

  private synchronized void run() {
    while (active) {
      try {
        Event event = queue.poll(1000, TimeUnit.MILLISECONDS);
        if (event == null) {
          continue;
        }
        process(event);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private void process(Event event) {
    if (handlers.containsKey(event.getEventType())) {
      for (EventHandler eventHandler : handlers.get(event.getEventType())) {
        eventHandler.process(event);
      }
    }
    for (EventHandler eventHandler : commonHandlers) {
      eventHandler.process(event);
    }
  }

  private void runTimer() {
    while (timerActive) {
      Event event = new Event(EventType.EVENT_TIMER);
      try {
        queue.offer(event);
        Thread.sleep(timerDuration);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  public void register(EventType eventType, EventHandler handler) {
    List<EventHandler> handlerList = this.handlers.getOrDefault(eventType, new CopyOnWriteArrayList<>());
    if (!handlerList.contains(handler)) {
      handlerList.add(handler);
    }
    this.handlers.putIfAbsent(eventType, handlerList);
  }

  public void unregister(EventType eventType, EventHandler handler) {
    List<EventHandler> handlerList = this.handlers.get(eventType);
    if (handlerList != null) {
      if (handlerList.contains(handler)) {
        handlerList.remove(handler);
      }
    }
    if (handlerList == null || handlerList.size() == 0) {
      this.handlers.remove(eventType);
    }
  }

  public void registerCommonHandler(EventHandler handler) {
    if (this.commonHandlers != null) {
      if (!this.commonHandlers.contains(handler)) {
        this.commonHandlers.add(handler);
      }
    }
  }

  public void unregisterCommonHandler(EventHandler handler) {
    if (this.commonHandlers != null) {
      if (this.commonHandlers.contains(handler)) {
        this.commonHandlers.remove(handler);
      }
    }
  }

  public void put(Event event) {
    this.queue.offer(event);
  }

  /**
   * start event engine
   */
  public void start() {
    start(false);
  }

  /**
   * start engine
   *
   * @param timer :true start,false not start
   */
  public void start(boolean timer) {
    this.active = true;
    this.thread.start();
    if (timer) {
      this.timerActive = true;
      this.timer.start();
    }
  }

  /**
   * stop event engine
   */
  public void stop() {
    this.active = false;
    this.timerActive = false;
    try {
      this.timer.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}

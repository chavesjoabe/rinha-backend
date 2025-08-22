package org.jchaves.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ExecutorConfig {

  private final ThreadPoolExecutor threadPoolExecutor;

  public ExecutorConfig() {
    this.threadPoolExecutor = new ThreadPoolExecutor(
        10, // core threads
        10, // max platform threads
        60L,
        TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(100),
        Thread.ofVirtual().factory(),
        new ThreadPoolExecutor.CallerRunsPolicy());
  }

  public ExecutorService getExecutor() {
    return threadPoolExecutor;
  }
}

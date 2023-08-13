package org.gnucash;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Loggers {

  public static void main(String[] args) {

    Logger logger = LoggerFactory.getLogger(Loggers.class);

    logger.trace("Hello World");
    logger.debug("Hello World");
    logger.info("Hello World");
    logger.warn("Hello World");
    logger.error("Hello World");
  }
}
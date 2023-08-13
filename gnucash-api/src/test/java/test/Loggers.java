package test;

import java.util.logging.Level;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import java.util.logging.Logger;

public class Loggers {
  private static final Logger logger = Logger.getLogger(Class.class.getName());

  public static void main(String[] args) {

    // Logger logger = LoggerFactory.getLogger(Loggers.class);

    // logger.trace("Hello World");
    logger.log(Level.FINE, "Hello World");
    // logger.debug("Hello World");

    logger.info("Hello World");
    // logger.warn("Hello World");

    // logger.error("Hello World");

  }
}
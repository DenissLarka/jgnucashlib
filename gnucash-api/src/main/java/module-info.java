/**
 * @author Deniss Larka on 2021.12.06
 */
module gnucash.api {
  requires static org.slf4j;
  requires java.desktop;
  requires jakarta.xml.bind;
  requires java.logging;

//  requires slf4j.simple;

  exports org.gnucash.currency;
  exports org.gnucash.numbers;
  exports org.gnucash.read;
  exports org.gnucash.read.impl;
  exports org.gnucash.write;
  exports org.gnucash.write.impl;
}
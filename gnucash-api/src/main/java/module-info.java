/**
 * @author Deniss Larka
 */
module gnucash.api {
	requires static org.slf4j;
	requires java.desktop;
	requires jakarta.xml.bind;
	// requires junit;
	
	exports org.gnucash.currency;
	exports org.gnucash.numbers;
	exports org.gnucash.read;
	exports org.gnucash.read.aux;
	exports org.gnucash.read.spec;
	exports org.gnucash.read.impl;
	exports org.gnucash.read.impl.aux;
	exports org.gnucash.read.impl.spec;
	
	exports org.gnucash.write;
	exports org.gnucash.write.spec;
	exports org.gnucash.write.impl;
	exports org.gnucash.write.impl.aux;
	exports org.gnucash.write.impl.spec;
}
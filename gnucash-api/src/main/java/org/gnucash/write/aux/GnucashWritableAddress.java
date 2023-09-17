package org.gnucash.write.aux;

import org.gnucash.read.aux.Address;

public interface GnucashWritableAddress extends Address {

	void setAddressName(String a);

	void setAddressLine1(String a);

	void setAddressLine2(String a);

	void setAddressLine3(String a);

	void setAddressLine4(String a);

	void setTel(String a);

	void setFax(String a);

	void setEmail(String a);
}

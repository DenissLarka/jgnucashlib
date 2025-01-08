package org.gnucash.read.auxiliary;

public interface GCshAddress {

    /**
     *
     * @return name as used in the address
     */
    String getAddressName();

    /**
     *
     * @return first line below the name
     */
    String getAddressLine1();

    /**
     *
     * @return second and last line below the name
     */
    String getAddressLine2();
    /**
     *
     * @return third and last line below the name
     */
    String getAddressLine3();
    /**
     *
     * @return fourth and last line below the name
     */
    String getAddressLine4();

    /**
     *
     * @return telephone
     */
    String getTel();

    /**
     *
     * @return Fax
     */
    String getFax();

    /**
     *
     * @return Email
     */
    String getEmail();
    
}

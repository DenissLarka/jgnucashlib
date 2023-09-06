package org.gnucash.read;

import java.util.Collection;
import java.util.Locale;

import org.gnucash.generated.GncV2.GncBook.GncGncVendor.VendorTerms;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashCustVendInvoice.ReadVariant;
import org.gnucash.read.spec.GnucashVendorBill;
import org.gnucash.read.spec.GnucashVendorJob;

public interface GnucashVendor extends GnucashObject {

    /**
     * The prefered taxtable to use with this vendor (may be null).
     * @see {@link #getVendorTaxTableID()}
     */
    VendorTerms getVendorTerms();

    /**
     * Date is not checked so invoiced that have entered payments in the future are considered payed.
     * @return the current number of unpayed invoices
     */
    int getNofOpenInvoices();

    /**
     * @return the sum of payments for invoices to this client
     */
    FixedPointNumber getIncomeGenerated();

    /**
     * @see #getIncomeGenerated()
     * formatet acording to the current locale's currency-format
     */
    String getIncomeGeneratedFormatet();

    /**
     * @see #getIncomeGenerated()
     * formatet acording to the given locale's currency-format
     */
    String getIncomeGeneratedFormatet(Locale l);

    /**
     * @return the sum of left to pay unpayed invoiced
     */
    FixedPointNumber getOutstandingValue();

    /**
     * @see #getOutstandingValue()
     * formatet acording to the current locale's currency-format
     */
    String getOutstandingValueFormatet();

    /**
     *
     * @see #getOutstandingValue()
     * formatet acording to the given locale's currency-format
     */
    String getOutstandingValueFormatet(Locale l);


    interface Address {

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


    /**
     * The gnucash-file is the top-level class to contain everything.
     * @return the file we are associated with
     */
    GnucashFile getGnucashFile();

    /**
     * @return the unique-id to identify this object with across name- and hirarchy-changes
     */
    String getId();

    /**
     * @return the UNMODIFIABLE collection of jobs that have this vendor associated with them.
     */
    Collection<GnucashVendorJob> getJobs();

    /**
     *
     * @return the user-assigned number of this vendor (may contain non-digits)
     */
    String getNumber();

    /**
     *
     * @return the name of the vendor
     */
    String getName();

    /**
     * @return the address including the name
     */
    GnucashVendor.Address getAddress();

    // ----------------------------

    Collection<GnucashVendorBill> getUnpayedInvoices(ReadVariant readVar);
    
    // ----------------------------
    
    public static int getHighestNumber(GnucashVendor vend)
    {
      return vend.getGnucashFile().getHighestVendorNumber();
    }

    public static String getNewNumber(GnucashVendor vend)
    {
      return vend.getGnucashFile().getNewVendorNumber();
    }

}

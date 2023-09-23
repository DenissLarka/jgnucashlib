[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=DenissLarka_gnucash&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=DenissLarka_gnucash)

A Java-library for manipulating the XML file-format of the GnuCash open
source accounting software. Usable to automate accounting-tasks, taxes.

# Compatibility
Version 1.1 of the library has been tested with GnuCash 5.3 on Linux (locale de_DE) and OpenJDK 18.0.

**Caution:** Will not work on other locales (for details, cf. below).

# Major Changes 
## V. 1.0 --> 1.1
* Reading and writing of (technical/generic) invoices: Not just customer invoices, but also vendor bills now. According to the internal XML structure and the part of the code that is generated from it (i.e., the XSD file), both derive from a common class that represents a "generic" invoice. In addition to that, there is also a third type: job invoice (cf. next item).
    
    ==> Introduced specialized interfaces/classes: `GnucashCustomerInvoice(Impl)`, `GnucashVendorBill(Impl)` and `GnucashJobInvoice(Impl)`, which all derive from `GnucashGenerInvoice(Impl)`. Consequently, we have the same for their entries: `GnucashGenerInvoiceEntry(Impl)`, etc.

    (This didn't seem too difficult at first, but actually it was a lot of work.)

* Invoices and bills (both customer's and vendor's) can now be assigned directly to the customer/vendor or indirectly via a job (as opposed to V. 1.0, where everything was indirect via a job). In business terms, the indirect ones are still customer invoices/vendor bills, of course, but the technical XML structure mandates a sligthly different approach: If there is a job, then the invoice's/bill's owner is not the according customer/vendor, but rather the job object, which in turn is owned by a customer/vendor. Thus, analogously to the invoices, we now also have two types of jobs: customer jobs and vendor jobs.

    ==> Introduced specialized interfaces/classes: `GnucashCustomerJob(Impl)` and `GnucashVendorJob(Impl)`, which both derive from `GnucashGenerJob(Impl)`. Both interfaces and implementations.

   (Once the splitting-up of the classes for invoices was done, this part was not too difficult.)

* Introduced complete set of terms: 
   * Customer terms --> `GCshCustomerTerms`
   * Vendor terms --> `GCshVendorTerms`
   * Bill terms (no, this is not the same as the vendor terms) --> `GCshBillTerms`

   (Not fully tested yet!)

* Introduced new packages (both for interfaces and implementations):

   *  "spec" for the above-mentioned special variants of generic classes.
   *  "aux" for auxiliary classes (and for them, a special prefix: `GCsh`). Also, made code both redundancy-free and more readable by getting rid of nested classes (e.g., `Gnucash(xyz)Invoice.Address` --> `GCshAdress` or `GGshTaxTable.TaxTableEntry` --> `GCshTaxTableEntry`).

* Improved exception handling/actual working of code with real-life data (not fundamentally different, but rather small repair work -- the code partially did not work in the above-mentioned environment).

* JUnit-based set of regression test cases (with test data in dedicated test GnuCash file). This alone greatly gives more security and peace of mind when using the library, let alone learning how to actually use the library.

* Enhanced type safety and compile-time checks -- both were not always as strict as possible.

* (Partially) got rid of overly specific code (e.g., there were methods that only make sense when using the German standard SKR03/SRK04 chart of accounts, or the 19% VAT (originally, until 2006, 16%, which still was in the code)).

* Got rid of some redundancies here and there, introduced class `Const` for that (which in turn contains hard-coded values).

* Got rid of methods that accept internal IDs as arguments: First, internal IDs are -- well -- internal/internally generated and thus should not be part of an API, and second, GnuCash uses only UUIDs anyway, and there is simply no point in generating these outside (to be fair: for all of these methods, there were wrappers without).

* Some minor cleaning here and there (e.g., small inconsistencies in date-handling, `toString()`-methods, etc.).

# Planned
It should go without extra notice, but the following points are of cours subject to change:

* Classes for "commodities" (i.e. securities such as shares and bonds) and methods for buying/selling them in investment accounts.

* Classes for commodity (security) quotes.

* Classes for employees and employee vouchers.

* Generalizing tax-law-specific code (e.g., the 19% VAT in Germany that you will see here and there in the code).

* Generalizing (technically) locale-specific code (e.g., GnuCash stores certain interal XML-tags with locale-specific values for transaction splits' actions). Currently, all this is too tightly tied to the German locale (de_DE).

* Even more type safety (e.g., possibly wrapper type for internally-used UUIDs).

* Better test case coverage.

* Configurable values in Const class.

* More macro code (i.e., wrappers for generating specific variants of invoices/bills, say, or for booking a dividend payment from a share).

* Possibly taking over other original code from Marcus Wolschon (there are two more packages).

* Set of command-line tools for basic handling of reading/writing activities.

# Acknowlegdgements
Special thanks to: 

* **Marcus Wolschon (Sofwaredesign u. Beratung)** for the original version (from 2005) and the pioneering work.

    This project is based on Marcus's work. There have been major changes since then, but you can still see where it originated from.

  (Forked from http://sourceforge.net/projects/jgnucashlib)

* **Deniss Larka** for taking care of the project during the last couple of years.
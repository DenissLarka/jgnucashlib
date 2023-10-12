[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=DenissLarka_gnucash&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=DenissLarka_gnucash)

A Java-library for manipulating the XML file format of the GnuCash open
source accounting software. Usable to automate accounting-tasks, taxes.

# Compatibility
Version 1.1 of the library has been tested with GnuCash 5.3 and 5.4 on Linux (locale de_DE) and OpenJDK 18.0.

**Caution:** Will not work on other locales (for details, cf. below).

# Major Changes 
## V. 1.0 --> 1.1
* Reading and writing of (technical/generic) invoices: Not just customer invoices, but also vendor bills now. According to the internal XML structure and the part of the code that is generated from it (i.e., the XSD file), both derive from a common class that represents a "generic" invoice. In addition to that, there is also a third type: job invoice (cf. next item).
    
    ==> Introduced specialized interfaces/classes: `GnucashCustomerInvoice(Impl)`, `GnucashVendorBill(Impl)` and `GnucashJobInvoice(Impl)`, which all derive from `GnucashGenerInvoice(Impl)`. Consequently, we have the same for their entries: `GnucashGenerInvoiceEntry(Impl)`, etc.

    (This didn't seem too difficult at first, but actually it was a lot of work.)

* Invoices and bills (both customer's and vendor's) can now be assigned directly to the customer/vendor or indirectly via a job (as opposed to V. 1.0, where everything was indirect via a job). In business terms, the indirect ones are still customer invoices/vendor bills, of course, but the technical XML structure mandates a sligthly different approach: If there is a job, then the invoice's/bill's owner is not the according customer/vendor, but rather the job object, which in turn is owned by a customer/vendor. Thus, analogously to the invoices, we now also have two types of jobs: customer jobs and vendor jobs.

    ==> Introduced specialized interfaces/classes: `GnucashCustomerJob(Impl)` and `GnucashVendorJob(Impl)`, which both derive from `GnucashGenerJob(Impl)`. Both interfaces and implementations.

   (Once the splitting-up of the classes for invoices was done, this part was not too difficult.)

* Introduced handling for terms based on class `GCshBillTerms`: Both the infos of the general list as well as the customers'/vendors' default terms can be read with all details now. In this version, write-operations are only possible in the form of references (i.e., assigning already-existing terms to a customer or a vendor without changing the term details themselves).

* Better handling of tax tables based on class `GCshTaxtable(Entry)`: Analogous to terms above.

* More complete coverage of data access to customer/vendor data:

  * discount/credit (customers only)
  * default tax table (had been forgotten for vendors)
  * default terms (had not been available for both)

* Introduced new packages (both for interfaces and implementations):

   *  "spec" for the above-mentioned special variants of generic classes.
   *  "aux" for auxiliary classes (and for them, a special prefix: `GCsh`). Also, made code both redundancy-free and more readable by getting rid of nested classes (e.g., `Gnucash(xyz)Invoice.Address` --> `GCshAdress` or `GGshTaxTable.TaxTableEntry` --> `GCshTaxTableEntry`).

* Improved exception handling/actual working of code with real-life data (not fundamentally different, but rather small repair work -- the code partially did not work in the above-mentioned environment).

* JUnit-based set of regression test cases (with test data in dedicated test GnuCash file). This alone greatly improves security and peace of mind both for users and developers, let alone learning how to actually use the library (in lack of a proper documentation, simply look into the examples and the test cases to understand how to use the lib).

* Enhanced type safety and compile-time checks -- both were not always as strict as possible.

* (Partially) got rid of overly specific and/or obsolete code (e.g., there were methods that only make sense when using the german standard SKR03/04 chart of accounts, or the 19% VAT (originally, until 2006, 16%, which you still could find in the code)).

* Got rid of some redundancies here and there, introduced class `Const` for that (which in turn contains hard-coded values).

* Got rid of (public) methods that accept internal IDs as arguments: First, internal IDs are -- well -- internal/internally generated and thus should not be part of an API, and second, GnuCash uses only UUIDs anyway, and there is simply no point in generating these outside (to be fair: for all of these methods, there were wrappers without).

* Renamed some classes to honour naming conventions (e.g., `abcMyObjectWritingxyz` --> `abcWritableMyObjectxyz`).

* Some minor cleaning here and there (e.g., small inconsistencies in date-handling, `toString()`-methods, etc.).

* Provided an extensive set of example programs (not generally-usable tools!) in a module of its own. Moved the one single example program that was there before into this module.

# Planned
It should go without extra notice, but the following points are of cours subject to change:

* Classes for "commodities" (i.e. securities such as shares and bonds) and methods for buying/selling them in investment accounts.

* Classes for commodity (security) quotes.

* Classes for employees and employee vouchers.

* Invoices and bills: Support more variants, such as choosing the terms of payment or the "tax included" flag for entries.

* Generalizing (technically) locale-specific code (e.g., GnuCash stores certain interal XML-tags with locale-specific values for transaction splits' actions). Currently, all this is too tightly tied to the german locale (de_DE).

* Get rid of ugly code redundancies here and there, esp. in the class `Gnucash(Writable)GenerInvoiceImpl`.

* Even more type safety (e.g., possibly wrapper type for internally-used UUIDs).

* Better test case coverage.

* Configurable values in `Const` class.

* Possibly some macro code (i.e., wrappers for generating specific variants), e.g. wrappers for:

  * generating specific variants of invoices/bills,
  * booking invoice-payement transaction,
  * booking dividend payments from a share.

* Possibly taking over other original code from Marcus Wolschon (there are two more packages).

* Possibly write a set of generally-usable command-line tools for basic handling of reading/writing activities, based on existing set of simple examples (but in a separate module).

* Last not least: Provide user documentation.

# Known Issues
* As mentioned above: As of now, the lib only works well when your GnuCash files are generated on a german system (locale de_DE).

* When generating invoices, you cannot/should not call the `post()`-method immediately after composing the object. The post-method will work, but the amount of the post-transaction will be wrong (thus, the transaction will be useless as it cannot be corrected manually in GnuCash; post-transactions are read-only). Instead, you should first write the results to the output file using the `GnucashWritableFile.writeFile()`-method, then re-load/re-parse the invoice generated before and then use the `post()`-method. Then, the amount will be correct.

  Cf. test classes `TestGnucashWritableCustomerInvoiceImpl`, `TestGnucashWritableVendorBillImpl` and  `TestGnucashWritableJobInvoiceImpl`.

# Acknowlegdgements
Special thanks to: 

* **Marcus Wolschon (Sofwaredesign u. Beratung)** for the original version (from 2005) and the pioneering work.

    This project is based on Marcus's work. There have been major changes since then, but you can still see where it originated from.

  (Forked from http://sourceforge.net/projects/jgnucashlib)

* **Deniss Larka** for taking care of the project during the last couple of years.

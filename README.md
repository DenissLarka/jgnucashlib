[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=DenissLarka_gnucash&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=DenissLarka_gnucash)

A Java-library for manipulating the XML file format of the GnuCash open
source accounting software. Usable to automate accounting-tasks, taxes.

# Compatibility
Version 1.2 of the library has been tested with GnuCash 5.3 and 5.4 on Linux (locale de_DE) and OpenJDK 18.0.

**Caution:** Will not work on other locales (for details, cf. below).

# Major Changes 
## V. 1.1 &rarr; 1.2
* Introduced interfaces/classes `Gnucash(Writable)Commodity(Impl)` for reading and writing "commodities", in GnuCash lingo an umbrella term for 
  * Currencies
  * (Real) Securities (shares, bonds, funds, etc.)
  * Possibly other assets that can be mapped to this GnuCash entity as pseudo-securities, such as crypto-currencies, physical precious metals, etc.

* Reading of commodities' prices is possible, but writing them is not possible yet. Given the above polymorphism of the commodity (cf. above), a "price" is one of the following:

  * A currency's exchange rate
  * A security's quote
  * A pseudo-security's price

   (all of them usually noted in the default currency)

  Given that we have several variants of `CmdtyCurrID` (cf. below), we also have several methods returning various types.

In this context, the following was also necessary/also made sense:

* Introduced auxiliary interface/class `GCshPrice(Impl)` representing an entry of the GnuCash price database.

* Introduced auxiliary class `GCshCmdtyCurrID` (essentially a pair of commodity name space and commodity code), which represents a security-style pseudo-ID for commodities (thus, including currencies, cf. above). In addition to that, introduced (grand-)child classes `GCshCmdtyID`, `GCshCurrID`, `GCshCmdtyID_Exchange`, `GCshCmdtyID_MIC` and `GCshCmdtyID_SecIDType` to represent various variants with adequate type-safety, i.e. for both easy and safe handling of read and write operations. 

  Note that, as opposed to all other entities in the GnuCash XML file, commodities have no internal UUID (a fact that the author finds irritating). This is one of the reasons why (pseudo-)currencies necessitate a fundamentally different approach.

* Closely linked to `GCshCmdtyCurrID` and its descendants: Major rework on the class `GCshCmdtyCurrNameSpace` (formerly `CurrencyNameSpace`): introduced several enums for enhanced type safety.

  *Background*: In and of itself, GnuCash's commodity name space can be freely defined; only suggestions are made on how to define it, e.g. a major stock exchange's abbreviation or a major securities index name. But you *need* not do it like that. The author is well aware of the fact that the GnuCash developers have -- likely after having weighed the pros and cons -- purposefully decided *not* to enforce any pre-defined values. 

  In this lib, we follow this spirit (as well as the path that the original author Marcus Wolschon followed): We *allow* the users to freely define any name space they want, but we *encourage* them to use one of the pre-defined enums in `GCshCmdtyCurrNameSpace` (expecting that buying, holding and selling conventional securities on major markets will be the most typical use case by far).

  *Note that the current definition of the enums in the class `GCshCmdtyCurrNameSpace` is by no means final and "once and for all". The author rather expects some re-work iterations on the existing ones and the introduction of new ones.*

  [ Some readers might be interested in the fact that the author uses the name space `GCshCmdtyCurrNameSpace.SecIdType.ISIN`, because he believes that this is the only one that allows easy and consistent handling of a global portfolio (and the fact that he is not so much interested in where a specific security is traded, because he follows a buy-and-hold investment strategy). ]

Further improvements:

* Interface changes in:

  * `Gnucash(Writable)Account`
  * `Gnucash(Writable)Transaction`
  * `Gnucash(Writable)File`

  Essentially leveraging improved type safety and robustness, primarily through use of `GCshCmdtyCurrID` and descendants.

* Improved retrieval of objects in `GnucashFileImpl` with `getXYZByName()`-methods (e.g., `getAccountsByName()`): There are now two variants for (almost) each of them: One with exact matching and one with relaxed matching, the latter one possibly returning several objects. (Still some work to be done here (esp. with account variants.) Cf. test cases and example programs.

* Improved test coverage.

* Some minor bug-fixing here and there.

## V. 1.0 &rarr; 1.1
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
   *  "aux" for auxiliary classes (and for them, a special prefix: `GCsh`). Also, made code both redundancy-free and more readable by getting rid of nested classes (e.g., `Gnucash(xyz)Invoice.Address` &rarr; `GCshAdress` or `GGshTaxTable.TaxTableEntry` &rarr; `GCshTaxTableEntry`).

* Improved exception handling/actual working of code with real-life data (not fundamentally different, but rather small repair work -- the code partially did not work in the above-mentioned environment).

* JUnit-based set of regression test cases (with test data in dedicated test GnuCash file). This alone greatly improves security and peace of mind both for users and developers, let alone learning how to actually use the library (in lack of a proper documentation, simply look into the examples and the test cases to understand how to use the lib).

* Enhanced type safety and compile-time checks -- both were not always as strict as possible.

* (Partially) got rid of overly specific and/or obsolete code (e.g., there were methods that only make sense when using the german standard SKR03/04 chart of accounts, or the 19% VAT (originally, until 2006, 16%, which you still could find in the code)).

* Got rid of some redundancies here and there, introduced class `Const` for that (which in turn contains hard-coded values).

* Got rid of (public) methods that accept internal IDs as arguments: First, internal IDs are -- well -- internal/internally generated and thus should not be part of an API, and second, GnuCash uses only UUIDs anyway, and there is simply no point in generating these outside (to be fair: for all of these methods, there were wrappers without).

* Renamed some classes to honour naming conventions (e.g., `abcMyObjectWritingxyz` &rarr; `abcWritableMyObjectxyz`).

* Some minor cleaning here and there (e.g., small inconsistencies in date-handling, `toString()`-methods, etc.).

* Provided an extensive set of example programs (not generally-usable tools!) in a module of its own. Moved the one single example program that was there before into this module.

# Planned
It should go without saying, but the following points are of course subject to change and by no means a promise that they will actually be implemented soon:

* Methods for buying/selling securities ("commodities") in investment accounts.

* Classes for employees and employee vouchers.

* Invoices and bills: Support more variants, such as choosing the terms of payment or the "tax included" flag for entries.

* Generalize (technically) locale-specific code (e.g., GnuCash stores certain interal XML-tags with locale-specific values for transaction splits' actions). Currently, all this is too tightly tied to the german locale (de_DE).

* Re-iterate package `org.gnucash.currency`: leverage `GCshCmdtyCurrID` and descendtants.

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

# Acknowledgements
Special thanks to: 

* **Marcus Wolschon (Sofware-Design u. Beratung)** for the original version (from 2005) and the pioneering work 
    (which, by the way, contained way more than what you see here) and for putting the base of this project under 
    the GPL.

    This project is based on Marcus's work. There have been major changes since then, but you can still see where it originated from.

  (Forked from http://sourceforge.net/projects/jgnucashlib / revived in 2017, after some years of enchanted sleep.)

* **Deniss Larka** for kissing the beauty awake and taking care of her during the last couple of years.

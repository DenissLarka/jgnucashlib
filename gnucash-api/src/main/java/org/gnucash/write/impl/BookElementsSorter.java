package org.gnucash.write.impl;

import java.util.Comparator;

import org.gnucash.generated.GncAccount;
import org.gnucash.generated.GncBudget;
import org.gnucash.generated.GncPricedb;
import org.gnucash.generated.GncTransaction;
import org.gnucash.generated.GncV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sorter for the elements in a Gnc:Book.
 */
public class BookElementsSorter implements Comparator<Object> {

  private static final Logger LOGGER = LoggerFactory.getLogger(BookElementsSorter.class);

  /**
   * {@inheritDoc}
   */
  @Override
  public int compare(final Object aO1, final Object aO2) {
    // no secondary sorting
    return (new Integer(getType(aO1)).compareTo(new Integer(getType(aO2))));
  }

  /**
   * Return an integer for the type of entry. This is the primary ordering used.
   * 
   * @param element the object to examine
   * @return int > 0
   */
  private int getType(final Object element) {
    if (element instanceof GncV2.GncBook.GncCommodity) {
      return 1;
    } else if (element instanceof GncPricedb) {
      return 2;
    } else if (element instanceof GncAccount) {
      return 3;
    } else if (element instanceof GncBudget) {
      return 4;
    } else if (element instanceof GncTransaction) {
      return 5;
    } else if (element instanceof GncV2.GncBook.GncTemplateTransactions) {
      return 6;
    } else if (element instanceof GncV2.GncBook.GncSchedxaction) {
      return 7;
    } else if (element instanceof GncV2.GncBook.GncGncJob) {
      return 8;
    } else if (element instanceof GncV2.GncBook.GncGncTaxTable) {
      return 9;
    } else if (element instanceof GncV2.GncBook.GncGncInvoice) {
      return 10;
    } else if (element instanceof GncV2.GncBook.GncGncCustomer) {
      return 11;
    } else if (element instanceof GncV2.GncBook.GncGncEmployee) {
      return 12;
    } else if (element instanceof GncV2.GncBook.GncGncEntry) {
      return 13;
    } else if (element instanceof GncV2.GncBook.GncGncBillTerm) {
      return 14;
    } else if (element instanceof GncV2.GncBook.GncGncVendor) {
      return 15;
    } else {
      throw new IllegalStateException("Unexpected element in GNC:Book found! <" + element.toString() + ">");
    }
  }
}

/**
 * BookElementsSorter.java
 * created: 14.06.2009
 * (c) 2008 by <a href="http://Wolschon.biz">Wolschon Softwaredesign und Beratung</a>
 * This file is part of jgnucashLib-GPL by Marcus Wolschon <a href="mailto:Marcus@Wolscon.biz">Marcus@Wolscon.biz</a>.
 * You can purchase support for a sensible hourly rate or
 * a commercial license of this file (unless modified by others) by contacting him directly.
 * <p>
 * jgnucashLib-GPL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * jgnucashLib-GPL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with jgnucashLib-V1.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * **********************************
 * Editing this file:
 * -For consistent code-quality this file should be checked with the
 * checkstyle-ruleset enclosed in this project.
 * -After the design of this file has settled it should get it's own
 * JUnit-Test that shall be executed regularly. It is best to write
 * the test-case BEFORE writing this class and to run it on every build
 * as a regression-test.
 */

package org.gnucash.write.impl;

import java.util.Comparator;


import org.gnucash.generated.GncAccount;
import org.gnucash.generated.GncBudget;
import org.gnucash.generated.GncTransaction;
import org.gnucash.generated.GncV2;

/**
 * (c) 2009 by <a href="http://Wolschon.biz>Wolschon Softwaredesign und Beratung</a>.<br/>
 * Project: jgnucashLib-GPL<br/>
 * BookElementsSorter<br/>
 * created: 14.06.2009 <br/>
 *<br/><br/>
 * Sorter for the elements in a Gnc:Book.
 * @author  <a href="mailto:Marcus@Wolschon.biz">fox</a>
 */
public class BookElementsSorter implements Comparator<Object> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compare(final Object aO1, final Object aO2) {
		// no secondary sorting
		return (new Integer(getType(aO1)).compareTo(new Integer(getType(aO2))));
	}

	/**
	 * Return an integer for the type of entry.
	 * This is the primary ordering used.
	 * @param element the object to examine
	 * @return int > 0
	 */
	private int getType(final Object element) {
		if (element instanceof GncV2.GncBook.GncCommodity) {
			return 1;
		} else if (element instanceof GncV2.GncBook.GncPricedb) {
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

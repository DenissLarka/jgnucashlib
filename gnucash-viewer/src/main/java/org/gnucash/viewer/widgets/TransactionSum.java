/**
 * TransactionSum.java
 * created: 09.12.2007 12:06:22
 * (c) 2007 by
 * <a href="http://Wolschon.biz">Wolschon Softwaredesign und Beratung</a>
 * This file is part of jgnucashLib-V1 by Marcus Wolschon
 * <a href="mailto:Marcus@Wolscon.biz">Marcus@Wolscon.biz</a>.
 * You can purchase support for a sensible hourly rate or
 * a commercial license of this file (unless modified by others)
 * by contacting him directly.
 * jgnucashLib-V1 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * jgnucashLib-V1 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with jgnucashLib-V1.  If not, see <http://www.gnu.org/licenses/>.
 * **********************************
 * Editing this file:
 * -For consistent code-quality this file should be checked with the
 * checkstyle-ruleset enclosed in this project.
 * -After the design of this file has settled it should get it's own
 * JUnit-Test that shall be executed regularly. It is best to write
 * the test-case BEFORE writing this class and to run it on every build
 * as a regression-test.
 */
package org.gnucash.viewer.widgets;

//automatically created propertyChangeListener-Support

import java.awt.BorderLayout;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.LocalDate;
import java.time.chrono.ChronoZonedDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.gnucash.currency.ComplexCurrencyTable;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashTransaction;
import org.gnucash.read.GnucashTransactionSplit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * (c) 2007 by <a href="http://Wolschon.biz>
 * Wolschon Softwaredesign und Beratung</a>.<br/>
 * Project: jgnucashLib-V1<br/>
 * TransactionSum.java<br/>
 * created: 09.12.2007 12:06:22 <br/>
 * <br/><br/>
 * This panel displays a sum of all transaction-splits that are
 * to any of a list of accounts belonging to transactions with at
 * least one split in another list of accounts.<br/>
 * It is very handy for tax- and other reports.
 *
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 */
public class TransactionSum extends JPanel {

	/**
	 * For serializing.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Automatically created logger for debug and error-output.
	 */
	/**
	 * Our logger for debug- and error-ourput.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionSum.class);

	/**
	 * The financial data we operate on.
	 */
	private GnucashFile myBooks;

	/**
	 * We only operate on transactions that
	 * contain one of these accounts.
	 */
	private Set<GnucashAccount> mySourceAccounts;

	/**
	 * We sum all transaction-splits that are to
	 * one of these accounts.
	 *
	 * @see #mySourceAccounts
	 */
	private Set<GnucashAccount> myTargetAccounts;

	/**
	 * We ignore all transactions that are before this date.
	 */
	private LocalDate myMinDate;
	/**
	 * We ignore all transactions that are after this date.
	 */
	private LocalDate myMaxDate;

	/**
	 * The type of summations we are to calculate.
	 */
	private SUMMATIONTYPE mySummationType = SUMMATIONTYPE.ALL;

	/**
	 * (c) 2007 by <a href="http://Wolschon.biz>
	 * Wolschon Softwaredesign und Beratung</a>.<br/>
	 * Project: jgnucashLib-V1<br/>
	 * TransactionSum.java<br/>
	 * created: 09.12.2007 12:34:53 <br/>
	 * <br/><br/>
	 * The types of summations we can do.
	 *
	 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
	 */
	public enum SUMMATIONTYPE {
		/**
		 * Sum all splits.
		 */
		ALL,
		/**
		 * Sum only splits that increase the balance on
		 * the targetAccount.
		 *
		 * @see TransactionSum#myTargetAccounts
		 */
		ONLYTO,
		/**
		 * Sum only splits that decrease the balance on
		 * the targetAccount.
		 *
		 * @see TransactionSum#myTargetAccounts
		 */
		ONLYFROM;

		/**
		 * @param aProperty parse this string
		 * @return and return the value that matches the name
		 */
		public static SUMMATIONTYPE getByName(final String aProperty) {
			if (aProperty.equalsIgnoreCase("all")) {
				return ALL;
			}

			if (aProperty.equalsIgnoreCase("to")) {
				return ONLYTO;
			}
			if (aProperty.equalsIgnoreCase("onlyto")) {
				return ONLYTO;
			}

			if (aProperty.equalsIgnoreCase("from")) {
				return ONLYFROM;
			}
			if (aProperty.equalsIgnoreCase("onlyfrom")) {
				return ONLYFROM;
			}
			if (aProperty.equalsIgnoreCase("allrecursive")) {
				return ALL;
			}

			return ALL;
		}
	}

	/**
	 * The label that contains the sum.
	 */
	private final JLabel mySumLabel = new JLabel();
	/**
	 * The label that contains the name
	 * to display left of the sum.
	 */
	private final JLabel myNameLabel = new JLabel();
	/**
	 * The label to display a link
	 * for a drilldown to the list of
	 * transactions covered.
	 */
	private final JLabel myDrilldownLabel = new JLabel();

	/**
	 * The latest value calculated by {@link #reCalculate()}.
	 */
	private FixedPointNumber myValue = null;

	/**
	 * The count of transactions we counted n the last {@link #reCalculate()}.
	 */
	private int myTransactionsCounted = -1;

	/**
	 * @param books          The financial data we operate on.
	 * @param summationType  The type of summations we are to calculate.
	 * @param targetAccounts We sum all transaction-splits that are to
	 *                       one of these accounts.
	 * @param sourceAccounts We only operate on transactions that
	 *                       contain one of these accounts.
	 * @param name           the name to display left of the sum
	 * @param minDate        We ignore all transactions that are before this date.
	 * @param maxDate        We ignore all transactions that are after this date.
	 */
	public TransactionSum(final GnucashFile books,
			final Set<GnucashAccount> sourceAccounts,
			final Set<GnucashAccount> targetAccounts,
			final SUMMATIONTYPE summationType,
			final String name,
			final LocalDate minDate,
			final LocalDate maxDate) {
		initializeUI(name);
		setName(name);
		setBooks(books);
		setSummationType(summationType);
		setSourceAccounts(sourceAccounts);
		setTargetAccounts(targetAccounts);
		setMinDate(minDate);
		setMaxDate(maxDate);
	}

	/**
	 * Do the actual calculation.
	 */
	private void reCalculate() {
		if (getSummationType() == null
				|| getSourceAccounts() == null
				|| getTargetAccounts() == null
				|| getSummationType() == null
				//|| getMinDate() == null
				//|| getMaxDate() == null
				|| getBooks() == null) {
			mySumLabel.setText("---");
			return;
		}
		myTransactionsCounted = 0;

		Set<GnucashAccount> sourceAccounts = new HashSet<GnucashAccount>(
				getSourceAccounts());
		Set<GnucashAccount> targetAccounts = new HashSet<GnucashAccount>(
				buildTransitiveClosure(getTargetAccounts()));
		Set<String> targetAccountsIDs = new HashSet<String>();
		for (GnucashAccount targetAccount : targetAccounts) {
			targetAccountsIDs.add(targetAccount.getId());
		}

		////////////////////////////////////
		// find all applicable transacion
		Set<GnucashTransactionSplit> transactions = new HashSet<GnucashTransactionSplit>();
		FixedPointNumber sum = new FixedPointNumber(0);
		if (sourceAccounts.size() == 0) {
			LOGGER.warn("There are no source-accounts given for this transaction-sum");
		}
		for (GnucashAccount sourceAccount : sourceAccounts) {
			FixedPointNumber addMe =
					buildSum(sourceAccount,
							targetAccountsIDs,
							sourceAccount.getCurrencyNameSpace(),
							sourceAccount.getCurrencyID(),
							transactions);
			if (addMe == null) {
				mySumLabel.setText("   cannot determine sum");
				sum = null;
				break;
			}
			sum = sum.add(addMe);
		}

		setValue(sum);
		////////////////////////////////////
		// set output
		Iterator<GnucashAccount> iterator = targetAccounts.iterator();
		if (iterator.hasNext()) {
			mySumLabel.setText("   " + sum.toString() + ""
					+ iterator.next().getCurrencyID());
		} else {
			Iterator<GnucashAccount> iterator2 = sourceAccounts.iterator();
			if (iterator2.hasNext()) {
				mySumLabel.setText("   " + sum.toString() + ""
						+ iterator2.next().getCurrencyID());
			} else {
				mySumLabel.setText("   no account");
			}
		}
	}

	/**
	 * @param alreadyHandled all transactions we have already visited (if multiple target-accounts are involved)
	 */
	private FixedPointNumber buildSum(final GnucashAccount aSourceAccount,
			final Set<String> aTargetAccountsIDs,
			final String currencyNameSpace,
			final String currencyID,
			final Set<GnucashTransactionSplit> alreadyHandled) {

		FixedPointNumber sum = new FixedPointNumber();
		for (Object element : aSourceAccount.getChildren()) {
			GnucashAccount child = (GnucashAccount) element;
			sum = sum.add(buildSum(child, aTargetAccountsIDs, currencyNameSpace, currencyID, alreadyHandled));
		}

		List<? extends GnucashTransactionSplit> splits
				= aSourceAccount.getTransactionSplits();
		for (GnucashTransactionSplit split : splits) {
			GnucashTransaction transaction = split.getTransaction();
			if (getMinDate() != null && transaction.getDatePosted().isBefore( ChronoZonedDateTime.from(getMinDate().atStartOfDay()) )) {
				continue;
			}
			if (getMaxDate() != null && transaction.getDatePosted().isAfter( ChronoZonedDateTime.from(getMaxDate().atStartOfDay()) )) {
				continue;
			}
			if (aTargetAccountsIDs.size() > 0 && !hasSplitWithAccount(transaction, aTargetAccountsIDs)) {
				continue;
			}
			if (alreadyHandled.contains(split)) {
				continue;
			}
			alreadyHandled.add(split);

			if (getSummationType().equals(SUMMATIONTYPE.ONLYFROM) && split.getQuantity().isPositive()) {
				continue;
			} else if (getSummationType().equals(SUMMATIONTYPE.ONLYTO) && !split.getQuantity().isPositive()) {
				continue;
			}
			if (aSourceAccount.getCurrencyNameSpace().equals(currencyNameSpace)
					&& aSourceAccount.getCurrencyID().equals(currencyID)) {

				sum = sum.add(split.getQuantity());
			} else {
				FixedPointNumber addMe = new FixedPointNumber(split.getQuantity());
				// do not convert 0
				if (!addMe.equals(new FixedPointNumber())) {
					addMe = convert(aSourceAccount.getCurrencyNameSpace(), aSourceAccount.getCurrencyID(), addMe, currencyNameSpace,
							currencyID);
				}
				if (addMe == null) {
					return null;
				}
				sum = sum.add(addMe);
			}
			myTransactionsCounted++;

		}
		return sum;
	}

	/**
	 * @param aTransaction
	 * @param aTargetAccountsIDs
	 * @return
	 */
	private boolean hasSplitWithAccount(GnucashTransaction aTransaction, Set<String> aTargetAccountsIDs) {
		List<? extends GnucashTransactionSplit> splits = aTransaction.getSplits();
		for (GnucashTransactionSplit split : splits) {
			if (aTargetAccountsIDs.contains(split.getAccountID())) {
				return true;
			}
		}
		return false;
	}

	private FixedPointNumber convert(final String aCurrencyNameSpaceFrom,
			final String aCurrencyIDFrom,
			final FixedPointNumber aSum,
			final String aCurrencyNameSpaceTo,
			final String aCurrencyIDTo) {
		ComplexCurrencyTable currencyTable = getBooks().getCurrencyTable();

		if (currencyTable == null) {
			LOGGER.warn("SimpleAccount.getBalance() - cannot transfer "
					+ "to given currency because we have no currency-table!");
			return null;
		}
		FixedPointNumber sum = new FixedPointNumber(aSum);

		if (!currencyTable.convertToBaseCurrency(aCurrencyNameSpaceFrom,
				sum,
				aCurrencyIDFrom)) {
			Collection<String> currencies = getBooks().getCurrencyTable().getCurrencies(
					aCurrencyNameSpaceFrom);
			LOGGER.warn("SimpleAccount.getBalance() - cannot transfer "
					+ "from our currency '"
					+ aCurrencyNameSpaceFrom + "'-'"
					+ aCurrencyIDFrom
					+ "' to the base-currency!"
					+ " \n(we know " + getBooks().getCurrencyTable().getNameSpaces().size()
					+ " currency-namespaces and "
					+ (currencies == null ? "no" : "" + currencies.size())
					+ " currencies in our namespace)");
			return null;
		}

		if (!currencyTable.convertFromBaseCurrency(aCurrencyNameSpaceTo, sum, aCurrencyIDTo)) {
			LOGGER.warn("SimpleAccount.getBalance() - cannot transfer "
					+ "from base-currenty to given currency '"
					+ aCurrencyNameSpaceTo
					+ "-"
					+ aCurrencyIDTo
					+ "'!");
			return null;
		}
		return sum;
	}

	/**
	 * Build the transitive closure of a list of accounts
	 * by adding all child-accounts.
	 *
	 * @param accounts the account-list to walk
	 * @return a set of all given accounts and all their child-accounts.
	 */
	private Collection<? extends GnucashAccount> buildTransitiveClosure(
			final Collection<? extends GnucashAccount> accounts) {

		if (accounts.size() == 0) {
			return accounts;
		}

		Set<GnucashAccount> retval = new HashSet<GnucashAccount>(accounts);

		// TODO implement TransactionSum.buildTransitiveClosure
		for (GnucashAccount account : accounts) {
			Collection<? extends GnucashAccount> allChildren
					= buildTransitiveClosure(account.getChildren());
			retval.addAll(allChildren);
		}
		return retval;
	}

	/**
	 * Create the UI-components.
	 *
	 * @param name the name to display left of the sum
	 */
	public void initializeUI(final String name) {
		this.setLayout(new BorderLayout());
		myNameLabel.setText(name);
		mySumLabel.setText("...�");
		myDrilldownLabel.setText("");//TODO: implement drilldown
		this.add(myNameLabel, BorderLayout.WEST);
		this.add(mySumLabel, BorderLayout.CENTER);
		this.add(myDrilldownLabel, BorderLayout.EAST);
	}
	//------------------------ support for propertyChangeListeners -------------

	/**
	 * support for firing PropertyChangeEvents.
	 * (gets initialized only if we really have listeners)
	 */
	private volatile PropertyChangeSupport myPropertyChange = null;

	/**
	 * Returned value may be null if we never had listeners.
	 *
	 * @return Our support for firing PropertyChangeEvents
	 */
	protected final PropertyChangeSupport getPropertyChangeSupport() {
		return myPropertyChange;
	}

	/**
	 * Add a PropertyChangeListener to the listener list.
	 * The listener is registered for all properties.
	 *
	 * @param listener The PropertyChangeListener to be added
	 */
	@Override
	public final void addPropertyChangeListener(
			final PropertyChangeListener listener) {
		if (myPropertyChange == null) {
			myPropertyChange = new PropertyChangeSupport(this);
		}
		myPropertyChange.addPropertyChangeListener(listener);
	}

	/**
	 * Add a PropertyChangeListener for a specific property.  The listener
	 * will be invoked only when a call on firePropertyChange names that
	 * specific property.
	 *
	 * @param propertyName The name of the property to listen on.
	 * @param listener     The PropertyChangeListener to be added
	 */
	@Override
	public final void addPropertyChangeListener(final String propertyName,
			final PropertyChangeListener listener) {
		if (myPropertyChange == null) {
			myPropertyChange = new PropertyChangeSupport(this);
		}
		myPropertyChange.addPropertyChangeListener(propertyName, listener);
	}

	/**
	 * Remove a PropertyChangeListener for a specific property.
	 *
	 * @param propertyName The name of the property that was listened on.
	 * @param listener     The PropertyChangeListener to be removed
	 */
	@Override
	public final void removePropertyChangeListener(final String propertyName,
			final PropertyChangeListener listener) {
		if (myPropertyChange != null) {
			myPropertyChange.removePropertyChangeListener(propertyName,
					listener);
		}
	}

	/**
	 * Remove a PropertyChangeListener from the listener list.
	 * This removes a PropertyChangeListener that was registered
	 * for all properties.
	 *
	 * @param listener The PropertyChangeListener to be removed
	 */
	@Override
	public final synchronized void removePropertyChangeListener(
			final PropertyChangeListener listener) {
		if (myPropertyChange != null) {
			myPropertyChange.removePropertyChangeListener(listener);
		}
	}

	//-------------------------------------------------------

	/**
	 * @return Returns the books.
	 * @see #myBooks
	 */
	public GnucashFile getBooks() {
		return myBooks;
	}

	/**
	 * @param aBooks The books to set.
	 * @see #myBooks
	 */
	public void setBooks(final GnucashFile aBooks) {
		if (aBooks == null) {
			throw new IllegalArgumentException("null 'aBooks' given!");
		}

		Object old = myBooks;
		if (old == aBooks) {
			return; // nothing has changed
		}
		myBooks = aBooks;
		// <<insert code to react further to this change here
		reCalculate();
		PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
		if (propertyChangeFirer != null) {
			propertyChangeFirer.firePropertyChange("aBooks", old, aBooks);
		}
	}

	/**
	 * @return Returns the sourceAccounts.
	 * @see #mySourceAccounts
	 */
	public Set<GnucashAccount> getSourceAccounts() {
		return mySourceAccounts;
	}

	/**
	 * @param aSourceAccounts The sourceAccounts to set.
	 * @see #mySourceAccounts
	 */
	public void setSourceAccounts(final Set<GnucashAccount> aSourceAccounts) {
		if (aSourceAccounts == null) {
			throw new IllegalArgumentException("null 'aSourceAccounts' given!");
		}

		Object old = mySourceAccounts;
		if (old == aSourceAccounts) {
			return; // nothing has changed
		}
		mySourceAccounts = aSourceAccounts;
		// <<insert code to react further to this change here
		reCalculate();
		PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
		if (propertyChangeFirer != null) {
			propertyChangeFirer.firePropertyChange("aSourceAccounts", old,
					aSourceAccounts);
		}
	}

	/**
	 * @return Returns the summationType.
	 * @see #mySummationType
	 */
	public SUMMATIONTYPE getSummationType() {
		return mySummationType;
	}

	/**
	 * @param aSummationType The summationType to set.
	 * @see #mySummationType
	 */
	public void setSummationType(final SUMMATIONTYPE aSummationType) {
		if (aSummationType == null) {
			throw new IllegalArgumentException("null 'aSummationType' given!");
		}

		Object old = mySummationType;
		if (old == aSummationType) {
			return; // nothing has changed
		}
		mySummationType = aSummationType;
		// <<insert code to react further to this change here
		reCalculate();
		PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
		if (propertyChangeFirer != null) {
			propertyChangeFirer.firePropertyChange("aSummationType", old,
					aSummationType);
		}
	}

	/**
	 * @return Returns the targetAccounts.
	 * @see #myTargetAccounts
	 */
	public Set<GnucashAccount> getTargetAccounts() {
		return myTargetAccounts;
	}

	/**
	 * @param aTargetAccounts The targetAccounts to set.
	 * @see #myTargetAccounts
	 */
	public void setTargetAccounts(final Set<GnucashAccount> aTargetAccounts) {
		if (aTargetAccounts == null) {
			throw new IllegalArgumentException("null 'aTargetAccounts' given!");
		}

		Object old = myTargetAccounts;
		if (old == aTargetAccounts) {
			return; // nothing has changed
		}
		myTargetAccounts = aTargetAccounts;
		// <<insert code to react further to this change here
		reCalculate();
		PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
		if (propertyChangeFirer != null) {
			propertyChangeFirer.firePropertyChange("aTargetAccounts", old,
					aTargetAccounts);
		}
	}

	/**
	 * @return Returns the minDate.
	 * @see #myMinDate
	 */
	public LocalDate getMinDate() {
		return myMinDate;
	}

	/**
	 * @param aMinDate The minDate to set.
	 * @see #myMinDate
	 */
	public void setMinDate(final LocalDate aMinDate) {
		//        if (aMinDate == null) {
		//            throw new IllegalArgumentException("null 'aMinDate' given!");
		//        }

		Object old = myMinDate;
		if (old == aMinDate) {
			return; // nothing has changed
		}
		myMinDate = aMinDate;
		// <<insert code to react further to this change here
		reCalculate();
		PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
		if (propertyChangeFirer != null) {
			propertyChangeFirer.firePropertyChange("aMinDate", old, aMinDate);
		}
	}

	/**
	 * @return Returns the maxDate.
	 * @see #myMaxDate
	 */
	public LocalDate getMaxDate() {
		return myMaxDate;
	}

	/**
	 * @param aMaxDate The maxDate to set.
	 * @see #myMaxDate
	 */
	public void setMaxDate(final LocalDate aMaxDate) {
		//        if (aMaxDate == null) {
		//            throw new IllegalArgumentException("null 'aMaxDate' given!");
		//        }

		Object old = myMaxDate;
		if (old == aMaxDate) {
			return; // nothing has changed
		}
		myMaxDate = aMaxDate;
		// <<insert code to react further to this change here
		reCalculate();
		PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
		if (propertyChangeFirer != null) {
			propertyChangeFirer.firePropertyChange("aMaxDate", old, aMaxDate);
		}
	}

	/**
	 * @return the value
	 */
	public FixedPointNumber getValue() {
		return myValue;
	}

	/**
	 * @param aValue the value to set
	 */
	private void setValue(final FixedPointNumber aValue) {
		myValue = aValue;
	}

	/**
	 * @return the transactionsCounted
	 */
	public int getTransactionsCounted() {
		return myTransactionsCounted;
	}
}



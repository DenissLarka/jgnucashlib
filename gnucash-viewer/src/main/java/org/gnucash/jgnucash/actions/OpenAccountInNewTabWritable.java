/**
 * OpenAccountInNewTab.java
 * created: 12.03.2009
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

package org.gnucash.jgnucash.actions;

import org.gnucash.jgnucash.panels.WritableTransactionsPanel;
import org.gnucash.viewer.actions.AccountAction;
import org.gnucash.viewer.actions.TransactionSplitAction;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashTransaction;
import org.gnucash.read.GnucashTransactionSplit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

/**
 * (c) 2009 by <a href="http://Wolschon.biz>Wolschon Softwaredesign und Beratung</a>.<br/>
 * Project: jgnucashLib-GPL<br/>
 * OpenAccountInNewTab<br/>
 * created: 12.03.2009 <br/>
 * <br/><br/>
 * <b>Action to open an account in a new tab.</b>
 *
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 */
public class OpenAccountInNewTabWritable implements AccountAction, TransactionSplitAction {

	/**
	 * The account we open.
	 */
	private GnucashAccount myAccount;

	/**
	 * @see #getValue(String)
	 */
	private final Map<String, Object> myAddedTags = new HashMap<String, Object>();

	/**
	 * @see #addPropertyChangeListener(PropertyChangeListener)
	 */
	private final PropertyChangeSupport myPropertyChangeSupport = new PropertyChangeSupport(this);

	/**
	 * Optional the transaction to highlight.
	 */
	private GnucashTransaction myTransaction;

	/**
	 * The TabbedPane to open in.
	 */
	private final JTabbedPane myTabbedPane;

	/**
	 * Initialize.
	 *
	 * @param aTabbedPane The TabbedPane to open in.
	 */
	public OpenAccountInNewTabWritable(final JTabbedPane aTabbedPane) {
		this.putValue(Action.NAME, "Open Account in new Tab");
		this.putValue(Action.LONG_DESCRIPTION, "Open the given Account in new Tab.");
		this.putValue(Action.SHORT_DESCRIPTION, "Open Account in new Tab.");
		myTabbedPane = aTabbedPane;
	}

	/**
	 * @param aSplit      the split to show the account of.
	 * @param aTabbedPane The TabbedPane to open in.
	 */
	public OpenAccountInNewTabWritable(final JTabbedPane aTabbedPane, final GnucashTransactionSplit aSplit) {
		this(aTabbedPane);
		setSplit(aSplit);
	}

	/**
	 * @param anAccount   the account to show.
	 * @param aTabbedPane The TabbedPane to open in.
	 */
	public OpenAccountInNewTabWritable(final JTabbedPane aTabbedPane, final GnucashAccount anAccount) {
		this(aTabbedPane);
		setAccount(anAccount);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setAccount(final GnucashAccount anAccount) {
		myAccount = anAccount;
		myTransaction = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSplit(final GnucashTransactionSplit aSplit) {
		myAccount = aSplit.getAccount();
		myTransaction = aSplit.getTransaction();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addPropertyChangeListener(final PropertyChangeListener aListener) {
		myPropertyChangeSupport.addPropertyChangeListener(aListener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getValue(final String aKey) {
		return myAddedTags.get(aKey);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEnabled() {
		return getAccount() != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void putValue(final String aKey, final Object aValue) {
		myAddedTags.put(aKey, aValue);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removePropertyChangeListener(final PropertyChangeListener aListener) {
		myPropertyChangeSupport.removePropertyChangeListener(aListener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setEnabled(final boolean aB) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(final ActionEvent aE) {
		final WritableTransactionsPanel newTransactionsPanel = new WritableTransactionsPanel();
		newTransactionsPanel.setAccount(getAccount());
		if (myTransaction != null) {
			newTransactionsPanel.setTransaction(myTransaction);
		}
		String tabName = getAccount().getName();
		addTab(tabName, newTransactionsPanel);
	}

	/**
	 * @param tabName    the label of the tab
	 * @param tabContent the content
	 */
	private void addTab(final String tabName, final JComponent tabContent) {

		myTabbedPane.addTab(null, tabContent);
		JPanel tab = new JPanel(new BorderLayout(2, 0));
		tab.setOpaque(false);
		tab.add(new JLabel(tabName), BorderLayout.CENTER);
		JButton closeButton = new JButton("X");
		closeButton.setBorder(BorderFactory.createEmptyBorder());
		final int size = 10;
		closeButton.setPreferredSize(new Dimension(size, size));
		closeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent aE) {
				myTabbedPane.remove(tabContent);
			}

		});
		tab.add(closeButton, BorderLayout.EAST);
		myTabbedPane.setTabComponentAt(myTabbedPane.getTabCount() - 1, tab);
	}

	/**
	 * @return the account
	 */
	protected GnucashAccount getAccount() {
		return myAccount;
	}


}

/**
 * ShowWritableTransactionPanel.java
 * created: 21.09.2008 07:27:37
 * (c) 2008 by <a href="http://Wolschon.biz">Wolschon Softwaredesign und Beratung</a>
 * This file is part of jgnucashLib-V1 by Marcus Wolschon <a href="mailto:Marcus@Wolscon.biz">Marcus@Wolscon.biz</a>.
 * You can purchase support for a sensible hourly rate or
 * a commercial license of this file (unless modified by others) by contacting him directly.
 * <p>
 * jgnucashLib-V1 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * jgnucashLib-V1 is distributed in the hope that it will be useful,
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
package org.gnucash.jgnucash.panels;

//other imports

//automatically created logger for debug and error -output


import org.gnucash.viewer.panels.ShowTransactionPanel;
import org.gnucash.viewer.panels.SingleTransactionTableModel;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashTransaction;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.logging.Logger;


/**
 * (c) 2008 by <a href="http://Wolschon.biz>Wolschon Softwaredesign und Beratung</a>.<br/>
 * Project: jgnucashLib-GPL<br/>
 * ShowWritableTransactionPanel.java<br/>
 * created: 21.09.2008 07:27:37 <br/>
 *<br/><br/>
 * <b>This is a variant of {@link org.gnucash.viewer.panels.ShowTransactionPanel} that also allows
 * to edit the transaction.</b>
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 */
public class ShowWritableTransactionPanel extends ShowTransactionPanel {

	/**
	 * Automatically created logger for debug and error-output.
	 */
	static final Logger LOG = Logger
			.getLogger(ShowWritableTransactionPanel.class.getName());

	//------------------------ support for propertyChangeListeners ------------------

	/**
	 * support for firing PropertyChangeEvents.
	 * (gets initialized only if we really have listeners)
	 */
	private volatile PropertyChangeSupport myPropertyChange = null;

	/**
	 * Returned value may be null if we never had listeners.
	 * @return Our support for firing PropertyChangeEvents
	 */
	protected PropertyChangeSupport getPropertyChangeSupport() {
		return myPropertyChange;
	}

	/**
	 * Add a PropertyChangeListener to the listener list.
	 * The listener is registered for all properties.
	 *
	 * @param listener  The PropertyChangeListener to be added
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
	 * @param propertyName  The name of the property to listen on.
	 * @param listener  The PropertyChangeListener to be added
	 */
	@Override
	public final void addPropertyChangeListener(
			final String propertyName,
			final PropertyChangeListener listener) {
		if (myPropertyChange == null) {
			myPropertyChange = new PropertyChangeSupport(this);
		}
		myPropertyChange.addPropertyChangeListener(propertyName, listener);
	}

	/**
	 * Remove a PropertyChangeListener for a specific property.
	 *
	 * @param propertyName  The name of the property that was listened on.
	 * @param listener  The PropertyChangeListener to be removed
	 */
	@Override
	public final void removePropertyChangeListener(
			final String propertyName,
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
	 * @param listener  The PropertyChangeListener to be removed
	 */
	@Override
	public synchronized void removePropertyChangeListener(
			final PropertyChangeListener listener) {
		if (myPropertyChange != null) {
			myPropertyChange.removePropertyChangeListener(listener);
		}
	}

	//-------------------------------------------------------

	/**
	 * Just an overridden ToString to return this classe's name
	 * and hashCode.
	 * @return className and hashCode
	 */
	@Override
	public String toString() {
		return "ShowWritableTransactionPanel@" + hashCode();
	}

	/**
	 * @param aTransaction The transaction to set. MAY BE NULL
	 * @see #myTransaction
	 */
	@Override
	public void setTransaction(final GnucashTransaction aTransaction) {

		Object old = getTransaction();
		if (old == aTransaction) {
			return; // nothing has changed
		}
		super.setTransaction(aTransaction);

		SingleTransactionTableModel model = null;

		if (aTransaction == null) {
			model = new SingleWritableTransactionTableModel();
			setPreferredSize(new Dimension(0, 0));
			invalidate();
			super.setModel(model);
		} else {
			model = new SingleWritableTransactionTableModel(aTransaction);
			setPreferredSize(new Dimension(200, 200));
			invalidate();
			setModel(model);
		}
	}

	/**
	 * This method initializes transactionTable.
	 *
	 * @return javax.swing.JTable
	 */
	@Override
	protected JTable getTransactionTable() {
		JTable transactionTable = super.getTransactionTable();
		if (!(transactionTable.getModel() instanceof SingleWritableTransactionTableModel)) {
			transactionTable.setModel(new SingleWritableTransactionTableModel());
		}
		return transactionTable;
	}

	/**
	 * @param aModel The model to set.
	 * @see #model
	 */
	@Override
	protected void setModel(final SingleTransactionTableModel aModel) {
		super.setModel(aModel);

		// if editing is possible, install a jcomboBox as an editor for the accounts
		if (aModel != null && aModel instanceof SingleWritableTransactionTableModel) {
			GnucashTransaction transaction = aModel.getTransaction();
			if (transaction == null) {
				throw new IllegalArgumentException("Given model has no transaction");
			}
			JComboBox accountsCombo = new JComboBox() {

				/**
				 * ${@inheritDoc}.
				 */
				@Override
				public String getToolTipText() {
					Object selectedItem = getSelectedItem();
					if (selectedItem != null) {
						return selectedItem.toString();
					}
					return super.getToolTipText();
				}
			};
			accountsCombo.setToolTipText("Account-name"); //make sure a tooltip-manager exists
			if (transaction != null) {
				Collection<? extends GnucashAccount> accounts = transaction.getGnucashFile().getAccounts();
				for (GnucashAccount gnucashAccount : accounts) {
					accountsCombo.addItem(gnucashAccount.getQualifiedName());
				}
			}

//            getTransactionTable().getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(new JTextField()));
//            getTransactionTable().getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(new JTextField()));
//            getTransactionTable().getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JTextField()));
//            getTransactionTable().getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(accountsCombo));
//            getTransactionTable().getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(new JTextField()));
//            getTransactionTable().getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(new JTextField()));
			getTransactionTable().getColumn("account").setCellEditor(new DefaultCellEditor(accountsCombo));
		}
	}
}

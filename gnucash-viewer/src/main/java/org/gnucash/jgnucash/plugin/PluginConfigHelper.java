/**
 * PluginConfigHelper.javaTransactionMenuAction.java
 * created: 30.06.2009
 * (c) 2008 by <a href="http://Wolschon.biz">Wolschon Softwaredesign und Beratung</a>
 * This file is part of jgnucashLib-GPL by Marcus Wolschon <a href="mailto:Marcus@Wolscon.biz">Marcus@Wolscon.biz</a>.
 * You can purchase support for a sensible hourly rate or
 * a commercial license of this file (unless modified by others) by contacting him directly.
 * jgnucashLib-GPL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * jgnucashLib-GPL is distributed in the hope that it will be useful,
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

package org.gnucash.jgnucash.plugin;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.xml.bind.JAXBException;

import org.gnucash.fileformats.gnucash.GnucashWritableAccount;
import org.gnucash.fileformats.gnucash.GnucashWritableFile;

/**
 * (c) 2009 by <a href="http://Wolschon.biz>Wolschon Softwaredesign und Beratung</a>.<br/>
 * Project: jgnucashLib-GPL<br/>
 * PluginConfigHelper<br/>
 * created: 30.06.2009 <br/>
 * <br/><br/>
 * <b>This class contains helper-methods to simplify the implementation
 * of plugins.</b>
 *
 * @author <a href="mailto:Marcus@Wolschon.biz">fox</a>
 */
public final class PluginConfigHelper {

	/**
	 * Helper-classes have no public constructor.
	 */
	private PluginConfigHelper() {
	}

	/**
	 * Find the first account that has a value for the given
	 * key in  it's user-defined properties.
	 *
	 * @param aModel the book to operate on
	 * @param aKey   the key to look for
	 * @return an account or null
	 */
	@SuppressWarnings("unchecked")
	public static GnucashWritableAccount getAccountWithKey(final GnucashWritableFile aModel, final String aKey) {
		Collection<? extends GnucashWritableAccount> accounts = aModel.getWritableAccounts();
		for (GnucashWritableAccount gnucashAccount : accounts) {
			if (gnucashAccount.getUserDefinedAttribute(aKey) != null) {
				return gnucashAccount;
			}
		}
		return null;
	}

	/**
	 * Find all accounts that have a value for the given
	 * key in their user-defined properties.
	 *
	 * @param aModel the book to operate on
	 * @param aKey   the key to look for
	 * @return an account or null
	 */
	@SuppressWarnings("unchecked")
	public static Collection<GnucashWritableAccount> getAllAccountsWithKey(final GnucashWritableFile aModel, final String aKey) {
		Collection<GnucashWritableAccount> retval = new HashSet<GnucashWritableAccount>();
		Collection<? extends GnucashWritableAccount> accounts = aModel.getWritableAccounts();
		for (GnucashWritableAccount gnucashAccount : accounts) {
			if (gnucashAccount.getUserDefinedAttribute(aKey) != null) {
				retval.add(gnucashAccount);
			}
		}
		return retval;
	}

	/**
	 * Either get the first account that has any value for the given
	 * key in it's userDefinedProperties or ask the user to select
	 * an account and add the key.
	 *
	 * @param aModel        the book to operate on
	 * @param aKey          the key to look for
	 * @param aDefaultValue the value to apply if an account needed to be selected
	 * @param aQuestion     the translated question to ask the user when selecting an account.
	 * @return the account
	 * @throws JAXBException if setting the user-defined-property does not work
	 * @see #getAccountWithKey(GnucashWritableFile, String)
	 */
	@SuppressWarnings("unchecked")
	public static GnucashWritableAccount getOrConfigureAccountWithKey(final GnucashWritableFile aModel,
			final String aKey,
			final String aDefaultValue,
			final String aQuestion) throws JAXBException {
		GnucashWritableAccount retval = getAccountWithKey(aModel, aKey);
		if (retval != null) {
			return retval;
		}

		final JDialog selectAccountDialog = new JDialog((JFrame) null, "select account");
		selectAccountDialog.getContentPane().setLayout(new BorderLayout());
		final JList folderListBox = new JList(new Vector(aModel.getWritableAccounts()));
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent aE) {
				if (folderListBox.getSelectedIndices() != null) {
					if (folderListBox.getSelectedIndices().length == 1) {
						selectAccountDialog.setVisible(false);
					}
				}
			}

		});
		selectAccountDialog.getContentPane().add(new JLabel(aQuestion), BorderLayout.NORTH);
		selectAccountDialog.getContentPane().add(new JScrollPane(folderListBox), BorderLayout.CENTER);
		selectAccountDialog.getContentPane().add(okButton, BorderLayout.SOUTH);
		selectAccountDialog.setModal(true);
		selectAccountDialog.pack();
		selectAccountDialog.setVisible(true);
		retval = (GnucashWritableAccount) folderListBox.getSelectedValue();
		retval.setUserDefinedAttribute(aKey, aDefaultValue);
		return retval;
	}

	/**
	 * Either get the value as defined on the root account in it's userDefinedProperties
	 * or ask the user to enter one and add the key.
	 *
	 * @param aRootAccount  the root-account to operate on
	 * @param aKey          the key to look for
	 * @param aDefaultValue the default value to present to the user
	 * @param aQuestion     the translated question to ask the user when selecting an account.
	 * @return the entered value (not empty)
	 * @throws JAXBException if setting the user-defined-property does not work
	 * @see #getStringWithKey(GnucashWritableAccount, String)
	 */
	@SuppressWarnings("unchecked")
	public static String getOrConfigureStringWithKey(final GnucashWritableAccount aRootAccount,
			final String aKey,
			final String aDefaultValue,
			final String aQuestion) throws JAXBException {
		if (aRootAccount == null) {
			throw new IllegalArgumentException("null root account given!");
		}
		String retval = aRootAccount.getUserDefinedAttribute(aKey);
		if (retval != null) {
			return retval;
		}

		final JDialog selectAccountDialog = new JDialog((JFrame) null, "missing value");
		selectAccountDialog.getContentPane().setLayout(new BorderLayout());
		final JTextField folderListBox = new JTextField(aDefaultValue);
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent aE) {
				if (folderListBox.getText().trim().length() > 0) {
					selectAccountDialog.setVisible(false);
				}
			}

		});
		selectAccountDialog.getContentPane().add(new JLabel(aQuestion), BorderLayout.NORTH);
		selectAccountDialog.getContentPane().add(new JScrollPane(folderListBox), BorderLayout.CENTER);
		selectAccountDialog.getContentPane().add(okButton, BorderLayout.SOUTH);
		selectAccountDialog.setModal(true);
		selectAccountDialog.pack();
		selectAccountDialog.setVisible(true);
		retval = folderListBox.getText();
		aRootAccount.setUserDefinedAttribute(aKey, retval);
		return retval;
	}
}

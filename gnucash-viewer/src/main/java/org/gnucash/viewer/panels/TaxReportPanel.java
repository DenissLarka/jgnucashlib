package org.gnucash.viewer.panels;
/**
 * TaxReportPanel.java
 * created: 09.12.2007 13:09:26
 * (c) 2007 by <a href="http://Wolschon.biz">
 * Wolschon Softwaredesign und Beratung</a>
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

//automatically created propertyChangeListener-Support

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import javax.xml.bind.JAXBException;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.viewer.widgets.TransactionSum;
import org.gnucash.viewer.widgets.TransactionSum.SUMMATIONTYPE;
import org.gnucash.xml.GnucashAccount;
import org.gnucash.xml.GnucashFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * (c) 2007 by <a href="http://Wolschon.biz>
 * Wolschon Softwaredesign und Beratung</a>.<br/>
 * Project: jgnucashLib-V1<br/>
 * TaxReportPanel.java<br/>
 * created: 09.12.2007 13:09:26 <br/>
 * <br/><br/>
 * Panel to show some tax-related sums.
 *
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 */
public class TaxReportPanel extends JPanel {

	/**
	 * For serializing.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Our logger for debug- and error-ourput.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionSum.class);

	/**
	 * The sums we show.
	 */
	private final List<TransactionSum> mySums = new LinkedList<TransactionSum>();

	/**
	 * The panel to contain the created {@link TransactionSum}s.
	 */
	private final JPanel mySumsPanel = new JPanel();
	/**
	 * The panel to contain the buttons to export a CSV.
	 */
	private final JPanel myExportPanel = new JPanel();

	/**
	 * Button in the {@link #myExportPanel} to export
	 * a CSV.
	 */
	private final JButton myExportButton = new JButton("Export...");

	private enum ExportGranularities {
		Month {
			@Override
			public int getCalendarConstant() {
				return GregorianCalendar.MONTH;
			}
		},
		Year {
			@Override
			public int getCalendarConstant() {
				return GregorianCalendar.YEAR;
			}
		},
		Day {
			@Override
			public int getCalendarConstant() {
				return GregorianCalendar.DAY_OF_MONTH;
			}
		};

		@Override
		public String toString() {
			return "per " + super.toString();
		}

		/**
		 * @return a GregorianCalendar-constant to add 1 of;
		 */
		public abstract int getCalendarConstant();
	}

	;

	/**
	 * Combobox to select a yearly, monthly or dayly export
	 * with the {@link #myExportButton}.
	 */
	private final JComboBox myExportGranularityCombobox = new JComboBox(ExportGranularities.values());

	/**
	 * @param books The financial data we operate on.
	 */
	public TaxReportPanel(final GnucashFile books) {
		if (books != null) {
			initializeUI(books);
		}
	}

	/**
	 * @param books the accounts and transactions we work with.
	 */
	private void initializeUI(final GnucashFile books) {
		this.setLayout(new BorderLayout());
		Properties props = new Properties();
		final InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("TaxReportPanel.xml");

		if (resourceAsStream != null) {
			try {
				props.loadFromXML(resourceAsStream);
			}
			catch (Exception e) {
				LOGGER.error("Problem loading TaxReportPanel.xml", e);
				JLabel errorLabel = new JLabel(e.getMessage());
				this.add(errorLabel, BorderLayout.CENTER);
				return;
			}
		} else {
			throw new IllegalStateException("To use the OPTIONAL tax-report panel, please create a file TaxReportPanel.xml");
			/*try {
				props.loadFromXML(getClass().getResourceAsStream(
                        "TaxReportPanel.xml"));
                props.storeToXML(new FileOutputStream(configFile),
                        "UTF-8\n"
                        + "sum.([0-9]*).name - name of the entry\n"
                        + "sum.([0-9]*).target.([0-9]*) - (may be ommited) "
                        + "Look at all transactions containing these accounts You can specify qualified names, unqualified names or ids\n"
                        + "sum.([0-9]*).source.([0-9]*) - of these accounts add all splits that refer to these accounts\n"
                        + "sum.([0-9]*).type = ONLYTO        - sum only the ones that increase the balance of the account (other values:
                        ONLYFROM, ALL)\n"
                        + "sum.([0-9]*).type = ONLYFROM      - sum only the ones that decrease the balance of the account (other values:
                        ONLYFROM, ALL)\n"
                        + "sum.([0-9]*).type = ALL           - sum all the ones that increase or decreas the balance of the account (other
                        values: ONLYFROM, ALL)\n"
                        + "sum.([0-9]*).type = ALLRECURSIVE  - ignore targets and build the recursive balance\n");
                LOGGER.info("demo-config-file for TaxReportPanel has been stored in "
                        + configFile.getAbsolutePath());
            } catch (Exception e) {
                LOGGER.error("Problem loading or storing default-TaxReportPanel.xml", e);
                JLabel errorLabel = new JLabel(e.getMessage());
                this.add(errorLabel, BorderLayout.CENTER);
                return;
            }*/
		}

		LOGGER.info("calculating tax-panel...");
		for (int i = 0; props.containsKey("sum." + i + ".name"); i++) {
			try {
				createSum(books, props, i);
			}
			catch (Exception e) {
				LOGGER.error("[Exception] Problem in " + getClass().getName(), e);
			}
		}

		mySumsPanel.setLayout(new GridLayout(mySums.size(), 1));
		for (TransactionSum sum : mySums) {
			mySumsPanel.add(sum);
		}

		this.add(mySumsPanel, BorderLayout.CENTER);

		myExportButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent aE) {
				showExportCSVDialog();
			}
		});
		myExportPanel.add(myExportButton);
		myExportGranularityCombobox.setSelectedItem(ExportGranularities.Month);
		myExportGranularityCombobox.setEditable(false);
		myExportPanel.add(myExportGranularityCombobox);
		this.add(myExportPanel, BorderLayout.SOUTH);
		LOGGER.info("calculating tax-panel...DONE");
	}

	/**
	 * Show a dialog to export
	 * a CSV-file that contains the
	 * shown {@link TransactionSum}s
	 * for each month, year or day.
	 */
	protected void showExportCSVDialog() {
		JFileChooser fc = new JFileChooser();
		fc.setAcceptAllFileFilterUsed(true);
		fc.addChoosableFileFilter(new FileFilter() {

			@Override
			public boolean accept(final File aF) {
				return aF.isDirectory() || aF.getName().endsWith(".csv");
			}

			@Override
			public String getDescription() {
				return "CSV-file";
			}
		});
		int dialogResult = fc.showSaveDialog(this);
		if (dialogResult != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File file = fc.getSelectedFile();
		if (file.exists()) {
			int confirmation = JOptionPane.showConfirmDialog(this, "File exists. Replace file?");
			if (confirmation != JOptionPane.YES_OPTION) {
				showExportCSVDialog();
				return;
			}
		}
		ExportGranularities gran = (ExportGranularities) myExportGranularityCombobox.getSelectedItem();
		try {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			exportCSV(file, gran);
		}
		finally {
			setCursor(Cursor.getDefaultCursor());
		}
	}

	/**
	 * @param aFile the file to write to
	 * @param aGran the granularity
	 */
	private void exportCSV(final File aFile, final ExportGranularities aGran) {
		//TODO: implement CSV-export

		FileWriter fw = null;
		try {
			fw = new FileWriter(aFile);
			// write headers
			List<TransactionSum> sums = mySums;
			fw.write("day");
			for (TransactionSum transactionSum : sums) {
				fw.write(",");
				fw.write(transactionSum.getName());
			}
			fw.write("\n");

			// write data
			GregorianCalendar cal = new GregorianCalendar();
			int add = aGran.getCalendarConstant();
			DateFormat dateFormat = DateFormat.getDateInstance();
			//NumberFormat numberFormat = NumberFormat.getInstance();
			// we do NOT use getCurrencyInstance because it
			// contains a locale-specific currency-symbol
			for (int i = 0; i < 100; i++) {
				Date maxDate = cal.getTime();

				fw.write(dateFormat.format(maxDate));
				int transactionsCounted = 0;
				for (TransactionSum transactionSum : sums) {
					fw.write(",");
					transactionSum.setMaxDate(maxDate);
					FixedPointNumber value = transactionSum.getValue();
					if (value != null) {
						//fw.write(numberFormat.format(value));
						fw.write(value.toString());
					}
					transactionsCounted += transactionSum.getTransactionsCounted();

				}
				fw.write("\n");
				if (transactionsCounted == 0) {
					break;
					// we are walking back in time,
					// when there are no matching transactions,
					// all future runs will we a waste of time.
					// This happens often when add==YEAR
				}

				long old = cal.getTimeInMillis();
				if (add == GregorianCalendar.MONTH) {
					cal.set(GregorianCalendar.DAY_OF_MONTH, 1);
				}
				if (add == GregorianCalendar.YEAR) {
					cal.set(GregorianCalendar.DAY_OF_MONTH, 1);
					cal.set(GregorianCalendar.MONTH, 1);
				}
				// usually we are in the middle of a month,
				// so do not skip the first day of the current
				// month
				if (i != 0
						|| old == cal.getTimeInMillis()
						|| add == GregorianCalendar.DAY_OF_MONTH) {
					cal.add(add, -1);
				}
			}

			fw.close();
			fw = null;
		}
		catch (IOException e) {
			LOGGER.error("cannot write csv-file", e);
			JOptionPane.showMessageDialog(this, "Cannot write CSV-file\n"
					+ e.getMessage());
		}
		finally {
			if (fw != null) {
				try {
					fw.close();
				}
				catch (IOException e) {
					LOGGER.error("cannot close csv-file", e);
				}
			}
			for (TransactionSum transactionSum : mySums) {
				transactionSum.setMaxDate(null);
			}
		}
	}

	/**
	 * Load the settings for a TransactionSum from our properties-file.
	 *
	 * @param books the accounts and transactions we work with.
	 * @param prop  where to load the config from
	 * @param index the index into the configs
	 * @return thw widget created
	 */
	private TransactionSum createSum(final GnucashFile books, final Properties prop, final int index) {
		SUMMATIONTYPE type = SUMMATIONTYPE.getByName(prop.getProperty("sum." + index + ".type"));
		String name = prop.getProperty("sum." + index + ".name");
		Set<GnucashAccount> target = getAccountsByProperty(books, prop, "sum." + index + ".target");
		Set<GnucashAccount> source = getAccountsByProperty(books, prop, "sum." + index + ".source");
		TransactionSum sum = new TransactionSum(books, source, target, type, name, getMinDate(), getMaxDate());
		mySums.add(sum);
		return sum;
	}

	private Set<GnucashAccount> getAccountsByProperty(final GnucashFile aBooks,
			final Properties props, final String prefix) {

		Set<GnucashAccount> retval = new HashSet<GnucashAccount>();
		for (int i = 0; props.containsKey(prefix + "." + i); i++) {
			String idOrName = props.getProperty(prefix + "." + i);
			GnucashAccount account = aBooks.getAccountByIDorName(idOrName,
					idOrName);
			if (account == null) {
				LOGGER.error("account '" + idOrName + "' given in property '"
						+ prefix + "." + i + "' not found");
			} else {
				retval.add(account);
			}
		}
		return retval;
	}

	/**
	 * @param aBooks The books to set.
	 * @see GnucashFile
	 */
	public void setBooks(final GnucashFile aBooks) {
		if (mySumsPanel.getParent() == null) {
			initializeUI(aBooks);
		}

		JAXBException err = null;
		for (TransactionSum sum : mySums) {
			sum.setBooks(aBooks);
		}
		if (err != null) {
			throw new IllegalStateException("Cannot access XML-backend.", err);
		}
	}

	/**
	 * @return the minimum date for the {@link TransactionSum}.
	 */
	private Date getMinDate() {
		//TODO: provide an input-field for the year.
		return null;/*new Date ((new GregorianCalendar(1970, 01, 01))
				.getTimeInMillis())*/
	}

	/**
	 * @return the maximum date for the {@link TransactionSum}.
	 */
	private Date getMaxDate() {
		//      TODO: provide an input-field for the year.
		return null/*new Date ((new GregorianCalendar(2100, 12, 31)
		).getTimeInMillis())*/;
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

}



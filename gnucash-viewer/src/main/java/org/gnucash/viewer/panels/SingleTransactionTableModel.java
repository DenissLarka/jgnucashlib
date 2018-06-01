package org.gnucash.viewer.panels;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.event.TableModelListener;

import org.gnucash.viewer.models.GnucashTransactionsSplitsTableModel;
import org.gnucash.read.GnucashTransaction;
import org.gnucash.read.GnucashTransactionSplit;

/**
 * (c) 2009 by <a href="http://Wolschon.biz>Wolschon Softwaredesign und Beratung</a>.<br/>
 * Project: jgnucashLib-GPL<br/>
 * SingleTransactionTableModel<br/>
 * created: 11.03.2009 <br/>
 * <br/><br/>
 * <b>TableModel to show and edit the splits and details of a single transaction.</b>
 *
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 */
public class SingleTransactionTableModel implements GnucashTransactionsSplitsTableModel {

	/**
	 * The transaction we are showing.
	 */
	private GnucashTransaction myTransaction;

	/**
	 * The columns we display.
	 */
	private final String[] defaultColumnNames = new String[] {"date", "action", "description", "account", "+", "-"};

	/**
	 * How to format dates.
	 */
	public static final DateTimeFormatter DATEFORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	/**
	 * How to format currencies.
	 */
	public static final NumberFormat DEFAULTCURRENCYFORMAT = NumberFormat.getCurrencyInstance();

	/**
	 * @param aTransaction the transaction we are showing
	 */
	public SingleTransactionTableModel(final GnucashTransaction aTransaction) {
		super();
		myTransaction = aTransaction;
	}

	/**
	 * @return true if more then 1 currency is involved
	 */
	public boolean isMultiCurrency() {
		if (getTransaction() == null) {
			return false;
		}

		for (GnucashTransactionSplit split : getTransaction().getSplits()) {
			if (!split.getAccount().getCurrencyNameSpace().equals(getTransaction().getCurrencyNameSpace())
					|| !split.getAccount().getCurrencyID().equals(getTransaction().getCurrencyID())) {
				return true;
			}
		}

		return false;
	}

	/**
	 */
	public SingleTransactionTableModel() {
		super();
		myTransaction = null;
	}

	/**
	 * @return Returns the transaction.
	 * @see #myTransaction
	 */
	public GnucashTransaction getTransaction() {
		return myTransaction;
	}

	/**
	 * @param aTransaction The transaction to set.
	 * @see #myTransaction
	 */
	public void setTransaction(final GnucashTransaction aTransaction) {
		if (aTransaction == null) {
			throw new IllegalArgumentException("null 'aTransaction' given!");
		}

		Object old = myTransaction;
		if (old == aTransaction) {
			return; // nothing has changed
		}
		myTransaction = aTransaction;
	}

	/**
	 * ${@inheritDoc}.
	 */
	public GnucashTransactionSplit getTransactionSplit(final int aRowIndex) {
		return getTransactionSplits().get(aRowIndex);
	}

	/**
	 * ${@inheritDoc}.
	 */
	public List<GnucashTransactionSplit> getTransactionSplits() {
		GnucashTransaction transaction = getTransaction();
		if (transaction == null) {
			return new LinkedList<GnucashTransactionSplit>();
		}
		return new ArrayList<GnucashTransactionSplit>(transaction.getSplits());

	}

	/**
	 * {@inheritDoc}
	 *
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return defaultColumnNames.length;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {

		GnucashTransaction transaction = getTransaction();
		if (transaction == null) {
			return 0;
		}
		return 1 + getTransactionSplits().size();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see javax.swing.table.TableModel#getColumnClass(int)
	 */
	@SuppressWarnings("unchecked")
	public Class getColumnClass(final int columnIndex) {
		return String.class;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(final int rowIndex, final int columnIndex) {
		// "date", "action", "description", "account", "+", "-"};
		try {
			if (rowIndex == 0) {
				// show data of transaction
				switch (columnIndex) {
					case 0: //DATE
						return DATEFORMAT.format(getTransaction().getDatePosted());
					case 1: //action == transaction-Number
						return getTransactionNumber();
					case 2: //description
						return getTransactionDescription();
					case 3: { // account
						return "";
					}
					case 4: { // +
						return "";
					}
					case 5: { // -
						return "";
					}

					default:
						throw new IllegalArgumentException("illegal columnIndex " + columnIndex);
				}
			}

			GnucashTransactionSplit split = getTransactionSplit(rowIndex - 1);

			switch (columnIndex) {
				case 0: { //DATE
					return DATEFORMAT.format(split.getTransaction().getDatePosted());
				}
				case 1: { //action
					String action = split.getSplitAction();
					if (action == null || action.trim().length() == 0) {
						return "";
					}
					return action;
				}
				case 2: { //description
					String desc = split.getDescription();
					if (desc == null || desc.trim().length() == 0) {
						return "";
					}
					return desc;
				}
				case 3: { // account
					return split.getAccount().getQualifiedName();
				}
				case 4: { // +
					if (split.getValue().isPositive()) {
						if (split.getAccount().getCurrencyNameSpace().equals(getTransaction().getCurrencyNameSpace())
								&& split.getAccount().getCurrencyID().equals(getTransaction().getCurrencyID())) {
							return split.getValueFormatet();
						}
						return split.getValueFormatet() + " (" + split.getQuantityFormatet() + ")";
					} else {
						return "";
					}
				}
				case 5: { // -
					if (!split.getValue().isPositive()) {
						if (split.getAccount().getCurrencyNameSpace().equals(getTransaction().getCurrencyNameSpace())
								&& split.getAccount().getCurrencyID().equals(getTransaction().getCurrencyID())) {
							return split.getValueFormatet();
						}
						return split.getValueFormatet() + " (" + split.getQuantityFormatet() + ")";
					} else {
						return "";
					}
				}

				default:
					throw new IllegalArgumentException("illegal columnIndex " + columnIndex);
			}

		}
		catch (Exception x) {

			String message = "Internal Error in "
					+ getClass().getName() + ":getValueAt(int rowIndex="
					+ rowIndex
					+ ", int columnIndex="
					+ columnIndex
					+ ")!\n"
					+ "Exception of Type [" + x.getClass().getName() + "]\n"
					+ "\"" + x.getMessage() + "\"";
			StringWriter trace = new StringWriter();
			PrintWriter pw = new PrintWriter(trace);
			x.printStackTrace(pw);
			pw.close();
			message += trace.getBuffer();

			System.err.println(message);
			JOptionPane.showMessageDialog(null, message);
			return "ERROR";
		}
	}

	/**
	 * @return the description of the transaction as we display it. Never null.
	 */
	private Object getTransactionDescription() {
		String desc = getTransaction().getDescription();
		if (desc == null || desc.trim().length() == 0) {
			return "";
		}
		return desc;
	}

	/**
	 * @return the transaction-number as we display it. Never null.
	 */
	private Object getTransactionNumber() {
		String number = getTransaction().getTransactionNumber();
		if (number == null || number.trim().length() == 0) {
			return "";
		}
		return number;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
	 */
	public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
		// ignored, this model is read-only
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	public String getColumnName(final int columnIndex) {
		return defaultColumnNames[columnIndex]; //TODO: l10n
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see javax.swing.table.TableModel#addTableModelListener(javax.swing.event.TableModelListener)
	 */
	public void addTableModelListener(final TableModelListener l) {
		// ignored, this model is read-only
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see javax.swing.table.TableModel#removeTableModelListener(javax.swing.event.TableModelListener)
	 */
	public void removeTableModelListener(final TableModelListener l) {
		// ignored, this model is read-only
	}

	/**
	 * @param aRowIndex    the row
	 * @param aColumnIndex the column
	 * @return false
	 */
	public boolean isCellEditable(final int aRowIndex, final int aColumnIndex) {
		return false;
	}
}

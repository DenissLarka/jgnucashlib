/**
 * GnucashSimpleAccountTransactionsTableModel.java
 * Created on 15.05.2005
 * (c) 2005 by "Wolschon Softwaredesign und Beratung".
 *
 *
 * -----------------------------------------------------------
 * major Changes:
 *  15.05.2005 - initial version
 * ...
 *
 */
package org.gnucash.viewer.models;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.event.TableModelListener;

import org.gnucash.xml.GnucashAccount;
import org.gnucash.xml.GnucashTransactionSplit;

/**
 * created: 15.05.2005 <br/>
 *
 * A TableModel that shows the transaction and balance of an Account.
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 *
 */
public class GnucashSimpleAccountTransactionsTableModel implements GnucashTransactionsSplitsTableModel {

    /**
     * The account who's transactions we are showing.
     */
    private final GnucashAccount account;


    /**
     * The columns we display.
     */
    private final String[] defaultColumnNames = new String[] {"date", "transaction", "description", "+", "-", "balance"};

    /**
     * @param anAccount the account whos splits to display.
     */
    public GnucashSimpleAccountTransactionsTableModel(final GnucashAccount anAccount) {
        super();
        account = anAccount;
    }

    /**
     * the Table will be empty.
     *
     */
    public GnucashSimpleAccountTransactionsTableModel() {
        super();
        account = null;
    }

    /**
     * {@inheritDoc}
     */
    public int getColumnCount() {
        return defaultColumnNames.length;
    }

    /**
     * {@inheritDoc}
     */
    public int getRowCount() {

        List<? extends GnucashTransactionSplit> transactionSplits = getTransactionSplits();
        if (transactionSplits == null) {
            return 0;
        }
        return transactionSplits.size();
    }

    /**
     * @return the splits that affect this account.
     */
    public List<? extends GnucashTransactionSplit> getTransactionSplits() {
        if (account == null) {
            return new LinkedList<GnucashTransactionSplit>();
        }
        return account.getTransactionSplits();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public Class getColumnClass(final int columnIndex) {
        return String.class;
    }


    /**
     * How to format dates.
     */
    public static final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
    /**
     * How to format currencies.
     */
    private  NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
    /**
     * How to format currencies.
     */
    public static final NumberFormat defaultCurrencyFormat = NumberFormat.getCurrencyInstance();


    /**
     * Get the TransactionsSplit at the given index.
     * Throws an exception if the index is invalid.
     * @param rowIndex the split to get
     * @return the split
     */
    public GnucashTransactionSplit getTransactionSplit(final int rowIndex) {
        GnucashTransactionSplit split = getTransactionSplits().get(rowIndex);
        return split;
    }

    /**
     * {@inheritDoc}
     */
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        try {
            GnucashTransactionSplit split = getTransactionSplit(rowIndex);

            updateCurrencyFormat(split);

            switch(columnIndex) {
            case 0: { //DATE
                return dateFormat.format(split.getTransaction().getDatePosted());
            }
            case 1: { //transaction
                String desc = split.getTransaction().getDescription();
                if (desc == null || desc.trim().length() == 0) {
                    return "";
                }
                return desc;
            }
            case 2: { //description
                String desc = split.getDescription();
                if (desc == null || desc.trim().length() == 0) {
                    return "";
                }
                return desc;
            }
            case 3: { // +
              if (split.getQuantity().isPositive()) {
//                  //T O D O: use default-currency here
//                  if (account != null && !account.getCurrencyID().equals("EUR")) {
//                      return split.getValueFormatet();
//                  }
               return currencyFormat.format(split.getQuantity());
              } else {
                return "";
            }
            }
            case 4: { // -
                if (!split.getQuantity().isPositive()) {
//                    if (account != null && !account.getCurrencyID().equals("EUR")) {
//                        return split.getValueFormatet();
//                    }
                 return currencyFormat.format(split.getQuantity());
                } else {
                    return "";
                }
              }
            case 5: { // balance
                if (account != null) {
                    return currencyFormat.format(account.getBalance(split));
                } else {
                    return currencyFormat.format(split.getAccount().getBalance(split));
                }
              }
            default:
                throw new IllegalArgumentException("illegal columnIndex " + columnIndex);
            }


        } catch (Exception x) {

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

            final String message2 = message;
            System.err.println(message);
            Runnable runnable = new Runnable() {
                public void run() {
                    JOptionPane.showMessageDialog(null, message2);
                }
            };
            new Thread(runnable).start();
            return "ERROR";
        }
    }

    /**
     * @param split the split whos account to use for the currency
     */
    private void updateCurrencyFormat(final GnucashTransactionSplit split) {
        currencyFormat = NumberFormat.getNumberInstance();
        try {
            if (split.getAccount().getCurrencyNameSpace().equalsIgnoreCase("ISO4217")) {
                Currency currency = Currency.getInstance(split.getAccount().getCurrencyID());
                currencyFormat = NumberFormat.getCurrencyInstance();
                currencyFormat.setCurrency(currency);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {

    }

    /**
     * {@inheritDoc}
     */
    public String getColumnName(final int columnIndex) {
        return defaultColumnNames[columnIndex]; //TODO: l10n
    }

    /**
     * @see #addTableModelListener(TableModelListener)
     */
    private final Set<TableModelListener> myTableModelListeners = new HashSet<TableModelListener>();


    /**
     * @see #addTableModelListener(TableModelListener)
     * @return the tableModelListeners
     */
    protected Set<TableModelListener> getTableModelListeners() {
        return myTableModelListeners;
    }

    /**
     *
     * @see javax.swing.table.TableModel#addTableModelListener(javax.swing.event.TableModelListener)
     */
    public void addTableModelListener(final TableModelListener l) {
        myTableModelListeners.add(l);
    }

    /**
     *
     * @see javax.swing.table.TableModel#removeTableModelListener(javax.swing.event.TableModelListener)
     */
    public void removeTableModelListener(final TableModelListener l) {
        myTableModelListeners.remove(l);

    }

}

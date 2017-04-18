/**
 * GnucashTransactionsSplitsTableModel.java
 * created: 21.10.2006 17:38:52
 */
package org.gnucash.viewer.models;



import javax.swing.table.TableModel;

import org.gnucash.xml.GnucashTransactionSplit;


/**
 * (c) 2006 by Wolschon Softwaredesign und Beratung.<br/>
 * Project: gnucashReader<br/>
 * GnucashTransactionsSplitsTableModel.java<br/>
 * created: 21.10.2006 17:38:52 <br/>
 *<br/><br/>
 * <b>TableModels implementing this interface contain a list of transactions.
 * They may be all transactions of an account, a search-result or sth. similar.</b>
 * @author <a href="Marcus@Wolschon.biz">Marcus Wolschon</a>
 */
public interface GnucashTransactionsSplitsTableModel extends TableModel {

    /**
     * Get the number of transactons.
     * @return an integer >=0
     */
    int getRowCount();

    /**
     * Get the TransactionsSplit at the given index.
     * Throws an exception if the index is invalid.
     * @param rowIndex the split to get
     * @return the split
     */
    GnucashTransactionSplit getTransactionSplit(final int rowIndex);


}

/**
 * TransactionsPanel.java
 * created: 21.10.2006 17:17:17
 */
package org.gnucash.viewer.panels;

//other imports
//automatically created logger for debug and error -output

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalDateTime;
import java.util.Collection;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.gnucash.read.GnucashTransaction;
import org.gnucash.read.GnucashTransactionSplit;
import org.gnucash.viewer.actions.TransactionSplitAction;
import org.gnucash.viewer.models.GnucashTransactionsSplitsTableModel;

/**
 * (c) 2006 by Wolschon Softwaredesign und Beratung.<br/>
 * Project: gnucashReader<br/>
 * TransactionsPanel.java<br/>
 * created: 21.10.2006 17:17:17 <br/>
 * <br/><br/>
 * <b>This Panel shows all splits of a single transaction.</b>
 *
 * @author <a href="Marcus@Wolschon.biz">Marcus Wolschon</a>
 */
public class ShowTransactionPanel extends JPanel {


	/**
	 * Automatically created logger for debug and error-output.
	 */
	static final Logger LOGGER = LoggerFactory.getLogger(ShowTransactionPanel.class);

	/**
	 * for serializing.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The transaction we are showing.
	 */
	private GnucashTransaction myTransaction = null;


	/**
	 * The actions we have on Splits.
	 */
	private Collection<TransactionSplitAction> mySplitActions;


	/**
	 * @param aTransaction The transaction we are showing.
	 */
	public ShowTransactionPanel(final GnucashTransaction aTransaction) {
		super();
		myTransaction = aTransaction;

		initialize();
	}

	/**
	 * initialize the Gui.
	 */
	private void initialize() {
		this.setLayout(new BorderLayout());
		this.add(getTransactionTableScrollPane(), BorderLayout.CENTER);
		setTransaction(getTransaction());
	}

	/**
	 * @param row the row to create a popup for
	 * @return the popup-menu
	 */
	protected JPopupMenu getCellPopupMenu(final int row) {
		JPopupMenu menu = new JPopupMenu();
		final GnucashTransactionSplit split = model.getTransactionSplit(row - 1);
		if (split != null) {
			Collection<TransactionSplitAction> splitActions = getSplitActions();
			for (TransactionSplitAction splitAction2 : splitActions) {
				final TransactionSplitAction splitAction = splitAction2;
				JMenuItem newMenuItem = new JMenuItem(new Action() {

					@Override
					public void addPropertyChangeListener(final PropertyChangeListener aListener) {
						splitAction.addPropertyChangeListener(aListener);
					}

					@Override
					public Object getValue(final String aKey) {
						return splitAction.getValue(aKey);
					}

					@Override
					public boolean isEnabled() {
						splitAction.setSplit(split);
						return splitAction.isEnabled();
					}

					@Override
					public void putValue(final String aKey, final Object aValue) {
						splitAction.putValue(aKey, aValue);
					}

					@Override
					public void removePropertyChangeListener(final PropertyChangeListener aListener) {
						splitAction.removePropertyChangeListener(aListener);
					}

					@Override
					public void setEnabled(final boolean aB) {
						splitAction.setEnabled(aB);
					}

					@Override
					public void actionPerformed(final ActionEvent aE) {
						splitAction.setSplit(split);
						splitAction.actionPerformed(aE);
					}

				});
				menu.add(newMenuItem);
			}

			LOGGER.info("showing popup-menu with " + splitActions.size() + " split-actions");
		} else {
			LOGGER.info("no split found, not showing popup-menu");
		}
		return menu;
	}

	/**
	 * make us visible.
	 */
	public ShowTransactionPanel() {
		super();
		myTransaction = null;

		initialize();
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

		Object old = myTransaction;
		if (old == aTransaction) {
			return; // nothing has changed
		}
		myTransaction = aTransaction;

		SingleTransactionTableModel model = null;

		if (aTransaction == null) {
			model = new SingleTransactionTableModel();
			setPreferredSize(new Dimension(0, 0));
			invalidate();
		} else {
			model = new SingleTransactionTableModel(aTransaction);
			setPreferredSize(new Dimension(200, 200));
			invalidate();
		}
		setModel(model);
	}

	/**
	 * The model of our ${@link #transactionTable}.
	 */
	private GnucashTransactionsSplitsTableModel model;

	/**
	 * The table showing the splits.
	 */
	private JTable transactionTable;

	/**
	 * My SCrollPane over {@link #transactionTable}.
	 */
	private JScrollPane transactionTableScrollPane;


	/**
	 * @return Returns the model.
	 * @see #model
	 */
	public GnucashTransactionsSplitsTableModel getModel() {
		return model;
	}

	/**
	 * @param aModel The model to set.
	 * @see #model
	 */
	protected void setModel(final SingleTransactionTableModel aModel) {
		if (aModel == null) {
			throw new IllegalArgumentException("null 'aModel' given!");
		}

		Object old = model;
		if (old == aModel) {
			return; // nothing has changed
		}
		model = aModel;

		getTransactionTable().setModel(model);
		transactionTable.setAutoCreateRowSorter(false);
		// set column-width
		FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(transactionTable.getFont());


		final int extraDateWidth = 5;
		transactionTable.getColumn("date").setPreferredWidth(SwingUtilities.computeStringWidth(metrics,
				SingleTransactionTableModel.DATEFORMAT.format(LocalDateTime.now())) + extraDateWidth);

		final int maxAnticipatedValue = 10000;
		int currencyWidth = SwingUtilities.computeStringWidth(metrics, SingleTransactionTableModel.DEFAULTCURRENCYFORMAT.format(maxAnticipatedValue));

		if (aModel.isMultiCurrency()) {
			final int extraWidth = 20;
			currencyWidth = currencyWidth * 2 + extraWidth;
		}
		transactionTable.getColumn("+").setPreferredWidth(currencyWidth);
		transactionTable.getColumn("-").setPreferredWidth(currencyWidth);
		transactionTable.getColumn("action").setPreferredWidth(SwingUtilities.computeStringWidth(metrics, "VERKAUF"));


		transactionTable.getColumn("date").setMaxWidth(SwingUtilities.computeStringWidth(metrics, SingleTransactionTableModel.DATEFORMAT.format(LocalDateTime.now())) + 5);
		transactionTable.getColumn("+").setMaxWidth(currencyWidth);
		transactionTable.getColumn("-").setMaxWidth(currencyWidth);
		transactionTable.getColumn("action").setMaxWidth(SwingUtilities.computeStringWidth(metrics, "VERKAUF          "));
	}

	/**
	 * This method initializes transactionTableScrollPane.
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getTransactionTableScrollPane() {
		if (transactionTableScrollPane == null) {
			transactionTableScrollPane = new JScrollPane();
			transactionTableScrollPane.setViewportView(getTransactionTable());
		}
		return transactionTableScrollPane;
	}

	/**
	 * This method initializes transactionTable.
	 *
	 * @return javax.swing.JTable
	 */
	protected JTable getTransactionTable() {
		if (transactionTable == null) {
			transactionTable = new JTable();
			setModel(new SingleTransactionTableModel());
			transactionTable.addMouseListener(new MouseAdapter() {

				/** show ShowTransactionPanel#getCellPopupMenu() if mousePressed is a popupTrigger on this platform.
				 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
				 */
				@Override
				public void mousePressed(final MouseEvent aE) {
					try {
						if (aE.isPopupTrigger()) {
							int row = transactionTable.rowAtPoint(aE.getPoint());
							if (row > 0) {
								getCellPopupMenu(row).show((JComponent) aE.getSource(),
										aE.getX(), aE.getY());
							} else {
								LOGGER.info("no split-row below mouse found, not showing popup-menu");
							}
						}
					} catch (Exception e) {
						LOGGER.error("error showing popup-menu", e);
					}
				}

				/** show ShowTransactionPanel#getCellPopupMenu() if mouseReleased is a popupTrigger on this platform.
				 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
				 */
				@Override
				public void mouseReleased(final MouseEvent aE) {
					try {
						if (aE.isPopupTrigger()) {
							int row = transactionTable.rowAtPoint(aE.getPoint());
							if (row > 0) {
								getCellPopupMenu(row).show((JComponent) aE.getSource(),
										aE.getX(), aE.getY());
							} else {
								LOGGER.info("no split-row below mouse found, not showing popup-menu");
							}
						}
					} catch (Exception e) {
						LOGGER.error("error showing popup-menu", e);
					}
				}
			});
		}
		return transactionTable;
	}


	/**
	 * Used to populate context-menus.
	 *
	 * @param aSplitActions the actions we are to support on splits
	 */
	public void setSplitActions(final Collection<TransactionSplitAction> aSplitActions) {
		mySplitActions = aSplitActions;
		LOGGER.info("ShowTransactionPanel is given " + (mySplitActions == null ? "no" : mySplitActions.size()) + " split-actions");
	}

	/**
	 * @return the splitActions
	 */
	protected Collection<TransactionSplitAction> getSplitActions() {
		LOGGER.info("ShowTransactionPanel has " + (mySplitActions == null ? "no" : mySplitActions.size()) + " split-actions");
		return mySplitActions;
	}
}

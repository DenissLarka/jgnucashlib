/**
 * DesriptionCellRenderer.java
 * created: 30.09.2008 17:33:33
 * (c) 2008 by <a href="http://Wolschon.biz">Wolschon Softwaredesign und Beratung</a>
 * This file is part of jGnucashLib-GPL by Marcus Wolschon <a href="mailto:Marcus@Wolscon.biz">Marcus@Wolscon.biz</a>.
 * You can purchase support for a sensible hourly rate or
 * a commercial license of this file (unless modified by others) by contacting him directly.
 *
 *  jGnucashLib-GPL is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  jGnucashLib-GPL is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with jGnucashLib-GPL.  If not, see <http://www.gnu.org/licenses/>.
 *
 ***********************************
 * Editing this file:
 *  -For consistent code-quality this file should be checked with the
 *   checkstyle-ruleset enclosed in this project.
 *  -After the design of this file has settled it should get it's own
 *   JUnit-Test that shall be executed regularly. It is best to write
 *   the test-case BEFORE writing this class and to run it on every build
 *   as a regression-test.
 */
package org.gnucash.viewer.panels;

//automatically created logger for debug and error -output
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//automatically created propertyChangeListener-Support
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.gnucash.xml.GnucashTransactionSplit;
import org.gnucash.viewer.models.GnucashTransactionsSplitsTableModel;


/**
 * (c) 2008 by <a href="http://Wolschon.biz>Wolschon Softwaredesign und Beratung</a>.<br/>
 * Project: jGnucashLib-GPL<br/>
 * DesriptionCellRenderer.java<br/>
 * created: 30.09.2008 17:33:33 <br/>
 *<br/><br/>
 * <b>Renderer that turns any value that contains the string "TODO" bold.</a>
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 */
public class DesriptionCellRenderer implements TableCellRenderer {

	/**
	 * Automatically created logger for debug and error-output.
	 */
	private static final Logger LOG = Logger
			.getLogger(DesriptionCellRenderer.class.getName());

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
	 * @param propertyName  The name of the property that was listened on.
	 * @param listener  The PropertyChangeListener to be removed
	 */
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
	 * @param listener  The PropertyChangeListener to be removed
	 */
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
	public String toString() {
		return "DesriptionCellRenderer@" + hashCode();
	}

	/** 
	 * ${@inheritDoc}.
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		JLabel renderer = new JLabel(value == null?"":value.toString());
		Font f = renderer.getFont();
		renderer.setFont(f.deriveFont(f.getStyle() ^ Font.BOLD));

		try {
			markTokens(renderer, renderer.getText());

			// mark unbalanced transactions in red.
			TableModel model = table.getModel();
			if (model instanceof GnucashTransactionsSplitsTableModel) {
				GnucashTransactionsSplitsTableModel tmodel = (GnucashTransactionsSplitsTableModel) model;
				GnucashTransactionSplit split = tmodel.getTransactionSplit(row);
				if (split != null) {
					if (split.getTransaction() != null) {
						try {
							List<? extends GnucashTransactionSplit> splits = split.getTransaction().getSplits();
							for (GnucashTransactionSplit gnucashTransactionSplit : splits) {
								if (gnucashTransactionSplit != null) {
									markTokens(renderer, gnucashTransactionSplit.getDescription());
								}
							}
						} catch (Exception e) {
							LOG.log(Level.SEVERE,"[Exception] Problem in "
									+ getClass().getName() + ":getTableCellRendererComponent()"
									+ " while traversing splits",
									e);
						}
					}
					markUnbalanced(renderer, split);
				}
			}
		} catch (Exception e) {
			LOG.log(Level.SEVERE,"[Exception] Problem in "
			           + getClass().getName() + ":getTableCellRendererComponent()",
			             e);
		}

		// ideas for future enhancements: allow plugins to display icons.
		// e.g. display a hbci-icon if this is an hbci-imported transaction
		// to display the original transaction-details on click.
		return renderer;
	}

	/**
	 * Check for unbalanced transactions and mark them in red. 
	 * @param renderer the renderer to modify it's style
	 * @param split the transaction we display
	 */
	private void markUnbalanced(final JLabel renderer, final GnucashTransactionSplit split) {
		try {
			if (split.getTransaction() == null || !split.getTransaction().isBalanced()) {
				renderer.setForeground(Color.red);
			}
		} catch (Exception e) {
			LOG.log(Level.SEVERE,"[Exception] Problem in "
			           + getClass().getName() + ":markUnbalanced()",
			             e);
			renderer.setForeground(Color.red);
		}
	}

	/**
	 * Make them bold of they contain the Text "TODO".or " NAK".<br/>
	 * If not and they contain " OK", mark them dark-green.
	 * @param renderer the renderer to modify it's style
	 */
	private void markTokens(final JLabel renderer, final String text) {
		try {
			if (text.contains("TODO")
				|| text.contains(" NAK")) {
				Font f = renderer.getFont();
				renderer.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
			} else if (text.contains(" OK")) {
				renderer.setForeground(Color.GREEN.darker());
			}
		} catch (Exception e) {
			LOG.log(Level.SEVERE,"[Exception] Problem in "
			           + getClass().getName() + ":markTokens()",
			             e);
		}
	}
}



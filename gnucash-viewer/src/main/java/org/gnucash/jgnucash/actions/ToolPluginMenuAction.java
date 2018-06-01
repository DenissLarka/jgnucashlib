/**
 * ToolPluginMenuAction.java
 * Created on 19.11.2005
 * (c) 2005 by "Wolschon Softwaredesign und Beratung".
 * <p>
 * Permission is granted to use, modify, publish and sub-license this code
 * as specified in the contract. If nothing else is specified these rights
 * are given non-exclusively with no restrictions solely to the contractor(s).
 * If no specified otherwise I reserve the right to use, modify, publish and
 * sub-license this code to other parties myself.
 * <p>
 * Otherwise, this code is made available under GPLv3 or later.
 * <p>
 * -----------------------------------------------------------
 * major Changes:
 * 29.11.2005 - initial version, factored out of jGnucash
 * ...
 */
package org.gnucash.jgnucash.actions;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import org.java.plugin.registry.Extension;

import org.gnucash.write.GnucashWritableAccount;
import org.gnucash.write.GnucashWritableFile;
import org.gnucash.jgnucash.JGnucash;
import org.gnucash.jgnucash.plugin.ToolPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The action-listeners we use for the ImportMenu.
 */
public final class ToolPluginMenuAction implements ActionListener {
	/**
	 * Our logger for debug- and error-output.
	 */
	static final Logger LOGGER = LoggerFactory.getLogger(ToolPluginMenuAction.class);

	/**
	 * Our JGnucash.java.
	 * @see ToolPluginMenuAction
	 */
	private final JGnucash myJGnucashEditor;

	/**
	 * @param aPlugin The import-plugin.
	 * @param aPluginName The name of the plugin
	 * @param aGnucash TODO
	 */
	public ToolPluginMenuAction(final JGnucash aGnucash, final Extension aPlugin, final String aPluginName) {
		super();
		myJGnucashEditor = aGnucash;
		ext = aPlugin;
		pluginName = aPluginName;
	}

	/**
	 * The import-plugin.
	 */
	private final Extension ext;

	/**
	 * The name of the plugin.
	 */
	private final String pluginName;

	@Override
	public void actionPerformed(final ActionEvent e) {
		try {
			GnucashWritableFile wModel = myJGnucashEditor.getWritableModel();
			if (wModel == null) {
				JOptionPane.showMessageDialog(myJGnucashEditor, "No open file.",
						"Please open a gnucash-file first!",
						JOptionPane.WARNING_MESSAGE);
				return;
			}

			// Activate plug-in that declares extension.
			myJGnucashEditor.getPluginManager().activatePlugin(ext.getDeclaringPluginDescriptor().getId());
			// Get plug-in class loader.
			ClassLoader classLoader = myJGnucashEditor.getPluginManager().getPluginClassLoader(
					ext.getDeclaringPluginDescriptor());
			// Load Tool class.
			Class toolCls = classLoader.loadClass(ext.getParameter("class").valueAsString());
			// Create Tool instance.
			Object o = toolCls.newInstance();
			if (!(o instanceof ToolPlugin)) {
				LOGGER.error("Plugin '" + pluginName + "' does not implement ToolPlugin-interface.");
				JOptionPane.showMessageDialog(myJGnucashEditor, "Error",
						"Plugin '" + pluginName + "' does not implement ToolPlugin-interface.",
						JOptionPane.ERROR_MESSAGE);
				return;

			}
			ToolPlugin importer = (ToolPlugin) o;
			try {
				myJGnucashEditor.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				GnucashWritableAccount selectedAccount = (GnucashWritableAccount) myJGnucashEditor.getSelectedAccount();
				String message = importer.runTool(wModel, selectedAccount);
				if (message != null && message.length() > 0) {
					JOptionPane.showMessageDialog(myJGnucashEditor, "Tool OK",
							"The tool-use was a success:\n" + message,
							JOptionPane.INFORMATION_MESSAGE);
				}
			} catch (Exception e1) {
				LOGGER.error("Tool-use via Plugin '" + pluginName + "' failed.", e1);
				JOptionPane.showMessageDialog(myJGnucashEditor, "Error",
						"Tool-use via Plugin '" + pluginName + "' failed.\n"
								+ "[" + e1.getClass().getName() + "]: " + e1.getMessage(),
						JOptionPane.ERROR_MESSAGE);
			} finally {
				myJGnucashEditor.setCursor(Cursor.getDefaultCursor());
			}
		} catch (Exception e1) {
			LOGGER.error("Could not activate requested Tool-plugin '" + pluginName + "'.", e1);
			JOptionPane.showMessageDialog(myJGnucashEditor, "Error",
					"Could not activate requested Tool-plugin '" + pluginName + "'.\n"
							+ "[" + e1.getClass().getName() + "]: " + e1.getMessage(),
					JOptionPane.ERROR_MESSAGE);
		}
	}
}

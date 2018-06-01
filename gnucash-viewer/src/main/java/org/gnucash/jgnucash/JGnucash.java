/**
 * JGnucash.java
 * Created on 16.05.2005
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
 * 16.05.2005 - initial version
 * 06.11.2005 - added file-menu-plugins
 * ...
 */
package org.gnucash.jgnucash;

import org.gnucash.fileformats.gnucash.GnucashWritableFile;
import org.gnucash.fileformats.gnucash.jwsdpimpl.GnucashFileWritingImpl;
import org.gnucash.jgnucash.actions.*;
import org.gnucash.jgnucash.panels.WritableTransactionsPanel;
import org.gnucash.viewer.JGnucashViewer;
import org.gnucash.viewer.actions.AccountAction;
import org.gnucash.viewer.actions.TransactionSplitAction;
import org.gnucash.viewer.panels.TransactionsPanel;
import org.gnucash.xml.GnucashFile;
import org.java.plugin.PluginManager;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.IntegrityCheckReport;
import org.java.plugin.registry.PluginDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.xml.bind.JAXBException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * created: 16.05.2005 <br/>
 *
 * (Shall become a simple java-reimplementation of gnucash
 * that can read and write gnucash-files.)<br/>
 *
 * Extended version of JGnucashViewer that allows for
 * changing and writing the gnucash-file.
 * <br/>
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 */
public class JGnucash extends JGnucashViewer {

	/**
	 * empty constructor for GUI-designers.
	 * DO NOT CALL.
	 */
	public JGnucash() {
	}

	/**
	 * Use this constructor.
	 * @param manager the plugin-manager to use for loading plugins attached to extension-points of the main-plugin
	 * @param descriptor the descriptor for the main-plugin
	 */
	public JGnucash(final PluginManager manager, final PluginDescriptor descriptor) {
		this();
		setPluginManager(manager);
		setPluginDescriptor(descriptor);
		initializeGUI();
	}


	/**
	 * Our logger for debug- and error-output.
	 */
	static final Logger LOGGER = LoggerFactory.getLogger(JGnucash.class);

	/**
	 * The data-model.
	 */
	private GnucashWritableFile model;

	/**
	 * Overridden to create  {@link GnucashWritableFile}.
	 * ${@inheritDoc}.
	 */
	@Override
	protected final GnucashFile createModelFromFile(final File f) throws IOException {
		return new GnucashFileWritingImpl(f);
	}

	/**
	 * @param args ignored.
	 */
	public static void main(final String[] args) {
		JGnucash ste = new JGnucash();
		installNimbusLaF();
		ste.initializeGUI();
		ste.setVisible(true);
		if (args.length > 0) {
			ste.loadFile(new File(args[0]));
		}
		ste.getJSplitPane().setDividerLocation(0.3);
	}

	/**
	 * The file-menu.
	 */
	private JMenu fileMenu;

	/**
	 * File->Save.
	 */
	private JMenuItem fileSaveMenuItem = null;

	/**
	 * File->Save as...
	 */
	private JMenuItem fileSaveAsMenuItem = null;

//    /**
//     * File-Menu to import other gnucash-files
//     * into this one.
//     */
//    private JMenuItem fileImport = null;

	/**
	 * The Import-Menu.
	 */
	private JMenu importMenu = null;

	/**
	 * The Import-Menu.
	 */
	private JMenu toolMenu = null;

	/**
	 * The main-entry-point to our plugin-api.
	 */
	private PluginManager pluginManager = null;

	/**
	 * The descriptor for our top-level application-plugin.
	 */
	private PluginDescriptor pluginDescriptor = null;

	/**
	 * The panel to show the current transactions.
	 */
	private WritableTransactionsPanel writableTransactionsPanel;

	/**
	 * True if we did make changes to our model.
	 * Used to enable/disable the file->save -item.
	 */
	private boolean hasChanged = false;

	/**
	 * Our menu-bar.
	 */
	private JMenuBar menuBar;

	/**
	 * The "help"-menu.
	 */
	private JMenu helpMenu;

	/**
	 * The "help->PluginReport" menu-item.
	 */
	private JMenuItem helpPluginReport;

	/**
	 * The main-entry-point to our plugin-api.
	 * @return the pluginManager
	 */
	public PluginManager getPluginManager() {
		return pluginManager;
	}

	/**
	 * The main-entry-point to our plugin-api.
	 * @param aPluginManager the pluginManager to set
	 */
	public void setPluginManager(final PluginManager aPluginManager) {
		pluginManager = aPluginManager;
	}

	/**
	 * The descriptor for our top-level application-plugin.
	 * @return the pluginDescriptor
	 */
	public PluginDescriptor getPluginDescriptor() {
		return pluginDescriptor;
	}

	/**
	 * The descriptor for our top-level application-plugin.
	 * @param aPluginDescriptor the pluginDescriptor to set
	 */
	public void setPluginDescriptor(final PluginDescriptor aPluginDescriptor) {
		pluginDescriptor = aPluginDescriptor;
	}


	/**
	 * @return a menu-item to show a report on plugin-loading
	 */
	private JMenuItem getHelpPluginReportMenu() {
		if (helpPluginReport == null) {
			helpPluginReport = new JMenuItem();
			helpPluginReport.setText("Plugin Report");
			helpPluginReport.addActionListener(new ActionListener() {

												   @SuppressWarnings("unchecked")
												   @Override
												   public void actionPerformed(final ActionEvent e) {
													   PluginManager manager = getPluginManager();
													   IntegrityCheckReport report = manager.getRegistry().getRegistrationReport();
													   StringBuilder message = new StringBuilder();
													   Collection<IntegrityCheckReport.ReportItem> items = report.getItems();
													   for (IntegrityCheckReport.ReportItem reportItem : items) {
														   String severity = ""; /*unknown: ";
						if (reportItem.getSeverity() == IntegrityCheckReport.ReportItem.SEVERITY_ERROR) {
                            severity = "Error: ";
                        } else if (reportItem.getSeverity() == IntegrityCheckReport.ReportItem.SEVERITY_WARNING) {
                            severity = "Warning: ";
                        } else if (reportItem.getSeverity() == IntegrityCheckReport.ReportItem.SEVERITY_INFO) {
                            severity = "Info: ";
                        };*/
														   message.append(severity)
                               /*.append('(')
                               .append(reportItem.getCode())
                               .append(')')*/
																   .append(reportItem.getMessage())
																   .append('\n');
													   }
													   JOptionPane.showMessageDialog(JGnucash.this,
															   message.toString(),
															   "Plugins-Report",
															   JOptionPane.INFORMATION_MESSAGE);
												   }
											   }
			);
		}
		return helpPluginReport;
	}


	/**
	 * This method initializes transactionsPanel.
	 *
	 * @return javax.swing.JTable
	 */
	@Override
	protected TransactionsPanel getTransactionsPanel() {
		return getWritableTransactionsPanel();
	}

	/**
	 * This method initializes transactionsPanel.
	 *
	 * @return javax.swing.JTable
	 */
	protected WritableTransactionsPanel getWritableTransactionsPanel() {
		if (writableTransactionsPanel == null) {
			writableTransactionsPanel = new WritableTransactionsPanel(getPluginManager(), getPluginDescriptor());
			writableTransactionsPanel.setSplitActions(getSplitActions());
		}
		return writableTransactionsPanel;
	}


	/**
	 * {@inheritDoc}
	 * @see org.gnucash.jgnucash.JGnucash#getJMenuBar()
	 */
	@Override
	public JMenuBar getJMenuBar() {
		if (menuBar == null) {
			menuBar = super.getJMenuBar();
			// insert the "Import"-menu before the help-menu.
			menuBar.add(getImportMenu(), menuBar.getMenuCount() - 1);
			menuBar.add(getToolMenu(), menuBar.getMenuCount() - 1);
		}
		return menuBar;
	}

	/**
	 * This method initializes FileMenu
	 * including all menu-items added by plugins.
	 *
	 * @return javax.swing.JMenu
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected JMenu getFileMenu() {
		if (fileMenu == null) {
			fileMenu = super.getFileMenu();
			fileMenu.setText("File");
			fileMenu.setMnemonic('f');
			int i = 1;
			fileMenu.add(getFileSaveMenuItem(), i++);
			fileMenu.add(getFileSaveAsMenuItem(), i++);

			// allow plugins to supply file-open and file-save -actions
			PluginManager manager = getPluginManager();
			// if we are configured for the plugin-api
			if (manager != null) {
				ExtensionPoint toolExtPoint = manager.getRegistry().getExtensionPoint(
						getPluginDescriptor().getId(), "DataSource");
				for (Iterator<Extension> it = toolExtPoint.getConnectedExtensions().iterator(); it.hasNext(); ) {
					Extension ext = it.next();
					String pluginName = "unknown";

					try {
						pluginName = ext.getParameter("name").valueAsString();

						LOGGER.debug("adding menu-item for DataSource-plugin " + pluginName
								+ " - support for writeTo=" + ext.getParameter("supportsWritingTo"));
						JMenuItem newMenuItem = new JMenuItem();
						newMenuItem.putClientProperty("extension", ext);
						Parameter descrParam = ext.getParameter("description");
						Parameter iconParam = ext.getParameter("icon");
						URL iconUrl = null;
						if (iconParam != null) {
							try {
								iconUrl = getPluginManager().getPluginClassLoader(
										ext.getDeclaringPluginDescriptor()).getResource(iconParam.valueAsString());
								if (iconUrl != null) {
									newMenuItem.setIcon(new ImageIcon(iconUrl));
								}
							} catch (Exception e) {
								LOGGER.error("cannot load icon for Loader-Plugin '" + pluginName + "'", e);
							}
						}
						newMenuItem.setText("open via " + pluginName + "...");
						if (descrParam != null) {
							newMenuItem.setToolTipText(descrParam.valueAsString());
						}
						newMenuItem.addActionListener(new OpenFilePluginMenuAction(this, ext, pluginName));
						fileMenu.add(newMenuItem, 1); // Open
						if (ext.getParameter("supportsWritingTo").valueAsString().equalsIgnoreCase("true")) {
							LOGGER.debug("Plugin " + pluginName + " also supportes 'write to', adding menu-item");
							JMenuItem newSaveAsMenuItem = new JMenuItem();
							newSaveAsMenuItem.putClientProperty("extension", ext);
							newSaveAsMenuItem.setText("Save to via " + pluginName + "...");
							if (iconUrl != null) {
								newMenuItem.setIcon(new ImageIcon(iconUrl));
							}
							newSaveAsMenuItem.addActionListener(new SaveAsFilePluginMenuAction(this, ext, pluginName));
							fileMenu.add(newSaveAsMenuItem, 2); // Open

						}
					} catch (Exception e) {
						LOGGER.error("cannot load Loader-Plugin '" + pluginName + "'", e);
						JOptionPane.showMessageDialog(this, "Error",
								"Cannot load Loader-Plugin '" + pluginName + "'",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
		return fileMenu;
	}


	/**
	 * This method initializes import-menu with
	 * plugins.
	 *
	 * @return javax.swing.JMenu
	 */
	@SuppressWarnings("unchecked")
	protected JMenu getImportMenu() {
		if (importMenu == null) {
			importMenu = new JMenu();
			importMenu.setText("Import");
			importMenu.setMnemonic('i');
			//importMenu.setEnabled(false);// first we need to load a file

			PluginManager manager = getPluginManager();
			// if we are configured for the plugin-api
			if (manager != null) {
				ExtensionPoint toolExtPoint = manager.getRegistry().getExtensionPoint(
						getPluginDescriptor().getId(), "Importer");
				for (Iterator<Extension> it = toolExtPoint.getConnectedExtensions().iterator(); it.hasNext(); ) {
					Extension ext = it.next();
					String pluginName = "unknown";

					try {
						pluginName = ext.getParameter("name").valueAsString();
						JMenuItem newMenuItem = new JMenuItem();
						newMenuItem.putClientProperty("extension", ext);
						Parameter descrParam = ext.getParameter("description");
						Parameter iconParam = ext.getParameter("icon");
						URL iconUrl = null;
						if (iconParam != null) {
							try {
								iconUrl = getPluginManager().getPluginClassLoader(
										ext.getDeclaringPluginDescriptor()).getResource(iconParam.valueAsString());
								if (iconUrl != null) {
									newMenuItem.setIcon(new ImageIcon(iconUrl));
								}
							} catch (Exception e) {
								LOGGER.error("cannot load icon for Importer-Plugin '" + pluginName + "'", e);
							}
						}
						newMenuItem.setText(pluginName);
						if (descrParam != null) {
							newMenuItem.setToolTipText(descrParam.valueAsString());
						}
						newMenuItem.addActionListener(new ImportPluginMenuAction(this, ext, pluginName));
						importMenu.add(newMenuItem);
					} catch (Exception e) {
						LOGGER.error("cannot load Importer-Plugin '" + pluginName + "'", e);
						JOptionPane.showMessageDialog(this, "Error",
								"Cannot load Importer-Plugin '" + pluginName + "'",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
		return importMenu;
	}

	/**
	 * This method initializes import-menu
	 * with plugins.
	 *
	 * @return javax.swing.JMenu
	 */
	@SuppressWarnings("unchecked")
	protected JMenu getToolMenu() {
		if (toolMenu == null) {
			toolMenu = new JMenu();
			toolMenu.setText("Tools");
			toolMenu.setMnemonic('t');
			//importMenu.setEnabled(false);// first we need to load a file

			PluginManager manager = getPluginManager();
			// if we are configured for the plugin-api
			if (manager != null) {
				ExtensionPoint toolExtPoint = manager.getRegistry().getExtensionPoint(
						getPluginDescriptor().getId(), "Tool");
				for (Iterator<Extension> it = toolExtPoint.getConnectedExtensions().iterator(); it.hasNext(); ) {
					Extension ext = it.next();
					String pluginName = "unknown";

					try {
						pluginName = ext.getParameter("name").valueAsString();
						JMenuItem newMenuItem = new JMenuItem();
						newMenuItem.putClientProperty("extension", ext);
						Parameter descrParam = ext.getParameter("description");
						Parameter iconParam = ext.getParameter("icon");
						URL iconUrl = null;
						if (iconParam != null) {
							try {
								iconUrl = getPluginManager().getPluginClassLoader(
										ext.getDeclaringPluginDescriptor()).getResource(iconParam.valueAsString());
								if (iconUrl != null) {
									newMenuItem.setIcon(new ImageIcon(iconUrl));
								}
							} catch (Exception e) {
								LOGGER.error("cannot load icon for Tool-Plugin '" + pluginName + "'", e);
							}
						}
						newMenuItem.setText(pluginName);
						if (descrParam != null) {
							newMenuItem.setToolTipText(descrParam.valueAsString());
						}
						newMenuItem.addActionListener(new ToolPluginMenuAction(this, ext, pluginName));
						toolMenu.add(newMenuItem);
					} catch (Exception e) {
						LOGGER.error("cannot load Tool-Plugin '" + pluginName + "'", e);
						JOptionPane.showMessageDialog(this, "Error",
								"Cannot load Tool-Plugin '" + pluginName + "'",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
		return toolMenu;
	}

	/**
	 * This method initializes fileSaveMenuItem.
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getFileSaveMenuItem() {
		if (fileSaveMenuItem == null) {
			fileSaveMenuItem = new JMenuItem();
			fileSaveMenuItem.setText("Save...");
			fileSaveMenuItem.setMnemonic('s');
			fileSaveMenuItem.addActionListener(
					new java.awt.event.ActionListener() {
						public void actionPerformed(
								final java.awt.event.ActionEvent e) {
							saveFile();
						}
					});
		}
		return fileSaveMenuItem;
	}

	/**
	 * This method initializes fileSaveAsMenuItem.
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getFileSaveAsMenuItem() {
		if (fileSaveAsMenuItem == null) {
			fileSaveAsMenuItem = new JMenuItem();
			fileSaveAsMenuItem.setText("Save as...");
			fileSaveAsMenuItem.setMnemonic('a');
			fileSaveAsMenuItem.addActionListener(
					new java.awt.event.ActionListener() {
						public void actionPerformed(
								final java.awt.event.ActionEvent e) {
							saveFileAs();
						}
					});
		}
		return fileSaveAsMenuItem;
	}

	/**
	 * Show the file->save as... -dialog.
	 */
	private void saveFileAs() {
		int state = getJFileChooser().showSaveDialog(this);
		if (state == JFileChooser.APPROVE_OPTION) {
			File f = getJFileChooser().getSelectedFile();
			try {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

				getWritableModel().writeFile(f);
				saveFile();
				setTitle(f.getName());
			} catch (FileNotFoundException e) {
				LOGGER.error("cannot save file '" + f.getAbsolutePath() + "' (file not found)", e);
				JOptionPane.showMessageDialog(this, "Error",
						"cannot save file '" + f.getAbsolutePath() + "' (file not found)",
						JOptionPane.ERROR_MESSAGE);
			} catch (IOException e) {
				LOGGER.error("cannot save file '" + f.getAbsolutePath() + "' (io-problem)", e);
				JOptionPane.showMessageDialog(this, "Error",
						"cannot save file '" + f.getAbsolutePath() + "' (io-problem)",
						JOptionPane.ERROR_MESSAGE);
			} catch (JAXBException e) {
				LOGGER.error("cannot save file '" + f.getAbsolutePath() + "' (gnucash-format-problem)", e);
				JOptionPane.showMessageDialog(this, "Error",
						"cannot save file '" + f.getAbsolutePath() + "' (gnucash-format-problem)",
						JOptionPane.ERROR_MESSAGE);
			} finally {
				setCursor(Cursor.getDefaultCursor());
			}
		}
	}

	/**
	 * @param x the exception to show
	 */
	protected final void showErrorMessagePopup(final Exception x) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		x.printStackTrace(pw);
		pw.close();
		String file = "unknown";
		try {
			getJFileChooser().getSelectedFile().getAbsolutePath();
		} catch (Exception e) {
			e.printStackTrace();
		}
		JOptionPane.showMessageDialog(this, sw.toString(),
				"ERROR importing '" + file + "'",
				JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws JAXBException
	 */
	private void saveFile() {
		try {
			File oldfile = new File(getWritableModel().getFile().getAbsolutePath());
			oldfile.renameTo(new File(oldfile.getName() + (new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss.SS").format(new Date())) + ".backup"));

			getWritableModel().writeFile(getWritableModel().getFile());
			hasChanged = false;
		} catch (FileNotFoundException e) {
			File f = getWritableModel().getFile();
			if (f == null) {
				f = new File("unknown");
			}
			LOGGER.error("cannot save file '" + f.getAbsolutePath() + "' (file not found)", e);
			JOptionPane.showMessageDialog(this, "Error",
					"cannot save file '" + f.getAbsolutePath() + "' (file not found)",
					JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			File f = getWritableModel().getFile();
			if (f == null) {
				f = new File("unknown");
			}
			LOGGER.error("cannot save file '" + f.getAbsolutePath() + "' (io-problem)", e);
			JOptionPane.showMessageDialog(this, "Error",
					"cannot save file '" + f.getAbsolutePath() + "' (io-problem)",
					JOptionPane.ERROR_MESSAGE);
		} catch (JAXBException e) {
			File f = getWritableModel().getFile();
			if (f == null) {
				f = new File("unknown");
			}
			LOGGER.error("cannot save file '" + f.getAbsolutePath() + "' (gnucash-format-problem)", e);
			JOptionPane.showMessageDialog(this, "Error",
					"cannot save file '" + f.getAbsolutePath() + "' (gnucash-format-problem)",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doExit() {
		if (hasChanged) {
			int state = JOptionPane.showConfirmDialog(this,
					"File has been changed. Save before exit?");
			if (state == JOptionPane.YES_OPTION) {
				saveFile();
			} else if (state == JOptionPane.CANCEL_OPTION) {
				return;
			}
		}
		System.exit(0);
	}

	/**
	 *
	 * @return our model
	 */
	public GnucashWritableFile getWritableModel() {
		return model;
	}

	/**
	 * ${@inheritDoc}.
	 */
	@Override
	protected GnucashFile getModel() {
		return model;
	}

	/**
	 * ${@inheritDoc}.
	 */
	@Override
	public void setModel(final GnucashFile newModel) {
		if (!(newModel instanceof GnucashWritableFile)) {
			throw new IllegalArgumentException("given model is not writable!");
		}
		setWritableModel((GnucashWritableFile) newModel);
	}

	/**
	 *
	 * @param newModel our new model
	 */
	public void setWritableModel(final GnucashWritableFile newModel) {
		if (newModel == null) {
			throw new IllegalArgumentException(
					"null not allowed for field this.model");
		}

		boolean ok = false;
		GnucashWritableFile oldModel = model;
		try {
			model = newModel;
			super.setModel(newModel);
			ok = true;
			hasChanged = false;
			getFileSaveAsMenuItem().setEnabled(true);
			getFileSaveMenuItem().setEnabled(true);
			getImportMenu().setEnabled(true);
		} finally {
			if (!ok) {
				model = oldModel;
			}
		}
	}

	/**
	 * The actions we have on Splits.
	 */
	private Collection<TransactionSplitAction> myWritableSplitActions;

	/**
	 * The actions we have on accounts.
	 */
	private Collection<AccountAction> myAccountActions;

	/**
	 * @return the {@link AccountAction} we have
	 */
	@Override
	protected Collection<TransactionSplitAction> getSplitActions() {
		if (myWritableSplitActions == null) {
			myWritableSplitActions = new LinkedList<TransactionSplitAction>();
			myWritableSplitActions.add(new OpenAccountInNewTabWritable(getJTabbedPane()));
			myWritableSplitActions.add(new OpenAccountInNewWindowWritable());
		}
		String actionsCount = "no";
		if (myWritableSplitActions != null) {
			actionsCount = "" + myWritableSplitActions.size();
		}
		LOGGER.info("JGnucashEditor has " + actionsCount + " split-actions");
		return myWritableSplitActions;
	}


	/**
	 * @return the {@link AccountAction} we have including the ones offered by plugins.
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Collection<AccountAction> getAccountActions() {
		if (myAccountActions == null) {
			myAccountActions = super.getAccountActions();

			PluginManager manager = getPluginManager();
			// if we are configured for the plugin-api
			if (manager != null) {
				ExtensionPoint toolExtPoint = manager.getRegistry().getExtensionPoint(
						getPluginDescriptor().getId(), "AccountAction");
				for (Iterator<Extension> it = toolExtPoint.getConnectedExtensions().iterator(); it.hasNext(); ) {
					Extension ext = it.next();
					String pluginName = "unknown";

					try {
						getPluginManager().activatePlugin(ext.getDeclaringPluginDescriptor().getId());
						// Get plug-in class loader.
						ClassLoader classLoader = getPluginManager().getPluginClassLoader(
								ext.getDeclaringPluginDescriptor());
						// Load Tool class.
						Class toolCls = classLoader.loadClass(
								ext.getParameter("class").valueAsString());
						// Create Tool instance.
						Object o = toolCls.newInstance();
						if (!(o instanceof AccountAction)) {
							LOGGER.error("Plugin '" + pluginName + "' does not implement AccountAction-interface.");
							JOptionPane.showMessageDialog(this, "Error",
									"The AccountAction-Plugin '" + pluginName + "'"
											+ " does not implement AccountAction-interface.",
									JOptionPane.ERROR_MESSAGE);
						} else {
							myAccountActions.add((AccountAction) o);
						}
					} catch (Exception e) {
						LOGGER.error("cannot load TransactionMenuAction-Plugin '" + pluginName + "'", e);
						JOptionPane.showMessageDialog(this, "Error",
								"Cannot load AccountAction-Plugin '" + pluginName + "'",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}

		}
		return myAccountActions;
	}

}

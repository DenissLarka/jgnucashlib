/**
 * JGnucashViewer.java
 * Created on 15.05.2005
 * (c) 2005 by "Wolschon Softwaredesign und Beratung".
 * -----------------------------------------------------------
 * major Changes:
 * 15.05.2005 - initial version
 * 16.05.2005 - split into JGnucashViewer and JGnucash
 * ...
 */
package org.gnucash.viewer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.xml.bind.JAXBException;

import org.gnucash.viewer.actions.AccountAction;
import org.gnucash.viewer.actions.FileBugInBrowserAction;
import org.gnucash.viewer.actions.OpenAccountInNewTab;
import org.gnucash.viewer.actions.OpenAccountInNewWindow;
import org.gnucash.viewer.actions.TransactionSplitAction;
import org.gnucash.viewer.models.GnucashAccountsTreeModel;
import org.gnucash.viewer.panels.TaxReportPanel;
import org.gnucash.viewer.panels.TransactionsPanel;
import org.gnucash.xml.GnucashAccount;
import org.gnucash.xml.GnucashFile;
import org.gnucash.xml.impl.GnucashFileImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * created: 15.05.2005 <br/>
 * Simple Viewer for Gnucash-Files.
 *
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 */
@SuppressWarnings("serial")
public class JGnucashViewer extends JFrame {

	/**
	 * (c) 2010 by <a href="http://Wolschon.biz>Wolschon Softwaredesign und Beratung</a>.<br/>
	 * Project: jgnucashLib-GPL<br/>
	 * AccountActionWrapper<br/>
	 * created: 17.11.2010 <br/>
	 * <br/><br/>
	 * <b>Wrapper for an {@link AccountAction} that knows about {@link JGnucashViewer#getSelectedAccount()}.</b>
	 *
	 * @author <a href="mailto:Marcus@Wolschon.biz">marcus</a>
	 */
	private final class AccountActionWrapper implements Action {
		/**
		 * The {@link AccountAction} we are wrapping.
		 */
		private final AccountAction myAccountAction;

		/**
		 */
		private AccountActionWrapper(final AccountAction anAccountAction) {
			myAccountAction = anAccountAction;
		}

		@Override
		public void addPropertyChangeListener(final PropertyChangeListener aListener) {
			myAccountAction.addPropertyChangeListener(aListener);
		}

		@Override
		public Object getValue(final String aKey) {
			return myAccountAction.getValue(aKey);
		}

		@Override
		public boolean isEnabled() {
			try {
				myAccountAction.setAccount(getSelectedAccount());
				return myAccountAction.isEnabled();
			}
			catch (Exception e) {
				LOGGER.error("cannot query isEnabled for AccountAction", e);
				return false;
			}
		}

		@Override
		public void putValue(final String aKey, final Object aValue) {
			myAccountAction.putValue(aKey, aValue);
		}

		@Override
		public void removePropertyChangeListener(final PropertyChangeListener aListener) {
			myAccountAction.removePropertyChangeListener(aListener);
		}

		@Override
		public void setEnabled(final boolean aB) {
			myAccountAction.setEnabled(aB);
		}

		@Override
		public void actionPerformed(final ActionEvent aE) {
			try {
				myAccountAction.setAccount(getSelectedAccount());
				myAccountAction.actionPerformed(aE);
			}
			catch (Exception e) {
				LOGGER.error("cannot execute AccountAction", e);
			}
		}

		/**
		 * @return the accountAction we are wrapping.
		 */
		public AccountAction getAccountAction() {
			return myAccountAction;
		}
	}

	/**
	 * Our logger for debug- and error-output.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(JGnucashViewer.class);

	private GnucashFile myModel;

	private javax.swing.JPanel jContentPane = null;

	private javax.swing.JScrollPane treeScrollPane = null;

	private javax.swing.JFileChooser jFileChooser = null; //  @jve:visual-info  decl-index=0 visual-constraint="582,36"

	/**
	 * The currently selected account.
	 */
	private GnucashAccount selectedAccount = null;

	/**
	 * The title of the frame.
	 */
	private static final String TITLE = "JGnucash";

	/**
	 * The split-pane between account-tree and transactions-table.
	 */
	protected JSplitPane jSplitPane = null;

	/**
	 * The tree showing all accounts.
	 */
	private JTree accountsTree = null;
	/**
	 * The {@link JTabbedPane} containing {@link #transactionsPanel}
	 * and {@link #taxReportPanel}.
	 */
	private JTabbedPane myTabbedPane = null;
	private TransactionsPanel transactionsPanel = null;
	private TaxReportPanel taxReportPanel = null;
	private JMenuBar jJMenuBar = null;
	/**
	 * The File-Menu.
	 */
	private JMenu myFileMenu = null;

	/**
	 * File->Load.
	 */
	private JMenuItem myFileLoadMenuItem = null;
	/**
	 * File->Exit.
	 */
	private JMenuItem myFileExitMenuItem = null;

	/**
	 * Popup-menu on the account-tree.
	 */
	private JPopupMenu myAccountTreePopupMenu;

	/**
	 * The actions we have on accounts.
	 */
	private Collection<AccountAction> myAccountActions;

	/**
	 * The actions we have on Splits.
	 */
	private Collection<TransactionSplitAction> mySplitActions;

	/**
	 * @param args empty or contains a gnucash-file-name as a first param.
	 */
	public static void main(final String[] args) {
		JGnucashViewer ste = new JGnucashViewer();
		installNimbusLaF();
		ste.initializeGUI();
		ste.setVisible(true);
		if (args.length > 0) {
			ste.loadFile(new File(args[0]));
		}
		ste.getJSplitPane().setDividerLocation(0.3);
	}

	protected static void installNimbusLaF() {
		try {
			UIManager.setLookAndFeel(new NimbusLookAndFeel());
		}
		catch (UnsupportedLookAndFeelException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	/**
	 * This method initializes
	 * the GnucashViewer.
	 */
	public JGnucashViewer() {
		super();
	}

	/**
	 * This method initializes jSplitPane.
	 *
	 * @return javax.swing.JSplitPane
	 */
	protected JSplitPane getJSplitPane() {
		if (jSplitPane == null) {
			jSplitPane = new JSplitPane();
			jSplitPane.setLeftComponent(getTreeScrollPane());
			jSplitPane.setRightComponent(getJTabbedPane());
		}
		return jSplitPane;
	}

	/**
	 * This method initializes accountsTree.
	 *
	 * @return javax.swing.JTree
	 */
	protected JTree getAccountsTree() {
		if (accountsTree == null) {
			accountsTree = new JTree();

			if (getModel() == null) {
				accountsTree.setModel(new DefaultTreeModel(null));
			} else {
				accountsTree.setModel(new GnucashAccountsTreeModel(getModel()));
			}
			accountsTree.addMouseListener(new MouseAdapter() {

				/** show popup if mouseReleased is a popupTrigger on this platform.
				 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
				 */
				@Override
				public void mouseReleased(final MouseEvent aE) {
					if (aE.isPopupTrigger()) {
						getAccountTreePopupMenu().show((JComponent) aE.getSource(), aE.getX(), aE.getY());
					}
				}

				/** show popup if mousePressed is a popupTrigger on this platform.
				 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
				 */
				@Override
				public void mousePressed(final MouseEvent aE) {
					if (aE.isPopupTrigger()) {
						getAccountTreePopupMenu().show((JComponent) aE.getSource(), aE.getX(), aE.getY());
					}
				}
			});

			accountsTree.addTreeSelectionListener(new TreeSelectionListener() {
				public void valueChanged(final TreeSelectionEvent e) {

					TreePath path = e.getPath();
					if (path == null) {
						setSelectedAccount(null);
					} else {
						GnucashAccountsTreeModel.GnucashAccountTreeEntry entry
								= (GnucashAccountsTreeModel.GnucashAccountTreeEntry)
								path.getLastPathComponent();
						setSelectedAccount(entry.getAccount());
					}

				}
			});

		}
		return accountsTree;
	}

	/**
	 * This method initializes transactionsPanel.
	 *
	 * @return javax.swing.JTable
	 */
	protected JTabbedPane getJTabbedPane() {
		if (myTabbedPane == null) {
			myTabbedPane = new JTabbedPane();
			myTabbedPane.addTab("transactions", getTransactionsPanel());
			TaxReportPanel taxReportPanel2 = getTaxReportPanel();
			if (taxReportPanel2 != null) {
				myTabbedPane.addTab("taxes", taxReportPanel2);
			}
		}
		return myTabbedPane;
	}

	/**
	 * This method initializes transactionsPanel.
	 *
	 * @return javax.swing.JTable
	 */
	protected TransactionsPanel getTransactionsPanel() {
		if (transactionsPanel == null) {
			transactionsPanel = new TransactionsPanel();
			transactionsPanel.setSplitActions(getSplitActions());
		}
		return transactionsPanel;
	}

	/**
	 * This method initializes transactionsPanel.
	 *
	 * @return javax.swing.JTable
	 */
	protected TaxReportPanel getTaxReportPanel() {
		if (taxReportPanel == null) {
			try {
				taxReportPanel = new TaxReportPanel(getModel());
			}
			catch (Exception e) {
				LOGGER.info("The tax-report panel is probably not configured. THIS IS OKAY.", e);
			}
		}
		return taxReportPanel;
	}

	/**
	 * The currently selected account.
	 *
	 * @return the selectedAccount
	 */
	public GnucashAccount getSelectedAccount() {
		return selectedAccount;
	}

	/**
	 * The currently selected account.
	 *
	 * @param aSelectedAccount the selectedAccount to set (may be null)
	 */
	public void setSelectedAccount(final GnucashAccount aSelectedAccount) {
		selectedAccount = aSelectedAccount;

		getTransactionsPanel().setAccount(selectedAccount);
		if (selectedAccount != null) {
			LOGGER.debug("accoun " + selectedAccount.getId()
					+ " = " + selectedAccount.getQualifiedName()
					+ " selected");
		}
	}

	/**
	 * This method initializes jJMenuBar.
	 *
	 * @return javax.swing.JMenuBar
	 */
	@Override
	public JMenuBar getJMenuBar() {
		if (jJMenuBar == null) {
			jJMenuBar = new JMenuBar();
			jJMenuBar.add(getFileMenu());
		}
		return jJMenuBar;
	}

	/**
	 * This method initializes FileMenu.
	 *
	 * @return javax.swing.JMenu
	 */
	protected JMenu getFileMenu() {
		if (myFileMenu == null) {
			myFileMenu = new JMenu();
			myFileMenu.setText("File");
			myFileMenu.setMnemonic('f');
			myFileMenu.add(getFileLoadMenuItem());
			myFileMenu.add(new JSeparator());
			myFileMenu.add(getFileExitMenuItem());
		}
		return myFileMenu;
	}


	/**
	 * This method initializes FileLoadMenuItem.
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getFileLoadMenuItem() {
		if (myFileLoadMenuItem == null) {
			myFileLoadMenuItem = new JMenuItem();
			myFileLoadMenuItem.setText("Open...");
			myFileLoadMenuItem.setMnemonic('a');
			myFileLoadMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					JGnucashViewer.this.loadFile();
				}
			});
		}
		return myFileLoadMenuItem;
	}

	/**
	 * This method initializes fileExitMenuItem.
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getFileExitMenuItem() {
		if (myFileExitMenuItem == null) {
			myFileExitMenuItem = new JMenuItem();
			myFileExitMenuItem.setText("Exit...");
			myFileExitMenuItem.setMnemonic('x');
			myFileExitMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					doExit();
				}
			});
		}
		return myFileExitMenuItem;
	}


	/**
	 * This method initializes jContentPane.
	 *
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJContentPane() {
		if (jContentPane == null) {
			final int border = 5;
			jContentPane = new javax.swing.JPanel();
			jContentPane.setLayout(new java.awt.BorderLayout());
			jContentPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(
					border, border, border, border));
			jContentPane.add(getJSplitPane(), java.awt.BorderLayout.CENTER);
		}
		return jContentPane;
	}

	/**
	 * This method initializes this gui.
	 */
	protected void initializeGUI() {
		final int defaultWidth = 750;
		final int defaultHeight = 600;

		this.setJMenuBar(getJMenuBar());
		this.setContentPane(getJContentPane());
		this.setSize(defaultWidth, defaultHeight);
		this.setTitle(TITLE);
		this.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

		this.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(final java.awt.event.WindowEvent e) {
				doExit();
			}
		});

	}

	/**
	 * This method initializes treeScrollPane.
	 *
	 * @return javax.swing.JScrollPane
	 */
	private javax.swing.JScrollPane getTreeScrollPane() {
		if (treeScrollPane == null) {
			final int defaultWidth = 400;
			treeScrollPane = new JScrollPane();
			treeScrollPane.setViewportView(getAccountsTree());
			treeScrollPane.setPreferredSize(new Dimension(defaultWidth, Integer.MAX_VALUE));
		}
		return treeScrollPane;
	}

	/**
	 * This method initializes jFileChooser.
	 * If is used for the open-dialog.
	 * In JGnuCash it is also used for the save,
	 * save as and import -dialog.
	 *
	 * @return javax.swing.JFileChooser
	 */
	protected javax.swing.JFileChooser getJFileChooser() {
		if (jFileChooser == null) {
			jFileChooser = new javax.swing.JFileChooser();
		}
		jFileChooser.setMultiSelectionEnabled(false);
		jFileChooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(final File f) {
				return true; // accept all files
			}

			@Override
			public String getDescription() {
				return "gnucash files";
			}
		});
		return jFileChooser;
	}

	/**
	 * Given a file, create a GnucashFile for it.
	 *
	 * @param f the file
	 * @return the GnucashFile
	 * @throws IOException   if the file cannot be loaded from disk
	 */
	protected GnucashFile createModelFromFile(final File f) throws IOException, JAXBException {
		return new GnucashFileImpl(f);
	}

	/**
	 * @return true if the file was loaded successfully
	 */
	protected boolean loadFile() {
		int state = getJFileChooser().showOpenDialog(this);
		if (state == JFileChooser.APPROVE_OPTION) {
			File f = getJFileChooser().getSelectedFile();
			if (f == null) {
				return false;
			}
			if (!f.exists()) {
				JOptionPane.showMessageDialog(JGnucashViewer.this, "File does not exist", "missing file", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			return loadFile(f);
		}
		return false;
	}

	/**
	 * @param f the file to load.
	 * @return true if the file was loaded successfully
	 */
	public boolean loadFile(final File f) {
		try {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

			setModel(createModelFromFile(f));
			return true;
		}
		catch (Exception e1) {
			LOGGER.error("cannot load file '" + f.getAbsoluteFile() + "'", e1);
			e1.printStackTrace();
		}
		finally {
			setCursor(Cursor.getDefaultCursor());
		}
		return false;
	}

	/**
	 * Exit the JVM.
	 */
	protected void doExit() {
		System.exit(0);
	}

	/**
	 * @return the file we operate on.
	 */
	protected GnucashFile getModel() {
		return myModel;
	}

	/**
	 * @param model the file we operate on.
	 */
	public void setModel(final GnucashFile model) {
		if (model == null) {
			throw new IllegalArgumentException(
					"null not allowed for field this.model");
		}
		myModel = model;
		getAccountsTree().setModel(
				new GnucashAccountsTreeModel(myModel));
		try {
			getTaxReportPanel().setBooks(myModel);
		}
		catch (Exception e) {
			LOGGER.warn("cannot initialize (optional) TaxReportPanel", e);
			getTaxReportPanel().setVisible(false);
			getJTabbedPane().remove(getTaxReportPanel());
		}
		setSelectedAccount(null);
		setTitle(TITLE);

	}

	/**
	 * @return the accountTreePopupMenu
	 */
	protected JPopupMenu getAccountTreePopupMenu() {
		if (myAccountTreePopupMenu == null) {
			myAccountTreePopupMenu = new JPopupMenu();
			Collection<AccountAction> accountActions = getAccountActions();
			for (AccountAction accountAction2 : accountActions) {
				final AccountAction accountAction = accountAction2;
				JMenuItem newMenuItem = new JMenuItem(new AccountActionWrapper(accountAction));
				myAccountTreePopupMenu.add(newMenuItem);
			}
			LOGGER.debug("getAccountTreePopupMenu() created menu with " + myAccountTreePopupMenu.getComponentCount() + " entries");

		}
		int count = myAccountTreePopupMenu.getComponentCount();
		for (int i = 0; i < count; i++) {
			Component component = myAccountTreePopupMenu.getComponent(i);
			if (component instanceof JMenuItem) {
				JMenuItem item = (JMenuItem) component;
				Action action = item.getAction();
				if (action instanceof AccountActionWrapper) {
					AccountActionWrapper wrapper = (AccountActionWrapper) action;
					wrapper.getAccountAction().setAccount(getSelectedAccount());
					wrapper.setEnabled(wrapper.isEnabled());
				}
			}
		}
		return myAccountTreePopupMenu;
	}

	/**
	 * @return the {@link AccountAction} we have
	 */
	protected Collection<AccountAction> getAccountActions() {
		if (myAccountActions == null) {
			myAccountActions = new LinkedList<AccountAction>();
			myAccountActions.add(new OpenAccountInNewTab(getJTabbedPane()));
			myAccountActions.add(new OpenAccountInNewWindow());
		}
		return myAccountActions;
	}

	/**
	 * @return the {@link AccountAction} we have
	 */
	protected Collection<TransactionSplitAction> getSplitActions() {
		if (mySplitActions == null) {
			mySplitActions = new LinkedList<TransactionSplitAction>();
			mySplitActions.add(new OpenAccountInNewTab(getJTabbedPane()));
			mySplitActions.add(new OpenAccountInNewWindow());
		}
		LOGGER.info("JGnucashViewer has " + (mySplitActions == null ? "no" : mySplitActions.size()) + " split-actions");
		return mySplitActions;
	}

	/**
	 * @param account the account to show
	 */
	public void openAccountInTab(final GnucashAccount account) {
		final TransactionsPanel newTransactionsPanel = new TransactionsPanel();
		newTransactionsPanel.setAccount(account);
		String tabName = account.getName();
		addTab(tabName, newTransactionsPanel);
	}

	/**
	 * @param tabName    the label of the tab
	 * @param tabContent the content
	 */
	private void addTab(final String tabName, final JComponent tabContent) {

		final JTabbedPane tabbedPane = getJTabbedPane();
		tabbedPane.addTab(null, tabContent);
		JPanel tab = new JPanel(new BorderLayout(2, 0));
		tab.setOpaque(false);
		tab.add(new JLabel(tabName), BorderLayout.CENTER);
		JButton closeButton = new JButton("X");
		closeButton.setBorder(BorderFactory.createEmptyBorder());
		final int size = 10;
		closeButton.setPreferredSize(new Dimension(size, size));
		closeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent aE) {
				tabbedPane.remove(tabContent);
			}

		});
		tab.add(closeButton, BorderLayout.EAST);
		tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, tab);
	}

} //  @jve:visual-info  decl-index=0 visual-constraint="20,27"

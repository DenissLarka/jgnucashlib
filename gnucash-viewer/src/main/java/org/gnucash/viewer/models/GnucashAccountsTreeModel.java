/**
 * GnucashAccountsTreeModel.java
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.gnucash.xml.GnucashAccount;
import org.gnucash.xml.GnucashFile;

/**
 * created: 15.05.2005 <br/>
 *
 * A TreeModel representing the accounts in a Gnucash-File.
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 *
 */
public class GnucashAccountsTreeModel implements TreeModel {
    /**
     * Our logger for debug- and error-ourput.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GnucashAccountsTreeModel.class);

    /**
     * @param file where we get our data from
     */
    public GnucashAccountsTreeModel(final GnucashFile file) {
        super();
        setFile(file);
    }

    /**
     * The tree-root.
     */
    private GnucashAccountTreeRootEntry rootEntry;

    /**
     * (c) 2009 by <a href="http://Wolschon.biz>Wolschon Softwaredesign und Beratung</a>.<br/>
     * Project: jgnucashLib-GPL<br/>
     * GnucashAccountTreeRootEntry<br/>
     * <br/><br/>
     * <b>Helper-class representing a tree-entry.</b>
     * @author  <a href="mailto:Marcus@Wolschon.biz">fox</a>
     */
    public static class GnucashAccountTreeRootEntry extends GnucashAccountTreeEntry {

        /**
         * where we get our data from.
         */
        private final GnucashFile file;

        /**
         * @param aFile where we get our data from
         */
        public GnucashAccountTreeRootEntry(final GnucashFile aFile) {
            super(getRootAccount(aFile));
            file = aFile;
        }
        /**
         * @param aFile where we get our data from
         * @return the root-account checked for null
         */
        private static GnucashAccount getRootAccount(final GnucashFile aFile) {
            if (aFile == null) {
                throw new IllegalArgumentException("null file given");
            }
            Collection<? extends GnucashAccount> rootAccounts = aFile.getRootAccounts();
            if (rootAccounts.size() == 0) {
                throw new IllegalArgumentException("file nas no root-account");
            }
            if (rootAccounts.size() > 1) {
                StringBuilder roots = new StringBuilder();
                for (GnucashAccount gnucashAccount : rootAccounts) {
                    roots.append(gnucashAccount.getId()).append("=\"").append(gnucashAccount.getName()).append("\" ");
                }
                LOGGER.warn("file has more then one root-account! Attaching excess accounts to root-account: "
                         + roots.toString());

            }
            GnucashAccount root = rootAccounts.iterator().next();
            if (root == null) {
                throw new IllegalArgumentException("root-account is null");
            }
            return root;

        }
        /**
         * @return where we get our data from
         */
        public GnucashFile getFile() {
            return file;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "";
        }

        /**
         * @return the accounts below us
         */
        @Override
        public Collection<? extends GnucashAccount> getChildAccounts() {
            return file.getRootAccounts();
        }
    }

    /**
     * (c) 2009 by <a href="http://Wolschon.biz>Wolschon Softwaredesign und Beratung</a>.<br/>
     * Project: jgnucashLib-GPL<br/>
     * GnucashAccountTreeEntry<br/>
     * <br/>
     * <b>Helper-class representing a tree-entry.</b>
     * @author  <a href="mailto:Marcus@Wolschon.biz">fox</a>
     */
    public static class GnucashAccountTreeEntry {

        /**
         * The account we represent.
         */
        private final GnucashAccount myAccount;

        /**
         * @param anAccount The account we represent.
         */
        public GnucashAccountTreeEntry(final GnucashAccount anAccount) {
            super();
            if (anAccount == null) {
                throw new IllegalArgumentException("null account given");
            }
            myAccount = anAccount;
        }
        /**
         * @return The account we represent.
         */
        public GnucashAccount getAccount() {
            return myAccount;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            String hidden = getAccount().getUserDefinedAttribute("hidden");
            if (hidden != null && hidden.equalsIgnoreCase("true")) {
                return "[hidden]" + getAccount().getName();
            }
            return getAccount().getName();
        }

        /**
         * The tree-nodes below us.
         */
        private volatile List<GnucashAccountTreeEntry> childTreeNodes = null;

        /**
         * {@inheritDoc}
         */
        public List<GnucashAccountTreeEntry> getChildTreeNodes() {

            if (childTreeNodes == null) {
                Collection<? extends GnucashAccount> c = getChildAccounts();
                childTreeNodes = new ArrayList<GnucashAccountTreeEntry>(c.size());
                for (GnucashAccount gnucashAccount : c) {
                    GnucashAccount subaccount = gnucashAccount;
                    childTreeNodes.add(new GnucashAccountTreeEntry(subaccount));
                }
            }

            return childTreeNodes;
        }

        /**
         * @return See {@link GnucashAccount#getChildren()}
         */
        public Collection<? extends GnucashAccount> getChildAccounts() {
            return myAccount.getChildren();
        }
    }

    /**
     * {@inheritDoc}
     * @see javax.swing.tree.TreeModel#getRoot()
     */
    public Object getRoot() {
        return rootEntry;
    }

    /**
     * {@inheritDoc}
     * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
     */
    public int getChildCount(final Object parent) {
        return ((GnucashAccountTreeEntry) parent).getChildTreeNodes().size();
    }

    /**
     * {@inheritDoc}
     * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
     */
    public boolean isLeaf(final Object node) {
        return getChildCount(node) == 0;
    }


    /**
     * Our {@link TreeModelListener}s.
     */
    private final Set<TreeModelListener> listeners = new HashSet<TreeModelListener>();

    /**
     * {@inheritDoc}
     * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.TreeModelListener)
     */
    public void addTreeModelListener(final TreeModelListener l) {
        listeners.add(l);

    }

    /**
     * {@inheritDoc}
     * @see javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.TreeModelListener)
     */
    public void removeTreeModelListener(final TreeModelListener l) {
        listeners.remove(l);

    }

    /**
     * {@inheritDoc}
     * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
     */
    public Object getChild(final Object parent, final int index) {
        return ((GnucashAccountTreeEntry) parent).getChildTreeNodes().get(index);
    }

    /**
     * {@inheritDoc}
     * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
     */
    public int getIndexOfChild(final Object parent, final Object child) {
        return ((GnucashAccountTreeEntry) parent).getChildTreeNodes().indexOf(child);
    }

    /**
     * {@inheritDoc}
     * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
     */
    public void valueForPathChanged(final TreePath path, final Object newValue) {
        // TODO unsupported

    }

    /**
     * @return The gnucash-file we work on.
     */
    public GnucashFile getFile() {
        return rootEntry.getFile();
    }
    /**
     * @param file The gnucash-file we work on.
     */
    public void setFile(final GnucashFile file) {
        if (file == null) {
            throw new IllegalArgumentException(
                    "null not allowed for field this.file");
        }
       rootEntry = new GnucashAccountTreeRootEntry(file);

        fireTreeStructureChanged(getPathToRoot());
    }

    /**
     * {@inheritDoc}
     */
    protected TreePath getPathToRoot() {
     return new TreePath(getRoot());
    }

    /**
     * @param path the path to inform our {@link TreeModelListener}s about.
     */
    protected void fireTreeStructureChanged(final TreePath path) {
     TreeModelEvent evt = new TreeModelEvent(this, path);

     for (TreeModelListener listener : listeners) {
        listener.treeStructureChanged(evt);

    }
    }

}

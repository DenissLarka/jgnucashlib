package org.gnucash.read.impl.aux;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.gnucash.generated.GncV2;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.aux.GCshTaxTable;
import org.gnucash.read.aux.GCshTaxTableEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of GnucashTaxTable that uses JWSDP.
 * 
 * @see GCshTaxTable
 */
public class GCshTaxTableImpl implements GCshTaxTable {

  private static final Logger LOGGER = LoggerFactory.getLogger(GCshTaxTableImpl.class);

	/**
	 * the JWSDP-object we are facading.
	 */
	private final GncV2.GncBook.GncGncTaxTable jwsdpPeer;

	/**
	 * The file we belong to.
	 */
	private final GnucashFile file;

	/**
	 * @param peer the JWSDP-object we are facading.
	 * @see #jwsdpPeer
	 * @param gncFile the file to register under
	 */
	public GCshTaxTableImpl(
			final GncV2.GncBook.GncGncTaxTable peer,
			final GnucashFile gncFile) {
		super();
		jwsdpPeer = peer;
		file = gncFile;
	}

	/**
	 * The gnucash-file is the top-level class to contain everything.
	 * @return the file we are associated with
	 */
	public GnucashFile getFile() {
		return file;
	}

	/**
	 *
	 * @return The JWSDP-Object we are wrapping.
	 */
	public GncV2.GncBook.GncGncTaxTable getJwsdpPeer() {
		return jwsdpPeer;
	}

	/**
	 * @return the unique-id to identify this object with across name- and hirarchy-changes
	 */
	public String getId() {
		assert jwsdpPeer.getTaxtableGuid().getType().equals("guid");

		String guid = jwsdpPeer.getTaxtableGuid().getValue();
		if (guid == null) {
			throw new IllegalStateException(
					"taxtable has a null guid-value! guid-type="
							+ jwsdpPeer.getTaxtableGuid().getType());
		}

		return guid;
	}

	/**
	 * @see GCshTaxTable#getName()
	 */
	public String getName() {
		return jwsdpPeer.getTaxtableName();
	}

	/**
	 * @see GCshTaxTable#isInvisible()
	 */
	public boolean isInvisible() {
		return jwsdpPeer.getTaxtableInvisible() != 0;
	}

	/**
	 * @see GCshTaxTable#getParentID()
	 */
	public String getParentID() {
		GncV2.GncBook.GncGncTaxTable.TaxtableParent parent = jwsdpPeer.getTaxtableParent();
		if (parent == null) {
			return null;
		}
		return parent.getValue();
	}

	/**
	 * @see GCshTaxTable#getParent()
	 * @return the parent tax-table or null
	 */
	public GCshTaxTable getParent() {
		return getFile().getTaxTableByID(getParentID());
	}

	/**
	 * @see #getEntries()
	 */
	private Collection<GCshTaxTableEntry> entries = null;

	/**
	 * @see GCshTaxTable#getEntries()
	 * @return all entries to this tax-table
	 */
	@SuppressWarnings("unchecked")
	public Collection<GCshTaxTableEntry> getEntries() {
		if (entries == null) {
			GncV2.GncBook.GncGncTaxTable.TaxtableEntries jwsdpEntries = getJwsdpPeer().getTaxtableEntries();
			entries = new ArrayList<>(jwsdpEntries.getGncGncTaxTableEntry().size());
			for (Iterator<GncV2.GncBook.GncGncTaxTable.TaxtableEntries.GncGncTaxTableEntry> iter =
				 jwsdpEntries.getGncGncTaxTableEntry().iterator(); iter.hasNext(); ) {
				GncV2.GncBook.GncGncTaxTable.TaxtableEntries.GncGncTaxTableEntry element = iter.next();

				entries.add(new GCshTaxTableEntryImpl(element, getFile()));
			}

		}

		return entries;

	}
	
	// -----------------------------------------------------------
	
	public String toString() {
	        StringBuffer buffer = new StringBuffer();
	        
	        buffer.append("GCshTaxTableImpl: [\n");

	        buffer.append("  id:        ");
	        buffer.append(getId() + "\n");
	        
	        buffer.append("  name:      '");
	        buffer.append(getName() + "'\n");
	        
	        buffer.append("  parent-id: ");
	        buffer.append(getParentID() + "\n");
	        
	        buffer.append("  Entries:\n");
	        for ( GCshTaxTableEntry entry : getEntries() ) {
	            buffer.append("  - " + entry + "\n");
	        }

	        buffer.append("]\n");
	        
	        return buffer.toString();
	}
}

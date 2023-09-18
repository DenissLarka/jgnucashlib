package org.gnucash.read.impl.aux;

import org.gnucash.generated.GncV2;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.aux.GCshTaxTableEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GCshTaxTableEntryImpl implements GCshTaxTableEntry {

    private static final Logger LOGGER = LoggerFactory.getLogger(GCshTaxTableEntryImpl.class);

		/**
		 * the jwsdp-object we are wrapping.
		 */
		private GncV2.GncBook.GncGncTaxTable.TaxtableEntries.GncGncTaxTableEntry jwsdpPeer;

		/**
		 * the file we belong to.
		 */
		private final GnucashFile myFile;

		/**
		 * @param element the jwsdp-object we are wrapping
		 * @param file the file we belong to
		 */
		public GCshTaxTableEntryImpl(final GncV2.GncBook.GncGncTaxTable.TaxtableEntries.GncGncTaxTableEntry element, final GnucashFile
				file) {
			super();
			setJwsdpPeer(element);
			myFile = file;
		}

		/**
		 * @return the jwsdp-object we are wrapping
		 */
		protected GncV2.GncBook.GncGncTaxTable.TaxtableEntries.GncGncTaxTableEntry getJwsdpPeer() {
			return jwsdpPeer;
		}

		private void setJwsdpPeer(GncV2.GncBook.GncGncTaxTable.TaxtableEntries.GncGncTaxTableEntry jwsdpPeer) {
			if (jwsdpPeer == null) {
				throw new IllegalArgumentException("null 'jwsdpPeer' given!");
			}

			this.jwsdpPeer = jwsdpPeer;
		}

		/**
		 * @return the amount the tax is
		 * @link #getType()
		 */
		public FixedPointNumber getAmount() {
			return new FixedPointNumber(getJwsdpPeer().getTteAmount());
		}

/*
        public void setAmount(final FixedPointNumber amount) {
            if (amount == null) {
                throw new IllegalArgumentException("null 'amount' given!");
            }
            getJwsdpPeer().setTteAmount(amount.toGnucashString());
        }
*/

		/**
		 * usually ${@link GCshTaxTableEntry#TYPE_PERCENT}.
		 * @link #getAmount()
		 */
		public String getType() {
			return getJwsdpPeer().getTteType();
		}

/*
        public void setType(final String type) {
            if (type == null) {
                throw new IllegalArgumentException("null 'type' given!");
            }
            getJwsdpPeer().setTteType(type);
        }*/

		/**
		 * initialised lazy.
		 */
		private String myAccountID;
		/**
		 * initialised lazy.
		 */
		private GnucashAccount myAccount;

		/**
		 * @return Returns the account.
		 * @link #myAccount
		 */
		public GnucashAccount getAccount() {
			if (myAccount == null) {
				myAccount = myFile.getAccountByID(this.getAccountID());
			}

			return myAccount;
		}

		/**
		 * @param account The account to set.
		 * @link #myAccount
		 */
		public void setAccount(final GnucashAccount account) {
			if (account == null) {
				throw new IllegalArgumentException("null 'account' given!");
			}

			myAccount = account;
			myAccountID = account.getId();
			getJwsdpPeer().getTteAcct().setType("guid");
			getJwsdpPeer().getTteAcct().setValue(account.getId());
		}

		/**
		 * @return Returns the accountID.
		 * @link #myAccountID
		 */
		public String getAccountID() {
			if (myAccountID == null) {
				myAccountID = getJwsdpPeer().getTteAcct().getValue();

			}

			return myAccountID;
		}

		/**
		 * @param accountID The accountID to set.
		 * @see ${@link #myAccountID}
		 */
  /*      public void setAccountID(final String accountID) {
            if (accountID == null) {
                throw new IllegalArgumentException("null 'accountID' given!");
            }
            myAccount = null;
            myAccountID = accountID;
            getJwsdpPeer().getTteAcct().setType("guid");
            getJwsdpPeer().getTteAcct().setValue(accountID);
        }

*/
		// ---------------------------------------------------
		
		@Override
		public String toString() {
		    return "GCshTaxTableEntryImpl [myAccountID=" + myAccountID + ", myAccount=" + myAccount
			    + ", amount=" + getAmount() + ", type=" + getType() + "]";
		}

}

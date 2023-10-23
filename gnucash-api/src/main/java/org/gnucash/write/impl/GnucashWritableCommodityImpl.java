package org.gnucash.write.impl;

import org.gnucash.Const;
import org.gnucash.currency.CmdtyCurrID;
import org.gnucash.currency.CmdtyCurrNameSpace;
import org.gnucash.currency.CommodityID_Exchange;
import org.gnucash.currency.CurrencyID;
import org.gnucash.currency.InvalidCmdtyCurrIDException;
import org.gnucash.currency.InvalidCmdtyCurrTypeException;
import org.gnucash.generated.GncV2;
import org.gnucash.read.impl.GnucashCommodityImpl;
import org.gnucash.write.GnucashWritableCommodity;
import org.gnucash.write.GnucashWritableFile;
import org.gnucash.write.GnucashWritableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GnucashWritableCommodityImpl extends GnucashCommodityImpl 
                                          implements GnucashWritableCommodity,
                                                     GnucashWritableObject
{
    /**
     * Automatically created logger for debug and error-output.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GnucashWritableCommodityImpl.class);
    
    // ---------------------------------------------------------------

    /**
     * Please use ${@link GnucashWritableFile#createWritableCommodity()}.
     *
     * @param file      the file we belong to
     * @param jwsdpPeer the JWSDP-object we are facading.
     */
    protected GnucashWritableCommodityImpl(final GncV2.GncBook.GncCommodity jwsdpPeer,
	    final GnucashWritableFileImpl file) {
	super(jwsdpPeer, file);
    }

    /**
     * Please use ${@link GnucashWritableFile#createWritableCommodity()}.
     *
     * @param file the file we belong to
     * @param id   the ID we shall have
     */
    protected GnucashWritableCommodityImpl(final GnucashWritableFileImpl file) {
	super(createCommodity(file, file.createGUID()), file);
    }

    // ---------------------------------------------------------------

    /**
     * Delete this commodity and remove it from the file.
     * @throws InvalidCmdtyCurrIDException 
     * @throws ObjectCascadeException 
     * @throws InvalidCmdtyCurrTypeException 
     *
     * @see GnucashWritableCommodity#remove()
     */
    public void remove() throws InvalidCmdtyCurrTypeException, ObjectCascadeException, InvalidCmdtyCurrIDException {
	GncV2.GncBook.GncCommodity peer = getJwsdpPeer();
	(getGnucashFile()).getRootElement().getGncBook().getBookElements().remove(peer);
	(getGnucashFile()).removeCommodity(this);
    }

    // ---------------------------------------------------------------

    /**
     * Creates a new Transaction and add's it to the given gnucash-file Don't modify
     * the ID of the new transaction!
     *
     * @param file the file we will belong to
     * @param guid the ID we shall have
     * @return a new jwsdp-peer alredy entered into th jwsdp-peer of the file
     */
    protected static GncV2.GncBook.GncCommodity createCommodity(final GnucashWritableFileImpl file,
	    final String guid) {

	if (guid == null) {
	    throw new IllegalArgumentException("null guid given!");
	}

	GncV2.GncBook.GncCommodity cmdty = file.createGncGncCommodityType();

	cmdty.setCmdtyFraction(Const.CMDTY_FRACTION_DEFAULT);
	cmdty.setVersion(Const.XML_FORMAT_VERSION);
	cmdty.setCmdtyName("no name given");
	cmdty.setCmdtySpace(CmdtyCurrNameSpace.Exchange.EURONEXT.toString()); // ::TODO : soft
	cmdty.setCmdtyId("XYZ"); // ::TODO
	cmdty.setCmdtyXcode(Const.CMDTY_XCODE_DEFAULT);

	file.getRootElement().getGncBook().getBookElements().add(cmdty);
	file.setModified(true);
	
	return cmdty;
    }

    // ---------------------------------------------------------------

    @Override
    public void setQualifId(CmdtyCurrID qualifId) throws InvalidCmdtyCurrTypeException {
	getJwsdpPeer().setCmdtySpace(qualifId.getNameSpace());
	getJwsdpPeer().setCmdtyId(qualifId.getCode());

	getGnucashFile().setModified(true);
    }

    @Override
    public void setXCode(String xCode) {
	getJwsdpPeer().setCmdtyXcode(xCode);
	getGnucashFile().setModified(true);
    }

    @Override
    public void setName(String name) {
	getJwsdpPeer().setCmdtyName(name);
	getGnucashFile().setModified(true);
    }

    @Override
    public void setFraction(Integer fract) {
	if ( fract <= 0 )
	    throw new IllegalArgumentException("Fraction is <= 0");
	
	getJwsdpPeer().setCmdtyFraction(fract);
	getGnucashFile().setModified(true);
    }

    // ---------------------------------------------------------------

    /**
     * The gnucash-file is the top-level class to contain everything.
     *
     * @return the file we are associated with
     */
    public GnucashWritableFileImpl getWritableGnucashFile() {
	return (GnucashWritableFileImpl) super.getGnucashFile();
    }

    /**
     * The gnucash-file is the top-level class to contain everything.
     *
     * @return the file we are associated with
     */
    @Override
    public GnucashWritableFileImpl getGnucashFile() {
	return (GnucashWritableFileImpl) super.getGnucashFile();
    }

    // ---------------------------------------------------------------

    /**
     * @see GnucashWritableObject#setUserDefinedAttribute(java.lang.String,
     *      java.lang.String)
     */
    public void setUserDefinedAttribute(final String name, final String value) {
	// ::EMPTY
    }

}

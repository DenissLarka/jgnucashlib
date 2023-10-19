package org.gnucash.read.aux;

import java.time.LocalDate;

import org.gnucash.currency.InvalidCmdtyCurrIDException;
import org.gnucash.currency.InvalidCmdtyCurrTypeException;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashCommodity;

public interface GCshPrice {

    // Cf. https://github.com/Gnucash/gnucash/blob/stable/libgnucash/engine/gnc-pricedb.h
    public enum Source {
	EDIT_DLG,         // "user:price-editor"
	FQ,               // "Finance::Quote"
	USER_PRICE,       // "user:price"
	XFER_DLG_VAL,     // "user:xfer-dialog"
	SPLIT_REG,        // "user:split-register"
	SPLIT_IMPORT,     // "user:split-import"
	STOCK_SPLIT,      // "user:stock-split"
	STOCK_TRANSACTION,// "user:stock-transaction"
	INVOICE,          // "user:invoice-post"
	TEMP,             // "temporary"
	INVALID,          // "invalid"    
    }
	
    // ---------------------------------------------------------------
	
    String getId();

    String getCommodityQualifId();

    GnucashCommodity getCommodity() throws InvalidCmdtyCurrIDException, InvalidCmdtyCurrTypeException;

    String getCurrencyQualifId();

    String getCurrencyCode();

    GnucashCommodity getCurrency() throws InvalidCmdtyCurrIDException, InvalidCmdtyCurrTypeException;

    LocalDate getDate();

    String getSource();

    String getType();

    FixedPointNumber getValue();
    
}

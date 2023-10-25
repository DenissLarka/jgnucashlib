package org.example.gnucash.read;

import java.io.File;
import java.util.Collection;

import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashTransaction;
import org.gnucash.read.impl.GnucashFileImpl;

public class GetAcctInfo {
    // BEGIN Example data -- adapt to your needs
    private static String gcshFileName = "example_in.gnucash";
    private static String acctID       = "xyz";
    private static String acctName     = "abc";
    // END Example data

    // -----------------------------------------------------------------

    public static void main(String[] args) {
	try {
	    GetAcctInfo tool = new GetAcctInfo();
	    tool.kernel();
	} catch (Exception exc) {
	    System.err.println("Execution exception. Aborting.");
	    exc.printStackTrace();
	    System.exit(1);
	}
    }

    protected void kernel() throws Exception {
	GnucashFileImpl gcshFile = new GnucashFileImpl(new File(gcshFileName));

	// Choose one of the following variants:
	// Var. 1)
	GnucashAccount acct = gcshFile.getAccountByID(acctID);
	// Var. 2)
	// Collection<GnucashAccount> acctList = gcshFile.getAccountsByName(acctName);
	// GnucashAccount acct = acctList.iterator().next(); // first element

	printAcctInfo(acct, 0);
    }

    private void printAcctInfo(GnucashAccount acct, int depth) {
	System.out.println("Depth:           " + depth);

	try {
	    System.out.println("ID:              " + acct.getId());
	} catch (Exception exc) {
	    System.out.println("ID:              " + "ERROR");
	}

	try {
	    System.out.println("Type:            " + acct.getType());
	} catch (Exception exc) {
	    System.out.println("Type:            " + "ERROR");
	}

	try {
	    System.out.println("Name:            '" + acct.getName() + "'");
	} catch (Exception exc) {
	    System.out.println("Name:            " + "ERROR");
	}

	try {
	    System.out.println("Qualified name:  '" + acct.getQualifiedName() + "'");
	} catch (Exception exc) {
	    System.out.println("Qualified name:  " + "ERROR");
	}

	try {
	    System.out.println("Description:     '" + acct.getDescription() + "'");
	} catch (Exception exc) {
	    System.out.println("Description:     " + "ERROR");
	}

	try {
	    System.out.println("Cmdty/Curr:      '" + acct.getCmdtyCurrID() + "'");
	} catch (Exception exc) {
	    System.out.println("Cmdty/Curr:      " + "ERROR");
	}

	System.out.println("");
	try {
	    System.out.println("Balance:         " + acct.getBalanceFormatted());
	} catch (Exception exc) {
	    System.out.println("Balance:         " + "ERROR");
	}

	try {
	    System.out.println("Balance recurs.: " + acct.getBalanceRecursiveFormatted());
	} catch (Exception exc) {
	    System.out.println("Balance recurs.: " + "ERROR");
	}

	// ---

	showParents(acct, depth);
	showChildren(acct, depth);
	showTransactions(acct);
    }

    // -----------------------------------------------------------------

    private void showParents(GnucashAccount acct, int depth) {
	if (depth <= 0 && !acct.getType().equals("ROOT")) {
	    System.out.println("");
	    System.out.println(">>> BEGIN Parent Account");
	    printAcctInfo(acct.getParentAccount(), depth - 1);
	    System.out.println("<<< END Parent Account");
	}
    }

    private void showChildren(GnucashAccount acct, int depth) {
	System.out.println("");
	System.out.println("Children:");

	if (depth >= 0) {
	    System.out.println(">>> BEGIN Child Account");
	    for (GnucashAccount childAcct : acct.getChildren()) {
		printAcctInfo(childAcct, depth + 1);
	    }
	    System.out.println("<<< END Child Account");
	}
    }

    private void showTransactions(GnucashAccount acct) {
	System.out.println("");
	System.out.println("Transactions:");

	for (GnucashTransaction trx : acct.getTransactions()) {
	    System.out.println(" - " + trx.toString());
	}
    }
}

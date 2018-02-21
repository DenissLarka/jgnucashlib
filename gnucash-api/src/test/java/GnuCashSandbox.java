import org.gnucash.xml.GnucashAccount;
import org.gnucash.xml.GnucashTransaction;
import org.gnucash.xml.GnucashTransactionSplit;
import org.gnucash.xml.impl.GnucashFileImpl;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Created by Denis Larka
 * on 19.Apr.2017
 */
public class GnuCashSandbox {

	public static void main(String[] args) throws IOException {

		GnuCashSandbox sandbox = new GnuCashSandbox();
		sandbox.process();
	}

	private void process() throws IOException {

		GnucashFileImpl gnucashFile = new GnucashFileImpl(new File("/home/denis/Documents/family2.gnucash"));
		Collection<GnucashAccount> accounts = gnucashFile.getAccounts();
		for (GnucashAccount account : accounts) {
			System.out.println(account.getQualifiedName());
		}

		Collection<? extends GnucashTransaction> transactions = gnucashFile.getTransactions();
		for (GnucashTransaction transaction : transactions) {
			System.out.println(transaction.getDatePosted());
			List<GnucashTransactionSplit> splits = transaction.getSplits();
			for (GnucashTransactionSplit split : splits) {
				System.out.println("\t"+split.getQuantity());
			}
		}



	}
}


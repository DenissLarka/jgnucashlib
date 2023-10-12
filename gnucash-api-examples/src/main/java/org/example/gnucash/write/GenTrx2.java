package org.example.gnucash.write;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashTransaction;
import org.gnucash.read.GnucashTransactionSplit;
import org.gnucash.write.GnucashWritableTransaction;
import org.gnucash.write.GnucashWritableTransactionSplit;
import org.gnucash.write.impl.GnucashWritableFileImpl;

/**
 * Created by Deniss Larka
 */
public class GenTrx2 {

	public static void main(String[] args) throws IOException {

		GenTrx2 sandbox = new GenTrx2();
		sandbox.process();
	}

	private void process() throws IOException {

		GnucashWritableFileImpl gnucashFile = new GnucashWritableFileImpl(new File("/tmp/aaa/test.gnucash"));
		Collection<GnucashAccount> accounts = gnucashFile.getAccounts();
		for (GnucashAccount account : accounts) {
			System.out.println(account.getQualifiedName());
		}

		GnucashWritableTransaction writableTransaction = gnucashFile.createWritableTransaction();
		writableTransaction.setDescription("check");
		writableTransaction.setCurrencyID("EUR");
		writableTransaction.setDateEntered(LocalDateTime.now());
		
		GnucashWritableTransactionSplit writingSplit = writableTransaction.createWritingSplit(gnucashFile.getAccountByName("Root Account::Income::Bonus"));
		writingSplit.setValue(new FixedPointNumber(100));
		writingSplit.setDescription("descr");

		Collection<? extends GnucashTransaction> transactions = gnucashFile.getTransactions();
		for (GnucashTransaction transaction : transactions) {
			System.out.println(transaction.getDatePosted());
			List<GnucashTransactionSplit> splits = transaction.getSplits();
			for (GnucashTransactionSplit split : splits) {
				System.out.println("\t" + split.getQuantity());
			}
		}
		
		// Caution: output file will always be in uncompressed XML format,
		// regardless of whether the input file was compressed or not. 
		gnucashFile.writeFile(new File("/tmp/aaa/test_out.gnucash"));
	}
}


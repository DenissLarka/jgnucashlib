package org.gnucash.write.impl;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.TimeZone;

import org.gnucash.ConstTest;
import org.gnucash.read.GnucashTransaction;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.impl.TestGnucashTransactionImpl;
import org.gnucash.write.GnucashWritableTransaction;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestGnucashWritableTransactionImpl {
	private static final String TRX_1_ID = TestGnucashTransactionImpl.TRX_1_ID;
	private static final String TRX_2_ID = TestGnucashTransactionImpl.TRX_2_ID;

	// -----------------------------------------------------------------

	private GnucashWritableFileImpl gcshInFile = null;
	private GnucashFileImpl gcshOutFile = null;

	private String outFileGlobNameAbs = null;
	private File outFileGlob = null;

	@BeforeMethod
	public void initialize() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		// URL gcshFileURL = classLoader.getResource(Const.GCSH_FILENAME);
		// System.err.println("GnuCash test file resource: '" + gcshFileURL + "'");
		InputStream gcshInFileStream = null;
		try {
			gcshInFileStream = classLoader.getResourceAsStream(ConstTest.GCSH_FILENAME_IN);
		}
		catch (Exception exc) {
			System.err.println("Cannot generate input stream from resource");
			return;
		}

		try {
			gcshInFile = new GnucashWritableFileImpl(gcshInFileStream);
		}
		catch (Exception exc) {
			System.err.println("Cannot parse GnuCash in-file");
			exc.printStackTrace();
		}

		URL outFileNameAbsURL = classLoader.getResource(ConstTest.GCSH_FILENAME_IN); // sic
		//    System.err.println("Out file name (glob, URL): '" + outFileNameAbsURL + "'");
		outFileGlobNameAbs = outFileNameAbsURL.getPath();
		outFileGlobNameAbs = outFileGlobNameAbs.replace(ConstTest.GCSH_FILENAME_IN, ConstTest.GCSH_FILENAME_OUT);
		//    System.err.println("Out file name (glob): '" + outFileGlobNameAbs + "'");
		outFileGlob = new File(outFileGlobNameAbs);
	}

	// -----------------------------------------------------------------
	// PART 1: Read existing objects as modifyable ones
	//         (and see whether they are fully symmetrical to their read-only
	//         counterparts)
	// -----------------------------------------------------------------
	// Cf. TestGnucashTransaction.test01/02
	//
	// Check whether the GnucashWritableTransaction objects returned by
	// GnucashWritableFileImpl.getWritableTransactionByID() are actually
	// complete (as complete as returned be GnucashFileImpl.getTransactionById().

	@Test
	public void test01_1() throws Exception {
		GnucashWritableTransaction trx = gcshInFile.getTransactionByID(TRX_1_ID);
		Assert.assertNotEquals(null, trx);

		Assert.assertEquals(TRX_1_ID, trx.getId());
		Assert.assertEquals(0.0, trx.getBalance().getBigDecimal().doubleValue(), ConstTest.DIFF_TOLERANCE);
		Assert.assertEquals("Dividenderl", trx.getDescription());
		Assert.assertEquals("2023-08-06T10:59Z", trx.getDatePosted().toString());
		Assert.assertEquals("2023-08-06T08:21:44Z", trx.getDateEntered().toString());

		Assert.assertEquals(3, trx.getSplitsCount());
		Assert.assertEquals("7abf90fe15124254ac3eb7ec33f798e7", trx.getSplits().get(0).getId());
		Assert.assertEquals("ea08a144322146cea38b39d134ca6fc1", trx.getSplits().get(1).getId());
		Assert.assertEquals("5c5fa881869843d090a932f8e6b15af2", trx.getSplits().get(2).getId());
	}

	@Test
	public void test01_2() throws Exception {
		GnucashWritableTransaction trx = gcshInFile.getTransactionByID(TRX_2_ID);
		Assert.assertNotEquals(null, trx);

		Assert.assertEquals(TRX_2_ID, trx.getId());
		Assert.assertEquals(0.0, trx.getBalance().getBigDecimal().doubleValue(), ConstTest.DIFF_TOLERANCE);
		Assert.assertEquals("Unfug und Quatsch GmbH", trx.getDescription());
		Assert.assertEquals("2023-07-29T10:59Z", trx.getDatePosted().toString());
		Assert.assertEquals("2023-09-13T08:36:54Z", trx.getDateEntered().toString());

		Assert.assertEquals(2, trx.getSplitsCount());
		Assert.assertEquals("f2a67737458d4af4ade616a23db32c2e", trx.getSplits().get(0).getId());
		Assert.assertEquals("d17361e4c5a14e84be4553b262839a7b", trx.getSplits().get(1).getId());
	}

	// -----------------------------------------------------------------
	// PART 2: Modify existing objects
	// -----------------------------------------------------------------
	// Check whether the GnucashWritableTransaction objects returned by
	// can actually be modified -- both in memory and persisted in file.

	@Test
	public void test02_1() throws Exception {
		GnucashWritableTransaction trx = gcshInFile.getTransactionByID(TRX_1_ID);
		Assert.assertNotEquals(null, trx);
		Assert.assertEquals(TRX_1_ID, trx.getId());

		// ----------------------------
		// Modify the object

		trx.setDescription("Super dividend");
		trx.setDatePosted(LocalDate.of(1970, 1, 1));

		// ::TODO not possible yet
		// trx.getSplitByID("7abf90fe15124254ac3eb7ec33f798e7").remove()
		// trx.getSplitByID("7abf90fe15124254ac3eb7ec33f798e7").setXYZ()

		// ----------------------------
		// Check whether the object can has actually be modified
		// (in memory, not in the file yet).

		test02_1_check_memory(trx);

		// ----------------------------
		// Now, check whether the modified object can be written to the
		// output file, then re-read from it, and whether is is what
		// we expect it is.

		File outFile = Files.createTempFile("gc", ConstTest.GCSH_FILENAME_OUT).toFile();
		//  System.err.println("Outfile for TestGnucashWritableCustomerImpl.test01_1: '" + outFile.getPath() + "'");
		outFile.delete(); // sic, the temp. file is already generated (empty),
		// and the GnuCash file writer does not like that.
		gcshInFile.writeFile(outFile);

		test02_1_check_persisted(outFile);
	}

	private void test02_1_check_memory(GnucashWritableTransaction trx) {
		Assert.assertEquals(trx.getBalance().getBigDecimal().doubleValue(), 0.0, ConstTest.DIFF_TOLERANCE); // unchanged
		Assert.assertEquals(trx.getDescription(), "Super dividend"); // changed

		Assert.assertEquals(trx.getDatePosted().toString(), "1970-01-01T00:00+01:00[" + TimeZone.getDefault().getID() + "]"); // changed
		Assert.assertEquals(trx.getDateEntered().toString(), "2023-08-06T08:21:44Z"); // unchanged

		Assert.assertEquals(trx.getSplitsCount(), 3); // unchanged
		Assert.assertEquals(trx.getSplits().get(0).getId(), "7abf90fe15124254ac3eb7ec33f798e7"); // unchanged
		Assert.assertEquals(trx.getSplits().get(1).getId(), "ea08a144322146cea38b39d134ca6fc1"); // unchanged
		Assert.assertEquals(trx.getSplits().get(2).getId(), "5c5fa881869843d090a932f8e6b15af2"); // unchanged
	}

	public void test02_1_check_persisted(File outFile) throws Exception {
		gcshOutFile = new GnucashFileImpl(outFile);

		GnucashTransaction trx = gcshInFile.getTransactionByID(TRX_1_ID);
		Assert.assertNotEquals(null, trx);

		Assert.assertEquals(TRX_1_ID, trx.getId());
		Assert.assertEquals(0.0, trx.getBalance().getBigDecimal().doubleValue(), ConstTest.DIFF_TOLERANCE); // unchanged
		Assert.assertEquals(trx.getDescription(), "Super dividend"); // changed
		Assert.assertEquals(trx.getDatePosted().toString(), "1970-01-01T00:00+01:00[" + TimeZone.getDefault().getID() + "]"); // changed
		Assert.assertEquals(trx.getDateEntered().toString(), "2023-08-06T08:21:44Z"); // unchanged

		Assert.assertEquals(trx.getSplitsCount(), 3); // unchanged
		Assert.assertEquals(trx.getSplits().get(0).getId(), "7abf90fe15124254ac3eb7ec33f798e7"); // unchanged
		Assert.assertEquals(trx.getSplits().get(1).getId(), "ea08a144322146cea38b39d134ca6fc1"); // unchanged
		Assert.assertEquals(trx.getSplits().get(2).getId(), "5c5fa881869843d090a932f8e6b15af2"); // unchanged
	}

}

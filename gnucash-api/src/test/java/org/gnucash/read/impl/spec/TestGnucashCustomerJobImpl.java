package org.gnucash.read.impl.spec;

import java.io.InputStream;

import org.gnucash.ConstTest;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashGenerJob;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.impl.TestGnucashGenerJobImpl;
import org.gnucash.read.spec.GnucashCustomerJob;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestGnucashCustomerJobImpl {
	private GnucashFile gcshFile = null;
	private GnucashGenerJob jobGener = null;
	private GnucashCustomerJob jobSpec = null;

	private static final String JOB_1_ID = TestGnucashGenerJobImpl.JOB_1_ID;

	@BeforeMethod
	public void initialize() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		// URL gcshFileURL = classLoader.getResource(Const.GCSH_FILENAME);
		// System.err.println("GnuCash test file resource: '" + gcshFileURL + "'");
		InputStream gcshFileStream = null;
		try {
			gcshFileStream = classLoader.getResourceAsStream(ConstTest.GCSH_FILENAME);
		}
		catch (Exception exc) {
			System.err.println("Cannot generate input stream from resource");
			return;
		}

		try {
			gcshFile = new GnucashFileImpl(gcshFileStream);
		}
		catch (Exception exc) {
			System.err.println("Cannot parse GnuCash file");
			exc.printStackTrace();
		}
	}

	// -----------------------------------------------------------------

	@Test
	public void test01() throws Exception {
		jobGener = gcshFile.getGenerJobByID(JOB_1_ID);
		jobSpec = new GnucashCustomerJobImpl(jobGener);

		Assert.assertTrue(jobSpec instanceof GnucashCustomerJob);
		Assert.assertEquals(jobSpec.getId(), JOB_1_ID);
		Assert.assertEquals(jobSpec.getNumber(), "000001");
		Assert.assertEquals(jobSpec.getName(), "Do more for others");
	}

	@Test
	public void test02() throws Exception {
		jobGener = gcshFile.getGenerJobByID(JOB_1_ID);
		jobSpec = new GnucashCustomerJobImpl(jobGener);

		// Note: That the following two return the same result
		// is *not* trivial (in fact, a serious implemetation error was
		// found with this test)
		Assert.assertEquals(jobGener.getPaidInvoices().size(), 0);
		Assert.assertEquals(jobSpec.getPaidInvoices().size(), 0);

		// Note: That the following two return the same result
		// is *not* trivial (in fact, a serious implemetation error was
		// found with this test)
		Assert.assertEquals(jobGener.getUnpaidInvoices().size(), 1);
		Assert.assertEquals(jobSpec.getUnpaidInvoices().size(), 1);
	}

	@Test
	public void test03() throws Exception {
		jobGener = gcshFile.getGenerJobByID(JOB_1_ID);
		jobSpec = new GnucashCustomerJobImpl(jobGener);

		// Note: That the following three return the same result
		// is *not* trivial (in fact, a serious implemetation error was
		// found with this test)
		String custID = "f44645d2397946bcac90dff68cc03b76";
		Assert.assertEquals(jobGener.getOwnerId(), custID);
		Assert.assertEquals(jobSpec.getOwnerId(), custID);
		Assert.assertEquals(jobSpec.getCustomerId(), custID);
	}
}

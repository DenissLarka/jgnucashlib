package org.gnucash.read.impl.aux;

import java.io.InputStream;
import java.util.Collection;

import org.gnucash.ConstTest;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.aux.GCshBillTerms;
import org.gnucash.read.aux.GCshBillTermsDays;
import org.gnucash.read.aux.GCshBillTermsProximo;
import org.gnucash.read.impl.GnucashFileImpl;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestGCshBillTermsImpl {
	public static final String BLLTRM_1_ID = "599bfe3ab5b84a73bf3acabc5abd5bc7"; // "sofort" (5 Tage)
	public static final String BLLTRM_2_ID = "f4310c65486a47a5a787348b7de6ca40"; // "30-10-3"
	public static final String BLLTRM_3_ID = "f65a46140da94c81a4e1e3c0aa38c32b"; // "nächster-monat-mitte"

	private GnucashFile gcshFile = null;
	private GCshBillTerms bllTrm = null;

	// -----------------------------------------------------------------

	@SuppressWarnings("exports")

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
		Collection<GCshBillTerms> bllTrmList = gcshFile.getBillTerms();

		Assert.assertEquals(bllTrmList.size(), 3);

		// ::TODO: Sort array for predictability
		Object[] bllTrmArr = bllTrmList.toArray();

		// funny, this parent/child relationship full of redundancies...
		Assert.assertEquals(((GCshBillTerms) bllTrmArr[0]).getId(), BLLTRM_1_ID);
		Assert.assertEquals(((GCshBillTerms) bllTrmArr[1]).getId(), BLLTRM_2_ID);
		Assert.assertEquals(((GCshBillTerms) bllTrmArr[2]).getId(), BLLTRM_3_ID);
	}

	@Test
	public void test02_1_1() throws Exception {
		bllTrm = gcshFile.getBillTermsByID(BLLTRM_1_ID);
		Assert.assertNotNull(bllTrm);
		Assert.assertEquals(bllTrm.getId(), BLLTRM_1_ID);
		Assert.assertEquals(bllTrm.getName(), "sofort");
		Assert.assertEquals(bllTrm.getType(), GCshBillTerms.Type.DAYS);

		Assert.assertNull(bllTrm.getParentId());
		Assert.assertEquals(bllTrm.getChildren().size(), 0);

		GCshBillTermsDays btDays = bllTrm.getDays();
		Assert.assertNotNull(btDays);

		Assert.assertEquals(btDays.getDueDays(), Integer.valueOf(5));
		Assert.assertNull(btDays.getDiscountDays());
		Assert.assertNull(btDays.getDiscount());
	}

	@Test
	public void test02_1_2() throws Exception {
		bllTrm = gcshFile.getBillTermsByName("sofort");
		Assert.assertNotEquals(bllTrm, null);
		// System.err.println(bllTrm);

		Assert.assertEquals(bllTrm.getId(), BLLTRM_1_ID);
		Assert.assertEquals(bllTrm.getName(), "sofort");
		Assert.assertEquals(bllTrm.getType(), GCshBillTerms.Type.DAYS);

		Assert.assertEquals(bllTrm.getParentId(), null);
		Assert.assertEquals(bllTrm.getChildren().size(), 0);

		GCshBillTermsDays btDays = bllTrm.getDays();
		Assert.assertNotEquals(null, btDays);

		Assert.assertEquals(btDays.getDueDays(), Integer.valueOf(5));
		Assert.assertNull(btDays.getDiscountDays());
		Assert.assertNull(btDays.getDiscount());
	}

	@Test
	public void test02_2_1() throws Exception {
		bllTrm = gcshFile.getBillTermsByID(BLLTRM_2_ID);
		Assert.assertNotEquals(null, bllTrm);
		// System.err.println(bllTrm);

		Assert.assertEquals(BLLTRM_2_ID, bllTrm.getId());
		Assert.assertEquals("30-10-3", bllTrm.getName());
		Assert.assertEquals(GCshBillTerms.Type.DAYS, bllTrm.getType());

		Assert.assertEquals(null, bllTrm.getParentId());
		Assert.assertEquals(0, bllTrm.getChildren().size());

		GCshBillTermsDays btDays = bllTrm.getDays();
		Assert.assertNotEquals(null, btDays);

		Assert.assertEquals(btDays.getDueDays(), Integer.valueOf(30));
		Assert.assertEquals(btDays.getDiscountDays(), Integer.valueOf(10));
		Assert.assertEquals(btDays.getDiscount().doubleValue(), 3.0, ConstTest.DIFF_TOLERANCE);
	}

	@Test
	public void test02_2_2() throws Exception {
		bllTrm = gcshFile.getBillTermsByName("30-10-3");
		Assert.assertNotEquals(null, bllTrm);
		// System.err.println(bllTrm);

		Assert.assertEquals(BLLTRM_2_ID, bllTrm.getId());
		Assert.assertEquals("30-10-3", bllTrm.getName());
		Assert.assertEquals(GCshBillTerms.Type.DAYS, bllTrm.getType());

		Assert.assertEquals(null, bllTrm.getParentId());
		Assert.assertEquals(0, bllTrm.getChildren().size());

		GCshBillTermsDays btDays = bllTrm.getDays();
		Assert.assertNotNull(btDays);

		Assert.assertEquals(btDays.getDueDays(), Integer.valueOf(30));
		Assert.assertEquals(btDays.getDiscountDays(), Integer.valueOf(10));
		Assert.assertEquals(btDays.getDiscount().doubleValue(), 3.0, ConstTest.DIFF_TOLERANCE);
	}

	@Test
	public void test02_3_1() throws Exception {
		bllTrm = gcshFile.getBillTermsByID(BLLTRM_3_ID);
		Assert.assertNotEquals(null, bllTrm);
		// System.err.println(bllTrm);

		Assert.assertEquals(BLLTRM_3_ID, bllTrm.getId());
		Assert.assertEquals(bllTrm.getName(), "nächster-monat-mitte");
		Assert.assertEquals(GCshBillTerms.Type.PROXIMO, bllTrm.getType());

		Assert.assertNull(bllTrm.getParentId());
		Assert.assertEquals(bllTrm.getChildren().size(), 0);

		GCshBillTermsProximo btProx = bllTrm.getProximo();
		Assert.assertNotNull(btProx);

		Assert.assertEquals(btProx.getDueDay(), Integer.valueOf(15));
		Assert.assertEquals(btProx.getDiscountDay(), Integer.valueOf(3));
		Assert.assertEquals(btProx.getDiscount().doubleValue(), 2.0, ConstTest.DIFF_TOLERANCE);
	}

	@Test
	public void test02_3_2() throws Exception {
		bllTrm = gcshFile.getBillTermsByName("nächster-monat-mitte");
		Assert.assertNotEquals(null, bllTrm);
		// System.err.println(bllTrm);

		Assert.assertEquals(BLLTRM_3_ID, bllTrm.getId());
		Assert.assertEquals("nächster-monat-mitte", bllTrm.getName());
		Assert.assertEquals(GCshBillTerms.Type.PROXIMO, bllTrm.getType());

		Assert.assertEquals(bllTrm.getParentId(), null);
		Assert.assertEquals(bllTrm.getChildren().size(), 0);

		GCshBillTermsProximo btProx = bllTrm.getProximo();
		Assert.assertNotNull(btProx);

		Assert.assertEquals(Integer.valueOf(15), btProx.getDueDay());
		Assert.assertEquals(Integer.valueOf(3), btProx.getDiscountDay());
		Assert.assertEquals(2.0, btProx.getDiscount().doubleValue(), ConstTest.DIFF_TOLERANCE);
	}
}

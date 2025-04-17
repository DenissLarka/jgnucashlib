package org.gnucash.read.impl;

import java.io.InputStream;

import org.gnucash.ConstTest;
import org.gnucash.messages.ApplicationMessages;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashGenerJob;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestGnucashGenerJobImpl {
  private GnucashFile gcshFile = null;
  private GnucashGenerJob job = null;

  public static final String JOB_1_ID = "e91b99cd6fbb48a985cbf1e8041f378c";
  public static final String JOB_2_ID = "028cfb5993ef4d6b83206bc844e2fe56";

  // -----------------------------------------------------------------

  @BeforeMethod
  public void initialize() throws Exception {
    ClassLoader classLoader = getClass().getClassLoader();
    // URL gcshFileURL = classLoader.getResource(Const.GCSH_FILENAME);
    // System.err.println("GnuCash test file resource: '" + gcshFileURL + "'");
    ApplicationMessages.setup();

    InputStream gcshFileStream = null;
    try {
      gcshFileStream = classLoader.getResourceAsStream(ConstTest.GCSH_FILENAME);
    } catch (Exception exc) {
      System.err.println("Cannot generate input stream from resource");
      return;
    }

    try {
      gcshFile = new GnucashFileImpl(gcshFileStream);
    } catch (Exception exc) {
      System.err.println("Cannot parse GnuCash file");
      exc.printStackTrace();
    }
  }

  // -----------------------------------------------------------------

  @Test
  public void testCust01() throws Exception {
    job = gcshFile.getGenerJobByID(JOB_1_ID);

    Assert.assertEquals(job.getId(), JOB_1_ID);
    Assert.assertEquals(job.getNumber(), "000001");
    Assert.assertEquals(job.getOwnerType(), GnucashGenerJob.TYPE_CUSTOMER);
    Assert.assertEquals(job.getName(), "Do more for others");
  }

  @Test
  public void testCust02() throws Exception {
    job = gcshFile.getGenerJobByID(JOB_1_ID);

    Assert.assertEquals(job.getPaidInvoices().size(), 0);
    Assert.assertEquals(job.getUnpaidInvoices().size(), 1);
  }

  @Test
  public void testCust03() throws Exception {
    job = gcshFile.getGenerJobByID(JOB_1_ID);

    String custID = "f44645d2397946bcac90dff68cc03b76";
    Assert.assertEquals(job.getOwnerId(), custID);
  }

  // -----------------------------------------------------------------

  @Test
  public void testVend01() throws Exception {
    job = gcshFile.getGenerJobByID(JOB_2_ID);

    Assert.assertEquals(job.getId(), JOB_2_ID);
    Assert.assertEquals(job.getNumber(), "000002");
    Assert.assertEquals(job.getOwnerType(), GnucashGenerJob.TYPE_VENDOR);
    Assert.assertEquals(job.getName(), "Let's buy help");
  }

  @Test
  public void testVend02() throws Exception {
    job = gcshFile.getGenerJobByID(JOB_2_ID);

    Assert.assertEquals(job.getPaidInvoices().size(), 0);
    Assert.assertEquals(job.getUnpaidInvoices().size(), 1);
  }

  @Test
  public void testVend03() throws Exception {
    job = gcshFile.getGenerJobByID(JOB_2_ID);

    String vendID = "4f16fd55c0d64ebe82ffac0bb25fe8f5";
    Assert.assertEquals(job.getOwnerId(), vendID);
  }
}

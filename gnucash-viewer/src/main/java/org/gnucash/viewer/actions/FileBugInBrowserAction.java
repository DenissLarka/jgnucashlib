/**
 * FileBugInBrowserAction.java
 * created: 15.11.2008
 * (c) 2008 by <a href="http://Wolschon.biz">Wolschon Softwaredesign und Beratung</a>
 * This file is part of jgnucashLib-GPL by Marcus Wolschon <a href="mailto:Marcus@Wolscon.biz">Marcus@Wolscon.biz</a>.
 * You can purchase support for a sensible hourly rate or
 * a commercial license of this file (unless modified by others) by contacting him directly.
 *
 *  jgnucashLib-GPL is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  jgnucashLib-GPL is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with jgnucashLib-V1.  If not, see <http://www.gnu.org/licenses/>.
 *
 ***********************************
 * Editing this file:
 *  -For consistent code-quality this file should be checked with the
 *   checkstyle-ruleset enclosed in this project.
 *  -After the design of this file has settled it should get it's own
 *   JUnit-Test that shall be executed regularly. It is best to write
 *   the test-case BEFORE writing this class and to run it on every build
 *   as a regression-test.
 */

package org.gnucash.viewer.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * (c) 2008 by <a href="http://Wolschon.biz>Wolschon Softwaredesign und Beratung</a>.<br/>
 * Project: jgnucashLib-GPL<br/>
 * FileBugInBrowserAction<br/>
 * created: 15.11.2008 <br/>
 *<br/><br/>
 * <b>Open the system-web-browser to file a bug-report.</b>
 * @author  <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 */
public class FileBugInBrowserAction implements ActionListener {


    /**
     * Our logger for debug- and error-output.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FileBugInBrowserAction.class);

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(final ActionEvent aE) {
        try {
            final URL fileABugURL = new URL("https://sourceforge.net/tracker2/?group_id=147662&atid=769090");
            showDocument(fileABugURL);
        } catch (MalformedURLException e) {
            LOGGER.error("Error, cannot launch web browser", e);
            JOptionPane.showMessageDialog(null, "Error, cannot launch web browser:\n"
                    + e.getLocalizedMessage());
        }
    }
    /**
     * reference to browser-starter.
     */
    private static Object myJNLPServiceManagerObject;

    /**

     * Open the given URL in the default-browser.
     * @param url the URL to open
     * @return false if it did not work
     */
    @SuppressWarnings("unchecked")
    public static boolean  showDocument(final URL url) {

        if (myJNLPServiceManagerObject == null) {
            myJNLPServiceManagerObject = getJNLPServiceManagerObject();
        }

        // we cannot use JNLP -> make an educated guess
        if (myJNLPServiceManagerObject == null) {
            try {
                String osName = System.getProperty("os.name");
                if (osName.startsWith("Mac OS")) {
                    Class fileMgr = Class.forName("com.apple.eio.FileManager");
                    Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] {String.class});
                    openURL.invoke(null, new Object[] {url});
                } else if (osName.startsWith("Windows")) {
                    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
                } else { //assume Unix or Linux
                    String[] browsers = {"x-www-browser",
                            "firefox", "iceweasle",
                            "opera", "konqueror",
                            "epiphany", "mozilla", "netscape" };
                    String browser = null;
                    for (int count = 0;
                    count < browsers.length && browser == null;
                    count++) {
                        if (Runtime.getRuntime().exec(new String[] {"which",
                                browsers[count]}).waitFor() == 0) {
                            browser = browsers[count];
                        }
                        if (browser == null) {
                            return false;
                        } else {
                            Runtime.getRuntime().exec(new String[] {browser, url.toString()});
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Error attempting to launch web browser natively.", e);
                JOptionPane.showMessageDialog(null, "Error attempting to launch web browser:\n"
                        + e.getLocalizedMessage());
            }
        }



        if (myJNLPServiceManagerObject != null) {
            try {
                Method  method = myJNLPServiceManagerObject.getClass().getMethod("showDocument", new Class[] {URL.class});
                Boolean  resultBoolean = (Boolean)
                method.invoke(myJNLPServiceManagerObject, new Object[] {url});
                return resultBoolean.booleanValue();
            } catch (Exception  ex) {
                JOptionPane.showMessageDialog(null, "Error attempting to launch web browser:\n"
                        + ex.getLocalizedMessage());
            }
        }

        return false;
    }


    /**
     * @return instance of "javax.jnlp.ServiceManager" on platforms
     * that support it.
     */
    @SuppressWarnings("unchecked")
    private static Object getJNLPServiceManagerObject() {
        try {
            Class  serviceManagerClass = Class.forName("javax.jnlp.ServiceManager");
            Method lookupMethod        = serviceManagerClass.getMethod("lookup",
                    new Class[] {String.class});
            return lookupMethod.invoke(null, new Object[]{"javax.jnlp.BasicService"});
        } catch (Exception  ex) {
            LOGGER.info("Cannot instanciate javax.jnlp.ServiceManager "
                    + "- this platform seems not to support it.", ex);
            return null;
        }
    }



}

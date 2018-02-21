/**
 * JGnucashViewer.java
 * License: GPLv3 or later
 * Created on 27.09.2008
 * (c) 2005 by "Wolschon Softwaredesign und Beratung".
 * <p>
 * <p>
 * -----------------------------------------------------------
 * major Changes:
 * 27.09.2008 - initial version
 * ...
 */
package biz.wolschon.finance.jgnucash;

import java.io.File;

import org.java.plugin.boot.Application;
import org.java.plugin.boot.ApplicationPlugin;
import org.java.plugin.util.ExtendedProperties;

/**
 *
 * created: 27.09.2008 <br/>
 *
 * Entry-Point for the JPF-Library we are using to support
 * plugins..
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 */
public class JPFEditorMain extends ApplicationPlugin {

	/* (non-Javadoc)
	 * @see org.java.plugin.boot.ApplicationPlugin#initApplication(org.java.plugin.util.ExtendedProperties, java.lang.String[])
	 */
	@Override
	protected Application initApplication(final ExtendedProperties arg0, final String[] args) {
		JGnucash ste = new JGnucash(getManager(), getDescriptor());
		ste.setVisible(true);
		if (args.length > 0) {
			ste.loadFile(new File(args[0]));
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.java.plugin.Plugin#doStart()
	 */
	@Override
	protected void doStart() throws Exception {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see org.java.plugin.Plugin#doStop()
	 */
	@Override
	protected void doStop() throws Exception {
		// do nothing
	}

}

/**
 * ToolPlugin-java
 * created: 29.11.2008 <br/>
 *  (c) 2008 by <a href="http://Wolschon.biz">Wolschon Softwaredesign und Beratung</a>
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
package biz.wolschon.finance.jgnucash.plugin;

import biz.wolschon.fileformats.gnucash.GnucashWritableAccount;
import biz.wolschon.fileformats.gnucash.GnucashWritableFile;

/**
 * created: 29.11.2008 <br/>
 *
 * This is a plugin-interface that plugins that want to plug
 * into the extension-point "Tool" of the
 * "biz.wolschon.finance.jgnucash.editor.main"- plugin.<br/>
 * Extension - point declaration:<br/>
 * <pre>
 * <extension-point id="Tool">
        <parameter-def id="class" /> (classname of class implementing this interface)
        <parameter-def id="name" /> (name to be displayed in menus,...)
        <parameter-def id="hasFixedAccount" /> <!-- false=use currently selected account -->
        <parameter-def id="description" multiplicity="none-or-one" /> (optionsl, very short description)
        <parameter-def id="icon" multiplicity="none-or-one" /> (optional path to icon-ressource)
    </extension-point>
    </pre>
 *
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 *
 */
public interface ToolPlugin {

    /**
     * Runt the actual tool.
     * @param writableModel the gnucash-file opened.
     * @param currentAccount the currently selected account (may be null)
     * @return (optional) a message to be displayed after success (or null/empty message)
     */
    String runTool(final GnucashWritableFile writableModel, final GnucashWritableAccount currentAccount) throws Exception;

}

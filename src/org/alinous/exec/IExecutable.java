/**
 * CROSSFIRE JAPAN INCORPORATED
 * This source code is under GPL License.
 * info@crossfire.jp
 * Official web site
 * http://alinous.org
 * 
 *  Copyright (C) 2007 Tomohiro Iizuka
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.alinous.exec;

import java.util.List;

import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.runtime.VariableRepository;
import org.jdom.Element;

public interface IExecutable {
	public static final String TAG_EXECUTABLE = "EXECUTABLE";
	
	public static final String ATTR_CLASS = "execClass";
	
	
	public boolean execute(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException;
	
	public int getLine();
	
	// Transfer informations to Debug Client
	public void exportIntoJDomElement(Element parent) throws AlinousException ;
	public void importFromJDomElement(Element threadElement) throws AlinousException;
	
	// check scripts
	public void checkStaticErrors(ScriptCheckContext scContext, List<ScriptError> errorList);
}

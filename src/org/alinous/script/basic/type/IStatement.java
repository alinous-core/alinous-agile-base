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
package org.alinous.script.basic.type;

import java.util.List;

import org.alinous.debug.StepInCandidates;
import org.alinous.exec.ScriptCheckContext;
import org.alinous.exec.ScriptError;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.IScriptObject;
import org.alinous.script.IScriptSentence;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.VariableRepository;
import org.jdom.Element;

public interface IStatement extends IScriptObject{
	public static final String TAG_STATEMENT = "STATEMENT";
	public static final String ATTR_STATEMENT_CLASS = "className";

	public IScriptVariable executeStatement(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException;
	
	
	public void exportIntoJDomElement(Element parent) throws AlinousException;
	public void importFromJDomElement(Element element) throws AlinousException;
	
	public void canStepInStatements(StepInCandidates candidates);
	
	public void setCallerSentence(IScriptSentence callerSentence);
	//public void setCurrentDataSource(String dataSource);
	
	// check scripts
	public void checkStaticErrors(ScriptCheckContext scContext, List<ScriptError> errorList);
}

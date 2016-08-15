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
package org.alinous.poi.api;

import java.util.Stack;

import org.alinous.AlinousUtils;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.poi.PoiManager;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.functions.system.AbstractSystemFunction;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class PoiSetCellValue extends AbstractSystemFunction
{
	public static final String QUALIFIED_NAME = "POI.SETCELLVALUE";
	
	public static final String FILE_PATH = "filePath";
	public static final String SHEET = "sheet";
	public static final String COMMANDS = "commands";
	public static final String OUT_FILE_PATH = "outFilePath";
	
	public PoiSetCellValue()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", FILE_PATH);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", SHEET);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("@", COMMANDS);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", OUT_FILE_PATH);
		this.argmentsDeclare.addArgument(arg);
	}
	
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo)
			throws ExecutionException, RedirectRequestException
	{
		Stack<IStatement> stmtStack = context.getFuncArgStack();
		
		if(stmtStack.size() != this.argmentsDeclare.getSize()){
			throw new ExecutionException("Number of the function's arguments is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		
		IPathElement ipath = PathElementFactory.buildPathElement(FILE_PATH);
		IScriptVariable filePathVariable = newValRepo.getVariable(ipath, context);
		ipath = PathElementFactory.buildPathElement(SHEET);
		IScriptVariable sheetVariable = newValRepo.getVariable(ipath, context);
		ipath = PathElementFactory.buildPathElement(COMMANDS);
		IScriptVariable commandsVariable = newValRepo.getVariable(ipath, context);
		ipath = PathElementFactory.buildPathElement(OUT_FILE_PATH);
		IScriptVariable outFilePathVariable = newValRepo.getVariable(ipath, context);
		
		if(!(filePathVariable instanceof ScriptDomVariable) ||
				!(commandsVariable instanceof ScriptArray ||
				!(outFilePathVariable instanceof ScriptDomVariable))){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		String filePath = ((ScriptDomVariable)filePathVariable).getValue();
		filePath = AlinousUtils.getAbsolutePath(context.getCore().getHome(), filePath);
		
		String outFilePath = ((ScriptDomVariable)outFilePathVariable).getValue();
		outFilePath = AlinousUtils.getAbsolutePath(context.getCore().getHome(), outFilePath);
		
		String sheet = ((ScriptDomVariable)sheetVariable).getValue();
		
		PoiManager mgr = new PoiManager();
		
		try {
			mgr.open(filePath);
			
			ScriptArray arrayCmd = (ScriptArray)commandsVariable;
			
			for(int i = 0; i < arrayCmd.getSize(); i++){
				executeSetCommand(sheet, (ScriptDomVariable)arrayCmd.get(i), mgr);
			}
			
			mgr.write(outFilePath);
		} catch (Throwable e) {
			throw new ExecutionException(e, "Type of the function's argument is wrong : " + QUALIFIED_NAME);// i18n
		}finally{
			mgr.close();
		}
		
		return null;
	}
	
	private void executeSetCommand(String sheet, ScriptDomVariable cmd, PoiManager mgr)
	{
		String strRow = ((ScriptDomVariable)cmd.get("ROW")).getValue();
		String strCol = ((ScriptDomVariable)cmd.get("COL")).getValue();
		String strVALUE = ((ScriptDomVariable)cmd.get("VALUE")).getValue();
		
		mgr.setSellValue(Integer.parseInt(strCol), Integer.parseInt(strRow), Integer.parseInt(sheet), strVALUE);
	}

	public String getName()
	{
		return QUALIFIED_NAME;
	}

	public IScriptVariable getResult()
	{
		return null;
	}

	@Override
	public String codeAssistString() {
		return null;
	}

	@Override
	public String descriptionString() {
		return null;
	}

}

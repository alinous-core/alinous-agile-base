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
package org.alinous.tools.csv;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.alinous.exec.pages.FileResourceManager;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
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
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.functions.system.AbstractSystemFunction;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class CsvAddFields extends AbstractSystemFunction
{
	public static final String QUALIFIED_NAME = "CSV.ADDFIELDS";
	public static final String FILE_PATH = "filePath";
	public static final String FIELD_VALUES = "fieldValues";
	
	public CsvAddFields()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", FILE_PATH);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("@", FIELD_VALUES);
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
		ipath = PathElementFactory.buildPathElement(FIELD_VALUES);
		IScriptVariable fieldValueVariable = newValRepo.getVariable(ipath, context);

		
		if(!(filePathVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		String filePath = ((ScriptDomVariable)filePathVariable).getValue();
		List<String> fieldValues = getVariables((ScriptArray) fieldValueVariable);
		
		
		FileResourceManager fileManager = context.getUnit().getFileResourceManager();
		
		try {
			Writer writer = fileManager.openWriteFile(filePath, null);
			
			boolean first = true;
			Iterator<String> it = fieldValues.iterator();
			while(it.hasNext()){
				String csvValue = it.next();
				
				if(first){
					first = false;
				}else{
					writer.write(",");
				}
				
				csvValue = toCsvEscapedString(csvValue);
				writer.write(csvValue);
			}
			
			writer.flush();
		} catch (IOException e) {
			ScriptDomVariable val = new ScriptDomVariable("result");
			val.setValueType(IScriptVariable.TYPE_BOOLEAN);
			val.setValue("false");
			
			return val;
		}
		
		ScriptDomVariable val = new ScriptDomVariable("result");
		val.setValueType(IScriptVariable.TYPE_BOOLEAN);
		val.setValue("true");
		
		return val;
	}
	
	private String toCsvEscapedString(String csvValue)
	{
		if(csvValue == null){
			csvValue = "";
		}
		
		csvValue = csvValue.replace("\"", "\"\"");
		
		return "\"" + csvValue + "\"";
	}
	
	private List<String> getVariables(ScriptArray variableArray)
	{
		List<String> list = new ArrayList<String>();
		
		Iterator<IScriptVariable> it = variableArray.iterator();
		while(it.hasNext()){
			IScriptVariable val = it.next();
			
			if(val instanceof ScriptDomVariable){
				String strValue = ((ScriptDomVariable)val).getValue();
				
				list.add(strValue);
			}
		}
		
		return list;
	}

	
	public String getName()
	{
		return QUALIFIED_NAME;
	}

	@Override
	public String codeAssistString() {
		return "Csv.addFields($filePath, @fieldValues)";
	}


	@Override
	public String descriptionString() {
		return "Add new record into the file.\n" +
				"The file must be opened with Csv.openReadFile() function.\n" +
				"After calling this function, call Csv.outLineEnd().";
	}

}

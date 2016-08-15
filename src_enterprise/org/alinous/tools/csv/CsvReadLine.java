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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Stack;

import org.alinous.csv.CsvException;
import org.alinous.csv.CsvReader;
import org.alinous.csv.CsvRecord;
import org.alinous.exec.pages.FileResourceManager;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.functions.system.AbstractSystemFunction;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class CsvReadLine extends AbstractSystemFunction
{
	public static final String QUALIFIED_NAME = "CSV.READLINE";
	
	public static final String FILE_PATH = "filePath";
	
	public CsvReadLine()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", FILE_PATH);
		this.argmentsDeclare.addArgument(arg);
	}
	
	@Override
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo)
			throws ExecutionException, RedirectRequestException
	{
		ScriptArray resultArray = null;
		Stack<IStatement> stmtStack = context.getFuncArgStack();
		
		if(stmtStack.size() != this.argmentsDeclare.getSize()){
			throw new ExecutionException("Number of the function's arguments is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		
		IPathElement ipath = PathElementFactory.buildPathElement(FILE_PATH);
		IScriptVariable filePathVariable = newValRepo.getVariable(ipath, context);
		
		if(!(filePathVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		String filePath = ((ScriptDomVariable)filePathVariable).getValue();
		
		FileResourceManager fileManager = context.getUnit().getFileResourceManager();
		
		try {
			CsvReader csvReader = fileManager.openCsvReader(filePath, null);
			
			resultArray = readRecord(csvReader);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (CsvException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return resultArray;
	}
	
	private ScriptArray readRecord(CsvReader csvReader) throws CsvException, IOException
	{
		CsvRecord record = csvReader.readRecord();
		ScriptArray array = new ScriptArray();
		
		Iterator<String> it = record.iterator();
		while(it.hasNext()){
			String value = it.next();
			
			ScriptDomVariable dom = new ScriptDomVariable("value");
			dom.setValueType(IScriptVariable.TYPE_STRING);
			dom.setValue(value);
			
			array.add(dom);
		}
		
		return array;
	}
	
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}

	@Override
	public String codeAssistString() {
		return "Csv.readLine($filePath)";
	}

	@Override
	public String descriptionString() {
		return "Read a csv record from csv file.\n" +
				"The file must be opened with Csv.openReadFile() function.";
	}

}

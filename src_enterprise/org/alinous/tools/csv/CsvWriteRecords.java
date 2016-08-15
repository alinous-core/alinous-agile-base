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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.alinous.AlinousUtils;
import org.alinous.cloud.file.AlinousFile;
import org.alinous.cloud.file.AlinousFileOutputStream;
import org.alinous.csv.CsvWriter;
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

public class CsvWriteRecords extends AbstractSystemFunction
{
	public static final String QUALIFIED_NAME = "CSV.WRITERECORDS";
	
	public static final String FILE_PATH = "filePath";
	public static final String RECORDS = "records";
	public static final String PROPS = "props";
	public static final String APPEND = "append";
	public static final String ENCODE = "encode";
	
	public CsvWriteRecords()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", FILE_PATH);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("@", RECORDS);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("@", PROPS);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", APPEND);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", ENCODE);
		this.argmentsDeclare.addArgument(arg);
	}
	
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo)
			throws ExecutionException, RedirectRequestException
	{
		Stack<IStatement> stmtStack = context.getFuncArgStack();
		
		if(stmtStack.size() != this.argmentsDeclare.getSize()
				&& stmtStack.size() != this.argmentsDeclare.getSize() - 1){
			throw new ExecutionException("Number of the function's arguments is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		
		IPathElement ipath = PathElementFactory.buildPathElement(FILE_PATH);
		IScriptVariable filePathVariable = newValRepo.getVariable(ipath, context);
		ipath = PathElementFactory.buildPathElement(RECORDS);
		IScriptVariable recordsVariable = newValRepo.getVariable(ipath, context);
		ipath = PathElementFactory.buildPathElement(PROPS);
		IScriptVariable propsVariable = newValRepo.getVariable(ipath, context);
		ipath = PathElementFactory.buildPathElement(APPEND);
		IScriptVariable appendVariable = newValRepo.getVariable(ipath, context);
		ipath = PathElementFactory.buildPathElement(ENCODE);
		IScriptVariable encodeVariable = newValRepo.getVariable(ipath, context);
		
		if(!(filePathVariable instanceof ScriptDomVariable) ||
				!(recordsVariable instanceof ScriptArray) ||
				!(propsVariable instanceof ScriptArray) ||
				!(appendVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		String filePath = ((ScriptDomVariable)filePathVariable).getValue();
		String appendStr = ((ScriptDomVariable)appendVariable).getValue();
		String encode = "UTF-8";
		if(encodeVariable != null ){
			encode = ((ScriptDomVariable)encodeVariable).getValue();
		}
		
		String outPath = AlinousUtils.getAbsolutePath(context.getCore().getHome(), filePath);
		
		boolean append = false;
		if(appendStr != null && appendStr.toLowerCase().equals("true")){
			append = true;
		}
		
		ScriptArray recordDom = (ScriptArray)recordsVariable;
		List<String> properties = new ArrayList<String>();
		for(int i = 0; i < ((ScriptArray)propsVariable).getSize(); i++){
			ScriptDomVariable propVal = (ScriptDomVariable)((ScriptArray)propsVariable).get(i);
			
			String valStr = propVal.getValue();

			properties.add(valStr);
		}
		
		AlinousFile file = new AlinousFile(outPath);
		OutputStream stream = null;
		Writer writer = null;
		
		try {
			stream = new AlinousFileOutputStream(file, append);
			writer = new OutputStreamWriter(stream, encode);
			CsvWriter csvWriter = new CsvWriter(writer);
			
			for(int i = 0; i < recordDom.getSize(); i++){
				ScriptDomVariable oneRecord = (ScriptDomVariable)recordDom.get(i);
				
				handleRecord(oneRecord, csvWriter, properties);
			}
		} catch (FileNotFoundException e) {
			if(writer != null){
				try {
					writer.close();
					if(stream != null){
						stream.close();
					}
				} catch (IOException ex) {e.printStackTrace();}
			}
		} catch (UnsupportedEncodingException e) {
			if(writer != null){
				try {
					writer.close();
					stream.close();
				} catch (IOException ex) {e.printStackTrace();}
			}
		} catch (IOException e) {
			if(writer != null){
				try {
					writer.close();
					stream.close();
				} catch (IOException ex) {e.printStackTrace();}
			}
		}
		
		return null;
	}
	
	private void handleRecord(ScriptDomVariable oneRecord, CsvWriter csvWriter, List<String> properties) throws IOException
	{
		Iterator<String> it = properties.iterator();
		while(it.hasNext()){
			String key = it.next();
			
			if(key == null || key.equals("")){
				csvWriter.addField("");
				continue;
			}
			
			IScriptVariable val = oneRecord.get(key);
			if(val instanceof ScriptDomVariable){
				String valStr = ((ScriptDomVariable)val).getValue();
				
				csvWriter.addField(valStr);
			}
		}
		
		csvWriter.endRecord();
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
		return "Csv.writeRecords($filePath, @records, @props, $append, $encode)";
	}

	@Override
	public String descriptionString() {
		return "Write records into csv file.";
	}

}

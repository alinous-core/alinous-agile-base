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
package org.alinous.datasrc.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import org.alinous.AlinousCore;
import org.alinous.AlinousDebug;
import org.alinous.cloud.file.AlinousFile;
import org.alinous.cloud.file.AlinousFileInputStream;
import org.alinous.csv.CsvException;
import org.alinous.csv.CsvReader;
import org.alinous.csv.CsvRecord;
import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.datasrc.types.DataField;
import org.alinous.datasrc.types.DataTable;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.functions.system.AbstractSystemFunction;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElement;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.InsertSentence;
import org.alinous.script.sql.other.ColumnList;
import org.alinous.script.sql.other.VariableList;
import org.alinous.script.sql.statement.ColumnIdentifier;
import org.alinous.script.sql.statement.SQLVariable;

public class SQLLoadCsv extends AbstractSystemFunction
{
	public static String QUALIFIED_NAME = "SQL.LOADCSV";
	public static String DATA_SRC = "dataSrc";
	public static String TABLE = "table";
	public static String FILE_NAME = "fileName";
	
	private PrecompileBuffer precompileBuffer = new PrecompileBuffer();
	
	public SQLLoadCsv()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", DATA_SRC);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", TABLE);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", FILE_NAME);
		this.argmentsDeclare.addArgument(arg);
	}
	
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		Stack<IStatement> stmtStack = context.getFuncArgStack();
		
		if(stmtStack.size() != this.argmentsDeclare.getSize()){
			throw new ExecutionException("Number of the function's arguments is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		
		IPathElement ipath = PathElementFactory.buildPathElement(DATA_SRC);
		IScriptVariable dataSrcVariable = newValRepo.getVariable(ipath, context);
		
		ipath = PathElementFactory.buildPathElement(TABLE);
		IScriptVariable tableVariable = newValRepo.getVariable(ipath, context);
		
		ipath = PathElementFactory.buildPathElement(FILE_NAME);
		IScriptVariable fileVariable = newValRepo.getVariable(ipath, context);
		
		if(!(dataSrcVariable instanceof ScriptDomVariable) ||
				!(tableVariable instanceof ScriptDomVariable) ||
				!(fileVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		String table = ((ScriptDomVariable)tableVariable).getValue();
		String absPath = SQLFunctionUtils.getAbsolutePath(((ScriptDomVariable)fileVariable).getValue(), context.getCore());
		AlinousFileInputStream stream = null;
		String conStr = ((ScriptDomVariable)dataSrcVariable).getValue();
		
		DataSrcConnection con = null;
		try {
			con = context.getUnit().getConnectionManager().connect(conStr, context);
			
			AlinousFile file = new AlinousFile(absPath);
			stream = new AlinousFileInputStream(file);
			
			CsvReader csvReader = new CsvReader(stream,  "utf-8");
			CsvRecord record = null;
			
			// first record is columns
			record = csvReader.readRecord();
			ColumnList colList = getColumnList(record);
			DataTable dataTable = con.getDataTable(table);
			
			// reset precompile
			this.precompileBuffer.clearPreCompile(table, con);
			
			
			// normal records
			int count = 0;
			con.begin(Connection.TRANSACTION_SERIALIZABLE);
			do{
				record = csvReader.readRecord();
				
				if(!record.isEmpty()){
					insertRecord(context.getCore(), con, table, colList, record, dataTable, context);
					if(count == 100){
						con.commit(null);
						con.begin(Connection.TRANSACTION_SERIALIZABLE);
						count = 0;
					}else{
						count++;
					}
				}
				
			}while(!record.isEmpty());
			
			if(count > 0){
				con.commit(null);
			}
			
		} catch (DataSourceException e) {
			context.getCore().getLogger().reportError(e);
			throw new ExecutionException(e, "Failed in operation at " + QUALIFIED_NAME); // i18n
		} catch (FileNotFoundException e) {
			context.getCore().getLogger().reportError(e);
			throw new ExecutionException(e, "Failed in operation at " + QUALIFIED_NAME); // i18n
		} catch (CsvException e) {
			context.getCore().getLogger().reportError(e);
			throw new ExecutionException(e, "Failed in operation at " + QUALIFIED_NAME); // i18n
		} catch (IOException e) {
			context.getCore().getLogger().reportError(e);
			throw new ExecutionException(e, "Failed in operation at " + QUALIFIED_NAME); // i18n
		} catch (Throwable e) {
			context.getCore().getLogger().reportError(e);
			throw new ExecutionException(e, "Failed in operation at " + QUALIFIED_NAME); // i18n
		}
		finally{
			if(stream != null){
				try {
					stream.close();
				} catch (IOException e) {
					context.getCore().getLogger().reportError(e);
				}
			}
			
			con.close();
		}
		
		
		return null;
	}
	
	
	private ColumnList getColumnList(CsvRecord record)
	{
		ColumnList columnList = new ColumnList();
		
		
		Iterator<String> it =  record.iterator();
		while(it.hasNext()){
			String colName = it.next();
			
			ColumnIdentifier colId = new ColumnIdentifier();
			colId.setColumnName(colName);
			
			columnList.addColumns(colId);
		}
		
		return columnList;
	}
	
	private void insertRecord(AlinousCore core, DataSrcConnection con, String table, ColumnList colList, CsvRecord record, DataTable dataTable, PostContext context)
			throws ExecutionException, DataSourceException, RedirectRequestException
	{
		ArrayList<VariableList> valueListsList = new ArrayList<VariableList>();
		VariableList valList = new VariableList();
		valueListsList.add(valList);
		
		VariableRepository valrepo = new VariableRepository();
		
		int colSize = colList.size();
		
		int i = 0;
		Iterator<String> it = record.iterator();
		while(it.hasNext()){
			String value = it.next();
			
			// type check
			//DataField fld = con.getDataTable(table).getField(i++);
			String fieldName = colList.getSelectColumnElement(i++).getColumnName().extract(null, null, null, null, null);
			DataField fld = dataTable.getDataField(fieldName);
			
			//AlinousDebug.debugOut("fld inst : " + fld + " fldName by value: " + value);
			//AlinousDebug.debugOut("fld : " + fld.getName() + " : " + fld.getType());
			
			if(fld == null || fld.getType() == null){
				
				AlinousDebug.debugOut(core, "Warning column : " + colList.getSelectColumnElement(i-1).getColumnName().extract(null, null, null, null, null));
				continue;
			}
			/*
			if((fld.getType().equals(DataField.TYPE_TIMESTAMP) ||
					fld.getType().equals(DataField.TYPE_TIME) ||
					fld.getType().equals(DataField.TYPE_INTEGER)
					)
					&& (value == null || value.equals(""))
					){
				SQLVariable val = new SQLVariable();
				val.setPrefix("$");
				PathElement pathElement = new PathElement(fieldName);
				val.setPathElement(pathElement);
				
				ScriptDomVariable domVal = new ScriptDomVariable(fieldName);
				domVal.setValueType(IScriptVariable.TYPE_NULL);
				
				valrepo.putValue(domVal);
				valList.addValues(val);
				continue;
			}*/
					
			
			SQLVariable val = new SQLVariable();
			val.setPrefix("$");
			PathElement pathElement = new PathElement(fieldName);
			val.setPathElement(pathElement);
			
			ScriptDomVariable domVal = new ScriptDomVariable(fieldName);
			domVal.setValueType(dataFldType2ScriptType(fld.getType(), value));
			domVal.setValue(nullckeck(fld.getType(), value));
			
			valrepo.putValue(domVal, context);
			valList.addValues(val);
			
			if(colSize == i){
				break;
			}
			
		}
		
		con.setOutSql(true);
	//	con.begin();
		
		// precompile if possible
		InsertSentence insertSentence = new InsertSentence();
		
		context.getPrecompile().setSqlSentence(this.precompileBuffer.getSentence(table, insertSentence));
		con.insert(colList, valueListsList, table, context, valrepo);
		context.getPrecompile().setSqlSentence(null);
		
	//	con.commit();
	}
	
	private String nullckeck(String dataFldType, String value)
	{
		if(dataFldType.equals(DataField.TYPE_INTEGER) && value.equals("")){
			return null;
		}
		if(dataFldType.equals(DataField.TYPE_DOUBLE) && value.equals("")){
			return null;
		}
		if(dataFldType.equals(DataField.TYPE_TIMESTAMP) && value.equals("")){
			return null;
		}
		if(dataFldType.equals(DataField.TYPE_BOOLEAN) && value.equals("")){
			return null;
		}
		
		return value;
	}
	
	private String dataFldType2ScriptType(String dataFldType, String value)
	{
		if(value == null || value.equals("")){
			if(dataFldType.equals(DataField.TYPE_TIME)){
				return IScriptVariable.TYPE_NULL;
			}
			else if(dataFldType.equals(DataField.TYPE_INTEGER)){
				return IScriptVariable.TYPE_NULL;
			}
			else if(dataFldType.equals(DataField.TYPE_DOUBLE)){
				return IScriptVariable.TYPE_NULL;
			}
			else if(dataFldType.equals(DataField.TYPE_BOOLEAN)){
				return IScriptVariable.TYPE_NULL;
			}
			else if(dataFldType.equals(DataField.TYPE_TIMESTAMP)){
				return IScriptVariable.TYPE_NULL;
			}
		}
		
		
		if(dataFldType.equals(DataField.TYPE_TIME)){
			return IScriptVariable.TYPE_STRING;
		}			
		else if(dataFldType.equals(DataField.TYPE_INTEGER)) {
			return IScriptVariable.TYPE_NUMBER;
		}
		else if(dataFldType.equals(DataField.TYPE_DOUBLE)) {
			return IScriptVariable.TYPE_DOUBLE;
		}
		else if(dataFldType.equals(DataField.TYPE_BOOLEAN)) {
			return IScriptVariable.TYPE_BOOLEAN;
		}
		else if(dataFldType.equals(DataField.TYPE_TIMESTAMP)) {
			return IScriptVariable.TYPE_TIMESTAMP;
		}
		
		return IScriptVariable.TYPE_STRING;
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
		return "Sql.loadCsv($dataSrc, $table, $fileName)";
	}

	@Override
	public String descriptionString() {
		return "Load the csv file stored with Sql.backup()";
	}

}

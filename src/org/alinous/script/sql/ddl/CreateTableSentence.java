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
package org.alinous.script.sql.ddl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.datasrc.types.DataField;
import org.alinous.datasrc.types.DataTable;
import org.alinous.datasrc.types.TypeHelper;
import org.alinous.debug.StepInCandidates;
import org.alinous.exec.IExecutable;
import org.alinous.exec.ScriptCheckContext;
import org.alinous.exec.ScriptError;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.script.AlinousScript;
import org.alinous.script.ISQLSentence;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.adjustopt.AdjustSet;
import org.alinous.script.sql.adjustopt.AdjustWhere;
import org.alinous.script.sql.other.TableIdentifier;
import org.alinous.script.sql.statement.ISQLStatement;
import org.alinous.script.statement.FunctionCall;
import org.alinous.test.coverage.FileCoverage;
import org.alinous.test.coverage.LineCoverage;
import org.jdom.Element;

public class CreateTableSentence implements ISQLSentence
{
	private int line;
	private int linePosition;
	private String filePath;
	
	private TableIdentifier table;
	private List<DdlColumnDescriptor> columnsList = new ArrayList<DdlColumnDescriptor>();
	private PrimaryKeys keys;
	private List<Unique> unique = new ArrayList<Unique>();
	private List<CheckDefinition> check = new ArrayList<CheckDefinition>();
	private List<ForeignKey> foreignKey = new ArrayList<ForeignKey>();
	
	public String getFilePath()
	{
		return this.filePath;
	}

	public IScriptVariable getReturnedVariable(PostContext context)
	{
		return context.getReturnedVariable(this);
	}

	public StepInCandidates getStepInCandidates()
	{
		return null;
	}

	public void setFilePath(String filePath)
	{
		this.filePath = filePath;		
	}

	public boolean execute(PostContext context, VariableRepository valRepo) throws ExecutionException
	{
		context.getCore().reporttExecuted(this);
		
		String dataSrc = context.getDataSrc();
		if(dataSrc == null){
			dataSrc = context.getCore().getConfig().getSystemRepositoryConfig().getDefaultSrc();
		}
		
		DataSrcConnection con = null;
		try {
			con = context.getUnit().getConnectionManager().connect(dataSrc, context);
		} catch (DataSourceException e) {
			throw new ExecutionException(e, e.getMessage());
		}
		
		String tableName = this.table.extract(context, valRepo, null, null, con.getTypeHelper());
		
		DataTable dataTable = new DataTable(tableName);
		
		Iterator<DdlColumnDescriptor> it = this.columnsList.iterator();
		while(it.hasNext()){
			DdlColumnDescriptor columnDesc = it.next();
			
			String typeNameBefore = columnDesc.getTypeDescriptor().getTypeName().toLowerCase();
			
			// SQL Type to Alinous-Core SQL Type
			String type = null;
			if(typeNameBefore.equals("varchar") ||  typeNameBefore.equals("nvarchar") ||
					typeNameBefore.equals("char")){
				type = DataField.TYPE_STRING;
			}
			else if(typeNameBefore.equals("text")){
				type = DataField.TYPE_TEXT_STRING;
			}
			else if(typeNameBefore.equals("int")){
				type = DataField.TYPE_INTEGER;
			}
			else if(typeNameBefore.equals("double") || typeNameBefore.equals("real")){
				type = DataField.TYPE_DOUBLE;
			}
			else if(typeNameBefore.equals("timestamp") || typeNameBefore.equals("datetime")){
				type = DataField.TYPE_TIMESTAMP;
			}
			
			// Add column
			String columnName = columnDesc.getName();
			boolean isKey = isPrimaryKey(columnName);
			
			if(columnDesc.getTypeDescriptor().hasLength(context, valRepo)){
				ISQLStatement lengthVal = columnDesc.getTypeDescriptor().getLength();
				String strLen = lengthVal.extract(context, valRepo, null, null, con.getTypeHelper());
				
				dataTable.addField(columnName, type, isKey, Integer.parseInt(strLen));
				
			}else{
				dataTable.addField(columnName, type, isKey);
			}
			
			
			// Unique and not null
			dataTable.getDataField(columnName).setNotnull(columnDesc.isNotnull());
			dataTable.getDataField(columnName).setUnique(columnDesc.isUnique());
			
			// check
			dataTable.getDataField(columnName).setCheck(columnDesc.getCheck());
			
			// set Default value
			if(columnDesc.getDefaultValue() != null){
				String strDefault = columnDesc.getDefaultValue().extract(context, valRepo, null, null, con.getTypeHelper());
				boolean quoted = false;
				
				if(strDefault.startsWith("'") && strDefault.endsWith("'")){
					strDefault = strDefault.substring(1, strDefault.length() - 1);
					quoted = true;
				}
				if(strDefault.startsWith("\"") && strDefault.endsWith("\"")){
					strDefault = strDefault.substring(1, strDefault.length() - 1);
					quoted = true;
				}
				
				dataTable.setDefaultValue(columnName, strDefault, quoted);
			}
		}
		
		// unique, check, foreign key
		dataTable.setUnique(this.unique);
		dataTable.setCheck(this.check);
		dataTable.setForeignKey(this.foreignKey);
		
		// primary keys with order
		Iterator<String> pkIt = this.keys.getKey().iterator();
		while(pkIt.hasNext()){
			String keyName = pkIt.next();
			dataTable.addPrimaryKey(keyName);
		}
		
		try {
			con.setOutSql(true);
			con.createTable(dataTable);
		} catch (DataSourceException e) {
			con.close();
			throw new ExecutionException(e, "Failed in creating table at line " + this.getLine());
		}finally{
			con.setOutSql(false);
		}
		
		
		
		return true;
	}
	
	private boolean isPrimaryKey(String key)
	{
		if(this.keys == null){
			return false;
		}
		
		return this.keys.isKey(key);
	}
	
	public void exportIntoJDomElement(Element parent) throws AlinousException
	{
		Element selectElement = new Element(IExecutable.TAG_EXECUTABLE);
		
		selectElement.setAttribute(IExecutable.ATTR_CLASS, this.getClass().getName());
		
		parent.addContent(selectElement);
	}

	public int getLine()
	{
		return this.line;
	}

	public void importFromJDomElement(Element threadElement) throws AlinousException
	{

	}

	public int getLinePosition()
	{
		return this.linePosition;
	}

	public void setLine(int line)
	{
		this.line = line;		
	}

	public void setLinePosition(int pos)
	{
		this.linePosition = pos;
	}

	public String extract(PostContext context, VariableRepository valRepo, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		return null;
	}

	public boolean isReady(PostContext context, VariableRepository valRepo, AdjustWhere adjustWhere) throws ExecutionException
	{
		if(this.table != null || this.table.isReady(context, valRepo, adjustWhere)){
			return false;
		}
		
		if(this.columnsList.isEmpty()){
			return false;
		}
		
		return true;
	}

	public TableIdentifier getTable()
	{
		return table;
	}

	public void setTable(TableIdentifier table)
	{
		this.table = table;
	}

	public void addColumnDescriptor(DdlColumnDescriptor col)
	{
		this.columnsList.add(col);
	}

	public PrimaryKeys getKeys()
	{
		return keys;
	}

	public void setKeys(PrimaryKeys keys)
	{
		this.keys = keys;
	}
	
	public boolean isPrecompilable()
	{
		return false;
	}
	public void setPrecompilable(boolean b)
	{
	}

	public List<Unique> getUnique()
	{
		return unique;
	}

	public void addUnique(Unique unique)
	{
		this.unique.add(unique);
	}

	public List<CheckDefinition> getCheck()
	{
		return check;
	}

	public void addCheck(CheckDefinition check)
	{
		this.check.add(check);
	}

	public List<ForeignKey> getForeignKey()
	{
		return foreignKey;
	}
	
	public void addForeignKey(ForeignKey key)
	{
		this.foreignKey.add(key);
	}

	public void checkStaticErrors(ScriptCheckContext scContext,
			List<ScriptError> errorList)
	{
		
	}

	public List<DdlColumnDescriptor> getColumnsList() {
		return columnsList;
	}

	@Override
	public void getFunctionCall(ScriptCheckContext scContext, List<FunctionCall> call, AlinousScript script)
	{
		
	}
	
	@Override
	public void setupCoverage(FileCoverage coverage)
	{
		LineCoverage lineCoverage = new LineCoverage(this.line, this.linePosition, this);
		coverage.addLineCoverage(lineCoverage);
		
	}
}

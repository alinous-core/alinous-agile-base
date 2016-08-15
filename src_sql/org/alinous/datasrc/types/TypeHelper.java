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
package org.alinous.datasrc.types;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.alinous.datasrc.IAlinousDataSource;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.exec.pages.PostContext;
import org.alinous.script.ISQLSentence;
import org.alinous.script.sql.BeginSentence;
import org.alinous.script.sql.CommitSentence;
import org.alinous.script.sql.DeleteSentence;
import org.alinous.script.sql.FromClause;
import org.alinous.script.sql.ISQLScriptObject;
import org.alinous.script.sql.InsertSentence;
import org.alinous.script.sql.JoinClause;
import org.alinous.script.sql.RollbackSentence;
import org.alinous.script.sql.SelectSentence;
import org.alinous.script.sql.UpdateSentence;
import org.alinous.script.sql.ddl.AlterTableSentence;
import org.alinous.script.sql.ddl.CreateTableSentence;
import org.alinous.script.sql.ddl.DropTableSentence;
import org.alinous.script.sql.other.TableIdentifier;
import org.alinous.script.sql.other.TablesList;
public class TypeHelper
{
	protected IAlinousDataSource dataSource;
	protected ISQLSentence sentence;
	private MetadataBag metadataBag; 
	
	protected boolean forceQuote = false;
	private String fieldType;
	
	private List<String> listTypes = new ArrayList<String>();
	private int listCounter = 0; // use this insert sentence
	private boolean disableCounter;
	
	public TypeHelper(IAlinousDataSource dataSource)
	{
		this.dataSource = dataSource;
		this.metadataBag = new MetadataBag(this.dataSource);
		this.disableCounter = true;
	}
	
	public String getDataFieldType(String columnName, PostContext context) throws DataSourceException
	{
		if(columnName.indexOf(".") > 0){
			String tokens[] = columnName.split("\\.");
			String tableName = tokens[0];
			columnName = tokens[1];
			
			// resolving alias
			String alias = getAliasName(tableName);
			if(alias != null){
				tableName = alias;
			}
			
			return this.metadataBag.getFieldType(tableName, columnName, context);
		}
		
		String typeName = calcTableName(columnName, context);
		if(typeName != null){
			return typeName;
		}
		
		
		return DataField.TYPE_UNKNOWN;
	}
	
	private String getAliasName(String tableName)
	{
		if(this.sentence instanceof SelectSentence){
			FromClause from = ((SelectSentence)this.sentence).getFrom();
			return parseFromAlias(from, tableName);
		}
		
		return null;
	}
	
	private String parseFromAlias(FromClause from, String tableName)
	{
		TablesList tableList = from.getTableList();
		Iterator<ISQLScriptObject> it = tableList.iterator();
		while(it.hasNext()){
			ISQLScriptObject elObj = it.next();
			
			if(elObj instanceof TableIdentifier){
				TableIdentifier tblId = (TableIdentifier)elObj;
				
				if(tblId.getAsName() != null && tblId.getAsName().toUpperCase().equals(tableName.toUpperCase())){
					return tblId.getTableName();
				}
			}
			else if(elObj instanceof JoinClause){
				JoinClause join = (JoinClause)elObj;
				String alias = handleJoinClauseAlias(join, tableName);
				
				if(alias != null){
					return alias;
				}
			}
		}
		
		return null;		
	}
	
	private String handleJoinClauseAlias(JoinClause joinClause, String tableName)
	{
		ISQLScriptObject left = joinClause.getLeft();
		ISQLScriptObject right = joinClause.getRight();
		
		if(left instanceof TableIdentifier){
			TableIdentifier tableLeft = (TableIdentifier)left;
			
			if(tableLeft.getAsName() != null && tableLeft.getAsName().toUpperCase().equals(tableName.toUpperCase())){
				return tableLeft.getTableName();
			}
		}
		else if(left instanceof JoinClause){
			JoinClause joinLeft = (JoinClause)left;
			
			String alias = handleJoinClauseAlias(joinLeft, tableName);
			if(alias != null){
				return alias;
			}
		}
		
		if(right instanceof TableIdentifier){
			TableIdentifier tableRight = (TableIdentifier)right;
			
			if(tableRight.getAsName() != null && tableRight.getAsName().toUpperCase().equals(tableName.toUpperCase())){
				return tableRight.getTableName();
			}
		}
		else if(right instanceof JoinClause){
			JoinClause joinRight = (JoinClause)right;
			
			String alias = handleJoinClauseAlias(joinRight, tableName);
			if(alias != null){
				return alias;
			}
		}
		
		return null;
	}
	
	private String calcTableName(String columnName, PostContext context) throws DataSourceException
	{
		List<String> list = getCandidateTables(columnName);
		
		Iterator<String> it = list.iterator();
		while(it.hasNext()){
			String tableName = it.next();
			
			String typeName = this.metadataBag.getFieldType(tableName, columnName, context);
			if(typeName != null){
				return typeName;
			}
		}
		
		return null;
	}
	
	private List<String> getCandidateTables(String columnName)
	{
		if(this.sentence instanceof SelectSentence){
			FromClause from = ((SelectSentence)this.sentence).getFrom();
			return parseFromCandidate(from);
		}
		else if(this.sentence instanceof InsertSentence){
			TableIdentifier table = ((InsertSentence)this.sentence).getTbl();
			
			List<String> list = new ArrayList<String>();
			list.add(table.getTableName());
			return list;
		}
		else if(this.sentence instanceof DeleteSentence){
			FromClause from = ((DeleteSentence)this.sentence).getFrom();
			return parseFromCandidate(from);
		}
		else if(this.sentence instanceof UpdateSentence){
			TableIdentifier table = ((UpdateSentence)this.sentence).getTable();
			
			List<String> list = new ArrayList<String>();
			list.add(table.getTableName());
			return list;
		}
		else if(this.sentence instanceof BeginSentence){
			
		}
		else if(this.sentence instanceof CommitSentence){
			
		}
		else if(this.sentence instanceof RollbackSentence){
			
		}
		else if(this.sentence instanceof DropTableSentence){
			
		}
		else if(this.sentence instanceof CreateTableSentence){
			
		}
		else if(this.sentence instanceof AlterTableSentence){
			
		}
		
		return null;
	}
	
	private List<String> parseFromCandidate(FromClause from)
	{
		List<String> list = new ArrayList<String>();
		
		TablesList tableList = from.getTableList();
		Iterator<ISQLScriptObject> it = tableList.iterator();
		while(it.hasNext()){
			ISQLScriptObject elObj = it.next();
			
			if(elObj instanceof TableIdentifier){
				handleTableIndentifier((TableIdentifier)elObj, list);
			}
			else if(elObj instanceof JoinClause){
				handleJoinClauseCandidate((JoinClause)elObj, list);
			}
		}
		
		return list;
	}
	
	private void handleJoinClauseCandidate(JoinClause joinClause, List<String> list)
	{
		ISQLScriptObject left = joinClause.getLeft();
		ISQLScriptObject right = joinClause.getRight();
		
		if(left instanceof TableIdentifier){
			TableIdentifier tableLeft = (TableIdentifier)left;
			list.add(tableLeft.getTableName());
		}
		else if(left instanceof JoinClause){
			JoinClause joinLeft = (JoinClause)left;
			
			handleJoinClauseCandidate(joinLeft, list);
		}
		
		if(right instanceof TableIdentifier){
			TableIdentifier tableRight = (TableIdentifier)right;
			list.add(tableRight.getTableName());
		}
		else if(right instanceof JoinClause){
			JoinClause joinRight = (JoinClause)right;
			
			handleJoinClauseCandidate(joinRight, list);
		}
	}
	
	
	
	private void handleTableIndentifier(TableIdentifier tableId, List<String> list)
	{
		list.add(tableId.getTableName());
	}
	
	public boolean isQuoted()
	{
		if(this.forceQuote){
			return true;
		}
		
		if(this.fieldType == null){
			return true;
		}
		
		if(this.fieldType.equals(DataField.TYPE_STRING) ||
				this.fieldType.equals(DataField.TYPE_TEXT_STRING) ||
				this.fieldType.equals(DataField.TYPE_DATE) ||
				this.fieldType.equals(DataField.TYPE_TIMESTAMP)){
			return true;
		}if(this.fieldType.equals(DataField.TYPE_UNKNOWN)){
			return true;
		}
		
		return false;
	}

	public void setForceQuote(boolean forceQuote)
	{
		this.forceQuote = forceQuote;
	}
	
	public TypeHelper newHelper(boolean forceQuote)
	{
		return newHelper(forceQuote, null);
	}
	
	public TypeHelper newHelper(boolean forceQuote, ISQLSentence sentence)
	{
		TypeHelper helper = new TypeHelper(this.dataSource);
		
		if(sentence == null){
			helper.sentence = this.sentence;
		}else{
			helper.sentence = sentence;
		}
		
		helper.fieldType = this.fieldType;
		helper.disableCounter = this.disableCounter;
		
		helper.setForceQuote(forceQuote);
		
		return helper;
	}

	public String getFieldType()
	{
		return fieldType;
	}

	public void setFieldType(String fieldType)
	{
		this.fieldType = fieldType;
	}
	
	public void addListType(String type)
	{
		this.listTypes.add(type);
	}
	
	public void incCount()
	{
		if(this.disableCounter){
			return;
		}
		this.listCounter = this.listCounter + 1;
	}
	
	public void resetCount()
	{
		this.listCounter = 0;
	}
	
	public String getCurrentListType()
	{
		if(this.disableCounter){
			return this.fieldType;
		}
		
		return this.listTypes.get(this.listCounter);
	}

	public IAlinousDataSource getDataSource()
	{
		return dataSource;
	}

	public void setDataSource(IAlinousDataSource dataSource)
	{
		this.dataSource = dataSource;
	}

	public boolean isDisableCounter()
	{
		return disableCounter;
	}

	public void setDisableCounter(boolean disableCounter)
	{
		this.disableCounter = disableCounter;
	}
	
	
	
}


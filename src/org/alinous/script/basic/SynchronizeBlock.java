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
package org.alinous.script.basic;

import java.sql.Connection;
import java.util.Iterator;
import java.util.List;

import org.alinous.AlinousDebug;
import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.datasrc.types.Record;
import org.alinous.debug.StepInCandidates;
import org.alinous.exec.ScriptCheckContext;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.repository.AlinousSystemRepository;
import org.alinous.script.AlinousScript;
import org.alinous.script.IScriptSentence;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.FromClause;
import org.alinous.script.sql.InsertSentence;
import org.alinous.script.sql.SelectColumns;
import org.alinous.script.sql.SelectSentence;
import org.alinous.script.sql.SetClause;
import org.alinous.script.sql.UpdateSentence;
import org.alinous.script.sql.WhereClause;
import org.alinous.script.sql.condition.TwoClauseExpression;
import org.alinous.script.sql.lock.ForUpdateClause;
import org.alinous.script.sql.other.ColumnList;
import org.alinous.script.sql.other.SetPair;
import org.alinous.script.sql.other.TableIdentifier;
import org.alinous.script.sql.other.TablesList;
import org.alinous.script.sql.other.VariableList;
import org.alinous.script.sql.statement.ColumnIdentifier;
import org.alinous.script.sql.statement.Identifier;
import org.alinous.script.sql.statement.SQLFunctionCallStatement;
import org.alinous.script.sql.statement.SQLVariable;
import org.alinous.script.statement.FunctionCall;
import org.jdom.Element;

public class SynchronizeBlock extends AbstractScriptBlock
{
	public static final String BLOCK_NAME = "SynchronizedBlock";
	
	private IStatement lockTargetStatement;
	
	private SelectSentence selectGlobalLock;
	private SelectSentence selectLock4Update;
	
	private InsertSentence insertLockSentence;
	private UpdateSentence updateUsedTimeSentence;
	
	private IPathElement lockVariableIdPath;
	
	public SynchronizeBlock()
	{
		this.lockVariableIdPath = PathElementFactory.buildPathElement("LOCK_ID");
	}
	
	public String getName()
	{
		return BLOCK_NAME;
	}

	public StepInCandidates getStepInCandidates()
	{
		StepInCandidates candidates = new StepInCandidates();
		return candidates;
	}

	public boolean execute(PostContext context, VariableRepository valRepo)
			throws ExecutionException, RedirectRequestException
	{
		if(this.lockTargetStatement == null){
			return handleJavaSynchronize(context, valRepo);
		}
		
		IScriptVariable lockDom = this.lockTargetStatement.executeStatement(context, valRepo);
		ScriptDomVariable val = validateStatementResult(lockDom);
		
		String lockTargetId = val.getValue();
		String systemDataSource = context.getCore().getConfig().getSystemRepositoryConfig().getSystemSrc();
		
		DataSrcConnection con = null;
		
		VariableRepository syncValRepo = new VariableRepository();
		ScriptDomVariable lockIdDom = new ScriptDomVariable("LOCK_ID");
		lockIdDom.setValue(lockTargetId);
		lockIdDom.setValueType(IScriptVariable.TYPE_STRING);
		syncValRepo.putValue(this.lockVariableIdPath, lockIdDom, context);
		
		PostContext myPostContext = new PostContext(context.getCore(), context.getUnit());
		myPostContext.setDataSrc(systemDataSource);
		
		
		try {
			con = context.getCore().getDataSourceManager().connect(systemDataSource, myPostContext);
			myPostContext.setCurrentDataSrcConnection(con);
			
			// lock init check
			con.begin(Connection.TRANSACTION_READ_COMMITTED);
			
			List<Record> rec = selectLock4Update(con, myPostContext, syncValRepo);
			
			if(rec.isEmpty()){
				selectLockGlobal(con, myPostContext, syncValRepo);
				
				rec = selectLock4Update(con, myPostContext, syncValRepo);
				if(rec.isEmpty()){
					insertLockRecord(con, myPostContext, syncValRepo);
				}
				
				con.commit(null);
				
				con.begin(Connection.TRANSACTION_READ_COMMITTED);
				rec = selectLock4Update(con, myPostContext, syncValRepo);
			}
		} catch (Throwable e) {
			context.getCore().getLogger().reportError(e);
			
			if(con != null){
				con.close();
			}
			
			myPostContext.dispose();
			
			ExecutionException ex = new ExecutionException("Failed to access lock database");
			ex.addStackTrace(filePath, line);
			throw ex;
		}
		
		boolean execResult = false;
		
		try {
			execResult = executeBlock(context, valRepo);
		} catch (Throwable e) {
			context.getCore().getLogger().reportError(e);
			
			ExecutionException ex = new ExecutionException("Failed to access lock database");
			ex.addStackTrace(filePath, line);
			throw ex;
		}finally{
			try {
				updateUsedTime(con, myPostContext, syncValRepo);
				con.commit(null);
			} catch (DataSourceException e) {
				context.getCore().getLogger().reportError(e);
			}			
			
			if(con != null){
				con.close();
			}
			myPostContext.dispose();
		}
		
		return execResult;
	}
	
	private void updateUsedTime(DataSrcConnection con, PostContext context, VariableRepository valRepo) throws ExecutionException, DataSourceException
	{
		if(this.updateUsedTimeSentence == null){
			this.updateUsedTimeSentence = new UpdateSentence();
			
			// Set
			SetClause setClause = new SetClause();
			SetPair p = new SetPair();
			p.setColumn(new ColumnIdentifier(null, AlinousSystemRepository.LOCK_TABLE_USED));
			SQLFunctionCallStatement nowFunc = new SQLFunctionCallStatement();
			nowFunc.setName("now");
			p.setValue(nowFunc);
			
			setClause.addSet(p);
			this.updateUsedTimeSentence.setSet(setClause);
			
			// Where
			WhereClause where = new WhereClause();
			TwoClauseExpression eqExp = new TwoClauseExpression();
			Identifier id = new Identifier(AlinousSystemRepository.LOCK_TABLE_ID);
			eqExp.setLeft(id);
			
			eqExp.setOpe("=");
			
			SQLVariable sqlVal = new SQLVariable();
			sqlVal.setPrefix("$");
			sqlVal.setPathElement(this.lockVariableIdPath);
			eqExp.setRight(sqlVal);
			
			where.setExpression(eqExp);
			this.updateUsedTimeSentence.setWhere(where);
		}
		
		context.getPrecompile().setSqlSentence(this.updateUsedTimeSentence);
		con.update(AlinousSystemRepository.LOCK_TABLE, this.updateUsedTimeSentence.getSet(), this.updateUsedTimeSentence.getWhere(), context, valRepo, null, null);
		context.getPrecompile().setSqlSentence(null);
	}
	
	private void insertLockRecord(DataSrcConnection con, PostContext context, VariableRepository valRepo) throws ExecutionException, DataSourceException
	{
		if(this.insertLockSentence == null){
			this.insertLockSentence = new InsertSentence();
			
			ColumnList cols = new ColumnList();
			cols.addColumns(new ColumnIdentifier(null, AlinousSystemRepository.LOCK_TABLE_ID));
			cols.addColumns(new ColumnIdentifier(null, AlinousSystemRepository.LOCK_TABLE_USED));
			this.insertLockSentence.setCols(cols);
			
			VariableList valueList = new VariableList();
			
			SQLVariable sqlVal = new SQLVariable();
			sqlVal.setPrefix("$");
			sqlVal.setPathElement(this.lockVariableIdPath);
			valueList.addValues(sqlVal);
			
			SQLFunctionCallStatement nowFunc = new SQLFunctionCallStatement();
			nowFunc.setName("now");
			valueList.addValues(nowFunc);
			
			this.insertLockSentence.addValues(valueList);
		}
		
		context.getPrecompile().setSqlSentence(this.insertLockSentence);
		con.insert(this.insertLockSentence.getCols(), this.insertLockSentence.getValueLists(), AlinousSystemRepository.LOCK_TABLE
				, context, valRepo);
		context.getPrecompile().setSqlSentence(null);
	};
	
	private List<Record> selectLock4Update(DataSrcConnection con, PostContext context, VariableRepository valRepo) throws ExecutionException, DataSourceException
	{
		if(this.selectLock4Update == null){
			this.selectLock4Update = new SelectSentence();
			// *
			SelectColumns columns = new SelectColumns();
			this.selectLock4Update.setColumns(columns);
			
			// FROM
			FromClause from = new FromClause();
			TablesList tableList = new TablesList();
			TableIdentifier table = new TableIdentifier();
			table.setTableName(AlinousSystemRepository.LOCK_TABLE);
			tableList.addTable(table);
			from.setTableList(tableList);
			this.selectLock4Update.setFrom(from);
			
			// Where
			WhereClause where = new WhereClause();
			TwoClauseExpression eqExp = new TwoClauseExpression();
			Identifier id = new Identifier(AlinousSystemRepository.LOCK_TABLE_ID);
			eqExp.setLeft(id);
			
			eqExp.setOpe("=");
			
			SQLVariable sqlVal = new SQLVariable();
			sqlVal.setPrefix("$");
			sqlVal.setPathElement(this.lockVariableIdPath);
			eqExp.setRight(sqlVal);
			
			where.setExpression(eqExp);
			this.selectLock4Update.setWhere(where);
			
			// FOR UPDATE
			ForUpdateClause forUpdate = new ForUpdateClause();
			this.selectLock4Update.setForUpdate(forUpdate);
	
		}
		
		context.getPrecompile().setSqlSentence(this.selectLock4Update);
		
		con.setOutSql(true);
		List<Record> record = con.select(null, this.selectLock4Update.getColumns(), this.selectLock4Update.getFrom(), 
				this.selectLock4Update.getWhere(), null, null, null, this.selectLock4Update.getForUpdate(), context, valRepo, null);
		
		context.getPrecompile().setSqlSentence(null);
		
		return record;
	}
	
	
	private void selectLockGlobal(DataSrcConnection con, PostContext context, VariableRepository valRepo) throws ExecutionException, DataSourceException
	{
		if(this.selectGlobalLock == null){
			this.selectGlobalLock = new SelectSentence();
			
			// *
			SelectColumns columns = new SelectColumns();
			this.selectGlobalLock.setColumns(columns);
			
			// FROM
			FromClause from = new FromClause();
			TablesList tableList = new TablesList();
			TableIdentifier table = new TableIdentifier();
			table.setTableName(AlinousSystemRepository.GLOBAL_LOCK_TABLE);
			tableList.addTable(table);
			from.setTableList(tableList);
			this.selectGlobalLock.setFrom(from);
			
			// FOR UPDATE
			ForUpdateClause forUpdate = new ForUpdateClause();
			this.selectGlobalLock.setForUpdate(forUpdate);
		}
		
		context.getPrecompile().setSqlSentence(selectGlobalLock);
		
		con.setOutSql(true);
		con.select(null, this.selectGlobalLock.getColumns(), this.selectGlobalLock.getFrom(), null, null, null, null, this.selectGlobalLock.getForUpdate(), context, valRepo, null);
		
		context.getPrecompile().setSqlSentence(null);
		
	}
	
	private boolean executeBlock(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		Iterator<IScriptSentence> it = this.sentences.iterator();
		while(it.hasNext()){
			IScriptSentence exec = it.next();
			
			boolean blRes = executeSentence(exec, context, valRepo);			
			
			if(!blRes){
				return false;
			}
		}
		
		return true;
	}
	
	private ScriptDomVariable validateStatementResult(IScriptVariable lockDom) throws ExecutionException
	{
		if(!(lockDom instanceof ScriptDomVariable)){
			ExecutionException e = new ExecutionException("Argument of the Synchronize Block is wrong");
			e.addStackTrace(filePath, line);
			throw e;
		}
		
		ScriptDomVariable val = (ScriptDomVariable)lockDom;
		if(val.getValue() == null || val.getType() == IScriptVariable.TYPE_NULL){
			ExecutionException e = new ExecutionException("Argument of the Synchronize Block can't be null");
			e.addStackTrace(filePath, line);
			throw e;
		}
		if(val.getValue().equals("")){
			ExecutionException e = new ExecutionException("Argument of the Synchronize Block can't be blank string");
			e.addStackTrace(filePath, line);
			throw e;
		}
		
		return val;
	}
	
	private boolean handleJavaSynchronize(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		AlinousDebug.debugOut(context.getCore(), "SynchronizedBlock lock -> " + this.filePath + " line : " + this.line);
		synchronized (this) {
			Iterator<IScriptSentence> it = this.sentences.iterator();
			while(it.hasNext()){
				IScriptSentence exec = it.next();
				
				boolean blRes = executeSentence(exec, context, valRepo);			
				
				if(!blRes){
					return false;
				}
			}
		}
		return true;
	}

	public void exportIntoJDomElement(Element parent) throws AlinousException
	{
		
	}

	public void importFromJDomElement(Element threadElement)
			throws AlinousException
	{
	}

	@Override
	public void getFunctionCall(ScriptCheckContext scContext,
			List<FunctionCall> call, AlinousScript script)
	{
		super.getFunctionCall(scContext, call, script);
	}

	public IStatement getLockTargetStatement() {
		return lockTargetStatement;
	}

	public void setLockTargetStatement(IStatement lockTargetStatement) {
		this.lockTargetStatement = lockTargetStatement;
	}
	
	
}

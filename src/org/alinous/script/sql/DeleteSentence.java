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
package org.alinous.script.sql;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.alinous.AlinousCore;
import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.datasrc.types.TypeHelper;
import org.alinous.debug.StepInCandidates;
import org.alinous.exec.IExecutable;
import org.alinous.exec.ScriptCheckContext;
import org.alinous.exec.ScriptError;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.script.AlinousScript;
import org.alinous.script.ISQLSentence;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.adjustopt.AdjustSet;
import org.alinous.script.sql.adjustopt.AdjustWhere;
import org.alinous.script.sql.condition.ISQLExpression;
import org.alinous.script.sql.other.TableIdentifier;
import org.alinous.script.sql.other.TablesList;
import org.alinous.script.statement.FunctionCall;
import org.alinous.sql.config.SqlConfig;
import org.alinous.sql.config.SqlConstrainConfig;
import org.alinous.sql.config.SqlConstrainConfigParam;
import org.alinous.test.coverage.FileCoverage;
import org.alinous.test.coverage.LineCoverage;
import org.jdom.Element;

public class DeleteSentence implements ISQLSentence
{
	private FromClause from = null;
	private WhereClause where = null;
	private AdjustWhere adjustWhere = null;
	
	private int line;
	private int linePosition;
	private String filePath;
	
	private boolean precompilable = true;
	private boolean precompilableChecked = false;
	
	public DeleteSentence()
	{
		this.adjustWhere = new AdjustWhere();
	}

	public FromClause getFrom() {
		return from;
	}
	public void setFrom(FromClause from) {
		this.from = from;
	}
	public WhereClause getWhere() {
		return where;
	}
	public void setWhere(WhereClause where) {
		this.where = where;
	}

	public AdjustWhere getAdjustWhere()
	{
		return adjustWhere;
	}
	public void setAdjustWhere(AdjustWhere adjustWhere)
	{
		this.adjustWhere = adjustWhere;
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
			throw new ExecutionException(e, "");
		} catch(Throwable e){
			throw new ExecutionException(e, "execute delete failed on getting connection");
		}
		
		TypeHelper newHelper = con.getTypeHelper().newHelper(false, this);
		try {
			if(AlinousCore.debug(context)){
				con.setOutSql(true);
			}
			
			context.getPrecompile().setSqlSentence(this);
			
			// FROM has variable or not
			if(!this.precompilableChecked){
				context.getPrecompile().clear();
				context.getPrecompile().clearStatus();
				context.getPrecompile().setCompile(true);
				
				from.extract(context, valRepo, this.adjustWhere, null, newHelper);
				if(context.getPrecompile().isMarkVariable()){
					this.precompilable = false;
				}
				if(where != null){
					where.extract(context, valRepo, this.adjustWhere, null, newHelper);
					if(context.getPrecompile().isHasArray()){
						this.precompilable = false;
					}
				}
				
				if(!this.isPrecompilable()){
					context.getPrecompile().clear();
				}
				
				context.getPrecompile().setCompile(false);
				this.precompilableChecked  = true;
			}
			
			con.delete(from.extract(context, valRepo, this.adjustWhere, null, newHelper), this.where, context, valRepo, this.adjustWhere);
			context.getPrecompile().setSqlSentence(null);
			con.setOutSql(false);
		} catch (DataSourceException e) {
			con.close();
			throw new ExecutionException(e, "");
		} catch(Throwable e){
			throw new ExecutionException(e, "execute delete failed");
		}
		
		return true;
	}
	
	public int getLine() {
		return line;
	}
	public void setLine(int line) {
		this.line = line;
	}
	public int getLinePosition() {
		return linePosition;
	}
	public void setLinePosition(int linePosition) {
		this.linePosition = linePosition;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public void exportIntoJDomElement(Element parent)
	{
		Element selectElement = new Element(IExecutable.TAG_EXECUTABLE);
		
		selectElement.setAttribute(IExecutable.ATTR_CLASS, this.getClass().getName());
		
		parent.addContent(selectElement);
		
	}
	public void importFromJDomElement(Element threadElement)
	{
		
	}
	
	public StepInCandidates getStepInCandidates()
	{
		return null;
	}
	public IScriptVariable getReturnedVariable(PostContext context)
	{
		return context.getReturnedVariable(this);
	}

	public String extract(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		return null;
	}
	public boolean isReady(PostContext context, VariableRepository providor, AdjustWhere adjustWhere) throws ExecutionException
	{
		return false;
	}

	public boolean isPrecompilable()
	{
		return precompilable;
	}

	public void setPrecompilable(boolean b)
	{
		this.precompilable = b;
	}

	public void checkStaticErrors(ScriptCheckContext scContext,
			List<ScriptError> errorList)
	{
		if(scContext.isSkipOnce()){
			scContext.setSkipOnce(false);
			return;
		}
		
		// gather tables to access
		LinkedList<String> table2use = new LinkedList<String>();
		LinkedList<ISQLScriptObject> joinConditions = new LinkedList<ISQLScriptObject>();
		
		TablesList tblist= this.from.getTableList();
		if(tblist != null){
			Iterator<ISQLScriptObject> it = tblist.iterator();
			while(it.hasNext()){
				ISQLScriptObject obj = it.next();
				
				gatherTables(obj, table2use, joinConditions);
			}
		}
		else{
			// sub query
			if(this.from.getSelectSentence() != null){
				this.from.getSelectSentence().checkStaticErrors(scContext, errorList);
			}
		}
		
		// use table or not
		List<DmlCheckListElement> checkList = useErrcheckTable(scContext, table2use);
		if(checkList.isEmpty()){
			return;
		}
		
		// condition check
		//checkJoinCondition(checkList, joinConditions, table2use.get(0));
		
		// where condition
		checkWhereCondition(checkList, where, table2use.get(0));
		
		// uncomment here after escaping chack. report
		reportError(checkList, errorList);
	}
	
	public void reportError(List<DmlCheckListElement> checkList, List<ScriptError> errorList)
	{
		Iterator<DmlCheckListElement> it = checkList.iterator();
		while(it.hasNext()){
			DmlCheckListElement checkElement = it.next();
			
			ScriptError error = new ScriptError();
			error.setScriptElement(this);
			error.setMessage("error");
			//error.setConstrainType(checkElement.getConstrain());
			//error.setColumnName(checkElement.getColumnName());
			error.setTableName(checkElement.getTableName());
			
			errorList.add(error);
		}
	}
	
	private void checkWhereCondition(List<DmlCheckListElement> checkList, WhereClause where, String defaultTable)
	{
		if(where == null){
			return;
		}
		ISQLExpression exp = where.getExpression();
		ExpressionChecker.checkoutSqlExpression(checkList, exp, defaultTable);
	}
	
	private void gatherTables(ISQLScriptObject obj, LinkedList<String> table2use, LinkedList<ISQLScriptObject> joinConditions)
	{
		if(obj instanceof TableIdentifier){
			TableIdentifier tid = (TableIdentifier)obj;
			table2use.add(tid.getTableName());
		}
		else if(obj instanceof JoinClause){
			JoinClause jin = (JoinClause)obj;
			
			gatherJoinClause(jin, table2use, joinConditions);
		}
	}
	
	private List<DmlCheckListElement> useErrcheckTable(ScriptCheckContext scContext, LinkedList<String> table2use)
	{
		List<DmlCheckListElement> checktableList = new LinkedList<DmlCheckListElement>();
		
		SqlConfig config = scContext.getAlinousConfig().getSqlConfig();
		if(config == null){
			return checktableList;
		}
		
		Iterator<String> it = table2use.iterator();
		while(it.hasNext()){
			String tableName = it.next();
			
			Iterator<SqlConstrainConfig> confIt = config.getConstrains().iterator();
			while(confIt.hasNext()){
				SqlConstrainConfig cf = confIt.next();
				
				if(tableName == null || cf.getTableName() == null){
					continue;
				}
				
				if(cf.getTableName().toUpperCase().equals(tableName.toUpperCase())){
					DmlCheckListElement el = new DmlCheckListElement();
					el.setTableName(tableName);
					
					List<SqlConstrainConfigParam> paramList = cf.getParamList();
					Iterator<SqlConstrainConfigParam> paramIt = paramList.iterator();
					while(paramIt.hasNext()){
						SqlConstrainConfigParam p = paramIt.next();
						
						// params
						DmlCheckParam param = new DmlCheckParam();
						param.setConstrain(p.getChecktype());
						param.setColumnNames(p.getValue());
						
						el.addParam(param);
					}
					
					checktableList.add(el);
				}
			}
		}
		
		return checktableList;
	}
	
	
	private void gatherJoinClause(JoinClause joinClause, LinkedList<String> table2use, LinkedList<ISQLScriptObject> joinConditions)
	{
		joinConditions.add(joinClause.getCondition());
		
		if(joinClause.getLeft() instanceof TableIdentifier){
			TableIdentifier tid = (TableIdentifier)joinClause.getLeft();
			table2use.add(tid.getTableName());
		}
		else if(joinClause.getLeft() instanceof JoinClause){
			JoinClause jin = (JoinClause)joinClause.getLeft();
			
			gatherJoinClause(jin, table2use, joinConditions);
		}
		
		if(joinClause.getRight() instanceof TableIdentifier){
			TableIdentifier tid = (TableIdentifier)joinClause.getRight();
			table2use.add(tid.getTableName());
		}
		else if(joinClause.getRight() instanceof JoinClause){
			JoinClause jin = (JoinClause)joinClause.getRight();
			
			gatherJoinClause(jin, table2use, joinConditions);
		}
		else{
			return;
		}
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

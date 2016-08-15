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

import java.util.List;

import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
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
import org.alinous.script.statement.FunctionCall;
import org.alinous.test.coverage.FileCoverage;
import org.alinous.test.coverage.LineCoverage;
import org.jdom.Element;

public class AlterTableSentence implements ISQLSentence
{
	private int line;
	private int linePosition;
	private String filePath;
	
	private TableIdentifier table;
	private IAlterAction action;
	
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
			throw new ExecutionException(e, "");
		}
		
		try {
			con.alterTable(this.table, this.action, context, valRepo);
		} catch (DataSourceException e) {
			con.close();
			throw new ExecutionException(e, "Data srouce exception at line : " + this.line);
		}
		
		return true;
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
		return this.table.isReady(context, valRepo, adjustWhere) && this.action.isReady(context, valRepo, adjustWhere);
	}

	public TableIdentifier getTable()
	{
		return table;
	}

	public void setTable(TableIdentifier table)
	{
		this.table = table;
	}

	public IAlterAction getAction()
	{
		return action;
	}

	public void setAction(IAlterAction action)
	{
		this.action = action;
	}

	public boolean isPrecompilable()
	{
		return false;
	}
	public void setPrecompilable(boolean b)
	{
	}

	public void checkStaticErrors(ScriptCheckContext scContext,
			List<ScriptError> errorList)
	{
		
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

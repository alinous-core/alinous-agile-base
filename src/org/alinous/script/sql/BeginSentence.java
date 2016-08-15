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

import java.sql.Connection;
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
import org.alinous.script.sql.statement.Identifier;
import org.alinous.script.statement.FunctionCall;
import org.alinous.test.coverage.FileCoverage;
import org.alinous.test.coverage.LineCoverage;
import org.jdom.Element;

public class BeginSentence implements ISQLSentence
{
	private Identifier level1;
	private Identifier level2;
	
	private int line;
	private int linePosition;
	private String filePath;
	

	public boolean execute(PostContext context, VariableRepository valRepo) throws ExecutionException
	{
		context.getCore().reporttExecuted(this);
		
		String dataSrc = context.getDataSrc();
		if(dataSrc == null){
			dataSrc = context.getCore().getConfig().getSystemRepositoryConfig().getDefaultSrc();
		}
		
		// execute
		DataSrcConnection con = null;
		try {
			con = context.getUnit().getConnectionManager().connect(dataSrc, context);
		} catch (DataSourceException e) {
			throw new ExecutionException(e, "");
		} catch(Throwable e){
			throw new ExecutionException(e, "execute begin failed on getting connection");
		}
		
		// execute begin
		try {
			con.begin(getIsolationLevel());
		} catch (DataSourceException e) {
			con.close();
			throw new ExecutionException(e, "executing begin failed.");
		}catch(Throwable e){
			throw new ExecutionException(e, "execute begin failed");
		}
		
		return true;
	}

	public String getFilePath()
	{
		return this.filePath;
	}

	public IScriptVariable getReturnedVariable(PostContext context)
	{
		return null;
	}

	public StepInCandidates getStepInCandidates()
	{
		return null;
	}

	public void setFilePath(String filePath)
	{
		this.filePath = filePath;
		
	}

	public void exportIntoJDomElement(Element parent) throws AlinousException
	{
		Element beginElement = new Element(IExecutable.TAG_EXECUTABLE);
		
		beginElement.setAttribute(IExecutable.ATTR_CLASS, this.getClass().getName());
		
		parent.addContent(beginElement);
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

	public String extract(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
						AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		return null;
	}

	public boolean isReady(PostContext context, VariableRepository valRepo, AdjustWhere adjustWhere) throws ExecutionException
	{
		return false;
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

	public Identifier getLevel1() {
		return level1;
	}

	public void setLevel1(Identifier level1) {
		this.level1 = level1;
	}

	public Identifier getLevel2() {
		return level2;
	}

	public void setLevel2(Identifier level2) {
		this.level2 = level2;
	}
	
	public int getIsolationLevel() throws ExecutionException
	{
		if(this.level1 == null){
			return Connection.TRANSACTION_READ_COMMITTED;
		}
		
		 //ISOLATION LEVEL { SERIALIZABLE | REPEATABLE READ | READ COMMITTED | READ UNCOMMITTED }
		String strLevel1 = null;
		String strLevel2 = null;
		
		if(this.level1 != null){
			strLevel1 = this.level1.getName();
		}
		if(this.level2 != null){
			strLevel2 = this.level2.getName();
		}
		
		if(strLevel1 != null && strLevel1.toUpperCase().equals("SERIALIZABLE")){
			return Connection.TRANSACTION_SERIALIZABLE;
		}
		else if(strLevel1 != null && strLevel1.toUpperCase().equals("READ")){
			if(strLevel2 != null && strLevel2.toUpperCase().equals("COMMITTED")){
				return Connection.TRANSACTION_READ_COMMITTED;
			}else if(strLevel2 != null && strLevel2.toUpperCase().equals("UNCOMMITTED")){
				return Connection.TRANSACTION_READ_UNCOMMITTED;
			}
		}
		else if(strLevel1 != null && strLevel1.toUpperCase().equals("REPEATABLE")){
			return Connection.TRANSACTION_REPEATABLE_READ;
		}

		String levelString = "strLevel1 : " + strLevel1 + " strLevel2 : " + strLevel2;
		
		throw new ExecutionException("execute begin failed : " + levelString); 
		
	}
	
	
}

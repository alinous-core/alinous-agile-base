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
import org.alinous.expections.RedirectRequestException;
import org.alinous.plugin.xa.AlinousRegisterdGlobalId;
import org.alinous.script.AlinousScript;
import org.alinous.script.ISQLSentence;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.adjustopt.AdjustSet;
import org.alinous.script.sql.adjustopt.AdjustWhere;
import org.alinous.script.statement.FunctionCall;
import org.alinous.test.coverage.FileCoverage;
import org.alinous.test.coverage.LineCoverage;
import org.jdom.Element;

public class CommitSentence implements ISQLSentence
{
	private int line;
	private int linePosition;
	private String filePath;

	private IStatement trxIdentifier;
	
	public boolean execute(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		String strtrxIdentifier = null;
		
		context.getCore().reporttExecuted(this);
		
		if(this.trxIdentifier != null){
			IScriptVariable trxVariable = this.trxIdentifier.executeStatement(context, valRepo);
			if(trxVariable instanceof ScriptDomVariable){
				ScriptDomVariable domId = (ScriptDomVariable)trxVariable;
				strtrxIdentifier = domId.getValue();
				if(strtrxIdentifier == null){
					throw new ExecutionException("Global Transaction Id must not be null", this.filePath, this.line);
				}
			}
		}

		
		String dataSrc = context.getDataSrc();
		if(dataSrc == null){
			dataSrc = context.getCore().getConfig().getSystemRepositoryConfig().getDefaultSrc();
		}
		
		//
		//AlinousDebug.debugOut("context.getGlobalTrxIds() : " + context.showGlobalIdStatus());
		
		// execute
		DataSrcConnection con = null;
		try {
			con = context.getUnit().getConnectionManager().connect(dataSrc, context);
		} catch (DataSourceException e) {
			throw new ExecutionException(e, "");
		}catch(Throwable e){
			throw new ExecutionException(e, "execute commit failed on getting connection");
		}
		
		// execute commit
		try {
			con.commit(strtrxIdentifier);
		} catch (DataSourceException e) {
			con.close();
			throw new ExecutionException(e, "executing commit failed.");
		}catch(Throwable e){
			con.close();
			throw new ExecutionException(e, "execute commit failed");
		}
		
		// remove for detecting leak
		if(strtrxIdentifier != null){
			// register context
			//AlinousDebug.debugOut("remove registered trx : " + strtrxIdentifier);
			
			context.removeGlobalTrxId(new AlinousRegisterdGlobalId(context.getDataSrc(), strtrxIdentifier));
		}
		
		return true;
	}
	
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

	public void exportIntoJDomElement(Element parent) throws AlinousException
	{
		Element commitElement = new Element(IExecutable.TAG_EXECUTABLE);
		
		commitElement.setAttribute(IExecutable.ATTR_CLASS, this.getClass().getName());
		
		parent.addContent(commitElement);
	}

	public void importFromJDomElement(Element threadElement) throws AlinousException
	{
		
	}
	
	public int getLine()
	{
		return this.line;
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

	public IStatement getTrxIdentifier() {
		return trxIdentifier;
	}

	public void setTrxIdentifier(IStatement trxIdentifier) {
		this.trxIdentifier = trxIdentifier;
	}
	
}

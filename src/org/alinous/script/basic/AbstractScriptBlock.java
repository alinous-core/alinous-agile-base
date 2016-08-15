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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.alinous.AlinousCore;
import org.alinous.datasrc.AlinousDataSourceManager;
import org.alinous.debug.DebugThread;
import org.alinous.debug.StepInCandidates;
import org.alinous.debug.ThreadTerminatedException;
import org.alinous.exec.IExecutable;
import org.alinous.exec.ScriptCheckContext;
import org.alinous.exec.ScriptError;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.AlinousScript;
import org.alinous.script.ISQLSentence;
import org.alinous.script.IScriptBlock;
import org.alinous.script.IScriptSentence;
import org.alinous.script.functions.FunctionDeclaration;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.statement.FunctionCall;
import org.alinous.test.coverage.FileCoverage;

public abstract class AbstractScriptBlock implements IScriptBlock
{
	protected List<IScriptSentence> sentences = new LinkedList<IScriptSentence>();
	
	protected String filePath;
	protected int line;
	protected int linePosition;
	
	protected AlinousDataSourceManager dataSourceManager;
	
	public AbstractScriptBlock()
	{
		
	}
	
	//protected IScriptVariable returnedVariable;
	public AbstractScriptBlock(String filePath)
	{
		this.filePath = filePath;
		
		Iterator<IScriptSentence> it = this.sentences.iterator();
		while(it.hasNext()){
			IScriptSentence sent = it.next();
			sent.setFilePath(filePath);
		}
	}
	
	protected boolean executeSentence(IScriptSentence exec, PostContext context,
								VariableRepository valRepo) throws ExecutionException, RedirectRequestException, ThreadTerminatedException
	{
		// DEBUG:
		if(AlinousCore.debug(context) && !(exec instanceof DoWhileBlock)){	
			DebugThread thread = context.getCore().getAlinousDebugManager().getCurrentThread();
			if(thread != null){ // guard
				thread.getTopStackFrame().resetExecutedCandidate();
				
				StepInCandidates ca = exec.getStepInCandidates();
				thread.getTopStackFrame().setCurrentCandidate(ca);
				
				context.getCore().getAlinousDebugManager().aboutToExecuteSentence(this, exec, context);
			}
		}
		
		boolean blResult = true;
		
		if(exec instanceof ISQLSentence){
			try{
				blResult = executeSQLSentence(exec, context, valRepo);
			}catch(ExecutionException e){
				e.addStackTrace(exec.getFilePath(), exec.getLine());
				throw e;
			}
			//this.lastSentence = exec;
			context.setLastSentence(exec);
		}
		else if(exec instanceof UseSentence){
			exec.execute(context, valRepo);
			
			UseSentence useSentence = (UseSentence)exec;
			String curDataSource = useSentence.getDataSource();
			context.setDataSrc(curDataSource);
			
			//this.lastSentence = exec;
			context.setLastSentence(exec);
		}
		else if(exec instanceof BreakSentence){
			//this.lastSentence = exec;
			exec.execute(context, valRepo);
			context.setLastSentence(exec);
		}
		else if(exec instanceof ContinueSentence){
			//this.lastSentence = exec;
			exec.execute(context, valRepo);
			context.setLastSentence(exec);
		}
		else if(exec instanceof AbstractScriptBlock){
			AbstractScriptBlock block = (AbstractScriptBlock)exec;			
			
			block.setDataSourceManager(this.dataSourceManager);
			try{
				blResult = block.execute(context, valRepo);
			}catch(ExecutionException e){
				//
				//AlinousDebug.debugOut("block : " + block + " filePath : " + block.filePath + " line : " + block.line);
				
				e.addStackTrace(block.filePath, block.line);
				throw e;
			}
			
			if(!(exec instanceof FunctionDeclaration)){
				//this.returnedVariable = ((FunctionDeclaration)exec.getReturnedVariable();

				context.setReturnedVariable(this, exec.getReturnedVariable(context));

				context.setLastSentence(((IScriptBlock)exec).getLastSentence(context));
			}
		}
		else if(exec instanceof ReturnSentence){
			blResult = false;
			
			try{
				exec.execute(context, valRepo);
			}catch(ExecutionException e){
				e.addStackTrace(exec.getFilePath(), exec.getLine());
				throw e;
			}	
			//this.returnedVariable = ((ReturnSentence)exec).getReturnedValue(context);
			context.setReturnedVariable(this, ((ReturnSentence)exec).getReturnedValue(context));
			
			context.setLastSentence(exec);
		}
		else if(exec instanceof DownloadSentence){
			blResult = false;
			try{
				exec.execute(context, valRepo);
			}catch(ExecutionException e){
				e.addStackTrace(exec.getFilePath(), exec.getLine());
				throw e;
			}
			//this.returnedVariable = ((DownloadSentence)exec).getReturnedVariable();
			context.setReturnedVariable(this, ((DownloadSentence)exec).getReturnedVariable(context));
			
			context.setLastSentence(exec);
		}
		else if(exec instanceof RedirectSentence){
			blResult = false;
			
			try{
				exec.execute(context, valRepo);
			}catch(ExecutionException e){
				e.addStackTrace(exec.getFilePath(), exec.getLine());
				throw e;
			}
			
			//this.returnedVariable = ((RedirectSentence)exec).getReturnedVariable();
			context.setReturnedVariable(this, ((RedirectSentence)exec).getReturnedVariable(context));
			
			context.setLastSentence(exec);
		}
		else if(exec instanceof FunctionCallSentence){
			try{
				blResult = exec.execute(context, valRepo);
			}catch(ExecutionException e){
				e.addStackTrace(exec.getFilePath(), exec.getLine());
				throw e;
			}
			
			context.setLastSentence(exec);
		}
		else{
			try{
				blResult = exec.execute(context, valRepo);
			}catch(ExecutionException e){
				e.addStackTrace(exec.getFilePath(), exec.getLine());
				throw e;
			}
			
			//this.lastSentence = exec;
			context.setLastSentence(exec);
		}
				
		if(AlinousCore.debug(context)){
			DebugThread thread = context.getCore().getAlinousDebugManager().getCurrentThread();
			if(thread != null){
				thread.getTopStackFrame().setCurrentCandidate(null);
			}
		}
		
		return blResult;
	}
	
	private boolean executeSQLSentence(IExecutable exec, PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		ISQLSentence sqlSentence = (ISQLSentence)exec;
		
		return sqlSentence.execute(context, valRepo);
	}
	

	public void setDataSourceManager(AlinousDataSourceManager dataSourceManager)
	{
		this.dataSourceManager = dataSourceManager;
	}

	public void addSentence(IScriptSentence sentence)
	{
		this.sentences.add(sentence);
		sentence.setFilePath(this.filePath);

	}
	
	// Setter and Getter
	public String getFilePath()
	{
		return this.filePath;
	}

	public int getLine()
	{
		return line;
	}

	public void setLine(int line)
	{
		this.line = line;
	}

	public int getLinePosition()
	{
		return linePosition;
	}

	public void setLinePosition(int linePosition)
	{
		this.linePosition = linePosition;
	}

	public void setFilePath(String filePath)
	{
		this.filePath = filePath;
		
		Iterator<IScriptSentence> it = this.sentences.iterator();
		while(it.hasNext()){
			IScriptSentence sent = it.next();
			sent.setFilePath(filePath);
		}
	}

	
	public IScriptVariable getReturnedVariable(PostContext context)
	{
		return context.getReturnedVariable(this);
	}

	public List<IScriptSentence> getSentences()
	{
		return sentences;
	}

	public IScriptSentence getLastSentence(PostContext context)
	{
		return context.getLastSentence();
	}

	public IScriptVariable executeFunction(PostContext context,
			VariableRepository valRepo) throws ExecutionException, RedirectRequestException {
		return null;
	}
	
	public void checkStaticErrors(ScriptCheckContext scContext,
			List<ScriptError> errorList)
	{
		Iterator<IScriptSentence> it = this.sentences.iterator();
		while(it.hasNext()){
			IScriptSentence element = it.next();
			
			element.checkStaticErrors(scContext, errorList);			
		}
	}
	
	public void getFunctionCall(ScriptCheckContext scContext, List<FunctionCall> call, AlinousScript script)
	{
		Iterator<IScriptSentence> it = this.sentences.iterator();
		while(it.hasNext()){
			IScriptSentence element = it.next();
			
			element.getFunctionCall(scContext, call, script);
		}
	}
	
	@Override
	public void setupCoverage(FileCoverage coverage)
	{
		Iterator<IScriptSentence> it = this.sentences.iterator();
		while(it.hasNext()){
			IScriptSentence element = it.next();
			
			element.setupCoverage(coverage);
		}
	}
}

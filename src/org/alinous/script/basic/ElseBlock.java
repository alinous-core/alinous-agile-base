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
import java.util.List;

import org.alinous.debug.StepInCandidates;
import org.alinous.exec.ExecutableJdomFactory;
import org.alinous.exec.IExecutable;
import org.alinous.exec.ScriptCheckContext;
import org.alinous.exec.ScriptError;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.AlinousScript;
import org.alinous.script.IScriptSentence;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.statement.FunctionCall;
import org.alinous.test.coverage.FileCoverage;
import org.jdom.Element;

public class ElseBlock extends AbstractScriptBlock
{
	public ElseBlock(String filePath) {
		super(filePath);
	}
	public ElseBlock()
	{
		super();
	}


	public static final String BLOCK_NAME = "ElseBlock";
	private IfBlock ifBlock;
	
	public String getName()
	{
		return BLOCK_NAME;
	}

	public StepInCandidates getStepInCandidates()
	{
		if(this.ifBlock != null){
			return this.ifBlock.getStepInCandidates();
		}
		return null;
	}

	public boolean execute(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		if(this.ifBlock != null){
			this.ifBlock.setDataSourceManager(this.dataSourceManager);
			
			boolean blRet = this.ifBlock.execute(context, valRepo);
			//this.returnedVariable = this.ifBlock.getReturnedVariable();
			context.setReturnedVariable(this, this.ifBlock.getReturnedVariable(context));
			
			return blRet;
		}
		
		Iterator<IScriptSentence> it = this.sentences.iterator();
		while(it.hasNext()){
			IScriptSentence exec = it.next();
			
			boolean blRes = executeSentence(exec, context, valRepo);
			
			if(!blRes){
				return false;
			}
			
			// Break and continue
			if(exec instanceof AbstractScriptBlock){
				AbstractScriptBlock block = (AbstractScriptBlock)exec ;
				if(block.getLastSentence(context) instanceof BreakSentence){
					break;
				}
				else if(block.getLastSentence(context) instanceof ContinueSentence){
					break;
				}
			}
			if(exec instanceof BreakSentence){
				exec.execute(context, valRepo);
				break;
			}
			else if(exec instanceof ContinueSentence){
				exec.execute(context, valRepo);
				break;
			}
		}
		
		return true;
	}

	public void exportIntoJDomElement(Element parent) throws AlinousException
	{
		Element elseElement = new Element(IExecutable.TAG_EXECUTABLE);
		elseElement.setAttribute(IExecutable.ATTR_CLASS, this.getClass().getName());
		
		parent.addContent(elseElement);
		
		if(this.ifBlock != null){
			this.ifBlock.exportIntoJDomElement(elseElement);
		}
		
	}

	public void importFromJDomElement(Element threadElement) throws AlinousException
	{
		Element execElement = threadElement.getChild(IExecutable.TAG_EXECUTABLE);
		
		if(execElement != null){
			this.ifBlock = (IfBlock) ExecutableJdomFactory.createFirstExecutable(execElement);
			this.ifBlock.importFromJDomElement(execElement);
		}
		
	}

	public IfBlock getIfBlock()
	{
		return ifBlock;
	}

	public void setIfBlock(IfBlock ifBlock)
	{
		this.ifBlock = ifBlock;
	}

	public boolean isCondResult(PostContext context)
	{
		if(this.ifBlock != null){
			return this.ifBlock.isCondResult(context);
		}
		
		return true;
	}

	@Override
	public IScriptVariable getReturnedVariable(PostContext context)
	{
		return context.getReturnedVariable(this);
	}
	@Override
	public void setFilePath(String filePath) {
		super.setFilePath(filePath);
		
		if(this.ifBlock != null){
			this.ifBlock.setFilePath(filePath);
		}
	}
	@Override
	public void getFunctionCall(ScriptCheckContext scContext, List<FunctionCall> call, AlinousScript script)
	{
		super.getFunctionCall(scContext, call, script);
		
		if(this.ifBlock != null){
			this.ifBlock.getFunctionCall(scContext, call, script);
		}
	}
	@Override
	public void checkStaticErrors(ScriptCheckContext scContext,
			List<ScriptError> errorList)
	{
		super.checkStaticErrors(scContext, errorList);
		
		if(this.ifBlock != null){
			this.ifBlock.checkStaticErrors(scContext, errorList);
		}
	}
	@Override
	public void setupCoverage(FileCoverage coverage)
	{
		super.setupCoverage(coverage);
		
		if(this.ifBlock != null){
			this.ifBlock.setupCoverage(coverage);
		}
	}
	
	

}

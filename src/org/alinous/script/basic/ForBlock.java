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

import org.alinous.AlinousCore;
import org.alinous.debug.StepInCandidates;
import org.alinous.exec.IExecutable;
import org.alinous.exec.ScriptCheckContext;
import org.alinous.exec.ScriptError;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.AlinousScript;
import org.alinous.script.IScriptSentence;
import org.alinous.script.basic.condition.IScriptCondition;
import org.alinous.script.basic.condition.JDomConditionFactory;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.statement.FunctionCall;
import org.jdom.Element;

public class ForBlock extends AbstractScriptBlock
{
	public ForBlock(String filePath) {
		super(filePath);
	}
	public ForBlock()
	{
		super();
	}
	
	public static final String BLOCK_NAME = "ForBlock";
	
	private IScriptSentence initSentence;
	private IScriptSentence afterLoop;
	private IScriptCondition condition;
	
	public String getName()
	{
		return BLOCK_NAME;
	}

	public boolean execute(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		executeSentence(this.initSentence, context, valRepo);
		
		boolean condResult = this.condition.evaluate(context, valRepo);
		if(!condResult){
			return true;
		}
		
		while(condResult){
			Iterator<IScriptSentence> it = this.sentences.iterator();
			while(it.hasNext()){
				IScriptSentence exec = it.next();
		
				boolean res = executeSentence(exec, context, valRepo);
				
				if(!res){
					return false;
				}
				
				// handle Break and continue
				if(exec instanceof AbstractScriptBlock){
					AbstractScriptBlock block = (AbstractScriptBlock)exec ;
					if(block.getLastSentence(context) instanceof BreakSentence){
						context.setLastSentence(this);
						return true;
					}
					else if(block.getLastSentence(context) instanceof ContinueSentence){
						break;
					}
				}
				
				if(exec instanceof BreakSentence){
					return true;
				}
				else if(exec instanceof ContinueSentence){
					break;
				}
			}
			
			executeSentence(this.afterLoop, context, valRepo);
			
			//	DEBUG:
			if(AlinousCore.debug(context) && context.getCore().getAlinousDebugManager().getCurrentThread() != null){
				DummyConditionSentence dummyExec = new DummyConditionSentence(this);
				dummyExec.setCondition(this.condition);
				
				context.getCore().getAlinousDebugManager().aboutToExecuteSentence(this, dummyExec, context);
			}
			
			condResult = this.condition.evaluate(context, valRepo);
			if(!condResult){
				return true;
			}
			
		}
		
		return true;
	}

	public void exportIntoJDomElement(Element parent) throws AlinousException
	{
		Element forElement = new Element(IExecutable.TAG_EXECUTABLE);
		forElement.setAttribute(IExecutable.ATTR_CLASS, this.getClass().getName());
		
		parent.addContent(forElement);
		
		this.condition.exportIntoJDomElement(forElement);
		
	}

	public void importFromJDomElement(Element element) throws AlinousException
	{
		Element cond = element.getChild(IScriptCondition.TAG_CONDITION);
		this.condition = JDomConditionFactory.createConditionFromJDomElement(cond);
		
		this.condition.importFromJDomElement(cond);
	}

	public IScriptSentence getAfterLoop()
	{
		return afterLoop;
	}

	public void setAfterLoop(IScriptSentence afterLoop)
	{
		if(afterLoop == null){
			return;
		}
		
		afterLoop.setFilePath(this.filePath);
		afterLoop.setLine(this.line);
		afterLoop.setLinePosition(this.linePosition);
		
		afterLoop.setFilePath(filePath);
		
		this.afterLoop = afterLoop;
	}

	public IScriptCondition getCondition()
	{
		return condition;
	}

	public void setCondition(IScriptCondition condition)
	{
		if(condition == null){
			return;
		}
		condition.setCallerSentence(this);
		this.condition = condition;
		
		condition.setFilePath(BLOCK_NAME);
	}

	public IScriptSentence getInitSentence()
	{
		return initSentence;
	}

	public void setInitSentence(IScriptSentence initSentence)
	{
		if(initSentence == null){
			return;
		}
		this.initSentence = initSentence;
		initSentence.setFilePath(this.filePath);
		initSentence.setLine(this.line);
		initSentence.setLinePosition(this.linePosition);
		
		initSentence.setFilePath(this.filePath);
	}

	public StepInCandidates getStepInCandidates()
	{
		StepInCandidates candidates = new StepInCandidates();
		
		this.condition.canStepInStatements(candidates);
		
		return candidates;
	}

	@Override
	public IScriptVariable getReturnedVariable(PostContext context)
	{
		return context.getReturnedVariable(this);
	}
	@Override
	public void setFilePath(String filePath) {
		super.setFilePath(filePath);
		
		if(this.initSentence != null){
			this.initSentence.setFilePath(filePath);
		}
		if(this.condition != null){
			this.condition.setFilePath(filePath);
		}
		if(this.afterLoop != null){
			this.afterLoop.setFilePath(filePath);
		}
	}
	@Override
	public void checkStaticErrors(ScriptCheckContext scContext,
			List<ScriptError> errorList)
	{
		scContext.setCurrentExecutable(this);
		super.checkStaticErrors(scContext, errorList);
		scContext.setCurrentExecutable(null);
		
		
		if(this.initSentence != null){
			scContext.setCurrentExecutable(this);
			this.initSentence.checkStaticErrors(scContext, errorList);
			scContext.setCurrentExecutable(null);
		}
		if(this.condition != null){
			scContext.setCurrentExecutable(this);
			this.condition.checkStaticErrors(scContext, errorList);
			scContext.setCurrentExecutable(null);
		}
		if(this.afterLoop != null){
			scContext.setCurrentExecutable(this);
			this.afterLoop.checkStaticErrors(scContext, errorList);
			scContext.setCurrentExecutable(null);
		}
		
		
	}
	@Override
	public void getFunctionCall(ScriptCheckContext scContext, List<FunctionCall> call, AlinousScript script)
	{
		super.getFunctionCall(scContext, call, script);
		
		if(this.initSentence != null){
			this.initSentence.getFunctionCall(scContext, call, script);
		}
		if(this.condition != null){
			this.condition.getFunctionCall(scContext, call, script);
		}
		if(this.afterLoop != null){
			this.afterLoop.getFunctionCall(scContext, call, script);
		}
	}
	
	
	
}

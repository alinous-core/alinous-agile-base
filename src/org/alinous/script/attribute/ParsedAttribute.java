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
package org.alinous.script.attribute;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import org.alinous.AlinousCore;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.AlinousScript;
import org.alinous.script.IScriptObject;
import org.alinous.script.basic.condition.IScriptCondition;
import org.alinous.script.basic.type.BooleanConst;
import org.alinous.script.basic.type.DoubleConst;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.basic.type.NullConst;
import org.alinous.script.basic.type.NumericConst;
import org.alinous.script.basic.type.StringConst;
import org.alinous.script.basic.type.VariableDescriptor;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class ParsedAttribute
{
	private List<IScriptObject> elements = new ArrayList<IScriptObject>();
	
	public ParsedAttribute()
	{
		
	}
	
	
	public void addElement(IScriptObject element)
	{
		this.elements.add(element);
		
	}
	
	public IPathElement getPathElement()
	{
		if(this.elements.size() <= 0){
			return null;
		}
		
		if(!(elements.get(0) instanceof VariableDescriptor)){
			return null;
		}
		
		return ((VariableDescriptor)elements.get(0)).getPath();
	}
	
	public String expand(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		StringBuffer buffer = new StringBuffer();
		
		Iterator<IScriptObject> it = this.elements.iterator();
		while(it.hasNext()){
			IScriptObject obj = it.next();
			
			if(!(obj instanceof IStatement)){
				throw new ExecutionException("Cannot use condition here.");
			}
			
			if(context == null && obj instanceof StringConst){
				return ((StringConst)obj).getStr();
			}
			
			// check if null is OK
			IStatement stmt = (IStatement)obj;
			PostContext dummyContext = new PostContext(context.getCore(), context.getUnit());
			
			//	debug start
			AlinousScript script = null;
			
			if(context.getUnit().getExecModule() != null){
				script = context.getUnit().getExecModule().getScript();
			}
			
			if(AlinousCore.debug(context) && script != null
					&& context.getCore().getAlinousDebugManager() != null
					&& context.getCore().getAlinousDebugManager().getCurrentThread() != null){
				context.getCore().getAlinousDebugManager()
					.getCurrentThread().newStackFrame(script, valRepo, context);
			}
			
			IScriptVariable value = null;
			try{
				stmt.setCallerSentence(script);
				value = stmt.executeStatement(dummyContext, valRepo);
			}finally{
				// debug end
				if(AlinousCore.debug(context) && script != null
						&& context.getCore().getAlinousDebugManager() != null
						&& context.getCore().getAlinousDebugManager().getCurrentThread() != null){
					context.getCore().getAlinousDebugManager()
					.getCurrentThread().destroyStackFrame();
				}
				
				dummyContext.dispose();
			}
						
			if(!(value instanceof ScriptDomVariable)){
				throw new ExecutionException("Cannot use array here.");
			}
			
			buffer.append(((ScriptDomVariable)value).getValue());
		}
		
		return buffer.toString();
	}
	
	public boolean evaluate(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		if(this.elements.size() != 1){
			throw new ExecutionException("Cannot evaluate multiple conditions.");
		}
		
		if(!(this.elements.get(0) instanceof IScriptCondition)){
			throw new ExecutionException("Please input a condition.");
		}
		
		IScriptCondition cond = (IScriptCondition)this.elements.get(0);
		
		return cond.evaluate(context, valRepo);
	}
	
	// Optimize
	public boolean isDynamic()
	{
		Iterator<IScriptObject> it = this.elements.iterator();
		while(it.hasNext()){
			IScriptObject obj = it.next();
			
			if(obj instanceof BooleanConst || obj instanceof DoubleConst ||
					obj instanceof NullConst || obj instanceof NumericConst ||
					 obj instanceof StringConst){
				continue;
			}
			else{
				return true;
			}
		}
		
		return false;
	}
	
}

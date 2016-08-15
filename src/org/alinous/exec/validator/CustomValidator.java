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
package org.alinous.exec.validator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.alinous.AlinousCore;
import org.alinous.AlinousUtils;
import org.alinous.exec.AccessExecutionUnit;
import org.alinous.exec.pages.IParamValue;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class CustomValidator implements IValidator
{
	private List<String> reasons = new ArrayList<String>();
	private String InputName;
	private String formName;
	
	public void setRegExp(String regExp)
	{
		
	}

	public boolean validate(IParamValue param, PostContext context, VariableRepository valRepo, boolean isArray)
					throws AlinousException
	{
		AlinousCore core = context.getCore();
		
		String moduleName = context.getRequestPath();
		
		if(context.getNextAction() != null){
			String nextAction = context.getNextAction();
			moduleName = AlinousUtils.getModuleName(nextAction);
		}
		
		core.registerAlinousObject(context, moduleName);
		
		AccessExecutionUnit unit = null;
		IScriptVariable valResult = null;
		PostContext newContext = null;
		try{
			unit = core.createAccessExecutionUnit(context.getSessionId(), context.getUnit());
			// new context
			newContext = new PostContext(core, unit);
			newContext.initParams(context);
			
			valResult = unit.executeValidation(newContext, valRepo, moduleName,
					this.InputName, this.formName, isArray);
		}
		finally{
			newContext.dispose();
			unit.dispose();
		}

		if(valResult == null){
			return true;
		}
				
		// Store result
		if(valResult instanceof ScriptDomVariable){
			handleReason((ScriptDomVariable)valResult);
		}else if(valResult instanceof ScriptArray){
			handleReason((ScriptArray)valResult);
		}else{
			throw new ExecutionException("FATAL ERROR"); // i18n
		}
		
		
		if(reasons == null || reasons.size() == 0){
			return true;
		}
		
		return false;
	}
	
	private void handleReason(ScriptDomVariable val)
	{
		if(!val.getValueType().equals(IScriptVariable.TYPE_STRING))
		{
			return;
		}
		
		String str = val.getValue();
		this.reasons.add(str);
	}
	
	private void handleReason(ScriptArray arrayVal)
	{
		Iterator<IScriptVariable> it = arrayVal.iterator();
		while(it.hasNext()){
			IScriptVariable scVal = it.next();
			
			if(scVal instanceof ScriptDomVariable){
				handleReason((ScriptDomVariable)scVal);
			}
			else{
				handleReason((ScriptArray)scVal);
			}
		}
	}
	
	public List<String> getReasons()
	{
		return this.reasons;
	}

	public void setFormName(String formName)
	{
		this.formName = formName;
	}

	public void setInputName(String inputName)
	{
		InputName = inputName;
	}
	
}

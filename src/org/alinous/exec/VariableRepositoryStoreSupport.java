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
package org.alinous.exec;

import java.util.Iterator;
import java.util.Map;

import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class VariableRepositoryStoreSupport
{
	protected void dump(IScriptVariable variable, Map<IPathElement, ExecResultRecord> variableList,
			PostContext context, VariableRepository valRepo)
		throws ExecutionException, RedirectRequestException
	{
		if(variable instanceof ScriptDomVariable){
			dumpHashValue((ScriptDomVariable)variable, variableList, context, valRepo);
		}else{
			dumpArray((ScriptArray)variable, variableList, context, valRepo);
		}
	}
	
	
	private void dumpHashValue(ScriptDomVariable variable,
				Map<IPathElement, ExecResultRecord> variableList,
				PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		String name = variable.getPath().getPathString(context, valRepo);
		String value = variable.getValue();
		String type = variable.getType();
		String valueType = variable.getValueType();
		
		if(value != null){
			ExecResultRecord rec = new ExecResultRecord();
			rec.setName(name);
			rec.setValue(value);
			rec.setType(type);
			rec.setValueType(valueType);
			
			IPathElement variablePath = variable.getPath();
			
			variableList.put(variablePath, rec);
		}
		
		// properties
		Iterator<String> it = variable.getPropertiesIterator();
		while(it.hasNext()){
			String prop = it.next();
			IScriptVariable val = variable.get(prop);
			
			dump(val, variableList, context, valRepo);
		}
	}
	
	private void dumpArray(ScriptArray array,
			Map<IPathElement, ExecResultRecord> variableList,
			PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		Iterator<IScriptVariable> it = array.iterator();
		while(it.hasNext()){
			IScriptVariable val = it.next();
			
			dump(val, variableList, context, valRepo);
		}
	}
}

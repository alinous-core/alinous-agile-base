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
package org.alinous.script.sql.statement;


import org.alinous.AlinousUtils;
import org.alinous.datasrc.types.TypeHelper;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.plugin.precompile.PreCompileValue;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.adjustopt.AdjustSet;
import org.alinous.script.sql.adjustopt.AdjustWhere;

public class SQLVariable implements ISQLStatement
{
	private String prefix;
	private IPathElement pathElement;

	public IPathElement getPathElement()
	{
		return pathElement;
	}

	public void setPathElement(IPathElement pathElement)
	{
		this.pathElement = pathElement;
	}

	public String extract(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		if(context.getPrecompile().isCompile()){
			return extractPrecompile(context, providor, adjustWhere, adjustSet, helper);
		}
		
		IScriptVariable value = null;
		try {
			value = providor.getVariable(this.pathElement, context);
		} catch (RedirectRequestException e) {
			e.printStackTrace();
		}
		
		if(value == null){
			return "null";
		}
		
		if(value instanceof ScriptDomVariable){
			return extractScriptDom((ScriptDomVariable)value, helper);
		}
		// if value is ScriptArray
		else if(value instanceof ScriptArray){
			return extractScriptArray((ScriptArray)value, helper);
		}
		
		
		return value.toString();
	}
	
	public String extractPrecompile(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		IScriptVariable value = null;
		
		
		try {
			value = providor.getVariable(this.pathElement, context);
		} catch (RedirectRequestException e) {
			e.printStackTrace();
		}
		
		if(value == null){			
			String type = helper.getFieldType();
			if(type == null){
				type = IScriptVariable.TYPE_STRING;
			}
			
			PreCompileValue pValue = new PreCompileValue(type, this.pathElement);
			context.getPrecompile().addVariable(pValue);
			
			return "?";
		}
		
		if(value instanceof ScriptDomVariable){
			String type = helper.getFieldType();
			
			
			if(type == null){
				type = IScriptVariable.TYPE_STRING;
			}
			
			PreCompileValue pValue = new PreCompileValue(type, this.pathElement);
			context.getPrecompile().addVariable(pValue);
			
			return " ? ";
		}		
		// if value is ScriptArray
		else if(value instanceof ScriptArray){
			// has array
			context.getPrecompile().setHasArray(true);
			
			String type = helper.getFieldType();
			if(type == null){
				type = IScriptVariable.TYPE_STRING;
			}
			
			PreCompileValue pValue = new PreCompileValue(type, this.pathElement);
			
			ScriptArray array = (ScriptArray)value;
			int max = array.getSize();
			for(int i = 0; i < max; i++){
				IScriptVariable val = array.get(i);
				ScriptDomVariable domVal = (ScriptDomVariable)val;
				
				pValue.addArrayValue(domVal.getValue());
			}
			
			context.getPrecompile().addVariable(pValue);
			
			return "?";
		}
		
		return value.toString();
	}
	
	public IScriptVariable getScriptVariable(PostContext context, VariableRepository providor) throws ExecutionException
	{
		IScriptVariable value = null;
		try {
			value = providor.getVariable(this.pathElement, context);
		} catch (RedirectRequestException e) {
			e.printStackTrace();
		}
		
		return value;
	}
	
	private String extractScriptArray(ScriptArray array, TypeHelper helper) throws ExecutionException
	{
		StringBuffer buff = new StringBuffer();
		
		int max = array.getSize();
		boolean first = true;
		for(int i = 0; i < max; i++){
			if(first){
				first = false;
			}
			else{
				buff.append(", ");
			}
			
			IScriptVariable val = array.get(i);
			
			if(!(val instanceof ScriptDomVariable)){
				buff.append("null");
				continue;
			}

			ScriptDomVariable domVal = (ScriptDomVariable)val;
			
			buff.append(extractScriptDom(domVal, helper));
		}
		
		return buff.toString();
	}
	
	private String extractScriptDom(ScriptDomVariable value, TypeHelper helper)
	{
		// type valification
		if(helper.getFieldType() == null){
			return defaultExtractDom(value, helper);
		}
		
		if(helper.isQuoted()){
			return "'" + AlinousUtils.sqlEscape(value.getValue()) + "'";
		}
		
		return value.getValue();
	}
	
	private String defaultExtractDom(ScriptDomVariable value, TypeHelper helper)
	{
		//
		//AlinousDebug.debugOut("defaultExtractDom : " + value.getValue());
		//AlinousDebug.debugOut("defaultExtractDom value.getValueType() : " + value.getValueType());
		
		
		if(value.getValueType().equals(IScriptVariable.TYPE_STRING)){
			return "'" + AlinousUtils.sqlEscape(value.getValue()) + "'";
		}
		
		return value.getValue();
	}

	public boolean isReady(PostContext context, VariableRepository providor, AdjustWhere adjustWhere) throws ExecutionException
	{
		if(adjustWhere == null || !adjustWhere.adjust()){
			return true;
		}
		
		IScriptVariable value = null;
		try {
			value = providor.getVariable(this.pathElement, context);
		} catch (RedirectRequestException e) {
			e.printStackTrace();
		}

		if(value == null){
			return false;
		}
		
		if(value instanceof ScriptDomVariable){
			ScriptDomVariable domVal = (ScriptDomVariable)value;
			
			if(domVal.getValue() == null){
				return false;
			}
		}
		else if(value instanceof ScriptArray){
			ScriptArray ar = (ScriptArray)value;
			
			if(ar.isEmpty()){
				return false;
			}
		}
		
		return true;
	}

	public boolean isEmptyArray(PostContext context, VariableRepository providor) throws ExecutionException
	{
		IScriptVariable value = null;
		try {
			value = providor.getVariable(this.pathElement, context);
		} catch (RedirectRequestException e) {
			e.printStackTrace();
		}
		
		if(value instanceof ScriptArray){
			ScriptArray ar = (ScriptArray)value;
			
			if(ar.isEmpty()){
				return true;
			}
		}
		
		return false;
	}
	
	public String getPrefix()
	{
		return prefix;
	}

	public void setPrefix(String prefix)
	{
		this.prefix = prefix;
	}
	
	
}

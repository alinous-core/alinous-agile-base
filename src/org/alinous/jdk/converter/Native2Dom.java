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
package org.alinous.jdk.converter;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;

public class Native2Dom
{
	private Object nativeObject;
	
	public Native2Dom(Object nativeObject)
	{
		this.nativeObject = nativeObject;
	}
	
	public IScriptVariable convert()
		throws SecurityException, IllegalArgumentException,
		NoSuchMethodException, IllegalAccessException, InvocationTargetException
	{
		IScriptVariable retVal = handleObject(this.nativeObject);
		if(retVal == null){
			retVal = new ScriptDomVariable("Null");
			((ScriptDomVariable)retVal).setValueType(IScriptVariable.TYPE_NULL);
		}
		
		return retVal;
	}
	
	private IScriptVariable handleObject(Object obj)
		throws SecurityException, IllegalArgumentException,
		NoSuchMethodException, IllegalAccessException, InvocationTargetException
	{
		if(obj == null){
			return new ScriptDomVariable("null");
		}

		if(obj instanceof Map){
			return handleMap(obj);
		}
		if(obj instanceof List<?>){
			return handleList((List<?>)obj);
		}
		else if(obj.getClass().isArray()){
			return handleArrayObject((Object[])obj);
		}
		else if(PrimitiveHandler.isPrimitive(obj)){
			return handlePrimitive(obj);
		}
		
		return handleBean(obj);
	}
	
	private IScriptVariable handleMap(Object obj) throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException
	{
		@SuppressWarnings("unchecked")
		Map<String, Object> m = (Map<String, Object>)obj;
		
		ScriptDomVariable dom = new ScriptDomVariable("hash");
		
		Iterator<String> it = m.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			Object value = m.get(key);
			
			IScriptVariable prop = handleObject(value);
			if(prop instanceof ScriptDomVariable){
				((ScriptDomVariable)prop).setName(key);
			}
			dom.put(prop);
		}
		
		return dom;
	}
	
	private IScriptVariable handlePrimitive(Object obj)
	{
		ScriptDomVariable val = new ScriptDomVariable("");
		val.setValue(obj.toString());
		
		if(obj instanceof Integer){
			val.setValueType(IScriptVariable.TYPE_NUMBER);
		}
		else if(obj instanceof Boolean){
			val.setValueType(IScriptVariable.TYPE_BOOLEAN);
		}
		else if(obj instanceof Double || obj instanceof Float){
			val.setValueType(IScriptVariable.TYPE_DOUBLE);
		}
		else{
			val.setValueType(IScriptVariable.TYPE_STRING);
		}
		
		return val;
	}
	
	private IScriptVariable handleBean(Object bean)
		throws SecurityException, IllegalArgumentException,
		NoSuchMethodException, IllegalAccessException, InvocationTargetException
	{
		BeanHandler handler = new BeanHandler(bean);
		// DEBUG:
		ArrayList<BeanProperty> propList = handler.getSetterProperties();
		
		ScriptDomVariable val = new ScriptDomVariable("");
		Iterator<BeanProperty> it = propList.iterator();
		while(it.hasNext()){
			BeanProperty prop = it.next();
			
			Object obj = handler.invokeGetter(prop.getGetterName(), prop.getClazz());
			
			IScriptVariable pVal = handleObject(obj);
			pVal.setName(prop.getName());
			
			val.put(pVal);
		}
		
		
		return val;
	}
	
	private IScriptVariable handleArrayObject(Object[] objectArray)
		throws SecurityException, IllegalArgumentException, 
		NoSuchMethodException, IllegalAccessException, InvocationTargetException
	{
		ScriptArray ar = new ScriptArray("");
		for(int i = 0; i < objectArray.length; i++){
			IScriptVariable val = handleObject(objectArray[i]);
			
			ar.add(val);
		}
		
		return ar;
	}
	
	private IScriptVariable handleList(List<?> list)
		throws SecurityException, IllegalArgumentException,
		NoSuchMethodException, IllegalAccessException, InvocationTargetException
	{
		ScriptArray ar = new ScriptArray("");
		
		Iterator<?> it = list.iterator();
		while(it.hasNext()){
			Object o = it.next();
			IScriptVariable val = handleObject(o);
			ar.add(val);
		}
		
		return ar;
	}
	
	
	
}

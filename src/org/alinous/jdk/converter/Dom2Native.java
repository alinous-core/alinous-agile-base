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


import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;

public class Dom2Native
{
	private IScriptVariable domVariable;
	private Class<?> nativeClass;
	
	
	public Dom2Native(IScriptVariable domVariable, Class<?> nativeClass)
	{
		this.domVariable = domVariable;
		
		if(nativeClass.equals(Map.class)){
			nativeClass = HashMap.class;
		}
		
		this.nativeClass = nativeClass;
	}
	
	public Object convert()
		throws InstantiationException, IllegalAccessException,
		SecurityException, IllegalArgumentException, NoSuchMethodException,
		InvocationTargetException
	{
		if(this.domVariable == null){
			return null;
		}
		
		return getObject(this.domVariable, this.nativeClass);
	}
	
	public Object convertArray() throws SecurityException, IllegalArgumentException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException
	{
		if(this.domVariable == null){
			return null;
		}
		if(!(this.domVariable instanceof ScriptArray)){
			return null;
		}
		
		ArrayList<Object> list = new ArrayList<Object>();
		
		ScriptArray ar = (ScriptArray)this.domVariable;
		Class<?> clazz = this.nativeClass;
		
		int max = ar.getSize();
		for(int i = 0; i < max; i++){
			Object object = getObject(ar.get(i), clazz.getComponentType());
			list.add(object);
		}
		
		Object retArray = Array.newInstance(clazz.getComponentType(), list.size());
		
		return list.toArray((Object[])retArray);
	}
	
	
	private Object getObject(IScriptVariable val, Class<?> clazz) 
		throws InstantiationException, IllegalAccessException, SecurityException,
			IllegalArgumentException, NoSuchMethodException, InvocationTargetException
	{
		if(val == null){
			return null;
		}

		if(PrimitiveHandler.isPrimitive(clazz)){
			if(clazz.isArray()){
				return ((ScriptArray)val).toStringArray();
			}
			
			Object obj = PrimitiveHandler.newInstance(clazz, ((ScriptDomVariable)val).getValue());
			return obj;
		}
		else if(clazz.equals(HashMap.class) && val instanceof ScriptDomVariable){
			return handleMap((ScriptDomVariable) val);
		}
		else if(val instanceof ScriptArray){
			Object obj = newInstance(clazz);
			setupObjectAsCollection(obj, (ScriptArray)val);
			
			return obj;
		}
		else if(val instanceof ScriptDomVariable){
			Object obj = newInstance(clazz);
			setupObjectAsBean(obj, (ScriptDomVariable)val);
			
			return obj;
		}
		
		return newInstance(clazz);
	}
	
	private Object handleMap(ScriptDomVariable val)
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		Iterator<String> it = val.getPropertiesIterator();
		while(it.hasNext()){
			String key = it.next();
			
			IScriptVariable v = val.get(key);
			if(v instanceof ScriptDomVariable){
				String value = ((ScriptDomVariable)v).getValue();
				map.put(key, value);
			}
			else if(v instanceof ScriptArray){
				List<String> value = handleStringArray((ScriptArray) v);
				map.put(key, value);
			}
		}
		
		return map;
	}
	
	
	private List<String> handleStringArray(ScriptArray array)
	{
		ArrayList<String> list = new ArrayList<String>();
		
		int size = array.getSize();
		for(int i = 0; i < size; i++){
			String v = ((ScriptDomVariable)array.get(i)).getValue();
			list.add(v);
		}
		
		return list;
	}
	
	private Object newInstance(Class<?> clazz) throws InstantiationException, IllegalAccessException
	{
		if(clazz.getName().equals("java.util.List")){
			
			return null;
		}
		
		return clazz.newInstance();
	}
	
	
	/**
	 * If obj is List
	 * @param obj
	 * @param dom
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws InvocationTargetException 
	 * @throws NoSuchMethodException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 */
	@SuppressWarnings("unchecked")
	private void setupObjectAsCollection(Object obj, ScriptArray scriptArray)
			throws InstantiationException, IllegalAccessException, SecurityException,
			IllegalArgumentException, NoSuchMethodException, InvocationTargetException
	{
		if(obj instanceof List<?>){
			
			List<Object> list = (List<Object>)obj;
			
			int max = scriptArray.getSize();
			for(int i = 0; i < max; i++){
				IScriptVariable val = scriptArray.get(i);
				Class<?> clazz = getListTemplateType(obj);
				
				Object newObj = getObject(val, clazz);
				list.add(newObj);
			}
		}
	}
	
	private Class<?> getListTemplateType(Object obj)
	{
		Class<?> clazz = obj.getClass();
		/*
		Type t = clazz.getGenericSuperclass();
		if(t instanceof ParameterizedType){
			ParameterizedType pt = (ParameterizedType)t;
			
			Type types[] = pt.getActualTypeArguments();
			
			// types[1].getClass().
			System.out.println(types);
		}
		*/
		Method methods[] = clazz.getMethods();
		for(int i = 0; i < methods.length; i++){
			Method m = methods[i];
			
			if(m.getName().equals("add") && m.getParameterTypes().length == 1){
				return m.getParameterTypes()[0];
			}
			
		}
		
		return null;
	}
	
	/**
	 * If obj is Java  Bean
	 * @param obj
	 * @param dom
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 */
	private void setupObjectAsBean(Object obj, ScriptDomVariable dom)
		throws InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException,
			IllegalArgumentException, InvocationTargetException
	{
		BeanHandler beanHandler = new BeanHandler(obj);
		
		ArrayList<BeanProperty> setterProps = beanHandler.getSetterProperties();
		Iterator<BeanProperty> it = setterProps.iterator();
		while(it.hasNext()){
			BeanProperty p = it.next();
			
			IScriptVariable val = dom.get(p.getName());
			
			//set up array
			if(p.getClazz().isArray()){
				setupArray(obj, p.getClazz(), val, p);
				continue;
			}
			
			
			Object newObject = getObject(val, p.getClazz());
			
			if(newObject == null){
				continue;
			}
			
			Method m = obj.getClass().getMethod(p.getSetterName(), new Class[]{p.getClazz()});			
			m.invoke(obj, new Object[]{newObject});
		}
		
	}
	
	private void setupArray(Object beanObj, Class<?> arrayClass, IScriptVariable arrayVal
			, BeanProperty p)
		throws SecurityException, IllegalArgumentException, InstantiationException,
			IllegalAccessException, NoSuchMethodException, InvocationTargetException
	{
		if(!(arrayVal instanceof ScriptArray)){
			return;
		}
		
		ScriptArray ar = (ScriptArray)arrayVal;
		ArrayList<Object> retList = new ArrayList<Object>();
		int ar_size = ar.getSize();
		for(int i = 0; i < ar_size; i++){
			IScriptVariable val = ar.get(i);
			
			Class<?> cmpClazz = arrayClass.getComponentType();
			
			Object newObj = getObject(val, cmpClazz);
			retList.add(newObj);
		}
		
		if(retList.size() == 0){
			return;
		}
		
		Object beanArray = Array.newInstance(arrayClass.getComponentType(), retList.size());
		for(int i = 0; i < ((Object[])beanArray).length ; i++){
			((Object[])beanArray)[i] = retList.get(i);
		}
		
		Method m = beanObj.getClass().getMethod(p.getSetterName(), new Class[]{p.getClazz()});			
		m.invoke(beanObj, new Object[]{beanArray});
		
	}
	
	
}

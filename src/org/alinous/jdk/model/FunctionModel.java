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
package org.alinous.jdk.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.alinous.jdk.IFunctionUtils;
import org.alinous.jdk.converter.Dom2Native;
import org.alinous.jdk.converter.Native2Dom;
import org.alinous.script.functions.system.JavaConnectorFunctionCallback;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;

public class FunctionModel
{
	private List<ArgumentModel> arguments = new ArrayList<ArgumentModel>();
	
	private Method method;
	private Object object;
	
	public FunctionModel(Method m, Object object)
	{
		this.method = m;
		this.object = object;

		// init arguments
		Class<?> classes[] = m.getParameterTypes();
		for(int i = 0; i < classes.length; i++){
			ArgumentModel argModel = new ArgumentModel(i, classes[i]);
			
			this.arguments.add(argModel);
		}
	}
	
	public String getName()
	{
		return this.method.getName();
	}

	public List<ArgumentModel> getArguments()
	{
		return arguments;
	}
	
	public void setJavaConnectorFunctionCallback(JavaConnectorFunctionCallback callback)
	{
		IFunctionUtils.invokeFunction(this.object, "setAlinousCaller", callback, Object.class);
	}
	
	public IScriptVariable invoke(IScriptVariable[] arguments)
		throws SecurityException, IllegalArgumentException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException
	{
		//
		ArrayList<Object> argList = new ArrayList<Object>();
		for(int i = 0; i < arguments.length; i++){
			ArgumentModel argModel = this.arguments.get(i);
			Dom2Native d2n = new Dom2Native(arguments[i], argModel.getClazz());
			
			if(arguments[i] instanceof ScriptDomVariable){
				Object obj = d2n.convert();
				argList.add(obj);
			}
			if(arguments[i] instanceof ScriptArray){
				Object obj = d2n.convertArray();
				argList.add(obj);
			}
		}
		
		Object retObj = this.method.invoke(this.object, argList.toArray(new Object[argList.size()]));
		
		Native2Dom n2d = new Native2Dom(retObj);
		
		return n2d.convert();
	}
	
	public String codeAssistString()
	{
		try {
			Method m = this.object.getClass().getMethod("codeAssistString", String.class);
			
			String str = (String)m.invoke(this.object, getName());
			
			return str;
		} catch (NoSuchMethodException e) {
			//e.printStackTrace();
		} catch (SecurityException e) {
			//e.printStackTrace();
		} catch (IllegalAccessException e) {
			//e.printStackTrace();
		} catch (IllegalArgumentException e) {
			//e.printStackTrace();
		} catch (InvocationTargetException e) {
			//e.printStackTrace();
		}
		
		return null;
	}
	
	public String descriptionString()
	{
		try {
			Method m = this.object.getClass().getMethod("descriptionString", String.class);
			
			return (String)m.invoke(this.object, getName());
		} catch (NoSuchMethodException e) {
			//e.printStackTrace();
		} catch (SecurityException e) {
			//e.printStackTrace();
		} catch (IllegalAccessException e) {
			//e.printStackTrace();
		} catch (IllegalArgumentException e) {
			//e.printStackTrace();
		} catch (InvocationTargetException e) {
			//e.printStackTrace();
		}
		
		return null;
	}
	
}

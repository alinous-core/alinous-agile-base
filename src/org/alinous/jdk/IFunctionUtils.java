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
package org.alinous.jdk;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class IFunctionUtils
{
	public static String CLASS_NAME = "org.alinous.jdk.IAlinousFunction";
	
	public static String[] invokeGetFunctions(Object iFunction)
	{
		return (String[])invokeFunctions(iFunction, "getFunctions");
	}
	
	public static String invokeGetPrefix(Object iFunction)
	{
		return (String)invokeFunctions(iFunction, "getPrefix");
	}
	
	public static Object invokeFunctions(Object iFunction, String funcName)
	{
		Method m = null;
		try {
			m = iFunction.getClass().getMethod(funcName, new Class[]{});
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		
		
		Object retObj = null;
		try {
			retObj = m.invoke(iFunction, new Object[]{});
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return retObj;
	}
	
	public static Object invokeFunction(Object iFunction, String funcName, String param)
	{
		Method m = null;
		try {
			m = iFunction.getClass().getMethod(funcName, new Class[]{String.class});
		} catch (SecurityException e) {
			e.printStackTrace();
			return null;
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			return null;
		}
		
		if(m == null){
			return null;
		}
		
		
		Object retObj = null;
		try {
			retObj = m.invoke(iFunction, new Object[]{param});
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return retObj;
	}
	
	public static Object invokeFunction(Object iFunction, String funcName, Object param, Class<?> clazz)
	{
		Method m = null;
		try {
			m = iFunction.getClass().getMethod(funcName, new Class[]{clazz});
		} catch (SecurityException e) {
			e.printStackTrace();
			return null;
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			return null;
		}
		
		if(m == null){
			return null;
		}
		
		
		Object retObj = null;
		try {
			retObj = m.invoke(iFunction, new Object[]{param});
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return retObj;
	}
}

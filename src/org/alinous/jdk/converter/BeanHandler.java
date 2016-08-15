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
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class BeanHandler
{
	private Object obj;
	
	public BeanHandler(Object obj)
	{
		this.obj = obj;
	}
	
	public ArrayList<BeanProperty> getSetterProperties()
	{
		Method methods[] = this.obj.getClass().getMethods();
		ArrayList<BeanProperty> list = new ArrayList<BeanProperty>();
		
		for(int i = 0; i < methods.length; i++){
			Method m = methods[i];
			
			BeanProperty p = getSetterBeanProp(m);
			
			if(p != null){
				list.add(p);
			}
		}
		
		return list;
	}
	
	private BeanProperty getSetterBeanProp(Method m)
	{
		Type types[] = m.getGenericParameterTypes();
		
		if(types.length != 1){
			return null;
		}
		if(!m.getName().startsWith("set")){
			return null;
		}
		
		BeanProperty prop = new BeanProperty();
		
		if(types[0] instanceof ParameterizedType){
			ParameterizedType t = (ParameterizedType)types[0];
		
			prop.setClazz((Class<?>)t.getRawType());
		}else{
			prop.setClazz((Class<?>)types[0]);
		}
		
		String propName = m.getName().substring(3, m.getName().length());
		char first = propName.charAt(0);
		first = Character.toLowerCase(first);
		propName = first + propName.substring(1);
		
		
		prop.setName(propName);
		
		
		return prop;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object invokeGetter(String getterName, Class<?> paramType)
				throws SecurityException, NoSuchMethodException,
				IllegalArgumentException, IllegalAccessException,
				InvocationTargetException
	{
		Class cls = this.obj.getClass();
		
		//cls.getMethod(name, parameterTypes)
		
		Method m = cls.getMethod(getterName, new Class<?>[]{});
		Object ret = m.invoke(this.obj, new Object[]{});
		
		return ret;
	}
	
	
}

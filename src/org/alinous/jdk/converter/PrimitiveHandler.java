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

public class PrimitiveHandler
{
	public static boolean isPrimitive(Object obj)
	{
		if(obj instanceof String |obj instanceof Boolean |
				obj instanceof Integer |obj instanceof Double |
				obj instanceof Float |obj instanceof Long |
				obj instanceof Byte | obj instanceof Character
		){
			return true;
		}
		
		return false;
	}
	
	public static boolean isPrimitive(Class<?> clazz)
	{
		if(clazz.equals(String.class) | clazz.equals(Boolean.class) |
				clazz.equals(Integer.class) | clazz.equals(Double.class) |
				clazz.equals(Float.class) | clazz.equals(Long.class) |
				clazz.equals(Byte.class) | clazz.equals(Character.class)
		){
			return true;
		}
		
		return false;
	}
	
	public static Object newInstance(Class<?> clazz, String str)
	{
		if(clazz.equals(String.class)){
			return str;
		}
		else if(clazz.equals(Integer.class)){
			return new Integer(str);
		}
		else if(clazz.equals(Double.class)){
			return new Double(str);
		}
		else if(clazz.equals(Float.class)){
			return new Float(str);
		}
		else if(clazz.equals(Long.class)){
			return new Long(str);
		}
		else if(clazz.equals(Byte.class)){
			return new Byte(str);
		}
		else if(clazz.equals(Character.class)){
			return new Character(str.charAt(0));
		}
		else if(clazz.equals(Boolean.class)){
			return new Boolean(str);
		}
		
		return null;
	}
	
	
	public static Object fromString(String value, Class<?> clazz)
	{
		if(value == null){
			return null;
		}
		
		if(clazz.equals(Boolean.class)){
			return Boolean.parseBoolean(value);
		}
		else if(clazz.equals(String.class)){
			return value;
		}
		else if(clazz.equals(Integer.class)){
			return Integer.parseInt(value);
		}
		else if(clazz.equals(Double.class)){
			return Double.parseDouble(value);
		}
		else if(clazz.equals(Float.class)){
			return Float.parseFloat(value);
		}
		else if(clazz.equals(Long.class)){
			return Long.parseLong(value);
		}
		else if(clazz.equals(Byte.class)){
			return Byte.parseByte(value);
		}
		
		return null;
	}
}

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

public class BeanProperty
{
	private String name;
	private Class<?> clazz;
	
	public Class<?> getClazz()
	{
		return clazz;
	}
	public void setClazz(Class<?> clazz)
	{
		this.clazz = clazz;
	}
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getGetterName()
	{
		StringBuffer buff = new StringBuffer();
		
		buff.append("get");
		buff.append(Character.toUpperCase(this.name.charAt(0)));
		buff.append(this.name.substring(1));
		
		return buff.toString();
	}
	
	public String getSetterName()
	{
		StringBuffer buff = new StringBuffer();
		
		buff.append("set");
		buff.append(Character.toUpperCase(this.name.charAt(0)));
		buff.append(this.name.substring(1));
		
		return buff.toString();
	}
}

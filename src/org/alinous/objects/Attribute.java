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
package org.alinous.objects;

import java.io.IOException;
import java.io.Writer;

import org.alinous.exec.pages.PostContext;
import org.alinous.objects.optimize.StaticAttribute;
import org.alinous.script.runtime.VariableRepository;

public class Attribute implements Cloneable, IAttribute
{
	private String key;
	private IAttributeValue value;
	
	public String getKey() 
	{
		return key;
	}
	
	public void setKey(String key)
	{
		this.key = key.toLowerCase();
	}
	
	public IAttributeValue getValue()
	{
		return value;
	}
	
	public void setValue(IAttributeValue value)
	{
		this.value = value;
	}
	
	public IAttribute clone() throws CloneNotSupportedException
	{
		Attribute attr = new Attribute();
		attr.setKey(this.key);
		attr.setValue(this.value);
		
		return attr;
	}
	
	public void renderContents(Writer wr, int n, PostContext context, VariableRepository valRepo,
						boolean adjustUri)
		throws IOException
	{
		wr.append(this.key);
		
		if(this.value != null && adjustUri){
			wr.append("=");
			this.value.renderAdjustedUriContents(wr, n, context, valRepo);
			return;
		}
		if(this.value != null){
			wr.append("=");
			this.value.renderContents(wr, n, context, valRepo);
		}
	}
	
	public boolean isDynamic()
	{
		/*
		if(key.toLowerCase().equals("value") || key.toLowerCase().equals("checked")
				|| key.equals("selected")){
			return true;
		}*/
		if(this.value == null){
			return false;
		}
		
		return this.value.isDynamic();
	}

	public IAttribute toStatic()
	{
		StaticAttribute staticAttr = new StaticAttribute();
		staticAttr.setKey(this.key);
		
		if(this.value == null){
			return staticAttr;
		}
		
		staticAttr.setStaticValue(this.value);
		
		return staticAttr;
	}


}

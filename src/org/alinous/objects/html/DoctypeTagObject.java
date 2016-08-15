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
package org.alinous.objects.html;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.objects.IAlinousObject;
import org.alinous.objects.IAttributeValue;
import org.alinous.objects.XMLTagBase;
import org.alinous.script.runtime.VariableRepository;

public class DoctypeTagObject extends XMLTagBase implements IHtmlObject
{
	private List<String> identifers = new ArrayList<String>();
	private List<IAttributeValue> attributeValues = new ArrayList<IAttributeValue>();
	
	public void addIdentifier(String identifier)
	{
		this.identifers.add(identifier);
	}
	
	public void addAttribute(IAttributeValue attribute)
	{
		this.attributeValues.add(attribute);
	}
	
	public IAlinousObject fork() throws AlinousException
	{
		DoctypeTagObject newObj = new DoctypeTagObject();
		
		copyAttribute(this, newObj);
		
		forkInnerObjects(newObj);
		
		Iterator<String> idIt = this.identifers.iterator();
		while(idIt.hasNext()){
			String id = idIt.next();
			newObj.addIdentifier(id);
		}
		
		Iterator<IAttributeValue> it = this.attributeValues.iterator();
		while(it.hasNext()){
			IAttributeValue attrVal = it.next();
			newObj.addAttribute(attrVal);
		}
		
		return newObj;
	}

	public void renderContents(PostContext context, VariableRepository valRepo, Writer wr, int n) throws IOException, AlinousException
	{
		//wr.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
		
		wr.append("<!DOCTYPE HTML ");
		
		Iterator<String> idIt = this.identifers.iterator();
		while(idIt.hasNext()){
			String id = idIt.next();
			wr.append(id);
			wr.append(" ");
		}
		
		int i = 0;
		Iterator<IAttributeValue> it = this.attributeValues.iterator();
		while(it.hasNext()){
			IAttributeValue attrVal = it.next();
			attrVal.renderContents(wr, n, context, valRepo);
			
			i++;
			if(i != this.attributeValues.size()){
				wr.append(" ");
			}
			
		}
		
		wr.append(">");
		
	}

}

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
import java.util.Iterator;

import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.objects.IAlinousObject;
import org.alinous.objects.XMLTagBase;
import org.alinous.script.runtime.VariableRepository;

public class UnknownTagObject extends XMLTagBase implements IHtmlObject
{
	private String tagName;

	public IAlinousObject fork() throws AlinousException
	{
		UnknownTagObject newObj = new UnknownTagObject();
		newObj.setTagName(this.tagName);
		
		copyAttribute(this, newObj);
		
		forkInnerObjects(newObj);
		
		return newObj;
	}

	public void renderContents(PostContext context, VariableRepository valRepo, Writer wr, int n)
							throws IOException, AlinousException
	{
		if(!handleIf(context, valRepo)){
			return;
		}
		
		if(handleIterateAttribute(context, valRepo, wr, n)){
			return;
		}
		
		doRenderContent(context, valRepo, wr, n);
	}

	
	protected void doRenderContent(PostContext context, VariableRepository valRepo, Writer wr, int n)
			throws IOException, AlinousException
	{
		if(this.innerObj.size() == 0){
			
			wr.append("<" + this.tagName);
			renderAttributes(context, valRepo, wr, 0);
			wr.append("/>");
			return;
		}
		
		
		wr.append("<" + this.tagName);
		renderAttributes(context, valRepo, wr, 0);
		wr.append(">");
		
		renderInnerContents(context, valRepo, wr, n + 1);
		
		wr.append("</"+ this.tagName + ">");
	}
	
	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	@Override
	public void optimizeSelf(PostContext context, XMLTagBase owner, boolean forceDynamic) throws IOException, AlinousException
	{
		super.optimizeSelf(context, owner, forceDynamic);
	}

	@Override
	public boolean isDynamic()
	{
		Iterator<XMLTagBase> it = this.innerObj.iterator();
		while(it.hasNext()){
			XMLTagBase tagObj = it.next();
			
			if(tagObj.isDynamic()){
				return true;
			}
		}
		
		Iterator<String> itAttr = this.attributes.keySet().iterator();
		while(itAttr.hasNext()){
			String attrName = itAttr.next();
			
			if(this.attributes.get(attrName).isDynamic()){
				return true;
			}	
		}
		
		itAttr = this.alinousAttributes.keySet().iterator();
		while(itAttr.hasNext()){
			String attrName = itAttr.next();
			
			if(this.alinousAttributes.get(attrName).isDynamic()){
				return true;
			}
			
		}
		
		return false;
	}

	
	
}

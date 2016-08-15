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
import org.alinous.expections.ExecutionException;
import org.alinous.objects.IAlinousObject;
import org.alinous.objects.XMLTagBase;
import org.alinous.script.runtime.VariableRepository;


public class HtmlTagObject extends XMLTagBase implements IHtmlObject
{
	public void renderContents(PostContext context, VariableRepository valRepo, Writer wr, int n)
									throws IOException, AlinousException
	{
		if(!handleIf(context, valRepo)){
			return;
		}
		
		if(context.isInner()){
			BodyTagObject body = findBodyTagObject();
			body.renderContents(context, valRepo, wr, n);
			return;
		}
		
		wr.append("<html");
		renderAttributes(context, valRepo, wr, 0);
		wr.append(">");
		
		renderInnerContents(context, valRepo, wr, n + 1);
		
		wr.append("</html>\n");
	}
	
	private BodyTagObject findBodyTagObject() throws ExecutionException
	{
		Iterator<XMLTagBase> it = this.innerObj.iterator();
		while(it.hasNext()){
			XMLTagBase base = it.next();
			
			if(base instanceof BodyTagObject){
				return (BodyTagObject)base;
			}
		}
		
		throw new ExecutionException("Html has no Body Tag.");
	}
	
	public IAlinousObject fork() throws AlinousException
	{
		HtmlTagObject newObj = new HtmlTagObject();
		
		copyAttribute(this, newObj);
		
		forkInnerObjects(newObj);
		
		return newObj;
	}

	public String getTagName()
	{
		return "HTML";
	}
	
	// Always dynamic
	public void optimizeSelf(PostContext context, XMLTagBase owner, boolean forceDynamic) throws IOException, AlinousException
	{
		registerObject(context, owner, this.getClass(), forceDynamic);
		return;
	}

}

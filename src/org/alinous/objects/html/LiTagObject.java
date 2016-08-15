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
import java.util.Enumeration;

import org.alinous.AlinousUtils;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.objects.Attribute;
import org.alinous.objects.IAlinousObject;
import org.alinous.objects.XMLTagBase;
import org.alinous.script.runtime.VariableRepository;


public class LiTagObject extends XMLTagBase implements IHtmlObject
{
	public IAlinousObject fork() throws AlinousException
	{
		LiTagObject newObj = new LiTagObject();
		
		copyAttribute(this, newObj);
		
		forkInnerObjects(newObj);
		
		return newObj;
	}

	public void renderContents(PostContext context, VariableRepository valRepo, Writer wr, int n) throws IOException, AlinousException
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
		wr.append("<li");
		renderAttributes(context, valRepo, wr, 0);
		wr.append(">");
		
		renderInnerContents(context, valRepo, wr, n + 1);
		
		wr.append("</li>");
	}
	

	@SuppressWarnings("rawtypes")
	protected void renderExtractAttributes(PostContext context, VariableRepository valRepo, Writer wr, int n, boolean renderValue, boolean adjustUri) throws IOException
	{
		Enumeration enm = this.attributes.keys();
		while(enm.hasMoreElements()){
			Object key = enm.nextElement();
			Attribute atr =(Attribute)this.getAttribute((String)key);

			if(atr.getKey().toLowerCase().equals("src") && !renderValue){
				continue;
			}
			if(atr.getKey().toLowerCase().equals("value") && !renderValue){
				continue;
			}
			if(atr.getKey().toLowerCase().equals("checked") && !renderValue){
				continue;
			}
			if(atr.getKey().toLowerCase().equals("selected") && !renderValue){
				continue;
			}
			if(atr.getKey().toLowerCase().equals("href") && !renderValue){
				continue;
			}
			if(atr.getKey().toLowerCase().equals("action") && !renderValue){
				continue;
			}
			
			boolean doAdjustUri = false;
			if(adjustUri && AlinousUtils.isUriReleavant(atr.getKey())){
				doAdjustUri = true;
			}
			
			wr.write(" ");
			atr.renderContents(wr, n + 1, context, valRepo, doAdjustUri);
		}
	}
	
	public String getTagName()
	{
		return "LI";
	}

}

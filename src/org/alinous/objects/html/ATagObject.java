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
import java.io.StringReader;
import java.io.Writer;

import org.alinous.AlinousUtils;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.objects.AlinousAttrs;
import org.alinous.objects.HrefTargetParser;
import org.alinous.objects.IAlinousObject;
import org.alinous.objects.IAttribute;
import org.alinous.objects.XMLTagBase;
import org.alinous.parser.script.attr.AlinousAttrScriptParser;
import org.alinous.script.attribute.ParsedAttribute;
import org.alinous.script.runtime.VariableRepository;


public class ATagObject extends XMLTagBase implements IHtmlObject
{

	public IAlinousObject fork() throws AlinousException
	{
		ATagObject newObj = new ATagObject();
		
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
		
		wr.append("<a");
		renderAttributes(context, valRepo, wr, 0, false, true);
		
		renderHref(context, valRepo, wr, n);
		wr.append(">");
		
		renderInnerContents(context, valRepo, wr, n + 1);
		
		wr.append("</a>");
	}
	
	private void renderHref(PostContext context, VariableRepository valRepo, Writer wr, int n)
										throws IOException, AlinousException
	{
		IAttribute targetAttr = this.alinousAttributes.get(AlinousAttrs.ALINOUS_TARGET);
		IAttribute hrefAttr = this.attributes.get("href");
		if(targetAttr == null){
			if(hrefAttr != null){
				wr.append(" ");
				
				String hrefStr =hrefAttr.getValue().getParsedValue(context, valRepo);
				
				wr.append("href=\"");
				hrefStr = AlinousUtils.addRewriteSessionString(hrefStr, context.getSessionId(), context.isStatic(), context.getCore());
				wr.write(hrefStr);
				
				wr.append("\"");
			}
			
			return;
		}
		
		// Target is specified
		String value = makeHrefString(context, valRepo, hrefAttr.getValue().getValue(), targetAttr.getValue().getValue());
		
		wr.append(" ");
		wr.append("href=\"");
		
		String hrefStr = AlinousUtils.addRewriteSessionString(context.getFilePath(value), context.getSessionId(), context.isStatic(), context.getCore());
		wr.append(hrefStr);
		
		wr.append("\"");
	}
	

	private String makeHrefString(PostContext context, VariableRepository valRepo, String href, String targetTag) throws AlinousException
	{
		// parse and input valuable
		href = getParsedValue(context, valRepo, href);
		
		HrefTargetParser targetParser = new HrefTargetParser(href);
		
		AlinousTopObject thisPage = getTopObject();
		AlinousTopObject toptopObj = context.getTopTopObject();
		
		targetParser.setTopTopPage(toptopObj.getPath());
		targetParser.setThisPagePath(thisPage);
		targetParser.setTargetTagId(targetTag);
		
		return targetParser.getString(context);
	}

	private String getParsedValue(PostContext context, VariableRepository valRepo, String value)
	{
		StringReader reader = new StringReader("<" + value + ">");
		String str = null;
		
		AlinousAttrScriptParser parser = new AlinousAttrScriptParser(reader);
		try {
			ParsedAttribute attr = parser.parse();
			str = attr.expand(context, valRepo);
		} catch (Throwable e) {
			
			reader.close();
			return null;
		}
		
		reader.close();
		
		return str;
	}
	
	public String getTagName()
	{
		return "A";
	}






}

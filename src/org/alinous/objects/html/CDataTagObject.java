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

import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.objects.IAlinousObject;
import org.alinous.objects.XMLTagBase;
import org.alinous.parser.script.attr.AlinousAttrScriptParser;
import org.alinous.parser.script.attr.ParseException;
import org.alinous.script.attribute.ParsedAttribute;
import org.alinous.script.runtime.VariableRepository;

public class CDataTagObject extends XMLTagBase implements IHtmlObject
{
	private String content;
	
	public IAlinousObject fork() throws AlinousException
	{
		CDataTagObject newObj = new CDataTagObject();
		
		copyAttribute(this, newObj);
		
		forkInnerObjects(newObj);
		
		newObj.setContent(this.content);
		
		return newObj;
	}

	protected void registerSelf(PostContext context, XMLTagBase owner, XMLTagBase newObj)
	{
		((CDataTagObject)newObj).setContent(this.content);
	}

	public void renderContents(PostContext context, VariableRepository valRepo,
			Writer wr, int n) throws IOException, AlinousException
	{
		doRenderContent(context, valRepo, wr, n);
	}
	
	protected void doRenderContent(PostContext context, VariableRepository valRepo, Writer wr, int n)
	throws IOException, AlinousException
	{
		wr.append("<![CDATA[");
		//wr.append(this.content);
		//renderInnerContents(context, valRepo, wr, n + 1);
		
		//wr.append("]]>");
		
		String contentString = this.content.substring(0, this.content.length() - "]]>".length());
		
		StringReader reader = new StringReader("<" + contentString + ">");
		AlinousAttrScriptParser parser = new AlinousAttrScriptParser(reader);
		try {
			ParsedAttribute p = parser.parse();
			String resultStr = p.expand(context, valRepo);
			
			wr.write(resultStr);
			wr.write("]]>");
		} catch (ParseException e) {
			wr.write(this.content);
		}catch (Throwable e) {
			//e.printStackTrace();
			wr.write(this.content);
		}finally{
			reader.close();
		}
	}


	public String getContent()
	{
		return content;
	}


	public void setContent(String content)
	{
		this.content = content;
	}


	@Override
	public boolean isDynamic()
	{
		return true;
	}
	


	
}

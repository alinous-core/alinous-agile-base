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
import org.alinous.exec.ExecResultCache;
import org.alinous.exec.FormValueCache;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.objects.IAlinousObject;
import org.alinous.objects.XMLTagBase;
import org.alinous.parser.script.attr.AlinousAttrScriptParser;
import org.alinous.parser.script.attr.ParseException;

import org.alinous.script.attribute.ParsedAttribute;
import org.alinous.script.runtime.VariableRepository;

public class StringContainer extends XMLTagBase implements IHtmlObject
{
	private String str;
	
	public void post(PostContext context, ExecResultCache resCache,
						FormValueCache formValues, VariableRepository valRepo)
	{
		postInner(context, resCache, formValues, valRepo);
	}

	public IAlinousObject fork() throws AlinousException
	{
		StringContainer newObj = new StringContainer();
		newObj.str = this.str;
		
		copyAttribute(this, newObj);
		
		forkInnerObjects(newObj);
		
		return newObj;
	}

	public void renderContents(PostContext context, VariableRepository valRepo, Writer wr, int n) throws IOException
	{
		
		StringReader reader = new StringReader("<" + this.str + ">");
		AlinousAttrScriptParser parser = new AlinousAttrScriptParser(reader);
		try {
			ParsedAttribute p = parser.parse();
			String resultStr = p.expand(context, valRepo);
			
			wr.write(resultStr);
		} catch (ParseException e) {
			wr.write(this.str);
		}catch (Throwable e) {
			wr.write(this.str);
		}finally{
			reader.close();
		}
	
	}

	public String getStr() {
		return str;
	}

	public void setStr(String str) {
		this.str = str;
	}

	public String getTagName()
	{
		return null;
	}
	
	// 
	public void optimizeSelf(PostContext context, VariableRepository valRepo, XMLTagBase owner, boolean forceDynamic) throws IOException, AlinousException
	{
		if(isDynamic()){
			registerObject(context, owner, this.getClass(), forceDynamic);
			return;
		}
		
		// if static contents;
		renderContents(null, valRepo, owner.getStaticBuffer().getWriter(), 0);
		
		return;
	}
	
	public void registerObject(PostContext context, XMLTagBase owner, Class<?> clazz, boolean forceDynamic) throws IOException, AlinousException
	{
		// at first, add StaticBuffer element
		if(owner.isBufferExists()){
			owner.addInnerObject(owner.getStaticBuffer());
			// Fixme clear
			owner.clearStaticBuffer();
		}
		
		// After that, add dynamic
		StringContainer newObj = new StringContainer();
		newObj.str = this.str;
		
		owner.addInnerObject(newObj);
		
	}
	
	public boolean isDynamic()
	{
		StringReader reader = new StringReader("<" + this.str + ">");
		boolean result = true;
		
		AlinousAttrScriptParser parser = new AlinousAttrScriptParser(reader);
		try {
			ParsedAttribute attr = parser.parse();
			
			result = attr.isDynamic();
		} catch (Throwable e) {
			result =  false;
		}
		finally{
			reader.close();
		}
		
		return result;
	}
}

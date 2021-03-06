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
import java.io.StringReader;
import java.io.Writer;

import org.alinous.exec.pages.PostContext;
import org.alinous.parser.script.attr.AlinousAttrScriptParser;
import org.alinous.script.attribute.ParsedAttribute;
import org.alinous.script.runtime.VariableRepository;

public class DqString implements IAttributeValue
{
	private String value;
	private ParsedAttribute parsedAttr;
	
	public DqString(String str)
	{
		this.value = str;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public String toString()
	{
		return "\"" + this.value + "\"";
	}
	
	public String getParsedValue(PostContext context, VariableRepository valRepo)
	{
		StringReader reader = null;
		String str = null;
		
		AlinousAttrScriptParser parser = null;
		try {
			if(this.parsedAttr == null){
				reader = new StringReader("<" + this.value + ">");
				parser = new AlinousAttrScriptParser(reader);
				this.parsedAttr = parser.parse();
				reader.close();
			}

			str = this.parsedAttr.expand(context, valRepo);
		} catch (Throwable e) {
			// debug
			//e.printStackTrace();
			
			reader.close();
			return null;
		}
		
		
		return str;
	}
	
	public void renderContents(Writer wr, int n, PostContext context, VariableRepository valRepo) throws IOException
	{
		StringReader reader = null;
		
		AlinousAttrScriptParser parser = null;
		try {
			if(this.parsedAttr == null){
				reader = new StringReader("<" + this.value + ">");
				parser = new AlinousAttrScriptParser(reader);
				this.parsedAttr = parser.parse();
				reader.close();
			}
			
			String str = this.parsedAttr.expand(context, valRepo);
			wr.write("\"");
			wr.write(str);
			wr.write("\"");
		} catch (Throwable e) {
			// debug
			e.printStackTrace();
			
			wr.write(toString());
			if(reader != null){
				reader.close();
			}
			return;
		}
		
		
	
	}

	public void renderAdjustedUriContents(Writer wr, int n, PostContext context, VariableRepository valRepo)
			throws IOException
	{
		StringReader reader = new StringReader("<" + this.value + ">");
		
		AlinousAttrScriptParser parser = new AlinousAttrScriptParser(reader);
		try {
			ParsedAttribute attr = parser.parse();
			String str = attr.expand(context, valRepo);
			wr.write("\"");
			wr.write(context.getFilePath(str));
			wr.write("\"");
		} catch (Throwable e) {
			//e.printStackTrace();
			
			wr.write(toString());
			reader.close();
			return;
		}
		
		reader.close();
	
		
	}

	public boolean isDynamic()
	{
		StringReader reader = new StringReader("<" + this.value + ">");
		boolean result = true;
		
		AlinousAttrScriptParser parser = new AlinousAttrScriptParser(reader);
		try {
			this.parsedAttr = parser.parse();
			
			result = this.parsedAttr.isDynamic();
		} catch (Throwable e) {
			return false;
		}
		finally{
			reader.close();
		}
		
		return result;
	}

}

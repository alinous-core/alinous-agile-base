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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
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
import org.alinous.objects.css.CssFileConverter;
import org.alinous.parser.script.attr.AlinousAttrScriptParser;
import org.alinous.script.attribute.ParsedAttribute;
import org.alinous.script.runtime.VariableRepository;


public class LinkTagObject extends XMLTagBase implements IHtmlObject
{
	public IAlinousObject fork() throws AlinousException
	{
		LinkTagObject newObj = new LinkTagObject();
		
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
		
		if(isCss2Extract(context, valRepo)){
			renderExtract(context, valRepo, wr, n);
			return;
		}
		
		if(this.innerObj.size() > 0){
			wr.append("<link");
			renderAttributes(context, valRepo, wr, 0, false, true);
			renderHref(context, valRepo, wr, n);
			wr.append(">\n");
			
			renderInnerContents(context, valRepo, wr, n + 1);
			
			wr.append("</LINK>");
			return;
		}
		
		wr.append("<link");
		renderAttributes(context, valRepo, wr, 0, false, true);
		renderHref(context, valRepo, wr, n);
		wr.append(">");
	}
	
	private void renderExtract(PostContext context, VariableRepository valRepo, Writer wr, int n) throws IOException
	{
		IAttribute attr = this.getAttribute("href");
		String cssPath = attr.getValue().getParsedValue(context, valRepo);
		
		String absPath = AlinousUtils.getAbsolutePath(context.getCore().getHome(), cssPath);
		
		String replaceBase = AlinousUtils.getDirectory(cssPath);
		String scriptText = CssFileConverter.convert(absPath, replaceBase);
		
		wr.append("<style>\n");
		wr.append("<!--\n");
		
		wr.append(scriptText);
		
		wr.append("\n-->\n");
		wr.append("</style>\n");
	}
	
	private boolean isCss2Extract(PostContext context, VariableRepository valRepo)
	{
		IAttribute attr = this.getAttribute("rel");
		if(attr == null || attr.getValue() == null){
			return false;
		}
		
		String rel = attr.getValue().getParsedValue(context, valRepo);
		if(rel == null || !rel.toLowerCase().equals("stylesheet")){
			return false;
		}
		
		
		IAttribute exattr = this.getAlinousAttribute(AlinousAttrs.ALINOUS_EXTRACT);
		if(exattr == null || exattr.getValue() == null){
			return false;
		}
		
		String exValue = exattr.getValue().getParsedValue(context, valRepo);
		
		if(exValue == null || !exValue.toLowerCase().equals("true")){
			return false;
		}
		
		return true;		
	}

	private void renderHref(PostContext context, VariableRepository valRepo, Writer wr, int n)
			throws IOException, AlinousException
	{
		IAttribute targetAttr = this.alinousAttributes.get(AlinousAttrs.ALINOUS_TARGET);
		IAttribute hrefAttr = this.attributes.get("href");
		if(targetAttr == null){
			if(hrefAttr != null){
				wr.append(" ");
				hrefAttr.renderContents(wr, n, context, valRepo, true);
			}
		
			return;
		}
	
		// Target is specified
		String value = makeHrefString(context, valRepo, hrefAttr.getValue().getParsedValue(context, valRepo), targetAttr.getValue().getParsedValue(context, valRepo));
		
		wr.append(" ");
		wr.append("href=\"");
		
		wr.append(context.getFilePath(value));
		
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
			//e.printStackTrace();
			
			reader.close();
			return null;
		}
		
		reader.close();
		
		return str;
	}
	
	
	public String readAllText(String srcPath, String encode)
	{
		StringBuffer buff = new StringBuffer();
		
		File file = new File(srcPath);
		FileInputStream fStream = null;
		InputStreamReader reader = null;
		try {
			fStream = new FileInputStream(file);
			reader = new InputStreamReader(fStream, encode);
			
			char readBuffer[] = new char[1024];
			int n = 1;
			
			while(n > 0){
				n = reader.read(readBuffer, 0, readBuffer.length);
				
				if(n > 0){
					buff.append(readBuffer, 0, n);
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally{
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e) {e.printStackTrace();}
			}
			if(fStream != null){
				try {
					fStream.close();
				} catch (IOException e) {e.printStackTrace();}
			}
		}
		
		return buff.toString();	
	}
	
	public String getTagName()
	{
		return "LINK";
	}

}

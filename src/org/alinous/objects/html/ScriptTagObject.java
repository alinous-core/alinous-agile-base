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
import java.io.Writer;
import java.util.Enumeration;

import org.alinous.AlinousUtils;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.objects.AlinousAttrs;
import org.alinous.objects.Attribute;
import org.alinous.objects.IAlinousObject;
import org.alinous.objects.IAttribute;
import org.alinous.objects.XMLTagBase;
import org.alinous.script.runtime.VariableRepository;


public class ScriptTagObject extends XMLTagBase implements IHtmlObject
{
	public IAlinousObject fork() throws AlinousException
	{
		ScriptTagObject newObj = new ScriptTagObject();
		
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
		
		IAttribute attr = this.getAlinousAttribute(AlinousAttrs.ALINOUS_EXTRACT);
		
		if(attr != null && attr.getValue() != null && attr.getValue().getParsedValue(context, valRepo).equals("true")){
			attr = this.getAttribute("src");
			if(attr != null && attr.getValue() != null){
				renderExtract(context, valRepo, wr, n);
				return;
			}
		}
		
		wr.append("<script");
		renderAttributes(context, valRepo, wr, 0, true, true);
		wr.append(">");
		
		renderInnerContents(context, valRepo, wr, n + 1);
		
		wr.append("</script>");
	}
	
	private void renderExtract(PostContext context, VariableRepository valRepo, Writer wr, int n) throws IOException
	{
		IAttribute attr = this.getAttribute("src");
		String alinousPath = attr.getValue().getParsedValue(context, valRepo);
		
		alinousPath = AlinousUtils.getAbsolutePath(context.getCore().getHome(), alinousPath);
		
		String scriptText = readAllText(alinousPath, "utf-8");
		
		wr.append("<script>\n");
		wr.append("<!-- //\n");
		
		wr.append(scriptText);
		
		wr.append("// -->\n");
		wr.append("</script>\n");
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
		return "Script";
	}

}

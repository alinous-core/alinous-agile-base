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

import org.alinous.AlinousCore;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.objects.IAlinousObject;
import org.alinous.objects.XMLTagBase;
import org.alinous.script.runtime.VariableRepository;


public class BodyTagObject extends XMLTagBase implements IHtmlObject
{
	public void renderContents(PostContext context, VariableRepository valRepo, Writer wr, int n)
				throws IOException, AlinousException
	{
		if(!handleIf(context, valRepo)){
			return;
		}
		
		if(context.isInner()){
			renderInnerContents(context, valRepo, wr, n + 1);
			return;
		}
		
		wr.append("<body");
		renderAttributes(context, valRepo, wr, 0);
		wr.append(">");
		
		renderInnerContents(context, valRepo, wr, n + 1);
		
		showAd(wr);
		
		wr.append("</body>");
		
	}
	
	private void showAd(Writer wr) throws IOException
	{
		if(AlinousCore.showad){
			wr.append("<div style=\"position: absolute; top: 0px;right: 0px;");
			wr.append("background-color:#ffffff;");
			wr.append("border-left:1px solid #909090;");
			wr.append("border-left:1px solid #909090;");
			wr.append("border-right:1px solid #909090;");
			wr.append("border-top:1px solid #909090;");
			wr.append("border-bottom:1px solid #909090;");
			wr.append("font-size: 9pt;letter-spacing: 0.05em;");
			wr.append("line-height:14px;\">");
			wr.append("<a href=\"" + AlinousCore.adUrl + "\"><img src=\"" + AlinousCore.adImg + "\" border=\"0\" alt=\"" + AlinousCore.adAlt + "\"></a>");
			
			wr.append("</div>");
		}
	}

	public IAlinousObject fork() throws AlinousException
	{
		BodyTagObject newObj = new BodyTagObject();
		
		copyAttribute(this, newObj);
		
		forkInnerObjects(newObj);
		
		return newObj;
	}

	public String getTagName()
	{
		return "BODY";
	}
	
	// Always dynamic
	public void optimizeSelf(PostContext context, XMLTagBase owner, boolean forceDynamic) throws IOException, AlinousException
	{
		registerObject(context, owner, this.getClass(), forceDynamic);
		return;
	}
	
	@Override
	public boolean isDynamic()
	{
		return true;
	}
}

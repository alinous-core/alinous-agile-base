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

import org.alinous.components.IAlinousComponent;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.objects.AlinousAttrs;
import org.alinous.objects.IAlinousObject;
import org.alinous.objects.IAttribute;
import org.alinous.objects.IAttributeValue;
import org.alinous.objects.XMLTagBase;
import org.alinous.script.runtime.VariableRepository;


public class DivTagObject extends XMLTagBase implements IHtmlObject
{

	public IAlinousObject fork() throws AlinousException
	{
		DivTagObject newObj = new DivTagObject();
		
		copyAttribute(this, newObj);
		
		forkInnerObjects(newObj);
		
		return newObj;
	}

	public void renderContents(PostContext context, VariableRepository valRepo, Writer wr, int n)
						throws IOException, AlinousException
	{
		if(hasComponentAttribute()){
			// component
			String compoString = this.alinousAttributes.get(AlinousAttrs.ALINOUS_COMPONENT).getValue().getParsedValue(context, valRepo);
			
			IAlinousComponent cmp = context.getCore().getCmpManager().getComponentManager(compoString);
			
			if(cmp == null){
				throw new AlinousException("Wrong component name : " + compoString); //i18n
			}
			
			try {
				cmp.renderContents(this.alinousAttributes, context, valRepo, wr, n);
			} catch (DataSourceException e) {
				throw new AlinousException(e, "Datasource exeption"); // i18n
			}
			return;
		}
		
		
		if(!handleIf(context, valRepo)){
			return;
		}
		
		if(handleIterateAttribute(context, valRepo, wr, n)){
			return;
		}
		
		doRenderContent(context, valRepo, wr, n);
	}
	
	
	
	
	private boolean hasComponentAttribute()
	{
		IAttribute attr = this.alinousAttributes.get(AlinousAttrs.ALINOUS_COMPONENT);
		
		if(attr == null){
			return false;
		}
		IAttributeValue va = attr.getValue();
		if(va == null){
			return false;
		}
		if(va.getValue() == null){
			return false;
		}
		
		return true;
	}
	
	protected void doRenderContent(PostContext context, VariableRepository valRepo, Writer wr, int n)
			throws IOException, AlinousException
	{
		boolean ignore = false;
		IAttribute ignoreSelf = this.alinousAttributes.get(AlinousAttrs.ALINOUS_IGNORE_SELF_ITERATE);
		if(ignoreSelf != null){
			ignore = !ignoreSelf.getValue().getValue().equals("false");
		}
		
		if(!ignore){
			wr.append("<div");
			renderAttributes(context, valRepo, wr, 0);
			wr.append(">");
		}
		
		if(!handleInnerTag(context, valRepo, wr, n)){
			renderInnerContents(context, valRepo, wr, n + 1);
		}
		
		if(!ignore){
			wr.append("</div>");
		}
	}

	public String getTagName()
	{
		return "DIV";
	}

}

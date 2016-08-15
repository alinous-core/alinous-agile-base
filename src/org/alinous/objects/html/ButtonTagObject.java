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
import java.util.ArrayList;
import java.util.List;

import org.alinous.exec.InnerModulePath;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.objects.AlinousAttrs;
import org.alinous.objects.IAlinousObject;
import org.alinous.objects.IAttribute;
import org.alinous.objects.XMLTagBase;
import org.alinous.script.runtime.VariableRepository;



public class ButtonTagObject extends XMLTagBase implements IHtmlObject
{
	public static final String ATTR_ACTION = "action";

	public IAlinousObject fork() throws AlinousException
	{
		ButtonTagObject newObj = new ButtonTagObject();
		
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
		
		if(handleBack(context, valRepo, wr, n)){
			return;
		}
		
		wr.append("<button");
		renderAttributes(context, valRepo, wr, 0, true, true);
		wr.append(">");
		
		renderInnerContents(context, valRepo, wr, n + 1);
		
		wr.append("</button>");
	}
	
	private boolean handleBack(PostContext context, VariableRepository valRepo, Writer wr, int n)
					throws IOException, AlinousException
	{
		IAttribute backAttr = this.alinousAttributes.get(AlinousAttrs.ALINOUS_BACK);
		if(backAttr == null || !backAttr.getValue().getParsedValue(context, valRepo).toLowerCase().equals("true")){
			return false;
		}
		
		String lastAction = 
			context.getUnit().getBackingStatusCache().getLastPath(context, context.getModulePath(),
					getTopObject().getPath(), context.getSessionId());

		String action = null;
		if(context.isInner()){
			action = context.getTopTopObject().getPath();
		}else{
			if(lastAction != null){
				action = lastAction;
			}else{
				action = context.getFormLastAction();
				lastAction = action;
			}
		}
		
		wr.append("<form ");
		wr.append("action=\"");
		wr.append(context.getFilePath(action));
		wr.append("\"");
		wr.append(" method=\"POST\">\n");
		
		wr.append("<input type=\"submit\" name=\"back\" value=\"");
		
		if(!this.innerObj.isEmpty()){
			XMLTagBase base = this.innerObj.get(0);
			base.renderContents(context, valRepo, wr, n);
		}
		wr.append("\">");

		// Hidden information
		List<FormHiddenValue> hiddens = new ArrayList<FormHiddenValue>();
		
		InnerModulePath modulePath = context.getModulePath();
		FormHiddenValue hidden = new FormHiddenValue(FormTagObject.HIDDEN_FORM_TARGET_TAGID, modulePath.getStringPath());
		hiddens.add(hidden);
		
		hidden = new FormHiddenValue(FormTagObject.HIDDEN_FORM_ACTION, lastAction);
		hiddens.add(hidden);
		
		hidden = new FormHiddenValue(FormTagObject.HIDDEN_FORM_BACK, AlinousAttrs.VALUE_TRUE);
		hiddens.add(hidden);
		
		renderHiddens(context, wr, n, hiddens);
		
		wr.append("</form>\n");
		return true;
	}

	public String getTagName()
	{
		return "BUTTON";
	}

	@Override
	public void getFormInputObjects(List<XMLTagBase> formInputs)
	{
		formInputs.add(this);
	}
		
}

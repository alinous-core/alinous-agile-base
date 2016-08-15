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
import java.util.Iterator;
import java.util.List;

import org.alinous.exec.pages.PostContext;
import org.alinous.exec.validator.IValidator;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.objects.AlinousAttrs;
import org.alinous.objects.IAlinousObject;
import org.alinous.objects.IAttribute;
import org.alinous.objects.XMLTagBase;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;



public class SpanTagObject extends XMLTagBase implements IHtmlObject
{
	public IAlinousObject fork() throws AlinousException
	{
		SpanTagObject newObj = new SpanTagObject();
		
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
		
		if(handleValidationMsg(context, valRepo, wr, n)){
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
		boolean ignore = false;
		IAttribute ignoreSelf = this.alinousAttributes.get(AlinousAttrs.ALINOUS_IGNORE_SELF_ITERATE);
		if(ignoreSelf != null){
			ignore = !ignoreSelf.getValue().getValue().equals("false");
		}
		
		if(!ignore){
			wr.append("<span");
			renderAttributes(context, valRepo, wr, 0);
			wr.append(">");
		}
		
		if(!handleInnerTag(context, valRepo, wr, n)){
			renderInnerContents(context, valRepo, wr, n + 1);
		}
		
		if(!ignore){
			wr.append("</span>");
		}
	}
	
	private boolean handleValidationMsg(PostContext context, VariableRepository valRepo, Writer wr, int n) throws ExecutionException, RedirectRequestException
	{
		IAttribute attrMsg = this.alinousAttributes.get(AlinousAttrs.ALINOUS_MSG);
		IAttribute attrForm = this.alinousAttributes.get(AlinousAttrs.ALINOUS_FORM);
		IAttribute attrValidator = this.alinousAttributes.get(AlinousAttrs.ALINOUS_VALIDATE_TYPE);
		
		if(attrMsg == null || attrMsg.getValue().getValue().equals("")){
			return false;
		}
		
		// if other target fails ignore
		String strModPath = context.getModulePath().getStringPath();
		if(context.getFormLastTargetTagId() != null && 
				!context.getFormLastTargetTagId().equals(strModPath)){
			return true;
		}
		
		String msgInput = attrMsg.getValue().getParsedValue(context, valRepo);
		String msgForm = null;
		String msgValidators[] = null;
		
		if(attrForm != null){
			msgForm = attrForm.getValue().getParsedValue(context, valRepo);
		}
		if(attrValidator != null){
			msgValidators = attrValidator.getValue().getParsedValue(context, valRepo).split(",");
		}
		
		List<String> list = new ArrayList<String>();
		for(int i = 0; i < msgValidators.length; i++){
			StringBuffer buff = new StringBuffer();
			
			buff.append(IValidator.VARIABLE_NAME);
			
			if(msgForm != null){
				buff.append(".");
				buff.append(msgForm);
			}
			
			buff.append(".");
			buff.append(msgInput);
			
			buff.append(".");
			buff.append(msgValidators[i]);
			
			list.add(buff.toString());
		}
		
		boolean bl = hitValidator(list, context, valRepo);
		
		return !bl;
	}
	
	private boolean hitValidator(List<String> list, PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		Iterator<String> it = list.iterator();
		while(it.hasNext()){
			String path = it.next();
			
			IPathElement iPath = PathElementFactory.buildPathElement(path);
			IScriptVariable val = valRepo.getVariable(iPath, context);
			
			if(!(val instanceof ScriptDomVariable)){
				continue;
			}
			
			ScriptDomVariable dom = (ScriptDomVariable)val;
			if(dom.getValue() != null && dom.getValue().equals(AlinousAttrs.VALUE_TRUE)){
				return true;
			}
		}
		
		return false;
	}

	public String getTagName()
	{
		return "SPAN";
	}

}

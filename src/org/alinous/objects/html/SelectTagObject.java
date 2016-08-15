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

import org.alinous.exec.pages.IDesign;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.objects.AlinousAttrs;
import org.alinous.objects.IAlinousObject;
import org.alinous.objects.IAttribute;
import org.alinous.objects.XMLTagBase;
import org.alinous.script.runtime.VariableRepository;


public class SelectTagObject extends XMLTagBase implements IHtmlObject
{
	private int formArrayCount = -1;
	
	public IAlinousObject fork() throws AlinousException
	{
		SelectTagObject newObj = new SelectTagObject();
		
		copyAttribute(this, newObj);
		
		forkInnerObjects(newObj);
		
		return newObj;
	}

	public void renderContents(PostContext context, VariableRepository valRepo, Writer wr, int n)
								throws IOException, AlinousException
	{
		// debug Array count
		//AlinousDebug.debugOut("******************SelectTagObject Name : " + getName(context, valRepo));
		//AlinousDebug.debugOut("******************SelectTagObject formArrayCount : " + context.getFromVariableCounter(getName(context, valRepo)));
		
		if(!handleIf(context, valRepo)){
			return;
		}
		
		if(handleIterateAttribute(context, valRepo, wr, n)){
			return;
		}
		
		doRenderContent(context, valRepo, wr, n);
		
		List<FormHiddenValue> hiddens = new ArrayList<FormHiddenValue>();
		// handle validation info
		handleValidationInfo(context, valRepo, hiddens);
		
		// add alns:ignoreBlank
		handleIgnoreBlank(context, valRepo, hiddens);
		
		// render hiddens
		renderHiddens(context, wr, n, hiddens);
	}
	
	protected void doRenderContent(PostContext context, VariableRepository valRepo, Writer wr, int n)
		throws IOException, AlinousException
	{
		wr.append("<select");
		renderAttributes(context, valRepo, wr, 0);
		wr.append(">");
		
		renderInnerContents(context, valRepo, wr, n + 1);
		
		wr.append("</select>");
		
		context.incFromVariableCounter(getName(context, valRepo));
	}
	
	private void handleIgnoreBlank(PostContext context, VariableRepository valRepo, List<FormHiddenValue> hiddens)
	{
		IAttribute ignoreAttr = this.alinousAttributes.get(AlinousAttrs.ALINOUS_IGNOREBLANK);
		
		if(ignoreAttr == null){
			return;
		}
		
		StringBuffer buff = new StringBuffer();
		
		buff.append(AlinousAttrs.ALINOUS_IGNOREBLANK);
		buff.append(":");
		buff.append(getName(context, valRepo));
		
		FormHiddenValue hidden = new FormHiddenValue(buff.toString(), AlinousAttrs.VALUE_TRUE);
		hiddens.add(hidden);
	}
	
	public String getName(PostContext context, VariableRepository valRepo)
	{
		IAttribute nm = this.attributes.get(ATTR_NAME);
		if(nm != null){
			return removeArrayDesc(nm.getValue().getParsedValue(context, valRepo));
		}
		
		return "";
	}
	
	private String removeArrayDesc(String nameStr)
	{
		if(nameStr.endsWith("[]")){
			return nameStr.substring(0, nameStr.length() - 2);
		}
		
		return nameStr;
	}

	public String getTagName()
	{
		return "SELECT";
	}
	
	protected boolean isNameNeeded()
	{
		return true;
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

	public int getFormArrayCount() {
		return formArrayCount;
	}

	public void setFormArrayCount(int formArrayCount) {
		this.formArrayCount = formArrayCount;
	}
	
	@Override
	public void findFormParams(List<IHtmlObject> formParams)
	{
		formParams.add(this);
		
		super.findFormParams(formParams);
	}
	
	public String getDefaultStaticValue()
	{
		String def = null;
		Iterator<XMLTagBase> it = this.innerObj.iterator();
		while(it.hasNext()){
			IDesign design = it.next();
			
			if(design instanceof OptionTagObject){
				OptionTagObject opt = (OptionTagObject)design;
				
				if(opt.isStaticSelected()){
					def = opt.getStaticValue();
				}
				
				if(def == null){
					def = opt.getStaticValue();
				}
			}
		
		}
		
		return def;
	}
	
	@Override
	public void getFormInputObjects(List<XMLTagBase> formInputs)
	{
		int xpathIndex = getXpathCount(formInputs);
		setXpathIndex(xpathIndex);
		
		formInputs.add(this);
	}
	
	protected int getXpathCount(List<XMLTagBase> formInputs)
	{
		int cnt = 0;
		
		Iterator<XMLTagBase> it = formInputs.iterator();
		while(it.hasNext()){
			XMLTagBase tagBase = it.next();
			
			if(tagBase instanceof SelectTagObject){
				if(tagBase.getStaticId() != null && getStaticId() != null && tagBase.getStaticId().equals(getStaticId())){
					cnt++;
				}
				else if(tagBase.getStaticName() != null && getStaticName() != null && tagBase.getStaticName().equals(getStaticName())){
					cnt++;
				}
			}
		}
				
		return cnt;
	}
}

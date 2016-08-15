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
import java.util.Map;

import org.alinous.exec.pages.ArrayParamValue;
import org.alinous.exec.pages.FormValues;
import org.alinous.exec.pages.IParamValue;
import org.alinous.exec.pages.PostContext;
import org.alinous.exec.pages.StringParamValue;
import org.alinous.expections.AlinousException;
import org.alinous.objects.IAlinousObject;
import org.alinous.objects.IAttribute;
import org.alinous.objects.XMLTagBase;
import org.alinous.script.runtime.VariableRepository;


public class OptionTagObject extends XMLTagBase implements IHtmlObject
{
	public static final String ATTR_NAME = "name";
	public static final String ATTR_VALUE = "value";
	public static final String ATTR_SELECTED = "selected";
	
	public IAlinousObject fork() throws AlinousException
	{
		OptionTagObject newObj = new OptionTagObject();
		
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
		
		doRenderContent(context, valRepo, wr, n);
	}
	
	protected void doRenderContent(PostContext context, VariableRepository valRepo, Writer wr, int n)
		throws IOException, AlinousException
	{
		wr.append("<option");
		renderAttributes(context, valRepo, wr, 0, false, false);
		
		wr.write(" ");
		renderValue(context, valRepo, wr);
		wr.append(">");
		
		renderInnerContents(context, valRepo, wr, n + 1);
		
		wr.append("</option>");	
	}

	private void renderValue(PostContext context, VariableRepository valRepo, Writer wr) throws IOException
	{
		IAttribute attrValue = this.attributes.get(ATTR_VALUE);
		
		if(attrValue == null || attrValue.getValue() == null){
			return;
		}
		
		// render attribute		
		attrValue.renderContents(wr, 0, context, valRepo, false);
		
		renderChecked(context, valRepo, wr);			

		
	}
	
	private boolean renderCheckedByParentValue(PostContext context, VariableRepository valRepo, SelectTagObject select, Writer wr) throws IOException
	{
		IAttribute attr = select.getAttribute(ATTR_VALUE);
		if(attr == null){
			return false;
		}
		
		String parentValue = attr.getValue().getParsedValue(context, valRepo);
		
		IAttribute thisAttr = this.getAttribute(ATTR_VALUE);
		if(thisAttr == null){
			return false;
		}
		String thisValue = thisAttr.getValue().getParsedValue(context, valRepo);
		
		if(parentValue.equals(thisValue)){
			wr.write(" ");
			wr.write(ATTR_SELECTED);
		}
		return true;
	}
	
	private void renderChecked(PostContext context, VariableRepository valRepo, Writer wr) throws IOException
	{
		// If cache is not used
		if(!context.isUseFormCache()){
			XMLTagBase parent = this.getParent();
			if(parent instanceof SelectTagObject){
				if(renderCheckedByParentValue(context, valRepo, (SelectTagObject) parent, wr)){
					return;
				}	
			}
			
			
			IAttribute attrChecked = this.attributes.get(ATTR_SELECTED);
			
			if(attrChecked != null){
				attrChecked.renderContents(wr, 0, context, valRepo, false);
			}			
			
			return;
		}
		
		// setup cached checkboxx and button
		IAttribute attrValue = this.attributes.get(ATTR_VALUE);
		if(attrValue == null){
			return;
		}
		
		FormTagObject formTag = findFormTag();
		if(formTag == null){
			return;
		}
		
		// render
		IParamValue val = getCachedValue(context, valRepo, formTag);
		String selfValue = attrValue.getValue().getParsedValue(context, valRepo);
		
		if(selfValue == null){
			return;
		}
		
		if(!(val instanceof ArrayParamValue)){
			handleSingle(val, selfValue, wr);
			return;
		}
		
		// 配列の場合
		ArrayParamValue arrayParam = (ArrayParamValue)val;
		
	//	context.getCore().getLogger().reportInfo("********************** OPTION ON select : " + getName(context, valRepo));
	//	context.getCore().getLogger().reportInfo("********************** idx : " + context.getFromVariableCounter(getName(context, valRepo)));
	//	context.getCore().getLogger().reportInfo("********************** arrayParam : " + arrayParam);
		
		
		int pos = context.getFromVariableCounter(getName(context, valRepo));
		String value = arrayParam.getValueAt(pos);

		
	//	context.getCore().getLogger().reportInfo("********************** arvalue : [" + value + "] vs : [" + selfValue + "]");
		
		if(value == null){
			return;
		}
		if(value.equals(selfValue)){
			wr.write(ATTR_SELECTED);
		}
	}
	
	private void handleSingle(IParamValue val, String selfValue, Writer wr) throws IOException
	{
		if(val == null){
			return;
		}
		
		String strValue = ((StringParamValue)val).getValue();
		if(strValue == null){
			return;
		}
		
		if(strValue.equals(selfValue)){
			wr.write(ATTR_SELECTED);
		}
	}
	
	private FormTagObject findFormTag()
	{
		XMLTagBase base = getParent();
		
		while(!(base instanceof AlinousTopObject)){
			if(base instanceof FormTagObject){
				return (FormTagObject)base;
			}
			
			base = base.getParent();
		}
		
		return null;
	}
	
	private IParamValue getCachedValue(PostContext context, VariableRepository valRepo, FormTagObject formTag)
	{
		String formId = formTag.getFormId(context, valRepo);
		
		FormValues formValues = context.getFormValues();
		Map<String, IParamValue> map = formValues.getMap(formId);
		
		IParamValue val = map.get(getName(context, valRepo));
		
		return val;
	}
	
	private String getName(PostContext context, VariableRepository valRepo)
	{
		XMLTagBase prt = getParent();
		
		if(!(prt instanceof SelectTagObject)){
			return "";
		}
		
		SelectTagObject selTag = (SelectTagObject)prt;
		
		IAttribute attr  = selTag.getAttribute(ATTR_NAME);
		
		if(attr == null ){
			return "";
		}
		
		String retStr = attr.getValue().getParsedValue(context, valRepo);
		if(retStr.endsWith("[]")){
			retStr = retStr.substring(0, retStr.length() -2);
		}
		
		return retStr;
	}

	public String getTagName()
	{
		return "OPTION";
	}
	
	protected boolean isValueNeeded()
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
		/*
		// get SELECT tag
		XMLTagBase parent = this.getParent();
		if(parent == null){
			return false;
		}
		while(parent instanceof SelectTagObject){
			if(parent instanceof AlinousTopObject){
				return false;
			}
			
			parent = parent.getParent();
		}
		
		Attribute valAttr = parent.getAttribute("value");
		if(valAttr == null){
			return false;
		}
		if(valAttr.isDynamic()){
			return true;
		}
		*/
		return true;
	}
	
	public String getStaticValue()
	{
		IAttribute typeAttr = this.attributes.get(ATTR_VALUE);
		if(typeAttr == null){
			return null;
		}
		
		String typeStr = typeAttr.getValue().getValue();
		return typeStr;
	}
	
	public boolean isStaticSelected()
	{
		IAttribute typeAttr = this.attributes.get(ATTR_SELECTED);
		if(typeAttr == null){
			return false;
		}
		
		return true;
	}
}

/**
 * CROSSFIRE JAPAN INCORPO"RATED
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
import java.util.Map;

import org.alinous.exec.pages.ArrayParamValue;
import org.alinous.exec.pages.FormValues;
import org.alinous.exec.pages.IParamValue;
import org.alinous.exec.pages.PostContext;
import org.alinous.exec.pages.StringParamValue;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.objects.AlinousAttrs;
import org.alinous.objects.Attribute;
import org.alinous.objects.DqString;
import org.alinous.objects.IAlinousObject;
import org.alinous.objects.IAttribute;
import org.alinous.objects.IAttributeValue;
import org.alinous.objects.XMLTagBase;
import org.alinous.script.runtime.VariableRepository;


public class InputTagObject extends XMLTagBase implements IHtmlObject
{
	public static final String ATTR_NAME = "name";
	public static final String ATTR_VALUE = "value";
	public static final String ATTR_TYPE = "type";
	public static final String ATTR_CHECKED = "checked";
	
	public static final String TYPE_TEXT = "text";
	public static final String TYPE_RADIO = "radio";
	public static final String TYPE_CHECKBOX = "checkbox";
	public static final String TYPE_HIDDEN = "hidden";
	
	public IAlinousObject fork() throws AlinousException
	{
		InputTagObject newObj = new InputTagObject();
		
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
		if(context.isUseFormCache()){
			handleCache(context, valRepo);
		}

		wr.append("<input");
		renderAttributes(context, valRepo, wr, 0, false, true);
		
		renderValue(context, valRepo, wr);
		wr.append("/>");
		
		
		List<FormHiddenValue> hiddens = new ArrayList<FormHiddenValue>();
		
		// handle validation info
		handleValidationInfo(context, valRepo, hiddens);
		handleRegex(context, valRepo, hiddens);
		
		// add alns:ignoreBlank
		handleIgnoreBlank(context, valRepo, hiddens);
		
		// handle alns:type
		handleParamType(context, valRepo, hiddens);
		
		// render hiddens
		//renderHiddens(context, wr, n, hiddens);
		registerHidden(context, hiddens);
	}
	
	private void registerHidden(PostContext context, List<FormHiddenValue> hiddens)
	{
		// get form
		FormTagObject formTagObject = this.getFormTagObject();
		
		if(formTagObject == null){
			return;
		}
		
		Iterator<FormHiddenValue> it = hiddens.iterator();
		while(it.hasNext()){
			FormHiddenValue hd = it.next();
			context.getChildsHiddens(formTagObject).add(hd);
		}
	}
	
	private void handleParamType(PostContext context, VariableRepository valRepo, List<FormHiddenValue> hiddens)
	{
		IAttribute typeAttr = this.alinousAttributes.get(AlinousAttrs.ALINOUS_TYPE);
		
		if(typeAttr == null){
			return;
		}
		
		String alnsTypeVal = typeAttr.getValue().getParsedValue(context, valRepo);
		if(alnsTypeVal == null){
			return;
		}
		
		StringBuffer buff = new StringBuffer();
		
		buff.append(AlinousAttrs.ALINOUS_TYPE);
		buff.append(":");
		buff.append(getName(context, valRepo));
		
		FormHiddenValue hidden = new FormHiddenValue(buff.toString(), alnsTypeVal);
		hiddens.add(hidden);
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
	
	private void renderValue(PostContext context, VariableRepository valRepo, Writer wr) throws IOException
	{
		IAttribute attrType = this.attributes.get(ATTR_TYPE);
		IAttribute attrValue = this.attributes.get(ATTR_VALUE);
		String type = null;
		
		if( (attrValue == null || attrValue.getValue() == null) && !context.isUseFormCache()){
			return;
		}
		
		if(attrType == null){
			type = TYPE_TEXT;
		}else{
			type = attrType.getValue().getParsedValue(context, valRepo);
		}
		
		// render attribute
		wr.append(" ");
		IAttribute attr = context.getOverrideMap(this);
		
		// if radio or checkbox
		if(type.equals(TYPE_RADIO) || type.equals(TYPE_CHECKBOX)){
			if(attr != null){
				attr.renderContents(wr, 0, context, valRepo, false);
				context.removeOverridedValue(this);
			}else if(attrValue != null){
				// handle null
				attrValue.renderContents(wr, 0, context, valRepo, false);
			}else{
				wr.write("value=\"\"");
			}
			
			renderChecked(context, valRepo, wr);
			return;
		}
		
		// handling cache
		if(context.isUseFormCache() && !isFormNoCached(context, valRepo)){
			FormTagObject formTag = findFormTag();
			
			if(formTag == null){
				wr.write("value=\"\"");
				return;
			}
			
			IParamValue val = getCachedValue(context, valRepo, formTag);
			
			// debug
			//AlinousDebug.debugOut("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ input isUseFormCache@renderValue : " + getName(context, valRepo)
			//		+ " val : " + val + " attr : " + attr + " attrValue : " + attrValue);
			
			if(val instanceof StringParamValue){
				String strValue = ((StringParamValue)val).getValue();
				
				DqString tmpAttrValue = new DqString(strValue);
				attrValue = new Attribute();
				
				((Attribute)attrValue).setKey(ATTR_VALUE);
				((Attribute)attrValue).setValue(tmpAttrValue);
			}
			else if(val instanceof ArrayParamValue) {
				ArrayParamValue arrayParam = (ArrayParamValue)val;
				
				int pos = context.getFromVariableCounter(getName(context, valRepo));
				context.incFromVariableCounter(getName(context, valRepo));
				
				String strValue = arrayParam.getValueAt(pos);
				
				if(strValue == null){
					attr = null;
				}else{
					attrValue = new Attribute();
					DqString tmpAttrValue = new DqString(strValue);
					((Attribute)attrValue).setKey(ATTR_VALUE);
					((Attribute)attrValue).setValue(tmpAttrValue);
				}
				
				attrValue.renderContents(wr, 0, context, valRepo, false);
				return;
			}
		}
		
		if(attr != null){
			attr.renderContents(wr, 0, context, valRepo, false);
			context.removeOverridedValue(this);
		}else if(attrValue != null){
			// handle null
			attrValue.renderContents(wr, 0, context, valRepo, false);
		}else{
			wr.write("value=\"\"");
		}
		

		
	}
	
	private boolean isFormNoCached(PostContext context, VariableRepository valRepo)
	{
		IAttribute attrNoCache= this.getAlinousAttribute(AlinousAttrs.ALINOUS_NO_FORM_CACHE);
		
		//AlinousDebug.debugOut("+++++++++++ attrNoCache : " + attrNoCache);
		
		if(attrNoCache == null){
			return false;
		}
		
		String cache = attrNoCache.getValue().getParsedValue(context, valRepo);
		//AlinousDebug.debugOut("+++++++++++ cache : " + cache);
		
		if(cache != null && cache.equals("true")){
			return true;
		}
		
		return false;
	}
	
	private void renderChecked(PostContext context, VariableRepository valRepo, Writer wr) throws IOException
	{
		// If cache is not used		
		if(!context.isUseFormCache() && !isFormNoCached(context, valRepo)){
			IAttribute attrChecked = this.attributes.get(ATTR_CHECKED);
			
			if(attrChecked != null){
				IAttributeValue attrVal = attrChecked.getValue();
				String strChecked = null;
				
				if(attrVal != null){
					strChecked = attrVal.getParsedValue(context, valRepo);
				}
				
				if(strChecked != null && (strChecked.toUpperCase().equals("FALSE") ||
						strChecked.toUpperCase().equals("DISABLED") ||
						strChecked.toUpperCase().equals("UNCHECKED"))){
					// Do not render
				}else{
					wr.append(" ");
					attrChecked.renderContents(wr, 0, context, valRepo, false);
				}
			}
			
			return;
		}
		
		// setup cached checkbox and button
		IAttribute attrValue = this.attributes.get(ATTR_VALUE);
		if(attrValue == null){
			return;
		}
		String selfValue = attrValue.getValue().getParsedValue(context, valRepo);
		if(selfValue == null){
			return;
		}
		

		XMLTagBase base = findFormTag();
		if(!(base instanceof FormTagObject)){
			return;
		}
		
		FormTagObject formTag = (FormTagObject)base;
		IParamValue val = getCachedValue(context, valRepo, formTag);
		
		if(!(val instanceof ArrayParamValue)){
			handleSingle(val, selfValue, wr);
			return;
		}
		
		ArrayParamValue arrayParam = (ArrayParamValue)val;
		
		if(arrayParam.containsValue(selfValue)){
			wr.append(" ");
			wr.write(ATTR_CHECKED);
			
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
			wr.write(ATTR_CHECKED);
		}
	}
	

	
	private void handleCache(PostContext context, VariableRepository valRepo) throws ExecutionException
	{
		FormTagObject formTag = findFormTag();
		
		if(formTag == null){
			return;
		}
		
		IParamValue val = getCachedValue(context, valRepo, formTag);
		
		IAttribute attrType = this.attributes.get(ATTR_TYPE);
		if(attrType == null || attrType.getValue() == null){
			return;
		}
		String type = attrType.getValue().getParsedValue(context, valRepo);
		
		if(val != null && !(type.equals(TYPE_RADIO) || type.equals(TYPE_CHECKBOX))){
			Attribute attr = new Attribute();
			attr.setKey(ATTR_VALUE);
			attr.setValue(new DqString(val.toString()));
			
			if(attr.getValue().getValue() != null){
				// register into context
				context.setOverridedValue(this, attr);
			}
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
	
	public String getName(PostContext context, VariableRepository valRepo)
	{
		IAttribute nm = this.attributes.get(ATTR_NAME);
		if(nm != null){
			return removeArrayDesc(nm.getValue().getParsedValue(context, valRepo));
		}
		
		return null;
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
		return "INPUT";
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
	
	public String getStaticType()
	{
		IAttribute typeAttr = this.attributes.get(ATTR_TYPE);
		if(typeAttr == null){
			return null;
		}
		
		String typeStr = typeAttr.getValue().getValue();
		return typeStr;
	}
	
	protected boolean isNameNeeded()
	{
		return true;
	}
	
	
	protected boolean isValueNeeded()
	{
		IAttribute typeAttr = this.attributes.get(ATTR_TYPE);
		
		if(typeAttr == null){
			return false;
		}
		
		String typeStr = typeAttr.getValue().getValue();
		if(typeStr != null && (typeStr.equals(TYPE_CHECKBOX) || typeStr.equals(TYPE_RADIO))){
			return true;
		}
		
		return false;
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
	@Override
	public void findFormParams(List<IHtmlObject> formParams)
	{
		formParams.add(this);
		
		super.findFormParams(formParams);
	}

	@Override
	public void getFormInputObjects(List<XMLTagBase> formInputs)
	{
		IAttribute typeAttr = this.attributes.get(ATTR_TYPE);
		
		if(typeAttr == null){
			return;
		}
		
		String typeStr = typeAttr.getValue().getValue();
		if(typeStr == null || typeStr.equals(TYPE_HIDDEN)){
			return;
		}
		
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
			
			if(!(tagBase instanceof InputTagObject)){
				continue;
			}
			
			if(((InputTagObject)tagBase).getStaticType() == null || getStaticType() == null){
				continue;
			}
			
			if(tagBase instanceof InputTagObject && ((InputTagObject)tagBase).getStaticType().equals(getStaticType())){
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

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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.alinous.AlinousUtils;
import org.alinous.exec.InnerModulePath;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.filter.SessionIdRewriteArea;
import org.alinous.objects.AlinousAttrs;
import org.alinous.objects.Attribute;
import org.alinous.objects.IAlinousObject;
import org.alinous.objects.IAttribute;
import org.alinous.objects.XMLTagBase;
import org.alinous.script.runtime.VariableRepository;



public class FormTagObject extends XMLTagBase implements IHtmlObject
{
	public static final String HIDDEN_FORM_ACTION = "alns:formAction";
	public static final String HIDDEN_FORM_TARGET_TAGID = "alns:formTargetTagId";
	public static final String HIDDEN_FORM_LAST_ACTION = "alns:formLastAction";
	public static final String HIDDEN_FORM_LAST_TARGET_TAGID = "alns:formLastTargetTagId";
	public static final String HIDDEN_FORM_LAST_FORM_ID = "alns:formLastFormId";
	public static final String HIDDEN_FORM_BACK = "alns:back";
	public static final String HIDDEN_ACCESS_TOKEN = "ACCESS_TOKEN";
	
	//private List<FormHiddenValue> childsHiddens = new ArrayList<FormHiddenValue>();
	

	
	public IAlinousObject fork() throws AlinousException
	{
		FormTagObject newObj = new FormTagObject();
		
		copyAttribute(this, newObj);
		
		forkInnerObjects(newObj);
		
		return newObj;
	}
	/*
	private void initFromindex(PostContext context, VariableRepository valRepo) throws ExecutionException
	{
		// SELECT tag
		HashMap<String, ArrayList<SelectTagObject>> selectMap = gatherSelects(context, valRepo);
		Iterator<String> selKeyIt = selectMap.keySet().iterator();
		while(selKeyIt.hasNext()){
			String key = selKeyIt.next();
			
			Iterator<SelectTagObject> it = selectMap.get(key).iterator();
			int cnt = 0;
			while(it.hasNext()){
				SelectTagObject obj = it.next();
				
				obj.setFormArrayCount(cnt);
				
				cnt++;
			}
			
		}
		
		// INPUT tag
	}
	
	private HashMap<String, ArrayList<SelectTagObject>> gatherSelects(PostContext context, VariableRepository valRepo) throws ExecutionException
	{
		HashMap<String, ArrayList<SelectTagObject>> ret = new HashMap<String, ArrayList<SelectTagObject>>();
		Iterator<XMLTagBase> it = this.innerObj.iterator();
		while(it.hasNext()){
			XMLTagBase obj = it.next();
			
			findSelects(ret, obj, context, valRepo);
		}
		
		return ret;
	}
	
	private void findSelects(HashMap<String, ArrayList<SelectTagObject>> ret, XMLTagBase currentObj,
			PostContext context, VariableRepository valRepo) throws ExecutionException
	{
		// self
		if(currentObj instanceof SelectTagObject){
			String selectName = ((SelectTagObject)(currentObj)).getName(context, valRepo);
			
			if(selectName != null){
				ArrayList<SelectTagObject> list = ret.get(selectName);
				if(list == null){
					list = new ArrayList<SelectTagObject>();
					ret.put(selectName, list);					
				}
				
				list.add((SelectTagObject)currentObj);
			}
		}
		
		// children
		Iterator<XMLTagBase> it = currentObj.getInnerTags().iterator();
		while(it.hasNext()){
			XMLTagBase obj = it.next();
			
			findSelects(ret, obj, context, valRepo);
		}
	}
	*/
	public void renderContents(PostContext context, VariableRepository valRepo, Writer wr, int n)
									throws IOException, AlinousException
	{
		//initFromindex(context, valRepo);
		
		if(!handleIf(context, valRepo)){
			return;
		}
		
		if(context.isInner()){
			renderAsInnerObject(context, valRepo, wr, n);
			return;
		}
		
		// Hidden information
		AlinousTopObject topObj = getTopObject();
		List<FormHiddenValue> hiddens = new ArrayList<FormHiddenValue>();
		
		wr.append("<form");
		if(isAlinousForm(context, valRepo)){
			renderAction(context, valRepo, wr, hiddens);
			renderAttributes(context, valRepo, wr, 0, false, true);
		}else{
			renderAttributes(context, valRepo, wr, 0, true, true);
		}
		wr.append(">");
		
		renderInnerContents(context, valRepo, wr, n + 1);
		
		InnerModulePath modulePath = context.getModulePath();
		FormHiddenValue hidden = new FormHiddenValue(HIDDEN_FORM_TARGET_TAGID, modulePath.getStringPath());
		hiddens.add(hidden);
		
		String lastAction = topObj.getPath();
		hidden = new FormHiddenValue(HIDDEN_FORM_LAST_ACTION, lastAction);
		hiddens.add(hidden);
		
		hidden = new FormHiddenValue(HIDDEN_FORM_LAST_TARGET_TAGID, modulePath.getStringPath());
		hiddens.add(hidden);
		
		hidden = new FormHiddenValue(HIDDEN_FORM_LAST_FORM_ID, getFormId(context, valRepo));
		hiddens.add(hidden);
		
		// access string
		long mill = System.currentTimeMillis();
		String accessTime = new Timestamp(mill).toString();
		hidden = new FormHiddenValue(HIDDEN_ACCESS_TOKEN, Integer.toString(this.hashCode()) + "@" + accessTime);
		hiddens.add(hidden);
		
		// action
		IAttribute attrAction  = this.attributes.get("action");
		if(attrAction == null){
			throw new ExecutionException("Action is necessary.");
		}
		
		String strAction = attrAction.getValue().getParsedValue(context, valRepo);
		String absPath = topObj.toAbsolutePath(strAction);
		hidden = new FormHiddenValue(HIDDEN_FORM_ACTION, absPath);
		hiddens.add(hidden);
		
		if(isAlinousForm(context, valRepo)){
			renderHiddens(context, wr, n, hiddens);
			renderHiddens(context, wr, n, context.getChildsHiddens(this));
		}
		
		context.clearHidden();
		
		wr.append("</form>");
	}
	
	private void renderAction(PostContext context, VariableRepository valRepo, Writer wr, List<FormHiddenValue> hiddens) throws ExecutionException, IOException
	{
		// action
		IAttribute attrAction  = this.attributes.get("action");
		if(attrAction == null){
			throw new ExecutionException("Action is necessary.");
		}
		
		String strAction = attrAction.getValue().getParsedValue(context, valRepo);
		String requestPath = AlinousUtils.getNotOSPath(context.getRequestPath());
		
		if(!strAction.startsWith("/")){
			requestPath = AlinousUtils.getWebDirectory(requestPath) + strAction;
		}
		
		
		// if param is specified, add hidden
		if(context.getCore().getConfig().getSessionIdRewriteConfig() != null &&
				context.getCore().getConfig().getSessionIdRewriteConfig().isInsideArea(requestPath)){
			SessionIdRewriteArea area = context.getCore().getConfig().getSessionIdRewriteConfig().getRewriteArea(requestPath);
			
			if(area.getParam() != null){
				FormHiddenValue hidden = new FormHiddenValue(area.getParam(), context.getSessionId());
				hiddens.add(hidden);
			}
		}else{
			strAction = AlinousUtils.addRewriteSessionString(strAction, context.getSessionId(), context.isStatic(), context.getCore());
		}		
		
		
		wr.write(" action=\"");
		wr.write(strAction);
		wr.write("\"");
	}
	

	private void renderAsInnerObject(PostContext context, VariableRepository valRepo, Writer wr, int n)
		throws IOException, AlinousException
	{
		AlinousTopObject topObj = getTopObject();
		List<FormHiddenValue> hiddens = new ArrayList<FormHiddenValue>();
		
		wr.append("<form");
		
		renderAsInnerAttributes(context, valRepo, wr, n, topObj, hiddens);
		wr.append(">\n");
		
		renderInnerContents(context, valRepo, wr, n + 1);
		
		// Hidden information
		InnerModulePath modulePath = context.getModulePath();
		FormHiddenValue hidden = new FormHiddenValue(HIDDEN_FORM_TARGET_TAGID, modulePath.getStringPath());
		hiddens.add(hidden);
		
		String lastAction = topObj.getPath();
		hidden = new FormHiddenValue(HIDDEN_FORM_LAST_ACTION, lastAction);
		hiddens.add(hidden);
		
		hidden = new FormHiddenValue(HIDDEN_FORM_LAST_TARGET_TAGID, modulePath.getStringPath());
		hiddens.add(hidden);
		
		hidden = new FormHiddenValue(HIDDEN_FORM_LAST_FORM_ID, getFormId(context, valRepo));
		hiddens.add(hidden);		
		
		// access string
		long mill = System.currentTimeMillis();
		String accessTime = new Timestamp(mill).toString();
		hidden = new FormHiddenValue(HIDDEN_ACCESS_TOKEN, Integer.toString(this.hashCode()) + "@" + accessTime);
		hiddens.add(hidden);
		
		// render hiddens
		if(isAlinousForm(context, valRepo)){
			
			renderHiddens(context, wr, n, hiddens);
			renderHiddens(context, wr, n, context.getChildsHiddens(this));
		}
		
		wr.append("</form>\n");		
	}
	
	private boolean isAlinousForm(PostContext context, VariableRepository valRepo)
	{
		IAttribute attr = this.alinousAttributes.get(AlinousAttrs.ALINOUS_ALINOUS_FORM);
		String alinousFormValue = null;
		
		if(attr == null){
			return true;
		}
		
		if(attr != null){
			alinousFormValue = attr.getValue().getParsedValue(context, valRepo);
		}

		if(alinousFormValue == null){
			return true;
		}
		
		return alinousFormValue.toLowerCase().equals(AlinousAttrs.VALUE_TRUE);
	}
	
	public String getFormId(PostContext context, VariableRepository valRepo)
	{
		IAttribute attr = this.getAttribute("id");
		if(attr != null && attr.getValue() != null){
			return attr.getValue().getParsedValue(context, valRepo);
		}
		
		attr = this.getAttribute("name");
		if(attr != null && attr.getValue() != null){
			return attr.getValue().getParsedValue(context, valRepo);
		}
		
		return "";
	}
	
	public String getName()
	{
		IAttribute attr = this.getAttribute("name");
		if(attr == null){
			return null;
		}
		
		return attr.getValue().getParsedValue(null, null);
	}
	
	@SuppressWarnings("rawtypes")
	private void renderAsInnerAttributes(PostContext context, VariableRepository valRepo, Writer wr, int n, AlinousTopObject topObj,
							List<FormHiddenValue> hiddens)
			throws IOException
	{
		Enumeration enm = this.attributes.keys();
		while(enm.hasMoreElements()){
			Object key = enm.nextElement();
			Attribute atr =(Attribute)this.getAttribute((String)key);
			wr.write(" ");
			
			if(atr.getKey().toLowerCase().equals("action")){
				if(!isAlinousForm(context, valRepo)){
					renderOneAttribute(wr, atr.getKey(), atr.getValue().getParsedValue(context, valRepo));
					continue;
				}
				
				AlinousTopObject toptopObj = context.getTopTopObject();
				
				renderOneAttribute(wr, "action", context.getFilePath(toptopObj.getPath()));
				
				String absPath = topObj.toAbsolutePath(atr.getValue().getValue());
				
				FormHiddenValue hidden = new FormHiddenValue(HIDDEN_FORM_ACTION, absPath);
				hiddens.add(hidden);
			}
			else{
				atr.renderContents(wr, n + 1, context, valRepo, true);
			}
		}
	}
	
	private void renderOneAttribute(Writer wr, String key, String value) throws IOException
	{
		wr.write(key);
		wr.write("=");
		wr.write("\"");
		wr.write(value);
		wr.write("\"");
	}

	public String getTagName()
	{
		return "FORM";
	}
	
	protected boolean isNameNeeded()
	{
		return true;
	}
	
/*	public void addChildHidden(FormHiddenValue hidden)
	{
		this.childsHiddens.add(hidden);
	}
	**/
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
	
	public void findFormObject(List<FormTagObject> formList)
	{		
		formList.add(this);
		
		Iterator<XMLTagBase> it =  this.innerObj.iterator();
		while(it.hasNext()){
			XMLTagBase tag = it.next();

			tag.findFormObject(formList);
		}
	}
	
	public String getAction()
	{
		IAttribute attr = this.getAttribute("action");
		if(attr != null && attr.getValue() != null){
			return attr.getValue().getValue();
		}
		
		return null;
	}
	
	@Override
	public void getFormObjects(List<FormTagObject> formList)
	{
		
		formList.add(this);
	}
}

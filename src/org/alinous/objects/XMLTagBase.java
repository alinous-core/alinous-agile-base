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
package org.alinous.objects;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;


import org.alinous.AlinousCore;
import org.alinous.AlinousUtils;
import org.alinous.PathUtils;
import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.exec.AccessExecutionUnit;
import org.alinous.exec.ExecResultCache;
import org.alinous.exec.FormValueCache;
import org.alinous.exec.InnerModulePath;
import org.alinous.exec.pages.IDesign;
import org.alinous.exec.pages.IParamValue;
import org.alinous.exec.pages.IPostable;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.AlinousSecurityException;
import org.alinous.expections.ExecutionException;
import org.alinous.objects.html.AlinousTopObject;
import org.alinous.objects.html.FormTagObject;
import org.alinous.objects.html.IHtmlObject;
import org.alinous.objects.html.UnknownTagObject;
import org.alinous.objects.optimize.IOptimizable;
import org.alinous.objects.optimize.StaticBuffer;
import org.alinous.objects.validate.AlinousAttributeValidator;
import org.alinous.objects.validate.ValidationError;
import org.alinous.parser.ParsedElement;
import org.alinous.parser.script.attr.AlinousAttrScriptParser;
import org.alinous.script.attribute.ParsedAttribute;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.security.AlinousSecurityManager;
import org.alinous.security.AuthenticationContext;



public abstract class XMLTagBase  
		implements ParsedElement, IPostable, IAlinousObject, IDesign, IOptimizable
{
	public static final String TAG_ID = "alns:tagid";
	
	public static final String ATTR_NAME = "name";
	public static final String ATTR_VALUE = "value";
	public static final String ATTR_ID = "id";
	public static final String FORM_HIDDEN_VALIDATOR = "alns:validator:";
	public static final String FORM_HIDDEN_VALIDATEIF = "alns:validateif:";
	
	protected Hashtable<String, IAttribute> attributes = new Hashtable<String, IAttribute>();
	protected Hashtable<String, IAttribute> alinousAttributes = new Hashtable<String, IAttribute>();
	protected List<XMLTagBase> innerObj = new CopyOnWriteArrayList<XMLTagBase>();
	
	//into PostContext
	//protected VariableRepository valRepo;
	//protected PostContext context;
	
	private int line;
	private int position;
	
	protected int xpathIndex = 0;
	
	// Optimize
	protected StaticBuffer staticBuffer;
	
	private XMLTagBase parent;
	
	public XMLTagBase()
	{

	}
	
	public void post(PostContext context, ExecResultCache resCache, FormValueCache formValues
			, VariableRepository valRepo)
	{
		postInner(context, resCache, formValues, valRepo);
	}
	
	@SuppressWarnings("rawtypes")
	protected void postInner(PostContext context, ExecResultCache resCache,
						FormValueCache formValues, VariableRepository valRepo)
	{
		Iterator it = this.innerObj.iterator();
		while(it.hasNext()){
			IPostable postable = (IPostable)it.next();
			postable.post(context, resCache, formValues, valRepo);
		}
	}
	
	@SuppressWarnings("rawtypes")
	protected void renderInnerContents(PostContext context, VariableRepository valRepo, Writer wr, int n)
			throws IOException, AlinousException
	{
		Iterator it = this.innerObj.iterator();
		while(it.hasNext()){
			IDesign design = (IDesign)it.next();
			design.renderContents(context, valRepo, wr, n + 1);
		}
	}
	
	protected void renderAttributes(PostContext context, VariableRepository valRepo, Writer wr, int n) throws IOException
	{
		renderAttributes(context, valRepo, wr, n, true, false);
	}
	
	@SuppressWarnings("rawtypes")
	protected void renderAttributes(PostContext context, VariableRepository valRepo, Writer wr, int n, boolean renderValue, boolean adjustUri) throws IOException
	{
		Enumeration enm = this.attributes.keys();
		while(enm.hasMoreElements()){
			Object key = enm.nextElement();
			Attribute atr =(Attribute)this.getAttribute((String)key);

			
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
	

	
	@SuppressWarnings("rawtypes")
	protected void forkInnerObjects(XMLTagBase parentObj) throws AlinousException
	{
		Iterator it = this.innerObj.iterator();
		while(it.hasNext()){
			IAlinousObject alinousObj = (IAlinousObject)it.next();
			IAlinousObject forked = alinousObj.fork();
			
			parentObj.innerObj.add((XMLTagBase)forked);
			forked.setParent(parentObj);
		}
	}
	

	@SuppressWarnings("rawtypes")
	protected void copyAttribute(XMLTagBase src, XMLTagBase dest)
	{
		Enumeration enm = src.attributes.keys();
		while(enm.hasMoreElements()){
			Object key = enm.nextElement();
			
			IAttribute atr = null;
			try {
				atr = (IAttribute)this.getAttribute((String)key).clone();
				
				if(AlinousAttrs.isAlinousAttr(atr)){
					dest.alinousAttributes.put(atr.getKey(), atr);
				}else{
					dest.attributes.put(atr.getKey(), atr);
				}
			} catch (CloneNotSupportedException e) {}
		}
	}
	
	@SuppressWarnings("rawtypes")
	protected void copyAttributeNormal(XMLTagBase src, XMLTagBase dest)
	{
		Enumeration enm = src.attributes.keys();
		while(enm.hasMoreElements()){
			Object key = enm.nextElement();
			
			IAttribute atr = null;
			try {
				atr = (IAttribute)this.getAttribute((String)key).clone();
				
				// static or not
				if(atr.isDynamic()){
					dest.attributes.put(atr.getKey(), atr);
				}else{
					// toStatic
					dest.attributes.put(atr.getKey(), atr.toStatic());
				}
				
				
			} catch (CloneNotSupportedException e) {}
		}
	}
	
	protected boolean handleInnerTag(PostContext context, VariableRepository valRepo, Writer wr, int n)
			throws AlinousException, IOException
	{
		boolean bl;
		
		AlinousTopObject topObj = getTopObject();
		AlinousCore core = topObj.getAlinousCore();
		
		// On optimization, automatically core is null
		if(core == null){
			return false;
		}
		
		AccessExecutionUnit unit = core.createAccessExecutionUnit(context.getSessionId(),
										context.getUnit());
		
		try{
			bl = doHandleInnerTag(context, valRepo, wr, n, unit);
		}
		catch(AlinousException e){
			throw e;
		}
		catch(IOException e){
			throw e;
		}
		catch(Throwable e){
			// error show
			e.printStackTrace();
			throw new ExecutionException(e, "Fatal Error");
		}finally{
			unit.dispose();
		}
		
		return bl;
		
	}
	
	protected boolean doHandleInnerTag(PostContext context, VariableRepository valRepo, Writer wr, int n, AccessExecutionUnit unit)
						throws AlinousException, IOException
	{
		//  INNER
		IAttribute inner = this.alinousAttributes.get(AlinousAttrs.ALINOUS_INNER);
		if(inner == null){
			return false;
		}
		
		AlinousTopObject topObj = getTopObject();
		AlinousCore core = topObj.getAlinousCore();
		
		
		// getNext Url
		InnerModulePath modPath = context.getModulePath().deepClone();
		modPath.addPath(getTopObject().getPath());
		modPath.addTarget(getTagId(context, valRepo));
		
		// check authentication fault
		if(handleAuthenticationFault(topObj, context, inner, modPath, wr)){
			
			return true;
		}
		
		InnserStatusContext innerStatus = getInnserObjectPath(topObj, context, inner, modPath, valRepo);
		
		//check default security
		if(innerStatus.isSecurityErrorDefaultPage()){
			AlinousSecurityManager.writeErrorInner(wr);
			return true;
		}
		
		// setting inner tag
		PostContext newContext = new PostContext(core, unit);
		newContext.setInner(true);
		newContext.initParams(context);
		newContext.setModulePath(context.getModulePath().deepClone());
		newContext.setTargetTagId(context.getTargetTagId());
		
		VariableRepository newValRepo = new VariableRepository();
		
		// Register
		core.registerAlinousObject(newContext, innerStatus.getNextModuleName());
		
		// extra params
		if(innerStatus.getExtraParams() != null){
			Iterator<String> it= innerStatus.getExtraParams().keySet().iterator();
			while(it.hasNext()){
				String key = it.next();
				
				newContext.getParamMap().put(key, innerStatus.getExtraParams().get(key));
			}
		}
		
		newContext.setUseValuavleRepositoryCache(innerStatus.isUseVariableCache());
		newContext.setUseFormCache(innerStatus.isUseFormValueCache());
		
		IDesign design = null;
		try {
			design = unit.gotoPage(innerStatus.getNextModuleName(), newContext, newValRepo,
					modPath, context.getTopTopObject());
			
		}catch(AlinousSecurityException e){
			String loginForm = e.getZone().getLoginForm();

			String modName = AlinousUtils.getModuleName(loginForm);

			PostContext errContext = new PostContext(core, unit);
	
			// Register
			core.registerAlinousObject(errContext, modName);
			design = unit.gotoPage(modName, errContext, newValRepo,
					modPath, context.getTopTopObject());
		}
		catch (ExecutionException e) {
			throw e;
		}finally{
			newContext.dispose();
		}
		
		// Render inner
		if(design != null){
			if(unit.getForwardResult() != null){
				newContext = unit.getForwardResult().getContext();
			}
			
			design.renderContents(newContext, newValRepo, wr, n);
			
			if(unit.getForwardResult() != null){
				newContext.getUnit().dispose();
				// last path has already saved
			}
			else{
				// save last path
				unit.getInnserStatusCache().storeLastPath(context, modPath, innerStatus.getNextModuleName(), newContext.getSessionId());
			}
		}
		
		return true;
	}
	
	/**
	 * If error page was not specified and authentication failed
	 * @param topObj
	 * @param context
	 * @param inner
	 * @param modPath
	 * @return
	 * @throws IOException 
	 */
	private boolean handleAuthenticationFault(AlinousTopObject topObj, PostContext context,
			IAttribute inner, InnerModulePath modPath, Writer wr) throws IOException
	{
		AuthenticationContext authContext = context.getAuthContext();
		
		if(authContext == null){
			return false;
		}
		if(authContext.getZone().getErrorPage() != null && 
			!authContext.getZone().getErrorPage().equals("")){
			
			return false;
		}

		String targetPath = context.getTargetTagId();
		if(!targetPath.equals(modPath.getStringPath())){
			
			return false;
		}

		AlinousSecurityManager.writeErrorInner(wr);
		
		return true;
	}
	
	private InnserStatusContext getInnserObjectPath(AlinousTopObject topObj, PostContext context,
			IAttribute inner, InnerModulePath modPath, VariableRepository valRepo) throws AlinousException
	{
		IAttribute tagIdAttr = this.alinousAttributes.get(AlinousAttrs.ALINOUS_TAGID);
		if(tagIdAttr == null){
			throw new ExecutionException(AlinousAttrs.ALINOUS_TAGID + " attribute does not exists at line " + getLine());
		}
		
		InnserStatusContext retContext = new InnserStatusContext();
		
		String targetPath = context.getTargetTagId();
		
		// If no tagId to post is specified.
		if(targetPath == null || targetPath.equals("")){
			// Clear Inner Status
			//context.getUnit().getInnserStatusCache().clear(modPath);		
			clearModpath(context, modPath);
	
			String htmlPath = inner.getValue().getParsedValue(context, valRepo);
			String modName = AlinousUtils.getModuleName(htmlPath);
			
			Map<String, IParamValue> paraMap = AlinousUtils.getParamsFromPath(htmlPath);
			retContext.setExtraParams(paraMap);
			
			modName = PathUtils.getAbsPath(getTopObject().getPath(), modName);
		
			retContext.setNextModuleName(modName);
			retContext.setUseVariableCache(false);
			
			return retContext;
		}
		
		// If this inner area is the target
		if(targetPath.equals(modPath.getStringPath())){
			String nextAction = context.getNextAction();
			
			if(nextAction == null){
				retContext.setSecurityErrorDefaultPage(true);
				return retContext;
			}
			
			retContext.setNextModuleName(AlinousUtils.getModuleName(nextAction));
			
			if(context.isBacking()){
				retContext.setUseVariableCache(true);
				retContext.setUseFormValueCache(true);
			}
			// failed validation
			else if(!context.getValidationStatus().getStatus()){
				// validation 
				retContext.setUseVariableCache(true);
				retContext.setUseFormValueCache(true);
				
				String lastAction = context.getFormLastAction();
				retContext.setNextModuleName(AlinousUtils.getModuleName(lastAction));
			}

			return retContext;
		}
		
		
		// If other inner area is the target
		String path = context.getUnit().getInnserStatusCache().getLastPath(context, modPath, context.getSessionId());
		
		if(path == null){
			// inner
			retContext.setNextModuleName(AlinousUtils.getModuleName(inner.getValue().getParsedValue(context, valRepo)));
			retContext.setUseVariableCache(false);
			retContext.setUseFormValueCache(false);
		}else{
			retContext.setNextModuleName(path);
			retContext.setUseVariableCache(true);
			retContext.setUseFormValueCache(true);
		}
		
		return retContext;
	}
	
	private void clearModpath(PostContext context, InnerModulePath modPath) throws ExecutionException
	{
		DataSrcConnection con = null;
		try {
			con = context.getCore().getSystemRepository().getConnection();
		} catch (DataSourceException e) {
			throw new ExecutionException(e, "Failed in connect on handling InnserStatusCache"); // i18n
		}
		
		try {
			context.getUnit().getInnserStatusCache().clear(con, modPath, context.getSessionId());
		} catch (DataSourceException e) {
			throw new ExecutionException(e, "Failed in connect on handling InnserStatusCache"); // i18n
		}
		finally{
			context.getCore().getSystemRepository().closeConnection(con);
		}
		
		
	}
	
	public boolean handleIterateAttribute(PostContext context, VariableRepository valRepo, Writer wr, int n)
							throws IOException, AlinousException
	{
		IAttribute iterateAttr = this.alinousAttributes.get(AlinousAttrs.ALINOUS_ITERATE);
		IAttribute variableAttr = this.alinousAttributes.get(AlinousAttrs.ALINOUS_VARIABLE);
		
		if(iterateAttr == null || variableAttr == null){
			return false;
		}
		
		String strValuePath = iterateAttr.getValue().getValue();
		if(!strValuePath.startsWith("@")){
			return false;
		}
		
		IPathElement variablePath = PathElementFactory.buildPathElement(strValuePath.substring(1));
		if(variablePath == null){
			return false;
		}

		IScriptVariable scVal = valRepo.getVariable(variablePath, context);
		
		// if array is null
		if(scVal == null){
			return true; // means rendering has finished
		}
		
		if(!(scVal instanceof ScriptArray)){
			return true; // means rendering has finished
		}
	
		ScriptArray itArray = (ScriptArray)scVal;
		Iterator<IScriptVariable> it = itArray.iterator();
		while(it.hasNext()){
			IScriptVariable val = it.next();
			
			try {
				IScriptVariable clonedVal = (IScriptVariable) val.clone();
				
				clonedVal.setName(variableAttr.getValue().getValue());
				valRepo.putValue(clonedVal, context);
			}
			catch (CloneNotSupportedException e) {
				e.printStackTrace();
				return false;
			}
			
			doRenderContent(context, valRepo, wr, n);
		}
		
		return true;
	}
	
	//
	protected void doRenderContent(PostContext context, VariableRepository valRepo, Writer wr, int n)
					throws IOException, AlinousException
	{
		// Override this
	}
	
	
	public AlinousTopObject getTopObject()
	{
		XMLTagBase tag = this.parent;
		
		while(!(tag instanceof AlinousTopObject)){
			tag = tag.getParent();
		}
		
		return (AlinousTopObject)tag;
	}
	
	public String getTagId(PostContext context, VariableRepository valRepo) throws ExecutionException
	{
		IAttribute tagId = this.alinousAttributes.get(AlinousAttrs.ALINOUS_TAGID);
		
		if(tagId == null){
			throw new ExecutionException(AlinousAttrs.ALINOUS_TAGID + " is necessary in " + getTopObject().getPath()
					+ " at " + this.line);
		}
		
		return tagId.getValue().getParsedValue(context, valRepo);
	}
	
	protected void renderHiddens(PostContext context, Writer wr, int n, List<FormHiddenValue> hiddens)
			throws IOException
	{
		Iterator<FormHiddenValue> it = hiddens.iterator();
		while(it.hasNext()){
			FormHiddenValue val = it.next();
			val.renderHidden(wr, n);
		}
	}
	
	protected IParamValue getFormValue(PostContext context, String formName, String key)
	{
		IParamValue val = null;
		
		if(context.isUseFormCache()){
			val = context.getFormValues().getMap(formName).get(key);
			return val;
		}
		
		val = context.getParams(key);
		return val;
	}
	
	// Getter and Setter
	public void putAttribute(String key, IAttributeValue value)
	{
		Attribute attribute = new Attribute();
		attribute.setKey(key.toLowerCase());
		attribute.setValue(value);
		this.attributes.put(key, attribute);
	}
	
	public IAttribute getAttribute(String key)
	{
		return this.attributes.get(key);
	}
	
	public IAttribute getAlinousAttribute(String key)
	{
		return this.alinousAttributes.get(key);
	}
	
	public void addInnerObject(XMLTagBase tagObj)
	{	
		if(tagObj == null){
			return;
		}
		this.innerObj.add(tagObj);
		tagObj.setParent(this);
	}

	public int getLine()
	{
		return line;
	}

	public int getLinePosition()
	{
		return position;
	}

	public void setLine(int line)
	{
		this.line = line;
	}

	public void setLinePosition(int pos)
	{
		this.position = pos;
	}

	public XMLTagBase getParent()
	{
		return parent;
	}

	public void setParent(XMLTagBase parent)
	{
		this.parent = parent;
	}

	public FormTagObject getFormTagObject()
	{
		XMLTagBase tag = this;
		
		while(tag != null){
			if(tag instanceof AlinousTopObject){
				return null;
			}
			else if(tag instanceof FormTagObject){
				return (FormTagObject) tag;
			}
			
			tag = tag.getParent();
		}
		
		return null;
	}
	
	
	public class FormHiddenValue
	{
		private String key;
		private String value;
		
		public FormHiddenValue(String key, String value)
		{
			this.key = key;
			this.value = value;
		}
		
		public String getKey()
		{
			return key;
		}
		public void setKey(String key)
		{
			this.key = key;
		}
		public String getValue()
		{
			return value;
		}
		public void setValue(String value)
		{
			this.value = value;
		}
		public void renderHidden(Writer wr, int n) throws IOException
		{
			wr.write("<INPUT type=\"HIDDEN\" name=\"" + this.key);
			wr.write("\" ");
			wr.write("value=\"" + this.value + "\"");
			wr.write(">\n");
		}
	}
	
	protected boolean handleIf(PostContext context, VariableRepository valRepo) throws ExecutionException
	{
		//Attribute ifAttr = this.getAttribute(AlinousAttrs.ALINOUS_IF);
		IAttribute ifAttr = this.alinousAttributes.get(AlinousAttrs.ALINOUS_IF);
		
		if(ifAttr == null){
			return true;
		}
		
		String ifStr = ifAttr.getValue().getValue();
		StringReader reader = new StringReader("<" + ifStr + ">");
		
		boolean bl = true;
		AlinousAttrScriptParser parser = new AlinousAttrScriptParser(reader);
		try {
			ParsedAttribute attr = parser.parse();
			
			bl = attr.evaluate(context, valRepo);
		} catch (Throwable e) {
			reader.close();
			return true;
		}
		reader.close();
		
		
		return bl;
	}
	
	protected boolean handleValidateIf(PostContext context, VariableRepository valRepo)
	{
		IAttribute ifAttr = this.alinousAttributes.get(AlinousAttrs.ALINOUS_VALIDATE_IF);
		
		if(ifAttr == null){
			return true;
		}
		
		String ifStr = ifAttr.getValue().getValue();
		StringReader reader = new StringReader("<" + ifStr + ">");
		
		boolean bl = true;
		AlinousAttrScriptParser parser = new AlinousAttrScriptParser(reader);
		try {
			ParsedAttribute attr = parser.parse();
			
			bl = attr.evaluate(context, valRepo);
		} catch (Throwable e) {
			reader.close();
			return true;
		}
		reader.close();
		
		
		return bl;
	}
	
	protected void handleValidationInfo(PostContext context, VariableRepository valRepo, List<FormHiddenValue> hiddens)
	{
		IAttribute attr = this.alinousAttributes.get(AlinousAttrs.ALINOUS_VALIDATE_TYPE);
		
		if(attr == null){
			return;
		}
		
		String strValidators = attr.getValue().getParsedValue(context, valRepo);
		if(strValidators == null){
			return;
		}
		
		String validators[] = strValidators.split(",");
		for(int i = 0; i < validators.length; i++){
			String validatorName = validators[i];
			
			StringBuffer buff = new StringBuffer();
			buff.append(FORM_HIDDEN_VALIDATOR);
			
			String formId = getFormTagObject().getFormId(context, valRepo);
			buff.append(formId);
			
			buff.append(";");
			buff.append(getFullName(context, valRepo));
			
			buff.append(";");
			buff.append(validatorName);
			
			FormHiddenValue hidden
				= new FormHiddenValue(buff.toString(), validatorName);
			
			hiddens.add(hidden);
			

		}
		
		// validate if
		IAttribute validateIfAttr = this.alinousAttributes.get(AlinousAttrs.ALINOUS_VALIDATE_IF);
		if(validateIfAttr == null){
			return;
		}
		
		StringBuffer buff = new StringBuffer();
		buff.append(FORM_HIDDEN_VALIDATEIF);
		
		String formId = getFormTagObject().getFormId(context, valRepo);
		buff.append(formId);
		
		buff.append(";");
		buff.append(getFullName(context, valRepo));
		
		FormHiddenValue hidden
			= new FormHiddenValue(buff.toString(), validateIfAttr.getValue().getValue());
	
		hiddens.add(hidden);
	}
	
	protected void handleRegex(PostContext context, VariableRepository valRepo, List<FormHiddenValue> hiddens)
	{
		// regex
		IAttribute attr = this.alinousAttributes.get(AlinousAttrs.ALINOUS_REGEX);
		if(attr == null){
			return;
		}
		
		String regexStr = attr.getValue().getParsedValue(context, valRepo);
		
		StringBuffer buff = new StringBuffer();
		buff.append(AlinousAttrs.ALINOUS_REGEX);
		buff.append(":");
		
		String formId = getFormTagObject().getFormId(context, valRepo);
		buff.append(formId);
		
		buff.append(";");
		buff.append(getFullName(context, valRepo));
		
		try {
			regexStr = URLEncoder.encode(regexStr, "utf-8");
		} catch (UnsupportedEncodingException e) {
			context.getCore().reportError(e);
		}
		
		FormHiddenValue hidden
			= new FormHiddenValue(buff.toString(), regexStr);
		
		hiddens.add(hidden);
	}
	
	public String getFullName(PostContext context, VariableRepository valRepo)
	{
		IAttribute nm = this.attributes.get(ATTR_NAME);
		if(nm != null){
			return nm.getValue().getParsedValue(context, valRepo);
		}
		
		return "";
	}
	
	public void validateHtmlObject(List<ValidationError> errors)
	{
		// Check name
		if(isNameNeeded()){
			checkAttributeExists(errors, ATTR_NAME);
		}
		
		if(isValueNeeded()){
			checkAttributeExists(errors, ATTR_VALUE);
		}
		
		AlinousAttributeValidator attrValidator = new AlinousAttributeValidator(this, this.attributes);
		attrValidator.validate(errors);
		
		// validate inner
		Iterator<XMLTagBase> it = this.innerObj.iterator();
		while(it.hasNext()){
			XMLTagBase base = it.next();
			
			base.validateHtmlObject(errors);
		}
	}
	
	protected boolean isValueNeeded()
	{
		return false;
	}
	
	protected boolean isNameNeeded()
	{
		return false;
	}
	
	public String getTagName()
	{
		return "";
	}
	
	private void checkAttributeExists(List<ValidationError> errors, String attrName)
	{
		IAttribute attr = this.attributes.get(attrName);
		
		if(attr == null){
			ValidationError er = new ValidationError(this.line, 
					"<" + getTagName() + ">" +
					" doesn't have " + attrName + " attribute");
			
			errors.add(er);
		}
		
	}
	
	// Optimization
	public void initOptimize(XMLTagBase optTag)
	{
		optTag.setLine(this.line);
		optTag.setLinePosition(this.position);
	}
	
	// default optimization logic
	public void optimizeSelf(PostContext context, XMLTagBase owner, boolean forceDynamic) throws IOException, AlinousException
	{
		if(isDynamic()){
			registerObject(context, owner, this.getClass(), forceDynamic); // force
			return;
		}
		
		if(forceDynamic || hasDynamicAttribute()){
			registerObject(context, owner, this.getClass(), forceDynamic);
			return;
		}
		
		// if static contents;
		renderContents(context, null, owner.getStaticBuffer().getWriter(), 0);
	}
	
	public boolean isDynamic()
	{
		// self
		if(hasAlinousAttribute()){
			return true;
		}
		if(hasDynamicAttribute()){
			return true;
		}
		
		
		Iterator<XMLTagBase> it = this.innerObj.iterator();
		while(it.hasNext()){
			XMLTagBase tg = it.next();
			
			if(tg.isDynamic()){
				return true;
			}
		}
		return false;
	}
	
	public void registerObject(PostContext context, XMLTagBase owner, Class<?> clazz, boolean forceDynamic) throws IOException, AlinousException
	{
		// at first, add StaticBuffer element
		if(owner.isBufferExists()){
			owner.addInnerObject(owner.getStaticBuffer());
			// Fixme clear
			owner.clearStaticBuffer();
		}
		
		// After that, add dynamic
		XMLTagBase newObj = null;
		try {
			newObj = (XMLTagBase) clazz.newInstance();
			registerSelf(context, owner, newObj);
		} catch (InstantiationException e) {
			e.printStackTrace();
			return;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return;
		}
		
		// if xml object
		if(newObj instanceof UnknownTagObject){
			UnknownTagObject newTag = (UnknownTagObject)newObj;
			UnknownTagObject curTag = (UnknownTagObject)this;
			
			newTag.setTagName(curTag.getTagName());
		}
		
		copyAttributeNormal(this, newObj);
		owner.addInnerObject(newObj);
		
		Iterator<XMLTagBase> it = this.innerObj.iterator();
		while(it.hasNext()){
			XMLTagBase inObj = it.next();
			
			inObj.optimizeSelf(context, newObj, forceDynamic);
		}
		
		// finally StaticBuffer exeists
		if(newObj.isBufferExists()){
			newObj.addInnerObject(newObj.getStaticBuffer());
			// Fixme clear
			newObj.clearStaticBuffer();
		}
	}
	
	protected void registerSelf(PostContext context, XMLTagBase owner, XMLTagBase newObj)
	{
		
	}


	public boolean hasDynamicAttribute()
	{
		Iterator<String> it = this.attributes.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			IAttribute attr = this.attributes.get(key);
			
			if(attr.isDynamic()){
				return true;
			}
		}
		
		return false;
	}
	
	public boolean hasAlinousAttribute()
	{
		Iterator<String> it = this.attributes.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			IAttribute attr = this.attributes.get(key);
			
			if(AlinousAttrs.isAlinousAttr(attr)){
				return true;
			}
		}

		return false;
	}

	public StaticBuffer getStaticBuffer()
	{
		if(this.staticBuffer == null){
			this.staticBuffer = new StaticBuffer();
		}
		return this.staticBuffer;
	}
	
	public boolean isBufferExists()
	{
		return this.staticBuffer != null;
	}
	
	public void clearStaticBuffer()
	{
		this.staticBuffer = null;		
	}
	
	public List<XMLTagBase> getInnerTags()
	{
		return this.innerObj;
	}

	public Hashtable<String, IAttribute> getAttributes()
	{
		return this.attributes;
	}
	
	public void findFormObject(List<FormTagObject> formList)
	{
		Iterator<XMLTagBase> it =  this.innerObj.iterator();
		while(it.hasNext()){
			XMLTagBase tag = it.next();

			tag.findFormObject(formList);
		}
	}
	
	public void findFormParams(List<IHtmlObject> formParams)
	{
		Iterator<XMLTagBase> it =  this.innerObj.iterator();
		while(it.hasNext()){
			XMLTagBase tag = it.next();

			tag.findFormParams(formParams);
		}
	}

	@Override
	public void getFormObjects(List<FormTagObject> formList)
	{
		Iterator<XMLTagBase> it =  this.innerObj.iterator();
		while(it.hasNext()){
			XMLTagBase tag = it.next();
			
			tag.getFormObjects(formList);
		}
		
	}

	@Override
	public void getFormInputObjects(List<XMLTagBase> formInputs)
	{
		Iterator<XMLTagBase> it =  this.innerObj.iterator();
		while(it.hasNext()){
			XMLTagBase tag = it.next();
			
			tag.getFormInputObjects(formInputs);
		}
	}
	
	public String getStaticId()
	{
		IAttribute typeAttr = this.attributes.get(ATTR_ID);
		if(typeAttr == null){
			return null;
		}
		
		String typeStr = typeAttr.getValue().getValue();
		return typeStr;
	}
	
	public String getStaticName()
	{
		IAttribute typeAttr = this.attributes.get(ATTR_NAME);
		if(typeAttr == null){
			return null;
		}
		
		String typeStr = typeAttr.getValue().getValue();
		return typeStr;
	}

	public int getXpathIndex()
	{
		return xpathIndex;
	}

	public void setXpathIndex(int xpathIndex)
	{
		this.xpathIndex = xpathIndex;
	}

}
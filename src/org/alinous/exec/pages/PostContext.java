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
package org.alinous.exec.pages;

import java.net.CookieManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.alinous.AlinousCore;
import org.alinous.AlinousDebug;
import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.IConnectionClosedListner;
import org.alinous.exec.AccessExecutionUnit;
import org.alinous.exec.InnerModulePath;
import org.alinous.exec.validator.ValidationStatus;
import org.alinous.expections.ExecutionException;
import org.alinous.objects.AlinousAttrs;
import org.alinous.objects.IAttribute;
import org.alinous.objects.XMLTagBase;
import org.alinous.objects.XMLTagBase.FormHiddenValue;
import org.alinous.objects.html.AlinousTopObject;
import org.alinous.objects.html.FormTagObject;
import org.alinous.parallel.AlinousThreadScope;
import org.alinous.parallel.IMainThreadContext;
import org.alinous.plugin.xa.AlinousGlobalIdDisposeShare;
import org.alinous.plugin.xa.AlinousRegisterdGlobalId;
import org.alinous.script.AlinousScript;
import org.alinous.script.IScriptSentence;
import org.alinous.script.basic.AbstractScriptBlock;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.functions.FuncDeclarations;
import org.alinous.script.functions.FunctionDeclaration;
import org.alinous.script.functions.system.debug.DebugCallbackHandler;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.security.AuthenticationContext;


public class PostContext implements IConnectionClosedListner
{
	private PostParams params = new PostParams();
	private HashMap<String, String> httpHeaders = new HashMap<String, String>();
	
	private String userName;
	private String requestPath;
	
	private PageHttpContext httpContext;
	
	private AccessExecutionUnit unit;
	private AlinousCore core;
	
	private boolean useFormCache;
	private boolean useValuavleRepositoryCache;
	
	private IScriptVariable scriptReturnedValue;
	protected IScriptSentence lastSentence;
	private Map<AbstractScriptBlock, Boolean> ifResult = new HashMap<AbstractScriptBlock, Boolean>();
	
	private FormValues formValues = new FormValues();
	private ValidationStatus validationStatus;
	
	private Stack<Stack<IStatement>> functionCallStack = new Stack<Stack<IStatement>>();
	//private Stack<RegisterPair> plusplusReserved = new Stack<RegisterPair>();
	
	private AuthenticationContext authContext;
	private String contextPath = "";
	private String servletPath = "";
	
	private String queryString;
	
	// returned
	private Map<IScriptSentence, IScriptVariable> returnedVariableMap = new HashMap<IScriptSentence, IScriptVariable>();
	
	// download
	private IScriptVariable contentTypeValue;
	private IScriptVariable downloadFileNameValue;
	
	// debug option
	private boolean debugEnabled;
	
	// include functions
	private Map<String, FunctionDeclaration> includeFuncDeclarations = new HashMap<String, FunctionDeclaration>();
	
	// for inner
	private AlinousTopObject topTopObject;
	private boolean inner;
	private InnerModulePath modulePath = new InnerModulePath();
	private HashMap<String, Integer> formVariableArrayCounter = new HashMap<String, Integer>();
	
	// Attributes
	private Map<FormTagObject, List<FormHiddenValue> > childsHiddens = new HashMap<FormTagObject, List<FormHiddenValue> >();
	private Map<XMLTagBase, IAttribute> overrideAttr = new HashMap<XMLTagBase, IAttribute>();
	
	// database
	private String dataSrc;
	private DataSrcConnection currentDataSrcConnection = null;
	
	// static mode
	private boolean isStatic = true;
	
	// precompile sql
	private PrecompileSQLContext precompile = new PrecompileSQLContext();
	
	// external resource handling
	private Map<String, IExtResource> extResources = new HashMap<String, IExtResource>();
	
	// exception
	private Throwable lastException;
	
	// parallel
	private Stack<AlinousThreadScope > parallelFinalVariableScope = new Stack<AlinousThreadScope>();
	private Stack<AlinousThreadScope > parallelExecutedScope = new Stack<AlinousThreadScope>();
	private Map<String, IMainThreadContext> mainThreadContextMap = new HashMap<String, IMainThreadContext>();
	
	// two phase commitment
	private AlinousGlobalIdDisposeShare globalTrxIds = new AlinousGlobalIdDisposeShare();
	
	// current line and path
	private String currentPath;
	private int currentLine;
	
	// Http access cookie
	private CookieManager httpAccessCookieManager = new CookieManager();
	
	// Web Debug mode
	private boolean debugMode = false;
	private String callbackScript;
	private DebugCallbackHandler debugCallbackHandler;
	
	public static PostContext createDummyContext(AlinousCore core)
	{
		return new PostContext(core, core.createAccessExecutionUnit("dummy"));
	}
	
	public PostContext(AlinousCore core, AccessExecutionUnit unit)
	{
		this.core = core;
		this.unit = unit;
		
		this.globalTrxIds.addHolder(this);
	}
	
	public void initIncludes(PostContext context)
	{
		Iterator<String> it = context.includeFuncDeclarations.keySet().iterator();
		while(it.hasNext()){
			String funcName = it.next();
			FunctionDeclaration funcDec = context.includeFuncDeclarations.get(funcName);
			
			this.includeFuncDeclarations.put(funcName, funcDec);
		}
		
	}
	
	public void initParams(String moduleName, Map<String, IParamValue> params)
	{
		this.params = new PostParams();
		this.params.initParams(params);
	}
	
	public void initParams(PostContext context)
	{
		// init param from web
		this.params.initParams(context.params);
		
		// transfer validation info
		this.validationStatus = context.getValidationStatus();
		
		// other params
		this.requestPath = context.getRequestPath();
		this.userName = context.getUserName();
		
		this.servletPath = context.servletPath;
		this.contextPath = context.contextPath;
		
		initHttpHeaders(context.httpHeaders);
		
		// static
		this.isStatic = context.isStatic;
		
		
		this.dataSrc = context.dataSrc;
		
		this.globalTrxIds = context.globalTrxIds;
		this.globalTrxIds.addHolder(this);
		
		// httpContext
		/*
		this.httpContext = new PageHttpContext();
		this.httpContext.setLocalAddr(context.httpContext.getLocalAddr());
		this.httpContext.setLocalName(context.httpContext.getLocalName());
		this.httpContext.setLocalPort(context.httpContext.getLocalPort());
		this.httpContext.setReferer(context.httpContext.getReferer());
		this.httpContext.setRemoteAddr(context.httpContext.getRemoteAddr());
		this.httpContext.setRemoteHost(context.httpContext.getRemoteHost());
		this.httpContext.setRemotePort(context.httpContext.getRemotePort());
		this.httpContext.setRemoteUser(context.httpContext.getRemoteUser());
		*/
	}
	
	public void initHttpHeaders(Map<String, String> m)
	{
		Iterator<String> it = m.keySet().iterator();
		while(it.hasNext()){
			String name = it.next();
			
			//AlinousDebug.debugOut("HTTP Header : " + name + " : " + m.get(name));
			this.httpHeaders.put(name, m.get(name));
		}
	}
	
	public IParamValue getParams(String key)
	{
		return this.params.get(key);
	}
	
	public Iterator<String> paramsIterator()
	{
		return this.params.paramKeyIterator();
	}
	
	public HashMap<String, IParamValue> getParamMap()
	{
		return this.params.getParamMap();
	}
	
	// Getter and Setter
	public boolean isBacking()
	{
		IParamValue backing = this.params.get(AlinousAttrs.ALINOUS_BACK);
		
		if(backing == null){
			return false;
		}
		
		return backing.toString().toLowerCase().equals(AlinousAttrs.VALUE_TRUE);
	}
	
	public void setBacking()
	{
		this.params.addParam(AlinousAttrs.ALINOUS_BACK, AlinousAttrs.VALUE_TRUE);
	}

	public String getNextAction()
	{
		IParamValue paramValue = this.params.get(FormTagObject.HIDDEN_FORM_ACTION);
		
		if(paramValue == null){
			return null;
		}
		return paramValue.toString();
	}
	
	public void resetNextAction()
	{
		this.params.removeParam(FormTagObject.HIDDEN_FORM_ACTION);
	}
	
	public void setNextAction(String value)
	{
		this.params.setParamValue(FormTagObject.HIDDEN_FORM_ACTION, value);
	}
	
	public String getUserName()
	{
		return userName;
	}

	public void setUserName(String userName)
	{
		this.userName = userName;
	}

	public AlinousCore getCore()
	{
		return core;
	}


	public String getSessionId()
	{
		return this.unit.getSessionId();
	}


	public String getTargetTagId()
	{
		IParamValue paramValue = this.params.get(FormTagObject.HIDDEN_FORM_TARGET_TAGID);
		
		if(paramValue == null){
			return null;
		}
		return paramValue.toString();
	}
	
	public void setTargetTagId(String tagId)
	{
		this.params.addParam(FormTagObject.HIDDEN_FORM_TARGET_TAGID, tagId);
	}

	public String getFormLastAction()
	{
		IParamValue paramValue = this.params.get(FormTagObject.HIDDEN_FORM_LAST_ACTION);
		
		if(paramValue == null){
			return null;
		}
		return paramValue.toString();
	}
	
	public String getFormLastTargetTagId()
	{
		IParamValue paramValue = this.params.get(FormTagObject.HIDDEN_FORM_LAST_TARGET_TAGID);
		
		if(paramValue == null){
			return null;
		}
		return paramValue.toString();
	}
	
	public String getFormLastFormId()
	{
		IParamValue paramValue = this.params.get(FormTagObject.HIDDEN_FORM_LAST_FORM_ID);
		
		if(paramValue == null){
			return null;
		}
		return paramValue.toString();
	}
	
	
	public AccessExecutionUnit getUnit()
	{
		return unit;
	}


	public boolean isUseFormCache()
	{
		return useFormCache;
	}


	public void setUseFormCache(boolean useFormCache)
	{
		this.useFormCache = useFormCache;
	}


	public boolean isUseValuavleRepositoryCache()
	{
		return useValuavleRepositoryCache;
	}


	public void setUseValuavleRepositoryCache(boolean useValuavleRepositoryCache)
	{
		this.useValuavleRepositoryCache = useValuavleRepositoryCache;
	}

/*
	// plus plus
	class RegisterPair{
		public ScriptDomVariable valable;
		public IPathElement path;
		public boolean inc;
	}
	
	public void addPlusPlus(ScriptDomVariable val, IPathElement path)
	{
		RegisterPair pair = new RegisterPair();
		pair.inc = true;
		pair.valable = val;
		pair.path = path;
		
		this.plusplusReserved.push(pair);
	}
	
	public void addMinusMinus(ScriptDomVariable val, IPathElement path)
	{
		RegisterPair pair = new RegisterPair();
		pair.inc = false;
		pair.valable = val;
		pair.path = path;
		
		this.plusplusReserved.push(pair);
	}
	
	public void handleReservedOperations(VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		while(this.plusplusReserved.size() > 0){
			RegisterPair pair = this.plusplusReserved.pop();
			
			if(pair.inc){
				pair.valable.incInt();
			}
			else{
				pair.valable.decInt();
			}
			valRepo.putValue(pair.path, pair.valable, this);
		}

	}
*/	
	public FormValues getFormValues()
	{
		return this.formValues;
	}

	public void setFormValues(FormValues formValues)
	{
		this.formValues = formValues;
	}


	public ValidationStatus getValidationStatus()
	{
		return validationStatus;
	}


	public void setValidationStatus(ValidationStatus validationStatus)
	{
		this.validationStatus = validationStatus;
	}


	public IScriptVariable getScriptReturnedValue()
	{
		return scriptReturnedValue;
	}


	public void setScriptReturnedValue(IScriptVariable returnedValue)
	{
		this.scriptReturnedValue = returnedValue;
	}
	
	
	
	public IScriptSentence getLastSentence()
	{
		return lastSentence;
	}

	public void setLastSentence(IScriptSentence lastSentence)
	{
		this.lastSentence = lastSentence;
	}

	public boolean isForwarded()
	{
		if(this.isBacking()){
			return false;
		}
		else if(this.isUseFormCache()){
			return false;
		}
		else if(this.isUseValuavleRepositoryCache()){
			return false;
		}
		else if(this.getValidationStatus() != null && !this.getValidationStatus().getStatus()){
			return false;
		}
		else if(this.scriptReturnedValue == null){
			return false;
		}
		else if(!(this.scriptReturnedValue instanceof ScriptDomVariable)){
			return false;
		}
		
		ScriptDomVariable dom = (ScriptDomVariable)this.scriptReturnedValue;
		
		if(!dom.getValueType().equals(IScriptVariable.TYPE_STRING)){
			return false;
		}
		
		return true;
	}

	
	public List<String> getParamsToIgnoreBlank()
	{
		List<String> list = new ArrayList<String>();
		
		Iterator<String> it = this.params.getParamMap().keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			
			if(key.startsWith(AlinousAttrs.ALINOUS_IGNOREBLANK)){
				list.add(key.substring(AlinousAttrs.ALINOUS_IGNOREBLANK.length() + 1));
			}
		}
		
		return list;
	}


	public String getRequestPath()
	{
		return requestPath;
	}


	public void setRequestPath(String requestPath)
	{
		this.requestPath = requestPath;
	}


	public AuthenticationContext getAuthContext()
	{
		return authContext;
	}


	public void setAuthContext(AuthenticationContext authContext)
	{
		this.authContext = authContext;
	}


	public String getContextPath() {
		return contextPath;
	}


	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}


	public String getServletPath() {
		return servletPath;
	}


	public void setServletPath(String servletPath) {
		this.servletPath = servletPath;
	}
	
	public String getFilePath(String path)
	{
		if(!path.startsWith("/")){
			return path;
		}
		
		String retStr = this.contextPath + this.servletPath + path;;
		if(retStr.startsWith("//")){
			return retStr.substring(1);
		}
		
		return retStr;
	}


	public PageHttpContext getHttpContext()
	{
		return httpContext;
	}

	public void setHttpContext(PageHttpContext httpContext)
	{
		this.httpContext = httpContext;
	}


	public HashMap<String, String> getHttpHeaders()
	{
		return httpHeaders;
	}

	public AlinousTopObject getTopTopObject()
	{
		return topTopObject;
	}

	public void setTopTopObject(AlinousTopObject topTopObject)
	{
		this.topTopObject = topTopObject;
	}
	
	public void setInner(boolean inner)
	{
		this.inner = inner;
	}

	public InnerModulePath getModulePath()
	{
		return modulePath;
	}

	public void setModulePath(InnerModulePath modulePath)
	{
		this.modulePath = modulePath;
	}

	public boolean isInner()
	{
		return inner;
	}
	
	public void clearHidden()
	{
		this.childsHiddens.clear();
	}

	public List<FormHiddenValue> getChildsHiddens(FormTagObject formTagObject)
	{
		List<FormHiddenValue> list = this.childsHiddens.get(formTagObject);
		if(list == null){
			list = new ArrayList<FormHiddenValue>();
			this.childsHiddens.put(formTagObject, list);
		}
		
		return list;
	}
	
	public IAttribute getOverrideMap(XMLTagBase tag)
	{
		return this.overrideAttr.get(tag);
	}
	
	public void setOverridedValue(XMLTagBase tag, IAttribute attr)
	{
		this.overrideAttr.put(tag, attr);
	}
	public void removeOverridedValue(XMLTagBase tag)
	{
		this.overrideAttr.remove(tag);
	}
	
	public void popFuncArgStack()
	{
		this.functionCallStack.pop();
	}
	
	public void pushNewFuncArgStack(Stack<IStatement> stmtStack)
	{
		this.functionCallStack.push(stmtStack);
	}
	
	public Stack<IStatement> getFuncArgStack()
	{
		return this.functionCallStack.peek();
	}

	public FunctionDeclaration getIncludeFuncDeclaration(String name)
	{
		// debug out
		/*
		Iterator<String> it = this.includeFuncDeclarations.keySet().iterator();
		while(it.hasNext()){
			AlinousDebug.debugOut("context has : " + it.next());
		}
		*/
		
		return this.includeFuncDeclarations.get(name);
	}

	public void putIncludeFuncDeclarations(FunctionDeclaration funcDeclaration)
	{
		this.includeFuncDeclarations.put(funcDeclaration.getName(), funcDeclaration);
	}
	
	
	// context and ececuting block
	public void registerIfResult(AbstractScriptBlock ifBlock, boolean bl)
	{
		this.ifResult.put(ifBlock, new Boolean(bl));
	}
	
	public boolean getIfResult(AbstractScriptBlock ifBlock)
	{
		return this.ifResult.get(ifBlock).booleanValue();
	}
	
	// debug enables
	public boolean isDebugEnabled()
	{
		return debugEnabled;
	}
	
	public void setDebugEnabled(boolean debugEnabled)
	{
		this.debugEnabled = debugEnabled;
	}
	
	// debug functions
	public void dumpFunctions()
	{
		AccessExecutionUnit unit = this.getUnit();
		AlinousExecutableModule mod = unit.getExecModule();
		AlinousScript sc = mod.getScript();
		
		FuncDeclarations dec = sc.getFuncDeclarations();
		
		AlinousDebug.debugOut(this.core, "functions: --------------------------------" + this.getUnit().getExecModule().getScript().getFilePath());
		
		Iterator<String> it = dec.iterateFuncNames();
		while(it.hasNext()){
			String func = it.next();
			
			AlinousDebug.debugOut(this.core, " " + func);
		}
	}

	public String getDataSrc()
	{
		return dataSrc;
	}

	public void setDataSrc(String dataSrc)
	{
		this.dataSrc = dataSrc;
	}

	public IScriptVariable getReturnedVariable(IScriptSentence sentence)
	{
		return this.returnedVariableMap.get(sentence);
	}

	public void setReturnedVariable(IScriptSentence sentence, IScriptVariable returnedVariable)
	{
		if(returnedVariable != null){
			this.returnedVariableMap.put(sentence, returnedVariable);
		}
	}

	public IScriptVariable getContentTypeValue()
	{
		return contentTypeValue;
	}

	public void setContentTypeValue(IScriptVariable contentTypeValue)
	{
		this.contentTypeValue = contentTypeValue;
	}

	public IScriptVariable getDownloadFileNameValue()
	{
		return downloadFileNameValue;
	}

	public void setDownloadFileNameValue(IScriptVariable downloadFileNameValue)
	{
		this.downloadFileNameValue = downloadFileNameValue;
	}

	public boolean isStatic() {
		return isStatic;
	}

	public void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public PrecompileSQLContext getPrecompile()
	{
		return precompile;
	}

	public DataSrcConnection getCurrentDataSrcConnection() {
		return currentDataSrcConnection;
	}

	public void setCurrentDataSrcConnection(
			DataSrcConnection currentDataSrcConnection)
	{
		this.currentDataSrcConnection = currentDataSrcConnection;
		currentDataSrcConnection.addConnectionCloseListner(this);
	}
	
	/**
	 * fired by connection on closed
	 */
	public void fireConnectionClosed(DataSrcConnection con)
	{
		if(this.currentDataSrcConnection == con && con.isClosed()){
			this.currentDataSrcConnection = null;
		}
	}
	
	protected void disposeCurrentDataSource()
	{
		if(this.currentDataSrcConnection != null){
			DataSrcConnection cur = this.currentDataSrcConnection;
			this.currentDataSrcConnection = null;
			
			cur.close();
		}
	}
	
	public void dispose()
	{
		disposeGlobalTransactions();
		disposeCurrentDataSource();
		disposeAllExternalResources();
		
		this.parallelExecutedScope.clear();
		this.parallelFinalVariableScope.clear();
	}
	
	private void disposeGlobalTransactions()
	{
		// dispose tpc
		try {
			this.globalTrxIds.removeWithDispose(this, core);
		} catch (ExecutionException e) {
			this.core.getLogger().reportError(e);
			e.printStackTrace();
		}
	}
	
	public void registerExternalResource(String type, String name, IExtResource resource)
	{
		String hashKey = type + "__" + name;
		
		synchronized (this.extResources) {
			this.extResources.put(hashKey, resource);
		}
		
	}
	
	public IExtResource getExtResource(String type, String name)
	{
		String hashKey = type + "__" + name;
		synchronized (this.extResources) {
			return this.extResources.get(hashKey);
		}
	}
	
	public void disposeExtResource(String type, String name)
	{
		String hashKey = type + "__" + name;
		
		synchronized (this.extResources) {
			IExtResource resource = this.extResources.get(hashKey);
			resource.discard();
			
			this.extResources.remove(hashKey);
		}
	}
	
	public void disposeAllExternalResources()
	{
		Iterator<String> it = this.extResources.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			IExtResource resource = this.extResources.get(key);
			
			resource.discard();
			
			this.extResources.remove(key);
		}
	}
	
	public int getFromVariableCounter(String key)
	{
		Integer cnt = this.formVariableArrayCounter.get(key);
		if(cnt == null){
			return 0;
		}
		
		return cnt.intValue();
	}
	
	public void incFromVariableCounter(String key)
	{
		Integer cnt = this.formVariableArrayCounter.get(key);
		if(cnt == null){
			this.formVariableArrayCounter.put(key, new Integer(1));
			return;
		}
		
		this.formVariableArrayCounter.put(key, new Integer(cnt.intValue() + 1));
	}

	public Throwable getLastException() {
		return lastException;
	}

	public void setLastException(Throwable lastException) {
		this.lastException = lastException;
	}

	public Stack<AlinousThreadScope> getParallelFinalVariableScope() {
		return parallelFinalVariableScope;
	}
	
	public Stack<AlinousThreadScope> getParallelExecutedScope() {
		return parallelExecutedScope;
	}
	
	public void addParallelExecutedScope(AlinousThreadScope scope)
	{
		if(!this.parallelExecutedScope.contains(scope)){
			this.parallelExecutedScope.push(scope);
		}
	}
	
	public void clearParallelExecutedScope() {
		parallelExecutedScope.clear();
	}
	
	@Override
	protected void finalize() throws Throwable {
		dispose();
		
		super.finalize();
	}

	public IMainThreadContext getMainThreadContext(String scopeRefStr)
	{
		synchronized (this.mainThreadContextMap) {
			return this.mainThreadContextMap.get(scopeRefStr);
		}
	}
	
	public void setMainThreadContext(String scopeRefStr, IMainThreadContext mainThreadContext)
	{
		synchronized (this.mainThreadContextMap) {
			this.mainThreadContextMap.put(scopeRefStr, mainThreadContext);
		}
	}

	
	/********************************************
	 * 
	 * @param gid
	 */
	public void addGlobalTrxId(AlinousRegisterdGlobalId gid)
	{
		this.globalTrxIds.register(this, gid);
	}
	
	public void removeGlobalTrxId(AlinousRegisterdGlobalId gid)
	{
		this.globalTrxIds.remove(this, gid);
	}
	
	public String showGlobalIdStatus()
	{
		return this.globalTrxIds.getStatusString();
	}

	public String getCurrentPath() {
		return currentPath;
	}

	public void setCurrentPath(String currentPath) {
		this.currentPath = currentPath;
	}

	public int getCurrentLine() {
		return currentLine;
	}

	public void setCurrentLine(int currentLine) {
		this.currentLine = currentLine;
	}

	public CookieManager getHttpAccessCookieManager() {
		return httpAccessCookieManager;
	}

	public void setHttpAccessCookieManager(CookieManager httpAccessCookieManager) {
		this.httpAccessCookieManager = httpAccessCookieManager;
	}

	public boolean isDebugMode() {
		return debugMode;
	}

	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}

	public String getCallbackScript() {
		return callbackScript;
	}

	public void setCallbackScript(String callbackScript) {
		this.callbackScript = callbackScript;
	}

	public DebugCallbackHandler getDebugCallbackHandler() {
		return debugCallbackHandler;
	}

	public void setDebugCallbackHandler(DebugCallbackHandler debugCallbackHandler) {
		this.debugCallbackHandler = debugCallbackHandler;
	}
	
	
}

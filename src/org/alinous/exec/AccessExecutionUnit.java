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
package org.alinous.exec;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.alinous.AlinousConfig;
import org.alinous.AlinousCore;
import org.alinous.AlinousUtils;
import org.alinous.datasrc.AlinousDataSourceManager;
import org.alinous.exec.pages.AlinousExecutableModule;
import org.alinous.exec.pages.ArrayParamValue;
import org.alinous.exec.pages.ConnectionManager;
import org.alinous.exec.pages.FileResourceManager;
import org.alinous.exec.pages.ForwardResult;
import org.alinous.exec.pages.IDesign;
import org.alinous.exec.pages.IParamValue;
import org.alinous.exec.pages.PostContext;
import org.alinous.exec.validator.ValidationRequest;
import org.alinous.exec.validator.ValidationStatus;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ModuleNotFoundException;
import org.alinous.expections.SimpleContentException;
import org.alinous.objects.html.AlinousTopObject;
import org.alinous.repository.AlinousModule;
import org.alinous.repository.AlinousModuleRepository;
import org.alinous.repository.AlinousSystemRepository;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.security.AlinousSecurityManager;

public class AccessExecutionUnit
{
	private AlinousModuleRepository parentRepository = null;
	private ExecResultCache execResultCache;
	private FormValueCache formValueCache;
	private InnerStatusCache innserStatusCache;
	private BackingStatusCache backingStatusCache;
	private SessionController sessionController;
	
	private AlinousConfig config;
	private AlinousSecurityManager securityManager;
	private String sessionId;
	private AlinousDataSourceManager dataSourceManager;
	private ConnectionManager connectionManager;
	private FileResourceManager fileResourceManager;
	
	private AlinousCore core;
	
	private AlinousExecutableModule execModule;
	
	private ForwardResult forwardResult;
	
	public AccessExecutionUnit(AlinousModuleRepository repo, String sessionId,
			AlinousSystemRepository sysrepo, AlinousDataSourceManager dataSourceManager, AlinousCore core)
	{
		this.core = core;
		
		this.parentRepository = repo;
		this.sessionId = sessionId;
		
		this.execResultCache = new ExecResultCache(sysrepo, this.sessionId, this.core);
		this.formValueCache = new FormValueCache(sysrepo, this.sessionId, this.core);
		this.innserStatusCache = new InnerStatusCache(sysrepo, this.sessionId, this.core);
		this.backingStatusCache = new BackingStatusCache(sysrepo, this.sessionId, this.core);
		this.sessionController = new SessionController(sysrepo, this.sessionId, this.core);
		
		this.dataSourceManager = dataSourceManager;
		
		this.connectionManager = new ConnectionManager(dataSourceManager);
		this.fileResourceManager = new FileResourceManager(core);
		
	}
	
	public AccessExecutionUnit(AlinousModuleRepository repo, String sessionId,
			AlinousSystemRepository sysrepo, AlinousDataSourceManager dataSourceManager,
			AccessExecutionUnit parent, AlinousCore core)
	{
		this.parentRepository = repo;
		this.sessionId = sessionId;
		this.core = core;
		
		this.execResultCache = new ExecResultCache(sysrepo, this.sessionId, this.core);
		this.formValueCache = new FormValueCache(sysrepo, this.sessionId, this.core);
		this.innserStatusCache = new InnerStatusCache(sysrepo, this.sessionId, this.core);
		this.backingStatusCache = new BackingStatusCache(sysrepo, this.sessionId, this.core);
		this.sessionController = new SessionController(sysrepo, this.sessionId, this.core);
		
		this.dataSourceManager = dataSourceManager;
		
		// no parent
		this.connectionManager = new ConnectionManager(dataSourceManager);
		this.fileResourceManager = new FileResourceManager(core);
		
	}
	
	public AccessExecutionUnit copyUnit()
	{
		AccessExecutionUnit unit = new AccessExecutionUnit(this.parentRepository, this.sessionId,
				this.execResultCache.getSystemRepository(), this.dataSourceManager, this.core);
		
		unit.execModule = this.execModule;
		
		return unit;
	}
	
	
	/**
	 * Called once from Top TopObject
	 * @param path
	 * @param context
	 * @return
	 * @throws AlinousException
	 */
	public IDesign gotoPage(String path, PostContext context, VariableRepository valRepo)
			throws AlinousException
	{
		// Check security
		this.securityManager.checkSecurity(path, this.config, context);
		
		// Store form value of last Page
		String formLastAction = context.getFormLastAction();
		String formLastTarget = context.getFormLastTargetTagId();
		String formLastFormId = context.getFormLastFormId();
		
		if(formLastAction != null && formLastTarget != null){
			if(formLastFormId == null){
				formLastFormId = "";
			}
			
			InnerModulePath innerModPath = new InnerModulePath(formLastTarget);
			this.formValueCache.storeFormValue(context, innerModPath, context.getParamMap(), 
						formLastAction, formLastFormId, context.getSessionId());
			
			if(context.getNextAction() != null){
				this.backingStatusCache.storeLastPath(context, innerModPath, context.getNextAction(), formLastAction, context.getSessionId());
			}
		}
		
		// Validator here
		handleValidation(context, valRepo);
	
		// init values
		initCacheStatus(context);

		// If validation failure, change destination
		path = handleValidationDestination(path, context);
		
		return gotoPage(path, context, valRepo, null, null);
	}
	
	/**
	 * Innner page execution
	 * @param path
	 * @param context
	 * @param currentPath
	 * @param topObj
	 * @return
	 * @throws AlinousException
	 */
	public IDesign gotoPageWithoutSecurity(String path, PostContext context, VariableRepository valRepo)
					throws AlinousException
	{
		// Store form value of last Page
		String formLastAction = context.getFormLastAction();
		String formLastTarget = context.getFormLastTargetTagId();
		String formLastFormId = context.getFormLastFormId();
		
		if(formLastAction != null && formLastTarget != null){
			if(formLastFormId == null){
				formLastFormId = "";
			}
			
			InnerModulePath innerModPath = new InnerModulePath(formLastTarget);
			this.formValueCache.storeFormValue(context, innerModPath, context.getParamMap(), 
						formLastAction, formLastFormId, context.getSessionId());
			
			if(context.getNextAction() != null){
				this.backingStatusCache.storeLastPath(context, innerModPath, context.getNextAction(), formLastAction, context.getSessionId());
			}
		}
		
		// Validator here
		handleValidation(context, valRepo);
	
		// init values
		initCacheStatus(context);

		// If validation failure, change destination
		path = handleValidationDestination(path, context);
		
		return gotoPage(path, context, valRepo, null, null);
	}
	
	
	/**
	 * For only top module
	 * @param context
	 */
	private void initCacheStatus(PostContext context)
	{
		String targetPath = context.getTargetTagId();
		
		//if request is for inner module, do nothing here
		if(targetPath != null && !targetPath.equals("")){
			context.setUseFormCache(true);
			context.setUseValuavleRepositoryCache(true);
			return;
		}

		
		if(context.isBacking()){
			context.setUseFormCache(true);
			context.setUseValuavleRepositoryCache(true);
		}
		
		if(!context.getValidationStatus().getStatus()){
			// Validation logics
			context.setUseFormCache(true);
			context.setUseValuavleRepositoryCache(true);
		}
	}
	
	/**
	 * Innner page execution
	 * @param path
	 * @param context
	 * @param currentPath
	 * @param topObj
	 * @return
	 * @throws AlinousException
	 */
	public IDesign gotoPage(String path, PostContext context, VariableRepository valRepo, InnerModulePath currentPath,
							AlinousTopObject topObj)
					throws AlinousException
	{
		// load latest Executable module
		AlinousModule parentModule = this.parentRepository.getModule(path);
		
		// if static module
		if(parentModule != null && parentModule.isStatic()){
			throw new SimpleContentException();
		}
		
		// if parent does not exists, it means the page was deleted or not exists
		if(parentModule == null){
			throw new ModuleNotFoundException(path);
		}
		
		// Usual case
		this.execModule = parentModule.fork();
		if(currentPath != null && this.execModule.getDesign() != null){
			InnerModulePath newPath = currentPath.deepClone();
			//newPath.addPath(this.execModule.getDesign().getPath());
			
			execModule.setCurrentInnerModulePath(context, newPath);
		}
		
		// set toptopobject
		if(topObj != null){
			context.setTopTopObject(topObj);
		}else{
			context.setTopTopObject(this.execModule.getDesign());
		}
		
		ForwardResult result = this.execModule.post(context, valRepo, this.execResultCache, this.dataSourceManager, this.config);
		this.forwardResult = result;
		
		return this.execModule.getDesign();
	}
	
	public IScriptVariable pluginExecute(String path, PostContext context, VariableRepository valRepo,
			boolean parepareDebug, boolean initOperation) throws AlinousException
	{
		// load latest Executable module
		AlinousModule parentModule = this.parentRepository.getModule(path);
		
		// if parent does not exists, it means the page was deleted or not exists
		if(parentModule == null){
			throw new ModuleNotFoundException(path);
		}
		
		// Usual case
		this.execModule = parentModule.fork();
		
		
		this.execModule.pluginPost(context, valRepo, dataSourceManager, parepareDebug, initOperation);
		
		return context.getScriptReturnedValue();
	}
	
	private String handleValidationDestination(String path, PostContext context)
										throws AlinousException
	{
		// destination
		if(context.getValidationStatus().getStatus()){
			return path;
		}
		
		String targetPath = context.getTargetTagId();
		
		//if request is for inner module, do nothing here
		if(targetPath != null && !targetPath.equals("")){
			return path;
		}
		
		String lastAction = context.getFormLastAction();
		
		lastAction = AlinousUtils.getModuleName(lastAction);
		context.getCore().registerAlinousObject(context, lastAction);
		
		return lastAction;
	}
	
	private void handleValidation(PostContext context, VariableRepository valRepo) throws AlinousException
	{
		List<ValidationRequest> list = new ArrayList<ValidationRequest>();
		
		// detect validation
		Iterator<String> it = context.paramsIterator();
		while(it.hasNext()){
			String paramName = it.next();
			String fullParamName = paramName;
			
			IParamValue tmp = context.getParams(paramName);
			if(tmp instanceof ArrayParamValue){
				fullParamName = paramName  + "[]";
			}
			
			ValidationRequest req = ValidationRequest.getRequest(fullParamName,
						context.getParams(paramName), context.getParamMap());
			
			if(req != null){
				list.add(req);
			}
			
		}
		
		// validate
		ValidationStatus status = new ValidationStatus();
		Iterator<ValidationRequest> reqIt = list.iterator();
		while(reqIt.hasNext()){
			ValidationRequest request = reqIt.next();
			
			boolean result = request.validate(context, valRepo);
			
			if(!result){
				status.addFailedRequest(request);
			}
			
		}
		
		context.setValidationStatus(status);
		
	}

	public IScriptVariable executeValidation(PostContext context, VariableRepository valRepo, String moduleName,
						String inputName, String formName, boolean isArray)
					throws AlinousException
	{
		// load latest Executable module
		AlinousModule parentModule = this.parentRepository.getModule(moduleName);
		
		// if parent does not exists, it means the page was deleted or not exists
		if(parentModule == null){
			throw new ModuleNotFoundException(moduleName);
		}
		
		// Usual case
		this.execModule = parentModule.fork();
		
		// execute
		IScriptVariable retVal = this.execModule.executeValidation(context, valRepo, inputName, formName, isArray);
		
		return retVal;
	}
	
	// Getter and Setter
	public void setConfig(AlinousConfig config)
	{
		this.config = config;
	}

	public void setSecurityManager(AlinousSecurityManager securityManager) {
		this.securityManager = securityManager;
	}
	
	public InnerStatusCache getInnserStatusCache() {
		return innserStatusCache;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void dispose()
	{		
		this.execModule = null;
		this.connectionManager.dispose();
		this.fileResourceManager.dispose();
	}

	public AlinousExecutableModule getExecModule()
	{
		return execModule;
	}

	public void setExecModule(AlinousExecutableModule execModule) {
		this.execModule = execModule;
	}

	public FormValueCache getFormValueCache()
	{
		return formValueCache;
	}
	
	public BackingStatusCache getBackingStatusCache()
	{
		return this.backingStatusCache;
	}

	public ConnectionManager getConnectionManager()
	{
		return connectionManager;
	}
	
	
	public void setConnectionManager(ConnectionManager connectionManager)
	{
		this.connectionManager = connectionManager;
	}

	public SessionController getSessionController()
	{
		return sessionController;
	}
	
	public AlinousCore getCore()
	{
		return core;
	}

	public void setCore(AlinousCore core)
	{
		this.core = core;
	}

	public ForwardResult getForwardResult()
	{
		return forwardResult;
	}

	public FileResourceManager getFileResourceManager()
	{
		return fileResourceManager;
	}

}

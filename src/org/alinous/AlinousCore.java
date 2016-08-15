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
package org.alinous;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Semaphore;

import org.alinous.cloud.AlinousCloudManager;
import org.alinous.components.ComponentManager;
import org.alinous.datasrc.AlinousDataSourceManager;
import org.alinous.datasrc.basic.ILogProvidor;
import org.alinous.debug.AlinousDebugManager;
import org.alinous.exec.AccessExecutionUnit;
import org.alinous.exec.lock.SessionLockManager;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.jdk.JavaConnectorFunctionManager;
import org.alinous.logger.AlinousLogger;
import org.alinous.parallel.AlinousParallelThreadManager;
import org.alinous.plugin.postgres.PoolTester;
import org.alinous.repository.AlinousModuleRepository;
import org.alinous.repository.AlinousSystemRepository;
import org.alinous.script.AlinousScript;
import org.alinous.script.IScriptObject;
import org.alinous.script.functions.system.JavaConnectorFunctionCallback;
import org.alinous.security.AlinousSecurityManager;
import org.alinous.stdio.AlinousStdIo;
import org.alinous.test.coverage.CoverateManager;
import org.alinous.test.coverage.FileCoverage;
import org.alinous.web.filter.WebFilterManager;

/**
 * AinousCore provides main functions.
 * ie. parse HTML+a, which means AlinousObject Object Tree
 *     parse AlinousScript
 * @author iizuka
 *
 */
public class AlinousCore
{
	private static boolean debug = true;
	
	public static boolean debug(PostContext context){
		if(context == null){
			return AlinousCore.debug;
		}
		
		// 
		// AlinousDebug.debugOut("Debug is available ? " + (AlinousCore.debug || context.isDebugMode()));
		
		return AlinousCore.debug || context.isDebugMode();
	}
	
	public static void setDebug(boolean indebug){
		//if(EDITION.equals(SConfigrator.DEVEL)){
		//	return;
		//}
		AlinousCore.debug  = indebug;
		
		usePrecompileSelect = false;
		usePrecompileUpdate = false;
		usePrecompileInsert = false;
		usePrecompileDelete = false;
	}
	
	public static boolean enterprise = true;
	public static boolean showad = true;
	
	public static String adImg = "";
	public static String adUrl = "";
	public static String adAlt = "";
	
	public static int defaultSEmaphore = 256;
	public static boolean optimize = true;
	public static boolean enableVirtualHost = true;
	
	public JavaConnectorFunctionCallback javaConnectorFunctionCallback;
	
	public static final String ENTERPRISE = "ENTER";
	
	public static final String DB_MIN_INTERCEPTOR = "org.alinous.plugin.MinInterceptor";
	public static final String DB_ENTRY_INTERCEPTOR = "org.alinous.plugin.EntryInterceptor";
	public static final String DB_LITE_INTERCEPTOR = "org.alinous.plugin.EntryInterceptor";
	
	private static boolean usePrecompileSelect = false;
	private static boolean usePrecompileUpdate = false;
	private static boolean usePrecompileInsert = false;
	private static boolean usePrecompileDelete = false;
	public static boolean usePrecompileSelect(){return usePrecompileSelect;}
	public static boolean usePrecompileUpdate(){return usePrecompileUpdate;}
	public static boolean usePrecompileInsert(){return usePrecompileInsert;}
	public static boolean usePrecompileDelete(){return usePrecompileDelete;}
	
	public static String DB_INTERCEPTOR_CLASS = null;
	
	public static int MAX_ONE_VALUE_RECORD = 1024 * 256;
	
	public static int MAX_CACHE_PAGE = 64;
	//public static int MAX_CACHE_PAGE = 2048; // MYSQL
	
	private static boolean coverageTest = false;
	public static boolean coverageTest(){ return coverageTest;}

	

	// Semaphore
	private Semaphore semaphore;
	
	// virtual host
	private boolean virtualHost;
	
	// Core Modules
	private AlinousModuleRepository moduleRepository;
	private String alinousHome;
	private AlinousConfig config;
	private AlinousSecurityManager securityManager;
	//private AlinousHomeScanner homeScanner;
	private ILogProvidor logger;
	private AlinousDataSourceManager dataSourceManager;
	private SessionLockManager sesssionLockManager;
	
	// output filter
	private WebFilterManager filterManager;
	
	// Java Connector
	private JavaConnectorFunctionManager javaConnector;
	
	// High Level Core Service Module using AlinousDataSourceManager
	private AlinousSystemRepository systemRepository;

	// Maintain System tables
	AlinousMaintainThreadManager maintainanceService;
	
	// Debug support
	private AlinousDebugManager debugManager;
	
	// Singleton instance
	//private static AlinousCore instance = null;
	
	// MIME types
	private MimeManager mime;
	
	// Component Manager
	private ComponentManager cmpManager;
	
	// Code coverage
	private CoverateManager codeCoverageManager;
	
	// Parallel
	private AlinousParallelThreadManager parallelThreadManager;
	
	// dispose check
	private boolean initialized;
	
	// stdio
	private AlinousStdIo stdio;
	
	public static final String EXT_HTML = "html";
	public static final String EXT_ALNS = "alns";
	
	public AlinousCore(){}
	
	public static synchronized AlinousCore getInstance(String alinousHome, InputStream stream) throws Throwable
	{
		AlinousCore instance = null;
		
		// If instance does not exists
		if(alinousHome != null){
			// init debug
			if(debug){
				PoolTester.init(alinousHome);
			}
			
			instance = new AlinousCore();
			instance.setVirtualHost(false);
			
			try{
				instance.initInstance(alinousHome, stream);
			}catch(Throwable e){
				instance = null;
				
				if(e.getMessage() != null && e.getMessage().equals(new String(new byte[]{76,73,67,69,78,83,69,32,69,82,82,79,82}))){
					throw new AlinousException(e.getMessage());
				}
				
				throw e;
			}
		}else if(alinousHome == null){
			throw new AlinousException("Could not exitialize because alinousHome equals null.");
		}
		
		// init mime
		if(instance.getMime() == null && stream != null){
			instance.mime = new MimeManager(stream);
		}
		
		return instance;
	}
	
	private void initInstance(String home, InputStream stream) throws AlinousException
	{
		AlinousCloudManager mgr = AlinousCloudManager.getInstance();
		
		this.alinousHome = home;
		this.config = new AlinousConfig(this.alinousHome);
		this.stdio = new AlinousStdIo();
		
		this.config.parseConfig(this); // parse config
		
		if(!mgr.isCoudEnabled()){
			this.logger = new AlinousLogger(this.config, this.alinousHome);
		}
		else{
			// cloud log
			this.logger = mgr.getLogProvidor();
		}
		
		this.parallelThreadManager = new AlinousParallelThreadManager(this.config.getSystemRepositoryConfig().getThreads());
		
		this.moduleRepository = new AlinousModuleRepository(this);
		this.dataSourceManager
			= new AlinousDataSourceManager(this.config.getDataSourceConfig(), this.logger);
		this.sesssionLockManager = new SessionLockManager();
		
		this.filterManager = new WebFilterManager(this);
		
		// Java Connector
		this.javaConnector = new JavaConnectorFunctionManager(home, this, true, true);
		this.javaConnectorFunctionCallback = new JavaConnectorFunctionCallback(this);
		try {
			this.javaConnector.init();
			this.javaConnector.startScan();
		} catch (InstantiationException e) {
			throw new AlinousException(e, "Failed to init JavaConnector");
		} catch (IllegalAccessException e) {
			throw new AlinousException(e, "Failed to init JavaConnector");
		} catch (ClassNotFoundException e) {
			throw new AlinousException(e, "Failed to init JavaConnector");
		}		
		
		dataSourceManager.init(this, this.config.getSystemRepositoryConfig().getSystemSrc()); // init data source connection
		
		this.systemRepository = new AlinousSystemRepository(this.dataSourceManager);
		this.systemRepository.install(this.config.getSystemRepositoryConfig());
		
		this.securityManager = new AlinousSecurityManager(this, this.dataSourceManager, this.systemRepository, this.config);
		this.securityManager.init();
		
		
		// Component Manager
		try{
			this.cmpManager = new ComponentManager(this);
		}catch(Throwable e){
			this.logger.reportError(e);
		}
		
		// first scan
		//this.homeScanner.scan();
		
		// debug manager
		//if(AlinousCore.debug){
			this.debugManager = new AlinousDebugManager(this);
		//}
		
		// maintain service start
		this.maintainanceService = new AlinousMaintainThreadManager(this.systemRepository,
				this.logger, this);
		
		if(mgr.isCloudThreadEnabled()){
			this.maintainanceService.start();
		}
		
		// setup semaphor
		this.semaphore = new Semaphore(AlinousCore.defaultSEmaphore);
		
		// coverage manager
		if(AlinousCore.coverageTest()){
			this.codeCoverageManager = new CoverateManager(this.config.getCoverageConfig(), this.alinousHome);
			
		}
		
		initialized = true;
		
	}
	
	
	
	public void registerAlinousObject(PostContext context, String dsPath, String acPath) throws AlinousException
	{
		this.moduleRepository.registerAlinousModule(context, dsPath, acPath);
		
	}
	
	public void registerAlinousObject(PostContext context, String moduleName) throws AlinousException
	{
		String designPath = moduleName + "." + EXT_HTML;
		String scriptPath = moduleName + "." + EXT_ALNS;
		
		registerAlinousObject(context, designPath, scriptPath);
	}
	
	public AccessExecutionUnit createAccessExecutionUnit(String sessionId)
	{
		AccessExecutionUnit retObj = new AccessExecutionUnit(this.moduleRepository, sessionId, systemRepository, dataSourceManager, this);
		retObj.setConfig(config);
		retObj.setSecurityManager(securityManager);
		
		return retObj;
	}
	
	public AccessExecutionUnit createAccessExecutionUnit(String sessionId, PostContext context)
	{
		AccessExecutionUnit retObj = new AccessExecutionUnit(this.moduleRepository, sessionId, systemRepository, dataSourceManager, this);
		retObj.setConfig(config);
		retObj.setSecurityManager(securityManager);
		
		retObj.setConnectionManager(context.getUnit().getConnectionManager());
		
		return retObj;
	}
	
	public AccessExecutionUnit createAccessExecutionUnit(String sessionId, AccessExecutionUnit parent)
	{
		AccessExecutionUnit retObj = new AccessExecutionUnit(this.moduleRepository, sessionId, systemRepository, dataSourceManager, parent, this);
		retObj.setConfig(config);
		retObj.setSecurityManager(securityManager);
		
		return retObj;
	}

	public void reportError(Throwable ex)
	{
		this.logger.reportError(ex);
	}
	
	
	// Getter and Setter
	public AlinousConfig getConfig() {
		return config;
	}
	
	public String getHome()
	{
		return this.alinousHome;
	}
	
	public AlinousDataSourceManager getDataSourceManager()
	{
		return dataSourceManager;
	}

	public AlinousDebugManager getAlinousDebugManager()
	{
		return this.debugManager;
	}
	
	public AlinousSystemRepository getSystemRepository()
	{
		return this.systemRepository;
	}

	public AlinousSecurityManager getSecurityManager()
	{
		return securityManager;
	}

	public SessionLockManager getSesssionLockManager()
	{
		return sesssionLockManager;
	}

	public JavaConnectorFunctionManager getJavaConnector()
	{
		return javaConnector;
	}
	
	public void dispose()
	{
		this.stdio.dispose();
		this.javaConnector.endScan();
		this.maintainanceService.stop();
		
		this.dataSourceManager.dispose();
		
		try {
			this.parallelThreadManager.dispose();
		} catch (Throwable e) {
			logger.reportError(e);
		}
	}

	public ILogProvidor getLogger()
	{
		return logger;
	}
	
	
	public MimeManager getMime()
	{
		return mime;
	}

	public WebFilterManager getFilterManager()
	{
		return filterManager;
	}
	
	

	public ComponentManager getCmpManager()
	{
		return cmpManager;
	}
	
	
	
	public AlinousModuleRepository getModuleRepository()
	{
		return moduleRepository;
	}

	@Override
	protected void finalize() throws Throwable
	{
		dispose();
		
		super.finalize();
	}
	
	public void acquireSemaphor()
	{
		try {
			this.semaphore.acquire();
		} catch (InterruptedException e) {
			this.getLogger().reportError(e);
		}
	}
	
	public void releaseSemaphore()
	{
		this.semaphore.release();
	}

	public boolean isVirtualHost()
	{
		return virtualHost;
	}

	public void setVirtualHost(boolean virtualHost)
	{
		this.virtualHost = virtualHost;
	}
	
	public void reporttExecuted(IScriptObject scriptObj)
	{
		if(this.codeCoverageManager == null){
			return;
		}
		
		//AlinousDebug.debugOut("reporttExecuted() : " + scriptObj.getFilePath()); 
		
		FileCoverage fileCoverage = this.codeCoverageManager.getFileCoverages().get(scriptObj.getFilePath());
		if(fileCoverage == null){
			return;
		}
		
		fileCoverage.reportExecuted(scriptObj);
	}
	
	public void initCoverage(AlinousScript script, long alnsFileTimestamp) throws AlinousException
	{
		if(this.codeCoverageManager == null){
			return;
		}
		
		this.codeCoverageManager.initCoverage(script, alnsFileTimestamp);
	}
	
	public void flashCoverageIntoFile(AlinousScript script) throws AlinousException
	{
		try {
			this.codeCoverageManager.flashCoverageIntoFile(script);
		} catch (IOException e) {
			e.printStackTrace();
			throw new AlinousException(e, "Failed in storing file.");
		}
	}
	public AlinousParallelThreadManager getParallelThreadManager() {
		return parallelThreadManager;
	}
	public boolean isInitialized() {
		return initialized;
	}

	public JavaConnectorFunctionCallback getJavaConnectorFunctionCallback() {
		return javaConnectorFunctionCallback;
	}

	public AlinousStdIo getStdio() {
		return stdio;
	}
	
}

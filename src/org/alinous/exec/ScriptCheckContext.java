package org.alinous.exec;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.alinous.AlinousConfig;
import org.alinous.exec.check.AlinousScriptCompiledCache;
import org.alinous.exec.check.FunctionCheckRequest;
import org.alinous.exec.check.IJavaConnectorManager;
import org.alinous.exec.check.IncludeUseChecker;
import org.alinous.script.AlinousScript;
import org.alinous.script.functions.FunctionDeclaration;

public class ScriptCheckContext
{
	private AlinousScript alinousScript;
	private AlinousConfig alinousConfig;
	private IJavaConnectorManager javaConnectorManager;
	private String alinousHome;
	private AlinousScriptCompiledCache cache;
	private IncludeUseChecker includeChecker;
	
	private List<FunctionCheckRequest> functionCheckRequests = new ArrayList<FunctionCheckRequest>();
	private List<AlinousScript> includeScripts = new ArrayList<AlinousScript>();
	
	private IExecutable currentExecutable;
	
	private boolean skipOnce;
	
	public void addIncludeScript(AlinousScript incScript)
	{
		this.includeScripts.add(incScript);
	}
	
	public FunctionDeclaration findFunctionDeclaration(String qualifiedName)
	{
		Iterator<AlinousScript> it = this.includeScripts.iterator();
		while(it.hasNext()){
			AlinousScript incScript = it.next();
			
			FunctionDeclaration funcDec = incScript.getFuncDeclarations().findFunctionDeclare(qualifiedName);
			if(funcDec != null){
				return funcDec;
			}
		}
		
		return null;
	}
	
	
	public AlinousScript getAlinousScript()
	{
		return alinousScript;
	}
	public void setAlinousScript(AlinousScript alinousScript)
	{
		this.alinousScript = alinousScript;
	}
	public AlinousConfig getAlinousConfig()
	{
		return alinousConfig;
	}
	public void setAlinousConfig(AlinousConfig alinousConfig)
	{
		this.alinousConfig = alinousConfig;
	}
	public boolean isSkipOnce()
	{
		return skipOnce;
	}
	public void setSkipOnce(boolean skipOnce)
	{
		this.skipOnce = skipOnce;
	}
	public IJavaConnectorManager getJavaConnectorManager()
	{
		return javaConnectorManager;
	}
	public void setJavaConnectorManager(IJavaConnectorManager javaConnectorManager)
	{
		this.javaConnectorManager = javaConnectorManager;
	}
	public String getAlinousHome()
	{
		return alinousHome;
	}
	public void setAlinousHome(String alinousHome)
	{
		this.alinousHome = alinousHome;
	}
	public AlinousScriptCompiledCache getCache()
	{
		return cache;
	}
	public void setCache(AlinousScriptCompiledCache cache)
	{
		this.cache = cache;
	}
	public IncludeUseChecker getIncludeChecker()
	{
		return includeChecker;
	}
	public void setIncludeChecker(IncludeUseChecker includeChecker)
	{
		this.includeChecker = includeChecker;
	}
	public void addFunctionCheckRequest(FunctionCheckRequest request)
	{
		this.functionCheckRequests.add(request);
	}
	public List<FunctionCheckRequest> getFunctionCheckRequests()
	{
		return functionCheckRequests;
	}

	public IExecutable getCurrentExecutable() {
		return currentExecutable;
	}

	public void setCurrentExecutable(IExecutable currentExecutable) {
		this.currentExecutable = currentExecutable;
	}
	
}

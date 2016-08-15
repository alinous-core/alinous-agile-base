package org.alinous.exec.check;

import java.util.HashMap;
import java.util.Map;

import org.alinous.script.AlinousScript;

public class AlinousScriptCompiledCache {
	private Map<String, AlinousScript> scriptsMap = new HashMap<String, AlinousScript>();
	
	public synchronized void addScript(AlinousScript script)
	{
		this.scriptsMap.put(script.getFilePath(), script);
	}
	
	public synchronized AlinousScript getScript(String path)
	{
		return this.scriptsMap.get(path);
	}
	
	public Map<String, AlinousScript> getScriptsMap()
	{
		return this.scriptsMap;
	}
}

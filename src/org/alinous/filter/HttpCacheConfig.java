package org.alinous.filter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HttpCacheConfig {
	private Map<String, HttpCacheSetting> cacheCtrl = new HashMap<String, HttpCacheSetting>();
	
	public String getCacheControl(String path)
	{
		String value = "public";
		String matchPath = "";
		Iterator<String> it = cacheCtrl.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			
			if(key.length() < matchPath.length()){
				continue;
			}
			
			if(path.startsWith(key)){
				value = this.cacheCtrl.get(key).getValue();
			}
		}
		
		return value;
	}
	
	public void addCtrl(String match, String ctrl)
	{
		HttpCacheSetting setting = new HttpCacheSetting();
		setting.setMatch(match);
		setting.setValue(ctrl);
		
		this.cacheCtrl.put(match, setting);
	}
	
	public void clear()
	{
		this.cacheCtrl.clear();
	}
}

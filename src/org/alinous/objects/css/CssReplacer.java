package org.alinous.objects.css;

import java.util.Iterator;
import java.util.Stack;

import org.alinous.AlinousUtils;

public class CssReplacer {
	//private String cssUrl;
	private String replaceBase;
	
	public CssReplacer(String replaceBase)
	{
		// this.cssUrl = cssUrl;
		this.replaceBase = replaceBase;
	}
	
	public String replace(String originalValue)
	{
		String replacedValue =AlinousUtils.getRelationalPath(this.replaceBase, originalValue);
		
		return replacedValue;
	}
	
	public String handleLocaoPath(String path, String currentFile)
	{
		if(currentFile.toLowerCase().startsWith("http://") ||
				currentFile.toLowerCase().startsWith("https://")){
			currentFile = getLocalPath(currentFile);
		}
		String baseDir = getUrlDirectory(currentFile);
		
		String fragments[] = baseDir.split("/");
		Stack<String> pathes = new Stack<String>();
		for(int i = 1; i < fragments.length; i++){
			pathes.push(fragments[i]);
		}
		
		fragments = path.split("/");
		for(int i = 0; i < fragments.length; i++){
			String el = fragments[i];
			if(el.equals(".")){
				continue;
			}
			if(el.equals("..")){
				pathes.pop();
				continue;
			}
			
			pathes.push(el);
		}
		
		StringBuffer buff = new StringBuffer();
		Iterator<String> it = pathes.iterator();
		while(it.hasNext()){
			String el = it.next();
			
			buff.append("/");
			buff.append(el);
		}
		
		return buff.toString();
	}
	
	public String getUrlDirectory(String url)
	{
		int lastidx = url.lastIndexOf("/");
		String sub = url.substring(0, lastidx);
		
		return sub + "/";
	}
	
	public String getLocalPath(String url)
	{
		String domain = getDomain(url);
		String path = url.substring(domain.length());
		
		return path;
	}
	
	public String getDomain(String url)
	{
		String low = url.toLowerCase();
		if(low.startsWith("https://")){
			int indexof = url.indexOf("/", "https://".length());
			String sub = url.substring(0, indexof);
			return sub;
		}
		
		int indexof = url.indexOf("/", "http://".length());
		String sub = url.substring(0, indexof);
		return sub;
	}

}

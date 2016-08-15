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
package org.alinous.web.filter;

import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;

import org.alinous.AlinousCore;
import org.alinous.expections.ExecutionException;


public class WebFilterManager
{
	private AlinousCore core;
	
	public WebFilterManager(AlinousCore core)
	{
		this.core = core;
	}	
	
	public boolean hasOutputFilter(String requestPath)
	{
		if(this.core.getConfig().getFilterConfig() == null){
			return false;
		}
		
		return this.core.getConfig().getFilterConfig().hasOutputFilter(requestPath);
	}
	
	public boolean hasServiceFilter(String requestPath)
	{
		if(this.core.getConfig().getServiceFilterConfig() == null){
			return false;
		}
		
		return true;
	}
	
	public boolean hasInputFilter(String requestPath)
	{
		if(this.core.getConfig().getFilterConfig() == null){
			return false;
		}
		return this.core.getConfig().getFilterConfig().hasInputFilter(requestPath);
	}
	
	public void doOutputFilter(IHttpWrapper wr, String requestPath, StringWriter stringWriter) throws Throwable
	{
		FilterZone zone = this.core.getConfig().getFilterConfig().getZone(requestPath);
		
		Class<?> clazz = core.getJavaConnector().loadClass(zone.getOutFilterClass());
		Class<?> wrapClass = core.getJavaConnector().loadClass("org.alinous.web.filter.HttpWrapperEx");
		
		
		Object filterObj = clazz.newInstance();
		Object wrapObj = wrapClass.newInstance();
		
		// create wrapper class
		Method m = wrapClass.getMethod("setWrapper", new Class<?>[]{Object.class});
		m.invoke(wrapObj, new Object[]{wr});
		
		// call filter
		m = clazz.getMethod("textContentOutput", new Class<?>[]{wrapClass, String.class});
		m.invoke(filterObj, new Object[]{wrapObj, stringWriter.getBuffer().toString()});
		
		//filter.textContentOutput(wr, stringWriter.getBuffer().toString());
	}
	
	public String doInputFilter(IHttpWrapper wr, String requestPath, String inString) throws Throwable
	{
		FilterZone zone = this.core.getConfig().getFilterConfig().getZone(requestPath);
		
		Class<?> clazz = core.getJavaConnector().loadClass(zone.getInFilterClass());
		Class<?> wrapClass = core.getJavaConnector().loadClass("org.alinous.web.filter.HttpWrapperEx");
		
		Object filterObj = clazz.newInstance();
		Object wrapObj = wrapClass.newInstance();
		
		// create wrapper class
		Method m = wrapClass.getMethod("setWrapper", new Class<?>[]{Object.class});
		m.invoke(wrapObj, new Object[]{wr});
		
		// call filter
		m = clazz.getMethod("filter", new Class<?>[]{wrapClass, String.class});
		 return (String)m.invoke(filterObj, new Object[]{wrapObj, inString});
		/*Class<?> clazz = core.getJavaConnector().loadClass(zone.getOutFilterClass());
		IAlinousInputFilter filter = (IAlinousInputFilter) clazz.newInstance();
		
		return filter.filter(wr, inString);*/
		
	}
	
	public String doServiceFilter(IHttpWrapper wr, String requestPath, AlinousCore core) throws Throwable
	{
		if(this.core.getConfig().getServiceFilterConfig() == null){
			return null;
		}
		String filterClass = this.core.getConfig().getServiceFilterConfig().getServiceFilterClass();
		if(filterClass == null || filterClass.equals("")){
			return null;
		}
		
		Class<?> clazz = core.getJavaConnector().loadClass(filterClass);
		Class<?> wrapClass = core.getJavaConnector().loadClass("org.alinous.web.filter.HttpWrapperEx");
		
		Object filterObj = clazz.newInstance();
		Object wrapObj = wrapClass.newInstance();
		
		// create wrapper class
		Method m = wrapClass.getMethod("setWrapper", new Class<?>[]{Object.class});
		m.invoke(wrapObj, new Object[]{wr});
		
		
		// setup Filter
		Map<String, String> params = this.core.getConfig().getServiceFilterConfig().getParams();
		Iterator<String> it = params.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			String value = params.get(key);
			
			callSetter(clazz, filterObj, key, value, String.class);
		}
		
		callSetter(clazz, filterObj, "alinousHome", core.getHome(), String.class);
		callSetter(clazz, filterObj, "alinousCaller", core.getJavaConnectorFunctionCallback(), Object.class);
		
		
		// call filter
		m = clazz.getMethod("filter", new Class<?>[]{wrapClass, String.class});
		String ret = (String)m.invoke(filterObj, new Object[]{wrapObj, requestPath});
		
		if(ret != null){
			return ret;
		}
		
		return null;
	}
	
	public void doEndFilter(IHttpWrapper wr, String requestPath, AlinousCore core, int status) throws ExecutionException
	{
		if(this.core.getConfig().getServiceFilterConfig() == null){
			return;
		}
		
		String filterClass = this.core.getConfig().getServiceFilterConfig().getServiceFilterClass();
		if(filterClass == null || filterClass.equals("")){
			return;
		}
		
		try{
			Class<?> clazz = core.getJavaConnector().loadClass(filterClass);
			Class<?> wrapClass = core.getJavaConnector().loadClass("org.alinous.web.filter.HttpWrapperEx");
			
			Object filterObj = clazz.newInstance();
			Object wrapObj = wrapClass.newInstance();
			
			// create wrapper class
			Method m = wrapClass.getMethod("setWrapper", new Class<?>[]{Object.class});
			m.invoke(wrapObj, new Object[]{wr});
			
			// setup Filter
			Map<String, String> params = this.core.getConfig().getServiceFilterConfig().getParams();
			Iterator<String> it = params.keySet().iterator();
			while(it.hasNext()){
				String key = it.next();
				String value = params.get(key);
				
				callSetter(clazz, filterObj, key, value, String.class);
			}
			
			callSetter(clazz, filterObj, "alinousHome", core.getHome(), String.class);
			callSetter(clazz, filterObj, "alinousCaller", core.getJavaConnectorFunctionCallback(), Object.class);
			
			
			// call filter
			try{
				m = clazz.getMethod("filterFinished", new Class<?>[]{wrapClass, String.class, Integer.class});
			}catch(NoSuchMethodException notFound){
				core.reportError(notFound);
				return;
			}
			
			// AlinousDebug.debugOut(core, "filterFinished before inveoke : " + requestPath + " -> status : " + status);
			
			m.invoke(filterObj, new Object[]{wrapObj, requestPath, new Integer(status)});
		}catch(Throwable e){
			throw new ExecutionException(e, "End filter failed");
		}
		
	}
	
	private void callSetter(Class<?> clazz, Object obj, String property, Object value, Class<?> paramClazz)
			throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		String setterName = getSetterName(property);
		Method m = clazz.getMethod(setterName, new Class<?>[]{paramClazz});
		
		m.invoke(obj, new Object[]{value});
	}
	
	private String getSetterName(String property)
	{
		StringBuffer buff = new StringBuffer();
		
		buff.append("set");
		buff.append(Character.toUpperCase(property.charAt(0)));
		buff.append(property.substring(1));
		
		return buff.toString();
	}
	
	
}

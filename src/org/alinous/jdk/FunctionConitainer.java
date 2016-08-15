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
package org.alinous.jdk;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alinous.AlinousCore;
import org.alinous.AlinousDebug;
import org.alinous.jdk.model.FunctionModel;

public class FunctionConitainer
{
	private Object function;
	
	private Map<String, Method> index = new HashMap<String, Method>();
	private AlinousClassLoader loader;
	
	public FunctionConitainer(Object func, AlinousClassLoader loader, AlinousCore core) throws ClassNotFoundException
	{
		this.function = func;
		this.loader = loader;
		
		// create hash
		for(int i = 0; i < IFunctionUtils.invokeGetFunctions(func).length; i++){
			String funcName = IFunctionUtils.invokeGetFunctions(func)[i];
			
			
			// find method
			AlinousDebug.debugOut(core, "FunctionConitainer() : " + funcName);
			
			Method m = fingClassMethod(funcName);
			if(m != null){
				this.index.put(funcName, m);
			}
		}
		if(core != null){
			IFunctionUtils.invokeFunction(this.function, "setAlinousHome", core.getHome());
			IFunctionUtils.invokeFunction(this.function, "setAlinousCaller", core.getJavaConnectorFunctionCallback(), Object.class);
		}
	}
	
	private Method fingClassMethod(String name) throws ClassNotFoundException
	{
		Class<?> clazz = this.loader.loadClass(this.function.getClass().getName());
		
		//Method methods[] = this.function.getClass().getMethods();
		Method methods[] = clazz.getMethods();
		for(int i = 0; i < methods.length; i++){
			Method m = methods[i];
			
			if(m.getName().equals(name)){
				return m;
			}
		}
		
		return null;
	}
	
	public String getPrefix()
	{
		return IFunctionUtils.invokeGetPrefix(this.function);
	}
	
	public FunctionModel findMethod(String name)
	{
		Method m = this.index.get(name);
		if(m == null){
			return null;
		}
		
		FunctionModel model = new FunctionModel(m, this.function);
		
		return model;
	}
	
	public List<FunctionModel> listFunctionModels()
	{
		List<FunctionModel> list = new ArrayList<FunctionModel>();
		
		Iterator<String> it = this.index.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			Method m = this.index.get(key);
			
			FunctionModel model = new FunctionModel(m, this.function);
			list.add(model);
		}
		
		Collections.sort(list, new Comparator<FunctionModel>() {

			@Override
			public int compare(FunctionModel o1, FunctionModel o2) {
				
				return o1.getName().compareTo(o2.getName());
			}
		});
		
		return list;		
	}
	
	
	public void dispose()
	{
		this.loader = null;
	}
	
}

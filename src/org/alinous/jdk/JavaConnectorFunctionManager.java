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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


import org.alinous.AlinousCore;
import org.alinous.AlinousDebug;
import org.alinous.cloud.AlinousCloudManager;
import org.alinous.cloud.file.AlinousFile;
import org.alinous.expections.ExecutionException;
import org.alinous.jdk.model.FunctionModel;

public class JavaConnectorFunctionManager implements Runnable
{
	public static final String LIB_PATH = "lib";
	
	private String libPath;
	private AlinousClassLoader loader;
	private AlinousCore alinousCore;
	
	// scan variable
	private boolean scanStart;
	private Thread thread;
	
	// IDE class loader count
	private int refcount = 0;
	
	private boolean loadResource;
	private boolean cacheNotConteiner;
	private boolean developemtClassloader;
	
	private Map<String, FunctionConitainer> functions = new HashMap<String, FunctionConitainer>();
	
	public JavaConnectorFunctionManager(String alinousHome, AlinousCore core, boolean loadResource, boolean cacheNotConteiner)
	{
		if(!alinousHome.endsWith(AlinousFile.separator)){
			alinousHome = alinousHome + AlinousFile.separator;
		}
		
		this.libPath = alinousHome + LIB_PATH + AlinousFile.separator;
		this.alinousCore = core;
		this.loadResource = loadResource;
		this.cacheNotConteiner = cacheNotConteiner;
	}
	
	public void init()
		throws InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		ClassLoader classLoader = null;
		
		if(this.developemtClassloader){
			classLoader = null;
		}else{
			classLoader = this.getClass().getClassLoader();
			while(classLoader.getParent() != null){
				classLoader = classLoader.getParent();
			}
		}
		
		this.loader
			= AlinousClassLoader.createClassLoader(classLoader, libPath, this);
	}
	
	public synchronized void update()
		throws InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		if(this.loader.isDirty(this.libPath)){
			this.loader = null;
			
			ClassLoader classLoader = this.getClass().getClassLoader();
			while(classLoader.getParent() != null){
				classLoader = classLoader.getParent();
			}
			
			// Create a new class loader
			this.loader
				= AlinousClassLoader.createClassLoader(classLoader, libPath, this);
		}

	}
	
	public synchronized Class<?> loadClass(String className) throws ClassNotFoundException
	{
		return this.loader.loadClass(className);
	}
	
	public synchronized void registerClassCallback(AlinousCore core, ClassEntry classEntry, AlinousClassLoader defaultloader)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		Class<?> clazz = null;
		
		try{
			clazz = defaultloader.loadClass(classEntry);
		}catch(NoClassDefFoundError e){
			AlinousDebug.debugOut(core, "Ignored " + classEntry.getName());
			if(this.alinousCore != null){
				this.alinousCore.getLogger().reportInfo("Ignored " + classEntry.getName());
			}
			return;
		}catch(UnsupportedClassVersionError e){
			AlinousDebug.debugOut(core, "Unsupported version of the .class file : " + classEntry.getName());
			if(this.alinousCore != null){
				this.alinousCore.getLogger().reportInfo("Unsupported version of the .class file : " + classEntry.getName());
			}
			//e.printStackTrace();
			return;
		}catch(IllegalAccessError e){
			AlinousDebug.debugOut(core, "Ignored(cannot access)  " + classEntry.getName());
			if(this.alinousCore != null){
				this.alinousCore.getLogger().reportInfo("Ignored(cannot access)  " + classEntry.getName());
			}
			//e.printStackTrace();
			return;
		}
		
		Class<?> interfaces[] = clazz.getInterfaces();
		for(int i = 0; i < interfaces.length; i++){
			Class<?> itf = interfaces[i];
			
			if(itf.getName().equals(IFunctionUtils.CLASS_NAME)){
				try{
					Object obj = clazz.newInstance();
					FunctionConitainer container = new FunctionConitainer(obj, defaultloader, this.alinousCore);
					
					this.functions.put(container.getPrefix(), container);
					System.out.println("Detected function Container : " + clazz.getName());
				}
				catch(Throwable e){
					if(this.alinousCore != null){
						this.alinousCore.getLogger().reportError(e);
					}
				}
			}
		}
	}
	
	public FunctionModel findFunction(String name) throws ExecutionException
	{
		String names[] = name.split("\\.");
		
		if(names.length != 2){
			return null;
		}
		
		FunctionConitainer container = this.functions.get(names[0]);
		
		if(container == null){
			throw new ExecutionException("The native function " + name + " does not registered.");//i18n
		}
		
		return container.findMethod(names[1]);
	}
	
	public void startScan()
	{
		this.scanStart = true;
		
		// GAE disable Thread
		AlinousCloudManager mgr = AlinousCloudManager.getInstance();
		
		if(mgr.isCloudThreadEnabled()){
			this.thread = new Thread(this);
			this.thread.start();
		}
		else{
			
		}
		
	}
	
	public void endScan()
	{
		this.scanStart = false;
		this.thread = null;
	}
	
	public void run()
	{
		while(this.scanStart){
			try {
				update();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (Throwable e){
				e.printStackTrace();
			}
			

			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}


		}
		
	}

	public AlinousClassLoader getLoader() {
		return loader;
	}

	public Map<String, FunctionConitainer> getFunctions() {
		return functions;
	}
	
	public synchronized void addRefCount()
	{
		this.refcount++;
	}
	
	public synchronized void decRefCount()
	{
		this.refcount--;
	}
	
	public synchronized int getRefCount()
	{
		return this.refcount;
	}

	public boolean isLoadResource() {
		return loadResource;
	}

	public boolean isCacheNotConteiner() {
		return cacheNotConteiner;
	}

	public void setDevelopemtClassloader(boolean developemtClassloader) {
		this.developemtClassloader = developemtClassloader;
	}
	
	public void dispose()
	{
		Iterator<String> it = this.functions.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			
			FunctionConitainer container = this.functions.get(key);
			container.dispose();
		}
		
		this.functions.clear();		
		this.loader = null;
		
		System.gc();
	}
}

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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.zip.ZipException;

import org.alinous.AlinousDebug;
import org.alinous.AlinousUtils;
import org.alinous.cloud.file.AlinousFile;

public class AlinousClassLoader extends ClassLoader
{
	private List<JarEntry> entries = new ArrayList<JarEntry>();
	private Map<String, Class<?>> loadedClass = new Hashtable<String, Class<?>>();
	
	private JarScanner scnner;
	private JavaConnectorFunctionManager mgr;
	
	public static AlinousClassLoader createClassLoader(ClassLoader parent, String libDir, JavaConnectorFunctionManager mgr)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		AlinousClassLoader loader = new AlinousClassLoader(parent);
		loader.mgr = mgr;
		loader.update(libDir);
		
		return loader;
	}
	
	public boolean isDirty(String libDir)
	{
		this.scnner = new JarScanner(libDir);
		
		List<String> jarFiles = scan();
		Iterator<String> it = jarFiles.iterator();
		while(it.hasNext()){
			String jarFileStr = it.next();
			
			String fileAbsPath = AlinousUtils.getAbsolutePath(libDir, jarFileStr);
			
			AlinousFile file = new AlinousFile(fileAbsPath);
			
			long tm = file.lastModified();
			JarEntry lastEntry = findEntry(jarFileStr);
			
			if(lastEntry == null){
				return true;
			}
			
			long lastTm = lastEntry.getTimestamp();
			
			if(tm > lastTm){
				return true;
			}
		}
		
		// check deleted
		Iterator<JarEntry> jarEntIt = this.entries.iterator();
		while(jarEntIt.hasNext()){
			JarEntry entry = jarEntIt.next();
			
			String jarFileStr = entry.getJarName();
			String fileAbsPath = AlinousUtils.getAbsolutePath(libDir, jarFileStr);
			
			AlinousFile file = new AlinousFile(fileAbsPath);
			if(!file.exists()){
				return true;
			}
		}
		
		return false;		
	}
	
	protected synchronized void update(String libDir)
		throws InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		this.scnner = new JarScanner(libDir);
		
		boolean changed = false;
		
		List<String> jarFiles = scan();
		Iterator<String> it = jarFiles.iterator();
		while(it.hasNext()){
			String jarFileStr = it.next();
			
			String fileAbsPath = AlinousUtils.getAbsolutePath(libDir, jarFileStr);
			
			AlinousFile file = new AlinousFile(fileAbsPath);
			
			long tm = file.lastModified();
			JarEntry lastEntry = findEntry(jarFileStr);
			
			if(lastEntry == null){
				newEntry(libDir, jarFileStr, file);
				changed = true;
				continue;
			}
			
			long lastTm = lastEntry.getTimestamp();
			
			if(tm > lastTm){
				this.entries.remove(lastEntry);
				newEntry(libDir, jarFileStr, file);
				changed = true;
			}
			
		}
		
		// check deleted
		List<JarEntry> removeEntry = new ArrayList<JarEntry>();
		Iterator<JarEntry> jarEntIt = this.entries.iterator();
		while(jarEntIt.hasNext()){
			JarEntry entry = jarEntIt.next();
			
			String jarFileStr = entry.getJarName();
			String fileAbsPath = AlinousUtils.getAbsolutePath(libDir, jarFileStr);
			
			AlinousFile file = new AlinousFile(fileAbsPath);
			if(!file.exists()){
				removeEntry.add(entry);
				changed = true;
			}
		}
		
		jarEntIt = removeEntry.iterator();
		while(jarEntIt.hasNext()){
			JarEntry entry = jarEntIt.next();
			AlinousDebug.print("removed : " + entry.getJarName() + "\n");
			this.entries.remove(entry);
		}
		
		// Register function containers
		jarEntIt = this.entries.iterator();
		while(jarEntIt.hasNext() && changed){
			JarEntry entry = jarEntIt.next();
			
			entry.registerFunctionContainer(null, this);
		}

	}
	
	private void newEntry(String basePath, String jarFileName, AlinousFile file)
				throws InstantiationException, IllegalAccessException
	{
		long tm = file.lastModified();
		
		// debug jar
		//AlinousDebug.debugOut("newEntry : " + jarFileName);
		
		JarEntry entry = new JarEntry(basePath, jarFileName, tm, mgr);
		try {
			entry.init(null, this);
		} catch (ZipException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		this.entries.add(entry);
	}
	
	private JarEntry findEntry(String jarFileName)
	{
		Iterator<JarEntry> it = this.entries.iterator();
		while(it.hasNext()){
			JarEntry entry = it.next();
			
			if(entry.getJarName().equals(jarFileName)){
				return entry;
			}
		}
		
		return null;
	}

	protected AlinousClassLoader(ClassLoader parent)
	{
		super(parent);
	}
	
	public List<String> scan()
	{
		return this.scnner.scanDir();
	}
		
	@Override
	public InputStream getResourceAsStream(String name)
	{
		URL retUrl = getResource(name);
		
		// AlinousDebug
		//AlinousDebug.debugOut("getResourceAsStream name : " + name);
		//AlinousDebug.debugOut("getResourceAsStream retUrl : " + retUrl);
		
		if(retUrl == null){
			return null;
		}
		
		try {
			return retUrl.openStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public Enumeration<URL> getResources(String name) throws IOException
	{
		//  AlinousDebug
		//AlinousDebug.debugOut("getResources name : " + name);
		
		URL retUrl = getResource(name);
		
		Vector<URL> vec = new Vector<URL>();
		
		if(retUrl == null){
			return vec.elements(); 
		}
		
		
		vec.add(retUrl);
		
		return vec.elements();
	}

	@Override
	public URL getResource(String name)
	{
		// AlinousDe
		//AlinousDebug.debugOut("getResource name : " + name);
				
		Iterator<JarEntry> it = this.entries.iterator();
		while(it.hasNext()){
			JarEntry jarEntry = it.next();
			
			ClassEntry entry = jarEntry.findClassEntry(name);
			if(entry != null){
				AlinousURLStreamHandler handler = new AlinousURLStreamHandler();
				handler.setByteData(entry.getBytes());
								
				try {
					URL url = new URL("alinous", "localhost", -1, entry.getName(), handler);
					
					return url;
				} catch (MalformedURLException e) {
					e.fillInStackTrace();
					return null;
				}
				
			}
		}
		
		return super.getResource(name);
	}
	
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException
	{
		Class<?> clazz = doFindClass(name);
		
		if(clazz != null){
			return clazz;
		}
		
		return super.findClass(name);
	}
	
	private Class<?> doFindClass(String name)
	{
		Iterator<JarEntry> it = this.entries.iterator();
		while(it.hasNext()){
			JarEntry jEntry = it.next();
			
			ClassEntry entry = jEntry.findClassEntry(name);
			
			if(entry != null){
				return loadClass(entry);
			}
		}
		
		return null;
	}
	
	public synchronized Class<?> loadClass(ClassEntry entry)
	{
		if(entry.isResource()){
			return null;
		}
		
		Class<?> clazz = this.loadedClass.get(entry.getName());
		if(clazz != null){
			return clazz;
		}
		
		byte b[] = entry.getBytes();
		clazz = this.defineClass(entry.getName(), b, 0, b.length);
		
		this.loadedClass.put(entry.getName(), clazz);
			
		
		return clazz;
	}

	public Class<?> loadClass(String name) throws ClassNotFoundException
	{
		return loadClass(name, true);
	}
	
	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
	{/*
		Class<?> clazz = this.loadedClass.get(name);
		if(clazz != null){
			return clazz;			
		}*/
		
		/*
		clazz = doFindClass(name);
		if(clazz != null){
			return clazz;
		}*/
		
		Class<?> clazz = super.loadClass(name, resolve);
		this.loadedClass.put(name, clazz);
		
		return clazz;
	}

	public List<JarEntry> getEntries() {
		return entries;
	}


}

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipException;

import org.alinous.AlinousCore;


public class JarEntry
{
	private long timestamp;
	private String jarName;
	private String basePath;
	private Map<String, ClassEntry> classEntries;
	
	private JavaConnectorFunctionManager mgr;
	
	public JarEntry(String basePath, String jarName, long tm, JavaConnectorFunctionManager mgr)
	{
		this.basePath = basePath;
		this.jarName = jarName;
		this.timestamp = tm;
		
		this.mgr = mgr;
	}
	
	public void init(AlinousCore core, AlinousClassLoader loader)
			throws ZipException, IOException, InstantiationException, IllegalAccessException
	{
		JarHandler jarHandler = new JarHandler(this.jarName, this.basePath, this.mgr.isLoadResource(), this.mgr.isCacheNotConteiner());
		
		this.classEntries = new HashMap<String, ClassEntry>();
		
		List<ClassEntry> entryList = jarHandler.initEntries(core, loader);
		Iterator<ClassEntry> it = entryList.iterator();
		while(it.hasNext()){
			ClassEntry ent = it.next();
			
			this.classEntries.put(ent.getName(), ent);
						
		}
		
	}
	
	public void registerFunctionContainer(AlinousCore core, AlinousClassLoader loader) throws InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		Iterator<String> it = this.classEntries.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			ClassEntry entry = this.findClassEntry(key);
			
			if(entry.isResource()){
				// 
				//AlinousDebug.debugOut("****** registerFunctionContainer knows " + entry.getName());
				continue;
			}
			
			this.mgr.registerClassCallback(core, entry, loader);
		}
		
	}
	
	
	public ClassEntry findClassEntry(String name)
	{
		//AlinousDebug.debugOut("********* HASH * " + this.classEntries.toString());
		return this.classEntries.get(name);
	}
	
	public long getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp(long timestamp)
	{
		this.timestamp = timestamp;
	}
	
	public String getBasePath()
	{
		return basePath;
	}

	public void setBasePath(String basePath)
	{
		this.basePath = basePath;
	}

	public String getJarName()
	{
		return jarName;
	}

	public void setJarName(String jarName)
	{
		this.jarName = jarName;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof JarEntry){
			return ((JarEntry)obj).getJarName().equals(this.jarName);
		}
		
		return super.equals(obj);
	}

}

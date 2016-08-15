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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.alinous.AlinousCore;
import org.alinous.AlinousDebug;
import org.alinous.cloud.file.AlinousFile;
import org.alinous.cloud.file.AlinousFileInputStream;


public class JarHandler
{
	private String jarName;
	private String basePath;
	private boolean loadResource;
	
	public JarHandler(String jarName, String basePath, boolean loadResource, boolean cacheNotConteiner)
	{
		this.jarName = jarName;
		this.basePath = basePath;
		
		this.loadResource = loadResource;
	}
	
	
	public List<ClassEntry> initEntries(AlinousCore core, AlinousClassLoader loader) throws ZipException, IOException
	{
		List<ClassEntry> list = new ArrayList<ClassEntry>();
		AlinousFile inFile = new AlinousFile(getJarPath());
		
		AlinousFileInputStream in = new AlinousFileInputStream(inFile);
		
		ZipInputStream stream = new ZipInputStream(in);
		
		
		ZipEntry entry = stream.getNextEntry();
		while(entry != null){
			ClassEntry clazz = makeClassEntry(core, entry, stream, loader);
			
			if(clazz != null){
				list.add(clazz);
			}
			
			entry = stream.getNextEntry();
		}
		
		
			/*
		ZipFile zipFile = new ZipFile(inFile);
		
		Enumeration entries = zipFile.entries();
		while(entries.hasMoreElements()){
			ZipEntry entry = (ZipEntry)entries.nextElement();			
			
			ClassEntry clazz = makeClassEntry(entry, zipFile, loader);
			
			if(clazz != null){
				list.add(clazz);
			}
		}
		
		zipFile.close();
		*/
			
		return list;
	}
	
	protected ClassEntry makeClassEntry(AlinousCore core,ZipEntry entry, ZipInputStream stream, AlinousClassLoader loader) throws IOException
	{
		ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
		byte buffer[] = new byte[256];
		int n = 1;
		
		while(n > 0){
			n = stream.read(buffer);
			
			if(n > 0){
				byteArray.write(buffer, 0, n);
			}
		}
		
		byteArray.flush();
		
		ClassEntry clazz = new ClassEntry();
		clazz.setBytes(byteArray.toByteArray());
		
		// load resource
		//AlinousDebug.debugOut("this.loadResource : " + this.loadResource);
		
		if(!entry.getName().endsWith(".class") && !entry.isDirectory()){
			if(this.loadResource){
				String name = entry.getName();
				clazz.setName(name);
				
				// 
				AlinousDebug.debugOut(core, "load resource : " + name);
				
				clazz.setResource(true);
			}else{
				String name = entry.getName();
				AlinousDebug.debugOut(core, "skip loading resource : " + name);
				
				return null;
			}
		}else if(entry.getName().endsWith(".class")){
			String name = entry.getName().replaceAll("/", ".");
			clazz.setName(name.substring(0, name.length() - 6));
			clazz.setResource(false);
			
			// 
			AlinousDebug.debugOut(core, "load class : " + name);
		}
		else{
			return null; //means ignore
		}
		
		byteArray.close();
		stream.closeEntry();
		
		return clazz;
	}
	
	protected ClassEntry makeClassEntry(AlinousCore core, ZipEntry entry, ZipFile zipFile, AlinousClassLoader loader) throws IOException
	{
		// create Java entry
	//	if(!(entry.getName().endsWith(".class") || entry.getName().endsWith(".properties"))){
	//		return null;
	//	}
		
		InputStream inStream = zipFile.getInputStream(entry);
		
		ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
		byte buffer[] = new byte[256];
		int n = 1;
		
		while(n > 0){
			n = inStream.read(buffer);
			
			if(n > 0){
				byteArray.write(buffer, 0, n);
			}
		}
		
		byteArray.flush();
		
		ClassEntry clazz = new ClassEntry();
		clazz.setBytes(byteArray.toByteArray());
		
		// load resource
		AlinousDebug.debugOut(core, "this.loadResource : " + this.loadResource);
		
		if(!entry.getName().endsWith(".class") && !entry.isDirectory()){
			if(this.loadResource){
				String name = entry.getName();
				clazz.setName(name);
				
				// 
				AlinousDebug.debugOut(core, "load resource : " + name);
				
				clazz.setResource(true);
			}else{
				String name = entry.getName();
				AlinousDebug.debugOut(core, "skip loading resource : " + name);
				
				return null;
			}
		}else if(entry.getName().endsWith(".class")){
			String name = entry.getName().replaceAll("/", ".");
			clazz.setName(name.substring(0, name.length() - 6));
			clazz.setResource(false);
			
			// 
			AlinousDebug.debugOut(core, "load class : " + name);
		}
		else{
			return null; //means ignore
		}
		
		byteArray.close();
		
		return clazz;
	}
	
	
	private String getJarPath()
	{
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(this.basePath);
		if(!this.basePath.endsWith("/")){
			buffer.append("/");
		}
		
		buffer.append(this.jarName);
		
		return buffer.toString();
	}
}

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
package org.alinous.repository;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.alinous.AlinousCore;
import org.alinous.AlinousUtils;
import org.alinous.cloud.file.AlinousFile;
import org.alinous.cloud.file.AlinousFileInputStream;
import org.alinous.exec.pages.AlinousExecutableModule;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.CompileError;
import org.alinous.expections.ModuleNotFoundException;
import org.alinous.objects.html.AlinousTopObject;
import org.alinous.parser.object.AlinousObjectParser;
import org.alinous.parser.object.ParseException;
import org.alinous.parser.object.TokenMgrError;
import org.alinous.parser.script.AlinousScriptParser;
import org.alinous.parser.xml.AlinousXmlObjectParser;
import org.alinous.script.AlinousScript;

public class AlinousModuleRepository {
	private Hashtable<String, AlinousModule> alinousObjects = new Hashtable<String, AlinousModule>();
	private List<AlinousModule> alinousObjectsIndex = new CopyOnWriteArrayList<AlinousModule>();
	private AlinousCore alinousCore;
	
	public AlinousModuleRepository(AlinousCore core)
	{
		this.alinousCore = core;
	}
	
	public void registerAlinousModule(PostContext context, String dsPath, String acPath) throws AlinousException
	{
		String moduleName = AlinousUtils.getModuleName(dsPath);
		AlinousModule mod = (AlinousModule)this.alinousObjects.get(moduleName);
		
		// check timestamp of the file
		// check minimum of design
		AlinousFile dsfile = getDesingFile(moduleName);
		
		AlinousFile acfile = new AlinousFile(AlinousUtils.getAbsolutePath(this.alinousCore.getHome(), acPath));
		long timeStamp = dsfile.lastModified() > acfile.lastModified() ? 
							dsfile.lastModified() : acfile.lastModified();
		
		if(timeStamp == 0){
			//AlinousDebug.debugOut("************************* lastMod : " + timeStamp);
			throw new ModuleNotFoundException(AlinousUtils.getModuleName(dsPath));
		}
		// the module is latest, nothing to do
		if(mod != null && timeStamp <= mod.getLastModified()){
			//System.out.println("NOT REG " + mod.getLastModified());
			//System.out.println("TIMESTAMP NEW " + + timeStamp);
			return;
		}
		
		// debug
		//AlinousDebug.debugOut("--------------- register module ----------");
		
		// remove previmous module
		if(mod != null){
			removeObject(mod);	
		}
		
		// Make dsPath from modulename and existing file
		if(dsfile.getPath().endsWith(".html")){
			dsPath = moduleName + ".html";
		}
		else if(dsfile.getPath().endsWith(".rss")){
			dsPath = moduleName + ".rss";
		}
		AlinousTopObject design = parseDesign(context, dsPath);
		
		AlinousScript script = parseScript(acPath);
		
		if(AlinousCore.coverageTest() && script != null){
			context.getCore().initCoverage(script, acfile.lastModified());
		}
		
		
		if(design == null && script == null){
			throw new ModuleNotFoundException(AlinousUtils.getModuleName(dsPath));
		}
		
		mod = new AlinousModule(acPath, design, script, timeStamp);
		
		this.alinousObjects.put(moduleName, mod);
	}
	
	public synchronized void addObject(String moduleName, AlinousModule mod)
	{
		if(this.alinousObjectsIndex.size() > AlinousCore.MAX_CACHE_PAGE){
			AlinousModule m = this.alinousObjectsIndex.get(0);
			
			removeObject(m);
		}
		
		this.alinousObjects.put(moduleName, mod);
		this.alinousObjectsIndex.add(mod);
	}
	
	public synchronized void removeObject(AlinousModule mod){
		this.alinousObjects.remove(mod);
		this.alinousObjectsIndex.remove(mod);
	}
	
	public void clean()
	{
		Iterator<String> it = this.alinousObjects.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			AlinousModule mod = this.alinousObjects.get(key);
			
			long timeStamp = System.currentTimeMillis();
			
			long lastModified = mod.getLastModified();
			
			Timestamp nowTm = new Timestamp(timeStamp);
			Timestamp updateTm = new Timestamp(lastModified);
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(updateTm);
			cal.add(Calendar.MINUTE, 100);
			
			updateTm = new Timestamp(cal.getTimeInMillis());
			
			if(updateTm.before(nowTm)){
				removeObject(mod);
			}
		}
	}
	
	private AlinousFile getDesingFile(String moduleName)
	{
		String fileName = AlinousUtils.getAbsolutePath(this.alinousCore.getHome(), moduleName + ".html");
		AlinousFile file = new AlinousFile(fileName);
		
		if(file.exists()){
			return file;
		}
		
		fileName = AlinousUtils.getAbsolutePath(this.alinousCore.getHome(), moduleName + ".rss");
		file = new AlinousFile(fileName);
		
		if(file.exists()){
			return file;
		}
		
		return file;
	}
	
	/**
	 * 
	 * @param path
	 * @return
	 * @throws AlinousException
	 */
	private AlinousTopObject parseDesign(PostContext context, String path) throws AlinousException
	{
		// The path is ALINOUS_HOME based path
		String fullPath = AlinousUtils.getAbsolutePath(this.alinousCore.getHome(), path);
		
		AlinousFile file = new AlinousFile(fullPath);
		
		if(!file.exists()){
			return null;
		}
		
		InputStream stream;
		try {
			stream = new AlinousFileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new AlinousException(e, "File I/O Error"); // I18N
		}
		
		AlinousTopObject topObj = null;
		try{
			topObj = parseAlinosObject(stream, fullPath);
			
			path = AlinousUtils.getNotOSPath(path);
			topObj.setPath(AlinousUtils.formatAbsolute(path));
		}finally{
			try {
				stream.close();
			} catch (IOException e) {}
		}
		
		if(AlinousCore.optimize == false){
			return (AlinousTopObject) topObj.fork();
		}
		
		// optimize
		try {
			topObj = topObj.optimize(context, this.alinousCore);
		} catch (IOException e) {
			throw new AlinousException(e, "Optimization miss.");
		}
		
		return topObj;
	}
	
	
	private AlinousScript parseScript(String path) throws AlinousException{
		// The path is ALINOUS_HOME based path
		String fullPath = AlinousUtils.getAbsolutePath(this.alinousCore.getHome(), path);
		
		AlinousFile file = new AlinousFile(fullPath);
		
		if(!file.exists()){
			return null;
		}
		
		InputStream stream;
		try {
			stream = new AlinousFileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new AlinousException(e, "File I/O Error"); // I18N
		}
		AlinousScript scriptObj = null;
		
		path = AlinousUtils.getNotOSPath(path);
		scriptObj = parseScriptObject(stream, AlinousUtils.formatAbsolute(path));
		
		return scriptObj;
	}
	
	
	private AlinousScript parseScriptObject(InputStream stream, String filePath) throws AlinousException
	{
		InputStreamReader reader;
		try {
			reader = new InputStreamReader(stream, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new AlinousException(e, "UTF-8 is not supported");
		}
		
		AlinousScriptParser parser = new AlinousScriptParser(reader);
		
		parser.setFilePath(AlinousUtils.getNotOSPath(filePath));
		
		AlinousScript scriptObj;
		try {
			scriptObj = parser.parse();
		}catch (org.alinous.parser.script.ParseException e) {
			throw new CompileError(e, "Compile Error"); // I18N
		}
		
		return scriptObj;
	}
	
	
	private AlinousTopObject parseAlinosObject(InputStream stream, String fullPath) throws AlinousException
	{
		
		InputStreamReader reader;
		try {
			reader = new InputStreamReader(stream, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new AlinousException(e, "UTF-8 is not supported");
		}

		
		
		AlinousTopObject topObj = null;
		
		if(fullPath.endsWith(".html")){
			AlinousObjectParser parser = new AlinousObjectParser(reader);
			try {
				topObj = parser.parse();
			} catch (ParseException e) {
				throw new CompileError(e, "Compile Error"); // I18N

			} catch(TokenMgrError e){
				if(!e.getMessage().endsWith("Encountered: <EOF> after : \"\"")){
					throw new CompileError(e, "Compile Error"); // I18N
				}else{
					topObj = parser.topObj;
				}
			}
		}
		else{
			AlinousXmlObjectParser parser = new AlinousXmlObjectParser(reader);
			
			try {
				topObj = parser.parse();
			} catch (org.alinous.parser.xml.ParseException e) {
				throw new CompileError(e, "Compile Error"); // I18N;
			} catch(org.alinous.parser.xml.TokenMgrError e){
				if(!e.getMessage().endsWith("Encountered: <EOF> after : \"\"")){
					throw new CompileError(e, "Compile Error " + e.getMessage()); // I18N
				}else{
					topObj = parser.topObj;
				}
			}
		}
		
		return topObj;
	}
	
	public AlinousModule getModule(String path) throws AlinousException{
		return (AlinousModule)this.alinousObjects.get(path);		
	}
	
	public AlinousExecutableModule loadExecutableModule(String key) throws AlinousException
	{
		AlinousModule module = (AlinousModule)this.alinousObjects.get(key);
		
		// if not found
		if(module == null){
			return null;
		}
		
		return module.fork();
		
	}

	
	
}

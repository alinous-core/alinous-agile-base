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
package org.alinous.scan;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.alinous.AlinousCore;
import org.alinous.AlinousUtils;
import org.alinous.cloud.file.AlinousFile;
import org.alinous.expections.AlinousException;

public class AlinousHomeScanner {
	private String path;
	private AlinousCore alinousCore;
	
	public AlinousHomeScanner(String folder, AlinousCore core)
	{
		this.path = folder;
		this.alinousCore = core;
	}
	
	public void scan()
	{
		AlinousFile dir = new AlinousFile(this.path);
		List<String> htmlList = new LinkedList<String>();
		List<String> alnsList = new LinkedList<String>();
		
		// gather html
		AlinousObjectFilter objectFilter = new AlinousObjectFilter();
		File[] htmls = dir.listFiles(objectFilter);
		
		for(int i = 0; i < htmls.length; i++){
			String tmp = AlinousUtils.getHomeBasedPath(this.path, htmls[i].getPath());
			htmlList.add(tmp);
		}
		
		// gather alns
		AlinousScriptScanner scriptFilter = new AlinousScriptScanner();
		File[] alnss = dir.listFiles(scriptFilter);
		
		for(int i = 0; i < htmls.length; i++){
			String tmp = AlinousUtils.getHomeBasedPath(this.path, alnss[i].getPath());
			alnsList.add(tmp);
		}
		
		// Do with children folders
		DirectoryFilter dirFilter = new DirectoryFilter();
		File[] childDir = dir.listFiles(dirFilter);
		
		for(int i = 0; i < childDir.length; i++){
			scan(childDir[i], htmlList, alnsList);
		}
		
		// Register Modules
		Iterator<String> it = htmlList.iterator();
		while(it.hasNext()){
			String html = it.next();
			String alns = findReleavantScript(html, alnsList);
			
			if(alns != null){
				alnsList.remove(alns);
			}
			
			try {
				this.alinousCore.registerAlinousObject(null ,html, alns);
			} catch (AlinousException ignore) {
				this.alinousCore.reportError(ignore);
			}
		}
		
		it = alnsList.iterator();
		while(it.hasNext()){
			String alns = it.next();
			
			try {
				this.alinousCore.registerAlinousObject(null ,null, alns);
			} catch (AlinousException ignore) {
				this.alinousCore.reportError(ignore);
			}
		}
		
	}
	
	private String findReleavantScript(String html, List<String> alnsList)
	{
		String modulePath = AlinousUtils.getModuleName(html);
		String targetAlns = modulePath + "." + AlinousCore.EXT_ALNS;
		
		if(alnsList.contains(targetAlns)){
			return targetAlns;
		}
		
		return null;
	}
	
	
	private void scan(File dir, List<String> htmlList, List<String> alnsList)
	{
		// gather html
		AlinousObjectFilter objectFilter = new AlinousObjectFilter();
		File[] htmls = dir.listFiles(objectFilter);
		
		for(int i = 0; i < htmls.length; i++){
			String tmp = AlinousUtils.getHomeBasedPath(this.path, htmls[i].getPath());
			htmlList.add(tmp);
		}
		
		// gather alns
		AlinousScriptScanner scriptFilter = new AlinousScriptScanner();
		File[] alnss = dir.listFiles(scriptFilter);
		
		for(int i = 0; i < htmls.length; i++){
			String tmp = AlinousUtils.getHomeBasedPath(this.path, alnss[i].getPath());
			alnsList.add(tmp);
		}
		
		// Do with children folders
		DirectoryFilter dirFilter = new DirectoryFilter();
		File[] childDir = dir.listFiles(dirFilter);
		
		for(int i = 0; i < childDir.length; i++){
			scan(childDir[i], htmlList, alnsList);
		}
	}
}

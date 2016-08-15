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

import java.util.ArrayList;
import java.util.List;

import org.alinous.cloud.file.AlinousFile;


public class JarScanner
{
	private String dir2Scan;
	
	public JarScanner(String dir2Scan)
	{
		this.dir2Scan = dir2Scan;
	}
	
	public List<String> scanDir()
	{
		List<String> dirList = new ArrayList<String>();
		
		// into absolute dir
		AlinousFile scanDir = new AlinousFile(this.dir2Scan);
		
		doScan(scanDir.getAbsolutePath(), dirList);
		
		return dirList;
	}
	
	private void doScan(String dir, List<String> dirList)
	{
		AlinousFile base = new AlinousFile(dir);
		
		if(!base.exists()){
			return;
		}
		
		String list[] = base.list();
		if(list == null){
			return;
		}
		for(int i = 0; i < list.length; i++){
			String f = list[i];
			
			AlinousFile file = new AlinousFile(base.getAbsolutePath(), f);
			
			if(file.isDirectory()){
				doScan(file.getAbsolutePath(), dirList);
				continue;
			}
			
			handleFile(f, dirList);
		}
	}
	
	private void handleFile(String fileName, List<String> dirList)
	{		
		if(fileName.toLowerCase().endsWith(".jar")){
			
			dirList.add(fileName);
		}
		
	}
}

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

import org.alinous.exec.pages.AlinousExecutableModule;
import org.alinous.expections.AlinousException;
import org.alinous.objects.IAlinousObject;
import org.alinous.objects.html.AlinousTopObject;
import org.alinous.script.AlinousScript;

/**
 * This Object means one Page
 * 
 * @author iizuka
 *
 */
public class AlinousModule
{
	private String path;
	private long lastModified;

	protected AlinousTopObject design;
	protected AlinousScript script;
	
	public AlinousModule(String path, AlinousTopObject ds, AlinousScript sc, long lastModified)
	{
		this.path = path;
		this.design = ds;
		this.script = sc;
		this.lastModified = lastModified;
	}
	
	protected AlinousModule(){}
	
	public AlinousExecutableModule fork() throws AlinousException
	{
		IAlinousObject newDesign = null;
		
		if(this.design != null){
			// without fork
			//newDesign = this.design.fork();
			newDesign = this.design;
		}
		
		AlinousExecutableModule execModule 
			= new AlinousExecutableModule((AlinousTopObject)newDesign, this.script);
		execModule.setLastModified(this.lastModified);
		
		return execModule;
		
	}
	
	public boolean isStatic()
	{
		if(this.script != null){
			return false;
		}
		
		if(this.design != null && !this.design.isDynamic()){
			return true;
		}
		
		return false;
	}
	
	public String getPath()
	{
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
	}


	public AlinousTopObject getDesign()
	{
		return design;
	}


	public void setDesign(AlinousTopObject design)
	{
		this.design = design;
	}

	public AlinousScript getScript()
	{
		return script;
	}


	public void setScript(AlinousScript script)
	{
		this.script = script;
	}


	public long getLastModified()
	{
		return lastModified;
	}


	public void setLastModified(long lastModified)
	{
		this.lastModified = lastModified;
	}	
	
}

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
package org.alinous.objects.html;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import org.alinous.AlinousCore;
import org.alinous.AlinousUtils;
import org.alinous.exec.ExecResultCache;
import org.alinous.exec.FormValueCache;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.objects.IAlinousObject;
import org.alinous.objects.XMLTagBase;
import org.alinous.script.runtime.VariableRepository;


public class AlinousTopObject extends XMLTagBase
{
	protected boolean lastModified;
	protected String path;
	private String sessionId;
	private AlinousCore alinousCore;
	
	//private boolean inner;
	//private InnerModulePath modulePath;
	
	//private AlinousTopObject topTopObject;
	
	public AlinousTopObject()
	{

	}	
	
	public void post(PostContext context, ExecResultCache resCache,
					FormValueCache formValues, VariableRepository valRepo)
	{
		this.alinousCore = context.getCore();
		this.sessionId = context.getSessionId();
		
		postInner(context, resCache, formValues, valRepo);

	}

	public void renderContents(PostContext context, VariableRepository valRepo, Writer wr, int n) throws IOException, AlinousException
	{
		renderInnerContents(context, valRepo, wr, n + 1);
	}

	public boolean isLastModified()
	{
		return lastModified;
	}

	public void setLastModified(boolean lastModified)
	{
		this.lastModified = lastModified;
	}

	public IAlinousObject fork() throws AlinousException
	{
		AlinousTopObject newObject = new AlinousTopObject();
		copyAttribute(this, newObject);
		
		forkInnerObjects(newObject);
		
		newObject.setPath(getPath());
		
		// 
		//InnerModulePath modPath = new InnerModulePath();
		//newObject.setModulePath(modPath);
		
		return newObject;
	}

	public String getPath()
	{
		return this.path;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	public AlinousCore getAlinousCore() {
		return alinousCore;
	}

	public String getSessionId() {
		return sessionId;
	}

	public String toAbsolutePath(String path)
	{
		if(path.startsWith(AlinousUtils.SEPARATOR)){
			return path;
		}
		
		String dir = AlinousUtils.getDirectory(this.path);
		
		return dir + path;
	}
	
	public AlinousTopObject optimize(PostContext context, AlinousCore core) throws IOException, AlinousException
	{
		AlinousTopObject topObj = new AlinousTopObject();
		initOptimize(topObj);
		topObj.setPath(this.path);
		
		this.optimizeSelf(context, topObj, false);
		
		return (AlinousTopObject) topObj.fork();
	}
	
	public void optimizeSelf(PostContext context, XMLTagBase owner, boolean forceDynamic) throws IOException, AlinousException
	{		
		Iterator<XMLTagBase> it =  this.innerObj.iterator();
		while(it.hasNext()){
			XMLTagBase tag = it.next();

			tag.optimizeSelf(context, owner, forceDynamic);
		}
	}

	public void findFormObject(List<FormTagObject> formList)
	{
		Iterator<XMLTagBase> it =  this.innerObj.iterator();
		while(it.hasNext()){
			XMLTagBase tag = it.next();

			tag.findFormObject(formList);
		}
		
	}

}

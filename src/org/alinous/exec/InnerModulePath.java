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
package org.alinous.exec;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.alinous.expections.AlinousException;

public class InnerModulePath
{
	private List<PathElement> pathList = new ArrayList<PathElement>();
	
	/**
	 * "/test.alns:tergetId;/form/index.alns"
	 * @param filePath
	 * @throws AlinousException 
	 */
	public InnerModulePath(String filePath) throws AlinousException
	{
		if(filePath.equals("")){
			return;
		}
		
		String strs[] = filePath.split(";");
		
		for(int i = 0; i < strs.length; i++){
			String str = strs[i];
			
			PathElement p = new PathElement(str);
			pathList.add(p);
		}
	}
	
	public InnerModulePath()
	{
		
	}
	
	public void addPath(String filePath)
	{
		PathElement p = new PathElement();
		p.setFilePath(filePath);
		
		this.pathList.add(p);
	}
	
	public void addTarget(String tagId)
	{
		PathElement p = this.pathList.get(pathList.size() - 1);
		p.setTagId(tagId);
	}
	
	public String getStringPath()
	{
		StringBuffer buffer = new StringBuffer();
		
		boolean first = true;
		Iterator<PathElement> it = this.pathList.iterator();
		while(it.hasNext()){
			PathElement p = it.next();
			
			if(first){
				first = false;
			}
			else{
				buffer.append(";");
			}
			
			buffer.append(p.toString());
		}
		
		return buffer.toString();
	}
	
	public InnerModulePath deepClone() throws AlinousException
	{
		InnerModulePath newObj = new InnerModulePath(getStringPath());
		
		return newObj;
		
	}
	
	class PathElement
	{
		private String filePath;
		private String tagId;
		
		public PathElement(){}
		
		public PathElement(String pathFormat) throws AlinousException
		{
			String strs[] = pathFormat.split(":");
			
			this.filePath = strs[0];
			
			if(strs.length == 2){
				this.tagId = strs[1];
			}
		}
		
		
		public String getFilePath()
		{
			return filePath;
		}
		
		public void setFilePath(String filePath)
		{
			this.filePath = filePath;
		}
		
		public String getTagId()
		{
			return tagId;
		}
		
		public void setTagId(String tagId)
		{
			this.tagId = tagId;
		}
		
		public String toString()
		{
			StringBuffer buff = new StringBuffer();
			
			buff.append(this.filePath);
			
			if(this.tagId != null){
				buff.append(":");
				buff.append(this.tagId);
			}
			
			return buff.toString();
		}
		
	}
}

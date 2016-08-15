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
package org.alinous.security;

import java.util.ArrayList;
import java.util.List;

public class ZoneMatchContext
{
	private List<String> segments = new ArrayList<String>();
	private Zone zone;
	
	public ZoneMatchContext(String path, Zone zone)
	{
		this.zone = zone;
		
		if(!path.endsWith("/")){
			path = path + "/";
		}
		
		String pathes[] = path.split("/");
		for(int i = 0; i < pathes.length; i++){
			String p = pathes[i];
			
			if(p.length() == 0){
				continue;
			}
			
			this.segments.add(p);
		}
	}
	
	public boolean match(String path)
	{
		// make path
		StringBuffer buffer = new StringBuffer();
		String pathes[] = path.split("/");
		for(int i = 0; i < pathes.length - 1; i++){
			if(pathes[i].equals("")){
				continue;
			}
			
			buffer.append("/");
			buffer.append(pathes[i]);
		}
		buffer.append("/");		
		
		ZoneMatchContext inputContext = new ZoneMatchContext(buffer.toString(), null);
		
		if(inputContext.getNumSegments() < getNumSegments()){
			return false;
		}
		
		int max = getNumSegments();
		for(int i = 0; i < max; i++){
			if(!(inputContext.get(i).equals(get(i)))){
				return false;
			}
		}
		
		return true;
	}
	
	public String get(int i)
	{
		return this.segments.get(i);
	}
	
	public int getNumSegments()
	{
		return this.segments.size();
	}

	public Zone getZone()
	{
		return zone;
	}
	
	
}

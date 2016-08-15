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
package org.alinous.web.filter;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FilterConfig
{
	private Map<String, FilterZone> zonesMap = new HashMap<String, FilterZone>();
	
	public FilterConfig()
	{
		
	}
	
	public Iterator<String> getZones()
	{
		return this.zonesMap.keySet().iterator();
	}
	
	public FilterZone getFilterZoneFromArea(String area)
	{
		return this.zonesMap.get(area);
	}
	
	public void addFilterZone(FilterZone zone)
	{
		this.zonesMap.put(zone.getArea(), zone);
	}
	
	public void removeFilterZoneByArea(String area)
	{
		this.zonesMap.remove(area);
	}
	
	public boolean hasOutputFilter(String requestPath)
	{
		FilterZone zone = getZone(requestPath);
		
		if(zone == null){
			return false;
		}
		
		if(zone.getOutFilterClass() == null){
			return false;
		}
		
		return true;
	}
	
	public boolean hasInputFilter(String requestPath)
	{
		FilterZone zone = getZone(requestPath);
		
		if(zone == null){
			return false;
		}
		
		if(zone.getInFilterClass() == null){
			return false;
		}
		
		return true;
	}
	
	public FilterZone getZone(String requestPath)
	{
		FilterZone retZone = null;
		
		Iterator<String> it = this.zonesMap.keySet().iterator();
		while(it.hasNext()){
			String area = it.next();
			FilterZone curZone = this.zonesMap.get(area);
			
			if(requestPath.startsWith(curZone.getArea())){
				if(retZone == null){
					retZone = curZone;
					continue;
				}
				
				if(curZone.getArea().length() > retZone.getArea().length()){
					retZone = curZone;
				}
			}
		}
		
		return retZone;
	}

	public void writeAsString(PrintWriter wr)
	{
		wr.println("	<filter>");
		
		Iterator<String> it = this.zonesMap.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			
			FilterZone zone = this.zonesMap.get(key);
			
			wr.println("		<zone>");
			
			wr.print("			<area>");
			wr.print(zone.getArea());
			wr.println("</area>");
			
			if(zone.getInFilterClass() != null){
				wr.print("			<in>");
				wr.print(zone.getInFilterClass());
				wr.println("</in>");
			}
			
			if(zone.getOutFilterClass() != null){
				wr.print("			<out>");
				wr.print(zone.getOutFilterClass());
				wr.println("</out>");
			}
			
			wr.println("		</zone>");
			
		}
		
		
		wr.println("	</filter>");
	}
	
}

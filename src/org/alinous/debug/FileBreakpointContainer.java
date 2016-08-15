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
package org.alinous.debug;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FileBreakpointContainer {
	private List<ServerBreakpoint> breakpoints = new ArrayList<ServerBreakpoint>();
	
	public void addBreakpoint(ServerBreakpoint breakpoint)
	{
		this.breakpoints.add(breakpoint);
	}
	
	public void removeBreakpoint(int line)
	{
		Iterator<ServerBreakpoint> it = this.breakpoints.iterator();
		while(it.hasNext()){
			ServerBreakpoint breakPoint = it.next();
			if(breakPoint.getLine() == line){
				it.remove();
			}
		}
	}
	
	public Iterator<ServerBreakpoint> iterator()
	{
		return this.breakpoints.iterator();
	}
	
	public void clear()
	{
		this.breakpoints.clear();
	}

	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		
		Iterator<ServerBreakpoint> it = this.breakpoints.iterator();
		while(it.hasNext()){
			ServerBreakpoint p = it.next();
			
			buff.append(p.getFilePath());
			buff.append(":");
			buff.append(p.getLine());
			buff.append("\n");
		}
		
		return buff.toString();
	}
	
	
}

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
package org.alinous.debug.command.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alinous.debug.AlinousDebugManager;
import org.alinous.debug.AlinousServerDebugHttpResponse;
import org.alinous.debug.ServerBreakpoint;
import org.alinous.exec.pages.PostContext;

public class SetupAllBreakPointsRequest implements IClientRequest
{
	private List<ServerBreakpoint> breakpoints = new ArrayList<ServerBreakpoint>();
	
	public void addBreakpoint(ServerBreakpoint breakpoint)
	{
		this.breakpoints.add(breakpoint);
	}
	
	public AlinousServerDebugHttpResponse executeRequest(AlinousDebugManager debugManager
			, PostContext context)
	{
		AlinousServerDebugHttpResponse res = new AlinousServerDebugHttpResponse(0);
		
		debugManager.clearBreakpoints();
		
		Iterator<ServerBreakpoint> it = this.breakpoints.iterator();
		while(it.hasNext()){
			ServerBreakpoint point = it.next();
			
			debugManager.addBreakPoint(point);
		}
		
		//AlinousDebug.debugOut("Breakpoints are setup.");
		
		return res;
	}

	public String getCommand()
	{
		return IClientRequest.CMD_SETUP_ALL_BREAKPOINTS;
	}

	public Map<String, String> getParamMap()
	{
		Map<String, String> m = new HashMap<String, String>();
		int i = 0;
		
		Iterator<ServerBreakpoint> it = this.breakpoints.iterator();
		while(it.hasNext()){
			ServerBreakpoint point = it.next();
			
			String number = Integer.toString(i);
			i++;
			
			m.put(number, point.toString());			
		}
		
		return m;
	}

	public void importParams(Map<String, String> params)
	{
		Iterator<String> it = params.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			
			String source = params.get(key);
			
			if(source.split("=").length != 2){
				continue;
			}
			
			ServerBreakpoint point = new ServerBreakpoint(source);
			this.breakpoints.add(point);		
		}
	}

}

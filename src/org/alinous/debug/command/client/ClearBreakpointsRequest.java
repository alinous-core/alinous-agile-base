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

import java.util.HashMap;
import java.util.Map;

import org.alinous.debug.AlinousDebugManager;
import org.alinous.debug.AlinousServerDebugHttpResponse;
import org.alinous.exec.pages.PostContext;

public class ClearBreakpointsRequest implements IClientRequest
{
	
	public AlinousServerDebugHttpResponse executeRequest(AlinousDebugManager debugManager, PostContext context)
	{
		//AlinousDebug.printClientEventAccepted(this.getClass().getName());
		
		AlinousServerDebugHttpResponse res = new AlinousServerDebugHttpResponse(0);
		
		debugManager.clearBreakpoints();
		
		return res;
	}

	public String getCommand()
	{
		return new String(IClientRequest.CMD_CLEAR_BREAKPOINTS);
	}

	public Map<String, String> getParamMap()
	{
		Map<String, String> m = new HashMap<String, String>();
		return m;
	}

	public void importParams(Map<String, String> params)
	{
	}

}

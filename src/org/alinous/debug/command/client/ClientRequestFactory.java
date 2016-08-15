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

import java.util.Map;

import org.alinous.debug.AlinousDebugCommandSender;


public class ClientRequestFactory {
	
	public static IClientRequest createRequest(Map<String, String> params)
	{
		String command = params.get(AlinousDebugCommandSender.COMMAND);
		IClientRequest retRequest = null;
		
		
		// debug
		//AlinousDebug.debugOut("createRequest() : " + command);
		
		if(command == null){
			return null;
		}
		else if(command.equals(IClientRequest.CMD_TERMINATE)){
			retRequest = new TerminateServerRequest();
		}
		else if(command.equals(IClientRequest.CMD_STATUS_THREAD)){
			retRequest = new StatusThreadRequest();
		}
		else if(command.equals(IClientRequest.CMD_CLEAR_BREAKPOINTS)){
			retRequest = new ClearBreakpointsRequest();
		}
		else if(command.equals(IClientRequest.CMD_ADD_BREAKPOINTS)){
			retRequest = new AddBreakpointsRequest();
		}
		else if(command.equals(IClientRequest.CMD_SETUP_ALL_BREAKPOINTS)){
			retRequest = new SetupAllBreakPointsRequest();
		}
		else if(command.equals(IClientRequest.CMD_RESUME)){
			retRequest = new ResumeRequest(0);
		}
		else if(command.equals(IClientRequest.CMD_STEP_OVER)){
			retRequest = new StepOverRequest();
		}
		else if(command.equals(IClientRequest.CMD_STEP_IN)){
			retRequest = new StepInRequest();
		}
		else if(command.equals(IClientRequest.CMD_STEP_RETURN)){
			retRequest = new StepReturnRequest();
		}	
		
		if(retRequest != null){
			retRequest.importParams(params);
		}
		
		return retRequest;
	}
	
	public static IClientRequest createDefault()
	{
		return new StatusThreadRequest();
	}

}

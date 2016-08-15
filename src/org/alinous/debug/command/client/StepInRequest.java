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
import org.alinous.debug.breakstatus.StepInOperation;
import org.alinous.exec.pages.PostContext;

public class StepInRequest implements IClientRequest
{
	public static final String THREAD_ID = "THREAD_ID";
	public static final String STACK_ID = "STACK_ID";
	
	private long threadId;
	private long stackId;
	
	public AlinousServerDebugHttpResponse executeRequest(AlinousDebugManager debugManager
			, PostContext context)
	{
		//AlinousDebug.printClientEventAccepted(this.getClass().getName());

		StepInOperation ope = new StepInOperation();
		debugManager.setOperation(this.threadId, ope);
		debugManager.resume(this.threadId, context);
		
		AlinousServerDebugHttpResponse responce = new AlinousServerDebugHttpResponse(0);
		return responce;
	}

	public String getCommand()
	{
		return IClientRequest.CMD_STEP_IN;
	}

	public Map<String, String> getParamMap()
	{
		Map<String, String> m = new HashMap<String, String>();
		m.put(THREAD_ID, Long.toString(this.threadId));
		m.put(STACK_ID, Long.toString(this.stackId));
		
		return m;
	}

	public void importParams(Map<String, String> params)
	{
		String strThreadId = params.get(THREAD_ID);
		if(strThreadId != null){
			this.threadId = Long.parseLong(strThreadId);
		}
		
		String strStackId = params.get(STACK_ID);
		if(strStackId != null){
			this.stackId = Long.parseLong(strStackId);
		}
	}

	public long getStackId()
	{
		return stackId;
	}

	public void setStackId(long stackId)
	{
		this.stackId = stackId;
	}

	public long getThreadId()
	{
		return threadId;
	}

	public void setThreadId(long threadId)
	{
		this.threadId = threadId;
	}

}

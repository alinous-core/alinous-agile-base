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

import org.alinous.AlinousCore;
import org.alinous.debug.breakstatus.DefaultOperation;
import org.alinous.debug.breakstatus.IDebuggerOperation;
import org.alinous.debug.command.client.IClientRequest;
import org.alinous.debug.command.server.NotifyThreadEndedCommand;
import org.alinous.debug.command.server.NotifyThreadStartCommand;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;



public class AlinousDebugManager extends DebugEventSupport
{
	public AlinousDebugManager(AlinousCore alinousCore)
	{
		super(alinousCore);

	}
	
	public void startAlinousOperation(PostContext context)
	{
		if(!AlinousCore.debug(context)){
			return;
		}
		if(this.alinousCore == null){
			return;
		}
		
		Thread th = Thread.currentThread();
		
		DebugThread newThread = new DebugThread(th, this, this);
		Long thId = new Long(th.getId());
		
		// 
		//AlinousDebug.debugOut("DDDDDDDDDD : startAlinousOperation() : " + thId);
		
		synchronized (this.threads) {
			this.threads.put(thId, newThread);
		}
		
		
		try {
			this.debugEventNotifier.notifyToClient(new NotifyThreadStartCommand(Thread.currentThread()), context);
		} catch (AlinousException e) {
			this.alinousCore.reportError(e);
		}
		
		//
		//AlinousDebug.debugOut("startAlinousOperation() Thread started: " + thId);
	}
	
	public void endAlinousOperation(PostContext context)
	{
		if(!AlinousCore.debug(context)){
			return;
		}
		if(this.alinousCore == null){
			return;
		}
		
		Thread th = Thread.currentThread();
		Long thId = new Long(th.getId());
		
		synchronized (threads) {
			this.threads.remove(thId);
		}
		
		
		try {
			this.debugEventNotifier.notifyToClient(new NotifyThreadEndedCommand(Thread.currentThread()), context);
		} catch (AlinousException e) {
			this.alinousCore.reportError(e);
		}
		
		//
		//AlinousDebug.debugOut("endAlinousOperation() Thread ended: " + thId);
	}
	
	public void clearBreakpoints()
	{
		if(this.alinousCore == null){
			return;
		}
		
		breakPointContainers.clear();
	}
	
	public void clearBreakpoints(String filePath)
	{
		// debug
		// AlinousDebug.debugOut("ClearBreakPoints: " + this.breakPointContainers);
		// AlinousDebug.debugOut("ClearBreakPoints filePath: " + filePath);
		
		FileBreakpointContainer container = this.breakPointContainers.get(filePath);
		if(container == null){
			return;
		}
		
		container.clear();
	}
	
	public void removeBreakpoint(String filePath, int line)
	{
		FileBreakpointContainer container = this.breakPointContainers.get(filePath);
		if(container == null){
			return;
		}
		
		container.removeBreakpoint(line);
	}
	
	public FileBreakpointContainer getFileBreakpointContainer(String filePath)
	{
		FileBreakpointContainer container = this.breakPointContainers.get(filePath);
		
		if(container == null){
			container = new FileBreakpointContainer();
			this.breakPointContainers.put(filePath, container);
		}
		
		return container;
	}

	public void addBreakPoint(ServerBreakpoint breakpoint)
	{
		getFileBreakpointContainer(breakpoint.getFilePath()).addBreakpoint(breakpoint);
	}
	
	public void setBreakpoints(ServerBreakpoint[] breakpoints)
	{
		if(this.alinousCore == null){
			return;
		}
		
		breakPointContainers.clear();
		
		for(int i = 0; i < breakpoints.length; i++){
			String filePath = breakpoints[i].getFilePath();
			
			FileBreakpointContainer container = breakPointContainers.get(filePath);
			if(container == null){
				container = new FileBreakpointContainer();
				this.breakPointContainers.put(filePath, container);
			}
			
			container.addBreakpoint(breakpoints[i]);
		}
	}

	public AlinousServerDebugHttpResponse handleClientRequest(IClientRequest request, PostContext context)
	{
		return request.executeRequest(this, context);
	}
	
	public void resume(long threadId, PostContext context)
	{
		if(this.alinousCore == null){
			return;
		}
		
		DebugThread thread = getThread(threadId);
		
		thread.resume(context);
	}
	
	public void setOperation(long threadId, IDebuggerOperation ope)
	{
		if(this.alinousCore == null){
			return;
		}
		
		DebugThread thread = getThread(threadId);
		
		if(thread == null){
			return;
		}
		
		thread.setOperation(ope, this);
	}
	
	public void initOperation()
	{
		if(this.alinousCore == null){
			return;
		}
		
		long threadId = Thread.currentThread().getId();
		
		setOperation(threadId, new DefaultOperation());
		
	}
	
	public DebugThread getCurrentThread()
	{
		long threadId = Thread.currentThread().getId();
		
		synchronized (this.threads) {
			return this.threads.get(new Long(threadId));
		}
		
	}

}

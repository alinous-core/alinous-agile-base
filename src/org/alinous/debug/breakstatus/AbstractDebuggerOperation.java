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
package org.alinous.debug.breakstatus;

import java.util.Iterator;

import org.alinous.debug.AlinousDebugEvent;
import org.alinous.debug.AlinousDebugManager;
import org.alinous.debug.DebugThread;
import org.alinous.debug.FileBreakpointContainer;
import org.alinous.debug.ServerBreakpoint;

public abstract class AbstractDebuggerOperation implements IDebuggerOperation{
	protected DebugThread thread;
	protected AlinousDebugManager debugManager;
	
	public void init(DebugThread thread, AlinousDebugManager manager)
	{
		this.thread = thread;
		this.debugManager = manager;
	}
	
	
	protected long hitBreakpoint(AlinousDebugEvent event)
	{
		// debug
		// AlinousDebug.debugOut("hitBreakpoint(AlinousDebugEvent event) : " + this);
		// AlinousDebug.debugOut(event.getFilePath());
		
		FileBreakpointContainer container 
			= this.debugManager.getFileBreakpointContainer(event.getFilePath());
		
		Iterator<ServerBreakpoint> it = container.iterator();
		while(it.hasNext()){
			ServerBreakpoint breakpoint = it.next();
			if(breakpoint.getLine() == event.getLine()){
				 
				// AlinousDebug.printBreakpointHit(event.getFilePath(), event.getLine());
				
				return event.getThread().getThreadId();
			}
		}
		
		
		// AlinousDebug.debugOut("hitBreakpoint(AlinousDebugEvent event) ignored : " + this);
		
		return -1;
	}
	
}

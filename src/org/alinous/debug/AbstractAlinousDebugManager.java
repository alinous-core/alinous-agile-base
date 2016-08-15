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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.alinous.AlinousCore;
import org.alinous.exec.pages.PostContext;
import org.alinous.script.IScriptBlock;
import org.alinous.script.runtime.VariableRepository;

public class AbstractAlinousDebugManager {
	protected Map<Long, DebugThread> threads = new HashMap<Long, DebugThread>();
	protected Map<String, FileBreakpointContainer> breakPointContainers = new HashMap<String, FileBreakpointContainer>();

	protected AlinousDebugEventNotifier debugEventNotifier;
	protected AlinousCore alinousCore;
	
	protected long hotThread;
	
	public AbstractAlinousDebugManager(AlinousCore alinousCore)
	{
		this.debugEventNotifier = new AlinousDebugEventNotifier(this);
		this.alinousCore = alinousCore;
		
		this.hotThread = -1;
	}
	
	public DebugThread[] getThreads()
	{
		ArrayList<DebugThread> list = new ArrayList<DebugThread>();
		
		synchronized (this.threads) {
			Iterator<Long> it = this.threads.keySet().iterator();
			while(it.hasNext()){
				Long thid = it.next();
				DebugThread th = this.threads.get(thid);
				
				list.add(th);
			}
		}

		
		return list.toArray(new DebugThread[list.size()]);
	}
	
	protected DebugThread getThread(long threadId)
	{
		synchronized (this.threads) {
			return this.threads.get(new Long(threadId));
		}
	}
	
	protected DebugThread getThread()
	{
		Thread th = Thread.currentThread();
		Long thId = new Long(th.getId());
		
		synchronized (this.threads) {
			return this.threads.get(thId);
		}
	}
	
	
	protected void createStackFrame(IScriptBlock scriptBlock, VariableRepository repo, PostContext context) throws ThreadTerminatedException
	{
		// debug
		// AlinousDebug.debugOut("CCCCCCcreateStackFrame()" + Thread.currentThread().getId());
		
		DebugThread currentThread = getThread();
		if(currentThread != null){
			currentThread.newStackFrame(scriptBlock, repo, context);
		}
		else{
			// debug
			// AlinousDebug.debugOut("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX createStackFrame() failed " + Thread.currentThread().getId());
		}
	}
	
	public void destoryCurrentStackFrame()
	{
		// debug
		// AlinousDebug.debugOut("DDDDDDdestoryCurrentStackFrame()" + Thread.currentThread().getId());
		
		DebugThread currentThread = getThread();
		if(currentThread != null){
			currentThread.destroyStackFrame();
		}else{
			// debug
			// AlinousDebug.debugOut("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX createStackFrame() failed " + Thread.currentThread().getId());
		}
	}
	
	public void setDebugPort(int port)
	{
		this.debugEventNotifier.setPort(port);
	}

	public long getHotThread() {
		return hotThread;
	}

	public void setHotThread(long hotThread) {
		this.hotThread = hotThread;
	}

	public AlinousDebugEventNotifier getDebugEventNotifier() {
		return debugEventNotifier;
	}


	
}

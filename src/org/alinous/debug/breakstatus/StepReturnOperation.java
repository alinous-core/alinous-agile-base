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

import org.alinous.debug.AlinousDebugEvent;
import org.alinous.debug.DebugStackFrame;
import org.alinous.debug.IThreadEventListner;
import org.alinous.debug.ThreadTerminatedException;
import org.alinous.exec.IExecutable;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;

public class StepReturnOperation extends AbstractDebuggerOperation
{
	private long currentStackId;
	
	public StepReturnOperation(long currentStackId)
	{
		this.currentStackId = currentStackId;
	}
	
	public void handleEvent(AlinousDebugEvent event, PostContext context) throws ThreadTerminatedException
	{
		switch(event.getEventType()){
		case AlinousDebugEvent.AFTER_CALLED_ARGUMENT:
			return;
		default:
			break;
		}
		
		this.thread.setNextExec(event.getSentence());
		
		long hitBreakPointId = hitBreakpoint(event);
		
		if(hitBreakPointId > 0){
			try {
				IExecutable exec = event.getSentence();
				
				this.debugManager.setHotThread(hitBreakPointId);
				this.thread.suspend(IThreadEventListner.REASON_BREAKPOINT, exec.getLine(), context);
				
				//AlinousDebug.debugOut("Thread {" + this.thread.getThreadId() + " resumed.");				
			} catch (InterruptedException e) {
				AlinousException ex = new AlinousException(e, "Could not suspend thread");
				this.debugManager.getAlinousCore().reportError(ex);
			}
			return;
		}
		
		//  
		// AlinousDebug.debugOut("STEP RETURN!!!!!!!!!!!!!!!");
		
		// stack check
		boolean returned = returned(event);
		
		//  
		// AlinousDebug.debugOut("STEP RETURN! --> " + returned);
		
		if(returned){
			suspendByStepReturn(event, context);
		}
		
	}
	
	private void suspendByStepReturn(AlinousDebugEvent event, PostContext context) throws ThreadTerminatedException
	{
		try {
			IExecutable exec = event.getSentence();
			
			this.debugManager.setHotThread(event.getThread().getThreadId());
			this.thread.suspend(IThreadEventListner.REASON_STEP_RETURN, exec.getLine(), context);
			
			//AlinousDebug.debugOut("Thread {" + this.thread.getThreadId() + " resumed.");				
		} catch (InterruptedException e) {
			AlinousException ex = new AlinousException(e, "Could not suspend thread");
			this.debugManager.getAlinousCore().reportError(ex);
		}
	}
	
	private boolean returned(AlinousDebugEvent event)
	{
		DebugStackFrame frame = event.getThread().getTopStackFrame();
		
		if(frame.getStackId() == this.currentStackId){
			return true;
		}
		return false;
	}

}

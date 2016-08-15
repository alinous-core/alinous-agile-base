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

public class StepInOperation extends AbstractDebuggerOperation
{

	public void handleEvent(AlinousDebugEvent event, PostContext context) throws ThreadTerminatedException
	{
		switch(event.getEventType()){
		case AlinousDebugEvent.AFTER_CALLED_ARGUMENT:
			try {
					handleAfterFunctionCallArgument(event, context);
				} catch (InterruptedException e) {
					AlinousException ex = new AlinousException(e, "Could not suspend thread");
					this.debugManager.getAlinousCore().reportError(ex);
				}
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
		
		// Suspend Normally
		suspendNormally(event, context);
		
	}
	
	private void handleAfterFunctionCallArgument(AlinousDebugEvent event, PostContext context) throws InterruptedException, ThreadTerminatedException
	{
		DebugStackFrame stack = event.getThread().getTopStackFrame();
		
		if(stack.canStepIn()){
			IExecutable exec = event.getSentence();
			
			this.debugManager.setHotThread(event.getThread().getThreadId());
			this.thread.suspend(IThreadEventListner.REASON_BREAKPOINT, exec.getLine(), context);
			
			//AlinousDebug.debugOut("Thread {" + this.thread.getThreadId() + " resumed.");
		}
		
	}
	
	private void suspendNormally(AlinousDebugEvent event, PostContext context) throws ThreadTerminatedException
	{
		IExecutable exec = event.getSentence();
		
		try {
			this.thread.suspend(IThreadEventListner.REASON_BREAKPOINT, exec.getLine(), context);
			this.debugManager.setHotThread(event.getThread().getThreadId());
			
			//AlinousDebug.debugOut("Thread {" + this.thread.getThreadId() + " resumed.");
		} catch (InterruptedException e) {
			AlinousException ex = new AlinousException(e, "Could not suspend thread");
			this.debugManager.getAlinousCore().reportError(ex);
		}
	}

}

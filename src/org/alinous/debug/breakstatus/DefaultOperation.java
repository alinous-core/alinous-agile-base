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
import org.alinous.debug.IThreadEventListner;
import org.alinous.debug.ThreadTerminatedException;
import org.alinous.exec.IExecutable;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;


public class DefaultOperation extends AbstractDebuggerOperation{

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
		
		if(hitBreakPointId > 0 && Thread.currentThread().getId() == hitBreakPointId){
			// debug
			/*StatusThreadRequest reqCmd = new StatusThreadRequest();
			AlinousServerDebugHttpResponse debugRes = this.debugManager.handleClientRequest(reqCmd);
			try {
				String strXml = debugRes.exportAsXml();
				AlinousDebug.debugOut("----------------BEFORE ----------------" + event.getThread().getThreadId());
				AlinousDebug.debugOut(strXml);
			} catch (IOException e1) {
				// Auto-generated catch block
				e1.printStackTrace();
			}*/
			
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
		
	}
	


}

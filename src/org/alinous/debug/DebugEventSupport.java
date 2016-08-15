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
import org.alinous.debug.breakstatus.IDebuggerOperation;
import org.alinous.debug.command.server.IServerCommand;
import org.alinous.debug.command.server.NotifyHitBreakpoint;
import org.alinous.debug.command.server.NotifyResumeComand;
import org.alinous.debug.command.server.NotifyStepOverFinishedCommand;
import org.alinous.debug.command.server.NotifyStepInFinishedCommand;
import org.alinous.debug.command.server.NotifyStepReturnFinishedCommand;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.script.IScriptBlock;
import org.alinous.script.IScriptSentence;
import org.alinous.script.runtime.VariableRepository;

public abstract class DebugEventSupport extends AbstractAlinousDebugManager
		implements IThreadEventListner
{
	
	public DebugEventSupport(AlinousCore alinousCore)
	{
		super(alinousCore);
		
	}

	protected void fireEvent(AlinousDebugEvent event, PostContext context) throws ThreadTerminatedException
	{
		switch(event.getEventType()){
		case AlinousDebugEvent.BEFORE_SENTENCE:
			handleBeforeSentence(event, context);
			break;
		case AlinousDebugEvent.BEFORE_CREATE_STACKFRAME:
		case AlinousDebugEvent.BEFORE_STATEMENT:
			break;
		default:
			break;
		}
	}
	
	public void sendCommand2Client(IServerCommand command, PostContext context)
	{
		try {
			if(!AlinousCore.debug(context)){
				return;
			}
			this.debugEventNotifier.notifyToClient(command, context);
		} catch (AlinousException e) {
			this.alinousCore.reportError(e);
		}
	}
	
	public void aboutToExecuteSentence(IScriptBlock block, IScriptSentence nextSentence, PostContext context) throws ThreadTerminatedException
	{
		AlinousDebugEvent event 
			= new AlinousDebugEvent(AlinousDebugEvent.BEFORE_SENTENCE, nextSentence.getLine(), block.getFilePath(),
					getThread());
		event.setSentence(nextSentence);
		
		fireEvent(event, context);
		
	}
	
	public void afterExecutedFunctionArgumentStatement(IScriptSentence callerSentenc, PostContext context) throws ThreadTerminatedException
	{
		AlinousDebugEvent event 
		= new AlinousDebugEvent(AlinousDebugEvent.AFTER_CALLED_ARGUMENT,
							callerSentenc.getLine(),
							callerSentenc.getFilePath(),
							getThread());
		event.setSentence(callerSentenc);
		
		fireEvent(event, context);
	}
	
	public void handleBeforeSentence(AlinousDebugEvent event, PostContext context) throws ThreadTerminatedException
	{
		DebugThread th =getThread();
		if(th == null){
			return;
		}
		IDebuggerOperation ope = getThread().getOperation();
		
		if(ope != null){
			ope.handleEvent(event, context);
		}
	}
	
	public void createStackFrame(IScriptBlock scriptBlock, VariableRepository repo, PostContext context) throws ThreadTerminatedException
	{
		super.createStackFrame(scriptBlock, repo, context);
		
		AlinousDebugEvent event 
		= new AlinousDebugEvent(AlinousDebugEvent.BEFORE_CREATE_STACKFRAME, scriptBlock.getLine(), scriptBlock.getFilePath(),
				getThread());
		
		fireEvent(event, context);
	}
	
	public AlinousCore getAlinousCore() {
		return alinousCore;
	}

	public void fireThreadAboutToSuspend(int reason, PostContext context) {
		if(!AlinousCore.debug(context)){
			return;
		}
		
		IServerCommand cmd = null;
		
		switch(reason){
		case IThreadEventListner.REASON_BREAKPOINT:
			cmd = new NotifyHitBreakpoint(Thread.currentThread());
			break;
		case IThreadEventListner.REASON_STEP_IN:
			cmd = new NotifyStepInFinishedCommand(Thread.currentThread());
			break;
		case IThreadEventListner.REASON_STEP_OVER:
			cmd = new NotifyStepOverFinishedCommand(Thread.currentThread());
			break;
		case IThreadEventListner.REASON_STEP_RETURN:
			cmd = new NotifyStepReturnFinishedCommand(Thread.currentThread());
			break;
		default:
			break;
		}
		
		try {
			if(cmd != null){

				this.debugEventNotifier.notifyToClient(cmd, context);
			}
		} catch (AlinousException e) {
			getAlinousCore().reportError(e);
		}
		
	}

	public void fireThreadResumed(PostContext context)
	{
		try {
			if(!AlinousCore.debug(context)){
				return;
			}
			this.debugEventNotifier.notifyToClient(new NotifyResumeComand(Thread.currentThread()), context);
		} catch (AlinousException e) {
			getAlinousCore().reportError(e);
		}
	}
	
}

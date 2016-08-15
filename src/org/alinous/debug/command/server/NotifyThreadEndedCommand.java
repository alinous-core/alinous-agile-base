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
package org.alinous.debug.command.server;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import org.alinous.exec.pages.PostContext;

public class NotifyThreadEndedCommand extends AbstractAlinousServerCommand {
	public static final String CMD_STRING = "THREAD_ENDED";

	
	public NotifyThreadEndedCommand(Thread thread)
	{
		super(thread);
	}
	
	@Override
	public void sendCommand(List<IServerCommand> liservedqueue, List<IServerCommand> needfinish,
			ICommandSender notifier, PostContext context) throws IOException
	{
		long threadId = getThread().getId();
		
		notifier.debugOut("# Destroyer currentThread : " + threadId);
		// necesarry sent
		if(containsThread(threadId, needfinish, true)){
			notifier.debugOut("### Resend NotifyThreadEndedCommand " + this.getThread().getId());
			notifier.sendCommand(this, context);
		}
		
		// Below here is cleanup process
		
		//LinkedList<IServerCommand> list = new LinkedList<>();
		synchronized (liservedqueue) {
			Iterator<IServerCommand> it = liservedqueue.iterator();
			while(it.hasNext()){
				IServerCommand cmd = it.next();
				
				// destroy queueed cmd for this thread
				notifier.debugOut("# Destroyed Target : " + cmd.getThread().getId());
				if(cmd.getThread().getId() == threadId){
					it.remove();
										
					notifier.debugOut("# Destroyed NotifyStartCommand " + cmd);
					continue;
				}
				
				// destroy queueed cmd for other terminated thread
				if(cmd.getThread().getState() == Thread.State.TERMINATED){
					it.remove();
					
					IServerCommand endCmd = new NotifyThreadEndedCommand(cmd.getThread());
					notifier.sendCommand(endCmd, context);
					
					continue;
				}
			}
		}
		

		checkTerminate(needfinish, notifier, context);
	}
	
	protected void checkTerminate(List<IServerCommand> needfinish,
			ICommandSender notifier, PostContext context) throws IOException
	{
		// terminate required threads
		Iterator<IServerCommand> it = needfinish.iterator();
		while(it.hasNext()){
			IServerCommand cmd = it.next();
			
			if(cmd.getThread().getState() == Thread.State.TERMINATED){
				it.remove();
					
				IServerCommand endCmd = new NotifyThreadEndedCommand(cmd.getThread());
				notifier.sendCommand(endCmd, context);
			}
		}
	}

	
	@Override
	public void writeCommand(Writer writer) throws IOException {
		writer.write(CMD_STRING);
		writer.flush();
	}

	@Override
	public String getName() {
		return CMD_STRING;
	}
}

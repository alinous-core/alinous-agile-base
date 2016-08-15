package org.alinous.debug.command.server;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.alinous.exec.pages.PostContext;

// debug command sender
public abstract class AbstractAlinousServerCommand implements IServerCommand {
	private Thread thread;
	
	public AbstractAlinousServerCommand(Thread thread)
	{
		this.thread = thread;
	}
	
	@Override
	public long getThreadId()
	{
		return this.thread.getId();
	}
	
	@Override
	public void sendCommand(List<IServerCommand> liservedqueue, List<IServerCommand> needfinish,
			ICommandSender notifier, PostContext context) throws IOException {
		sendReservedCommands(liservedqueue, needfinish, notifier, context);
		
		notifier.debugOut("# Server Command : " + this);
		notifier.sendCommand(this, context);
	}
	
	/**
	 * 
	 * @param liservedqueue
	 * @param needfinish
	 * @param notifier
	 * @throws IOException
	 */
	protected void sendReservedCommands(List<IServerCommand> liservedqueue, List<IServerCommand> needfinish, ICommandSender notifier
			,PostContext context) throws IOException
	{
		long threadId = getThread().getId();
	
		synchronized (liservedqueue) {
			Iterator<IServerCommand> it = liservedqueue.iterator();
			while(it.hasNext()){
				IServerCommand cmd = it.next();
				
				// resend
				notifier.debugOut("# Resend Target : " + cmd.getThread().getId());
				if(cmd.getThread().getId() == threadId){
					it.remove();
					
					//if(cmd instanceof NotifyThreadStartCommand){
					//	notifier.debugOut("### Register Terminate send Target : " + cmd.getThread().getId());
					//	needfinish.add(cmd);
					//}
					if(!containsThread(cmd.getThread().getId(), needfinish, false)){
						notifier.debugOut("### Register Terminate send Target : " + cmd.getThread().getId());
						needfinish.add(cmd);
					}
					
					
					notifier.sendCommand(cmd, context);
					
					continue;
				}
				
				if(cmd.getThread().getState() == Thread.State.TERMINATED){
					it.remove();
					
					notifier.debugOut("# Removed terminated " + cmd);
					continue;
				}
			}
		}
	}
	
	protected boolean containsThread(long threadId, List<IServerCommand> needfinish, boolean remove)
	{
		boolean retvalue = false;
		
		Iterator<IServerCommand> it = needfinish.iterator();
		while(it.hasNext()){
			IServerCommand cmd = it.next();
			if(cmd.getThread().getId() == threadId){
				if(remove){
					it.remove();
				}
				retvalue = true;
			}
		}
		
		return retvalue;
	}

	public Thread getThread() {
		return thread;
	}
	
}

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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import org.alinous.AlinousCore;
import org.alinous.debug.command.server.ICommandSender;
import org.alinous.debug.command.server.IServerCommand;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;

public class AlinousDebugEventNotifier implements Runnable, ICommandSender{
	// true -> false
	public static final boolean debugDebugger = false;
	private int port;
	
	private List<IServerCommand> commandqueue = new LinkedList<IServerCommand>();
	private boolean loop;
	
	private List<IServerCommand> reservedqueue = new LinkedList<IServerCommand>();
	private List<IServerCommand> needfinish = new LinkedList<IServerCommand>();
	
	private AbstractAlinousDebugManager debugManager;
	
	public AlinousDebugEventNotifier(AbstractAlinousDebugManager debugManager)
	{
		this.port = -1;
		this.debugManager = debugManager;
		
		debugOut("Start launching AlinousDebugEventNotifier ");
		
		this.loop = true;
		
		Thread th = new Thread(null, this, "Alinous Debug Notifier");
		th.start();
		
		while(th.getState() != Thread.State.WAITING){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		debugOut("Started AlinousDebugEventNotifier ");
	}
	
	public void notifyToClient(IServerCommand command, PostContext context) throws AlinousException
	{
		if(!AlinousCore.debug(context)){			
			return;
		}
		
		// if context contains debug mode, chnage it
		if(context != null && context.isDebugMode()){
			context.getDebugCallbackHandler().runCallback(command, context);
			return;
		}
		
		
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
		synchronized (this.commandqueue) {
			this.commandqueue.add(command);
		}
		synchronized (this.commandqueue) {
			this.commandqueue.notifyAll();
		}
		
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
	}
	
	@Override
	public void run() {
		IServerCommand nextCmd = null;
		int waitCount = 0;
		while(this.loop){
			boolean isEmpty;
			int queuesize = 0;
			synchronized (this.commandqueue){
				isEmpty = this.commandqueue.isEmpty();
				queuesize = this.commandqueue.size();
			}
			if(isEmpty && waitCount > 10){
				try {
					// do not wait here
					debugOut("# debugger wait for next request. queue size is : " + queuesize);
					synchronized (this.commandqueue){
						this.commandqueue.wait();
					}
					waitCount = 0;
					
					if(this.loop == false){
						return;
					}
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			waitCount++;

			
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			// blocking here
			try {
				synchronized (this.commandqueue){
					nextCmd = fetch();
					
					this.commandqueue.notifyAll();
				}
				
				while(nextCmd != null){					
					nextCmd.sendCommand(this.reservedqueue, this.needfinish, this, null);
					
					synchronized (this.commandqueue){
						nextCmd = fetch();
						this.commandqueue.notifyAll();
					}
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void sendCommand(IServerCommand command, PostContext context) throws IOException 
	{
		if(!AlinousCore.debug(context)){
			return;
		}
		
		Socket con = null;
		// Send socket after create
		con = new Socket("localhost", port);
		
		OutputStream stream = null;
		Writer writer = null;
		try {
			stream = con.getOutputStream();
			//OutputStreamWriter writer = new OutputStreamWriter(stream);
			writer = new PrintWriter(stream, true);
			
			command.writeCommand(writer);
			
			// debug
			// AlinousDebug.debugOut("Writed command : " + "threadId : " + command.getThread().getId() + writer.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			stream.close();
			writer.close();
			
			try {
				con.close();
			} catch (IOException e) {}
		}
		
		
	}
	public IServerCommand fetch()
	{
		if(this.commandqueue.isEmpty()){
			return null;
		}
		
		IServerCommand el = this.commandqueue.get(0);
		this.commandqueue.remove(0);
		
		return el;
	}

	
	public int getPort()
	{
		return port;
	}

	public void setPort(int port)
	{
		this.port = port;
	}
	
	@Override
	public void debugOut(String str)
	{
		if(!debugDebugger){
			return;
		}
		
		System.out.println(str);
		System.out.flush();
	}

	public AbstractAlinousDebugManager getDebugManager() {
		return debugManager;
	}
	
}

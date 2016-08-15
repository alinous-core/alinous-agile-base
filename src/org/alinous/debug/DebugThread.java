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
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;

import org.alinous.AlinousCore;
import org.alinous.debug.breakstatus.DefaultOperation;
import org.alinous.debug.breakstatus.IDebuggerOperation;
import org.alinous.exec.ExecutableJdomFactory;
import org.alinous.exec.IExecutable;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.script.IScriptBlock;
import org.alinous.script.runtime.VariableRepository;
import org.jdom.Element;

public class DebugThread {
	private Thread thread;
	private int status;
	private int statusReason;
	private long threadId;
	private List<IThreadEventListner> eventListners = new CopyOnWriteArrayList<IThreadEventListner>();
	
	private Stack<DebugStackFrame> stackFrames = new Stack<DebugStackFrame>();
	private IDebuggerOperation operation;
	private IExecutable nextExec;
	
	private DebugStackIdPublisher stackIdPublisher = new DebugStackIdPublisher();
	
	private Object sem = new Object();
	private boolean terminated = false;
	
	// status
	public static final int STATUS_NONE = 0;
	public static final int STATUS_RUNNING = 1;
	public static final int STATUS_SUSPEND = 10;

	
	public static final String TAG_THREAD = "THREAD";
	public static final String ATTR_THREAD_ID = "thread_id";
	public static final String ATTR_STATUS = "status";
	public static final String ATTR_STATUS_REASON = "status_reason";
	
	
	public DebugThread(long threadId){
		this.threadId = threadId;
	}
	
	public DebugThread(Thread thread, AlinousDebugManager manager, IThreadEventListner listner)
	{
		this.thread = thread;
		this.status = STATUS_RUNNING;
		
		this.threadId = thread.getId();
		
		this.eventListners.add(listner);
		
		this.operation = new DefaultOperation();
		this.operation.init(this, manager);
		
	}

	public IDebuggerOperation getOperation()
	{
		return this.operation;
	}
	
	public void setOperation(IDebuggerOperation ope, AlinousDebugManager manager)
	{
		this.operation = ope;
		this.operation.init(this, manager);
	}
	
	public boolean equals(Object obj)
	{
		Thread target = (Thread)obj;
		
		return this.thread.getId() == target.getId();
	}

	public int getStatus()
	{
		return status;
	}

	public void setStatus(int status)
	{
		this.status = status;
	}
	
	public void newStackFrame(IScriptBlock scriptBlock, VariableRepository repo, PostContext context)
	{
		synchronized (this.stackFrames) {
			DebugStackFrame frame = new DebugStackFrame(scriptBlock, repo, context);
			frame.setFileName(scriptBlock.getFilePath());
			frame.setLine(scriptBlock.getLine());
			
			long stackId = this.stackIdPublisher.publishId();
			frame.setStackId(stackId);
			
			frame.setPeek(true);
			
			if(!this.stackFrames.empty()){
				this.stackFrames.peek().setPeek(false);
			}
			this.stackFrames.push(frame);
		}

		
		// debug
		//AlinousDebug.debugOut("DDDD : newStackFrame() : " + this.threadId);
		//AlinousDebug.dumpStackTrace();
	}
	
	public synchronized void destroyStackFrame()
	{
		// debug
		//AlinousDebug.debugOut("destroyStackFrame() : " + this.threadId);
		//AlinousDebug.dumpStackTrace();
		
		synchronized (this.stackFrames) {
			this.stackFrames.pop();
			
			if(!this.stackFrames.empty()){
				this.stackFrames.peek().setPeek(true);
			}
		}
	}
	
	public synchronized void discard()
	{
		synchronized (this.stackFrames) {
			this.stackFrames.clear();
		}
	}
	
	public void exportIntoJDomElement(Element parent) throws ExecutionException
	{
		Element element = new Element(TAG_THREAD);
		element.setAttribute(ATTR_THREAD_ID, Long.toString(this.threadId));
		element.setAttribute(ATTR_STATUS, Integer.toString(this.status));
		element.setAttribute(ATTR_STATUS_REASON, Integer.toString(this.statusReason));
		
		synchronized (this.stackFrames) {
			Iterator<DebugStackFrame> it = this.stackFrames.iterator();
			while(it.hasNext()){
				DebugStackFrame frame = it.next();
				frame.exportIntoJDomElement(element);
			}
		}
		
		if(this.nextExec != null){
			try {
				this.nextExec.exportIntoJDomElement(element);
			} catch (AlinousException e) {
				try {
					AlinousCore.getInstance(null, null).reportError(e);
				} catch (Throwable ignore) {
					ignore.printStackTrace();
				}
			}
		}
		
		// debug
		//AlinousDebug.debugOut("dump start");
		//AlinousDebug.dumpElement(element);
		
		parent.addContent(element);
	}
	
	@SuppressWarnings("rawtypes")
	public void importFromJDomElement(Element threadElement) throws AlinousException
	{
		String strThreadId = threadElement.getAttributeValue(ATTR_THREAD_ID);
		if(strThreadId != null){
			this.threadId = Long.parseLong(strThreadId);
		}
		
		String strStatus = threadElement.getAttributeValue(ATTR_STATUS);
		if(strStatus != null){
			this.status = Integer.parseInt(strStatus);
		}
		
		String strStatusReason = threadElement.getAttributeValue(ATTR_STATUS_REASON);
		if(strStatusReason != null){
			this.statusReason = Integer.parseInt(strStatusReason);
		}
		
		Element execElement = threadElement.getChild(IExecutable.TAG_EXECUTABLE);
		if(execElement != null){
			this.nextExec = ExecutableJdomFactory.createFirstExecutable(execElement);
			this.nextExec.importFromJDomElement(execElement);
		}
		
		Iterator it = threadElement.getChildren(DebugStackFrame.TAG_STACKFRAME).iterator();
		while(it.hasNext()){
			Element stackFrameElement = (Element)it.next();
			DebugStackFrame frame = new DebugStackFrame(null, null, null);
			
			frame.importFromJDomElement(stackFrameElement);
			
			this.stackFrames.push(frame);
		}
	}
	
	public void terminate(PostContext context)
	{
		this.terminated = true;
		
		resume(context);
	}
	
	public void suspend(final int reason, int line, PostContext context) throws InterruptedException, ThreadTerminatedException
	{
		this.status = STATUS_SUSPEND;
		this.statusReason = reason;
		
		synchronized (this.stackFrames) {
			DebugStackFrame topStack = this.stackFrames.peek();
			topStack.setLine(line);
		}
		/*
		Thread backNotify = new Thread(new Runnable(){
			public void run() {
				fireSuspendThreadEventListner(reason);
			}
		});
		
		backNotify.run();
		backNotify.join();
		*/
		fireSuspendThreadEventListner(reason, context); // without threading
		
		// AlinousDebug
		// AlinousDebug.debugOut("DDDDDDD: wait() thread : " + this.threadId);
				
		
		boolean interrapted = true;
		
		while(interrapted){
			interrapted = false;
			
			synchronized (this.sem) {
				try {
					this.sem.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
					
					interrapted = true;
				}
			}
			
			if(this.terminated){
				throw new ThreadTerminatedException();
			}
			
		}
		
		//AlinousDebug.debugOut("DDDDDDD: wait() thread finished : " + this.threadId);
	}
	
	public void resume(PostContext context)
	{
		this.status = STATUS_RUNNING;
		fireResumeThreadEventListner(context);
		
		// AlinousDebug
		//AlinousDebug.debugOut("DDDDDDD: resume() thread : " + this.threadId);
		
		synchronized (this.sem) {
			this.sem.notify();
		}
		
	}
	
	private void fireResumeThreadEventListner(PostContext context)
	{
		Iterator<IThreadEventListner> it = this.eventListners.iterator();
		while(it.hasNext()){
			IThreadEventListner listner = it.next();
			listner.fireThreadResumed(context);
		}	
	}
	
	private void fireSuspendThreadEventListner(int reason, PostContext context)
	{
		// 
		// AlinousDebug.debugOut("*********** this.eventListners.size() : " + this.eventListners.size());
		
		Iterator<IThreadEventListner> it = this.eventListners.iterator();
		while(it.hasNext()){
			IThreadEventListner listner = it.next();
			
			
			// 
			// AlinousDebug.debugOut("*********** IThreadEventListner listner = it.next(); : " + this.threadId + " -> " + listner);
			
			listner.fireThreadAboutToSuspend(reason, context);
		}
	}

	public long getThreadId() {
		return this.threadId;
	}

	public int getStatusReason() {
		return statusReason;
	}

	public IExecutable getNextExec() {
		return nextExec;
	}

	public void setNextExec(IExecutable nextExec) {
		this.nextExec = nextExec;

	}
	
	public DebugStackFrame[] getStackFrames()
	{
		synchronized (this.stackFrames) {
			ArrayList<DebugStackFrame> arrayList = new ArrayList<DebugStackFrame>();
			
			Iterator<DebugStackFrame> it = this.stackFrames.iterator();
			while(it.hasNext()){
				DebugStackFrame frame = it.next();
				
				arrayList.add(frame);
			}
			
			return arrayList.toArray(new DebugStackFrame[arrayList.size()]);
		}
	}

	public DebugStackFrame getTopStackFrame()
	{
		synchronized (this.stackFrames) {
			return this.stackFrames.get(stackFrames.size() - 1);
		}
	}

	public Thread getThread() {
		return thread;
	}

	public boolean isTerminated() {
		return terminated;
	}
	
}

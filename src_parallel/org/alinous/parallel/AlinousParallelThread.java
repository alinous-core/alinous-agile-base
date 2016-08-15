package org.alinous.parallel;

import java.util.concurrent.Semaphore;

import org.alinous.expections.AlinousException;
import org.alinous.parallel.exam.NullExec;


public class AlinousParallelThread extends Thread {
	private boolean running;
	private IAlinousThreadScope scope;
	private IExecuteParallel exec = new NullExec();
	private Object[] params;
	
	private Semaphore semaphore;
	private boolean waitArea = false;
	
	private IMainThreadContext mainThread;
	
	public AlinousParallelThread(IAlinousThreadScope scope, int i) throws InterruptedException
	{
		super("Alinous Thread " + i);
		
		this.running = true;
		this.scope = scope;
		
		this.semaphore = new Semaphore(1); //new AlinousPararellSemaphore(1, "AlinousPararellThread");
		this.semaphore.acquire(1);
		
		
	}
	
	@Override
	public void run() {
		try {
			doRun();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public void doRun() throws InterruptedException
	{
		while(this.running || this.exec != null){
		
			try{
				this.exec.execute(this.params);
			}catch(Throwable e){
				e.printStackTrace();
				//this.exec.handleError(e);
			}
			finally{
				try {
					this.exec = null;
					this.params = null;
					
					this.scope.fireFinisded(this);
				} catch (AlinousException e) {
					e.printStackTrace();
				}
			}
		
			
			if(!this.running){
				break;
			}
			/*
			synchronized (this) {
				this.waitArea = true;
				wait();
				this.waitArea = false;
			}
			*/
			try {
				this.waitArea  = true;
				this.semaphore.acquire(1);
				this.waitArea = false;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
	
	public void setScope(IAlinousThreadScope scope) {
		this.scope = scope;
	}

	public void dispose()
	{
		this.exec = null;
		this.scope = null;
		this.params = null;
		
		this.running = false;
		this.semaphore.release(1);
	}

	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}

	public void threadStart(IExecuteParallel exec, IAlinousThreadScope scope, Object[] params) throws InterruptedException
	{
		while(!this.waitArea || !this.semaphore.hasQueuedThreads()){ //this.semaphore.getWaiting() == 0){
			Thread.sleep(5);
			//Thread.yield();
			//AlinousDebug.debugOut("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ LOSS 10");
		}
		
		this.exec = exec;
		this.scope = scope;
		this.params = params;
		
		this.semaphore.release(1);
		
	}

	public Object[] getParams() {
		return params;
	}

	public IMainThreadContext getMainThread() {
		return mainThread;
	}

	public void setMainThread(IMainThreadContext mainThread) {
		this.mainThread = mainThread;
	}
	
	
}

package org.alinous.parallel;

import java.util.Iterator;
import java.util.Stack;
import java.util.concurrent.Semaphore;

import org.alinous.AlinousDebug;


public class AlinousParallelThreadManager implements IAlinousThreadScope {
	private int maxThreads = 10;
	// Semaphore
	private Semaphore semaphore;
	private Stack<AlinousParallelThread> standby = new Stack<AlinousParallelThread>();
	private BollowLauncher BollowLauncher;
	
	
	public AlinousParallelThreadManager(int maxThreads)
	{
		this.maxThreads = maxThreads;
		this.semaphore = new Semaphore(maxThreads); // AlinousParallelSemaphore(maxThreads, "AlinousParallelThreadManager");
		this.BollowLauncher = new BollowLauncher();
		
		try {
			rumpUp();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
			
	}
	
	public AlinousParallelThread borrow() throws InterruptedException
	{
		//AlinousDebug.debugOut("borrow brfore acquireUninterruptibly :  " + this.semaphore.availablePermits());
		this.semaphore.acquire(1);
		
		AlinousParallelThread thread = null;
		synchronized (this.standby) {
			// borrow
			//AlinousDebug.debugOut("borrow : " + this.standby.size() + " " + this.semaphore.availablePermits());
			thread = this.standby.pop();
			//AlinousDebug.debugOut("borrow result: " + this.standby.size() + " " + this.semaphore.availablePermits());
		}
		
		return thread;
	}
	
	/**
	 * This is the only 
	 * @param thread
	 */
	public void back(AlinousParallelThread thread){
		thread.setScope(this);
		synchronized (this.standby) {
			// borrow
			//AlinousDebug.debugOut("back");
			this.standby.push(thread);
		}
		
		
		this.semaphore.release(1);
		
		// Notify launcher here
		this.BollowLauncher.notifyWakeup();
		
		// back
		//AlinousDebug.debugOut("back after released :  " + this.semaphore.availablePermits());
	}
	
	private void rumpUp() throws InterruptedException
	{
		for (int i = 0; i < this.maxThreads; i++) {
			AlinousParallelThread thread = new AlinousParallelThread(this, i);
			thread.start();
			this.standby.push(thread);
	
		}

	}
	
	public void dispose() throws InterruptedException
	{
		synchronized (this.standby) {
			Iterator<AlinousParallelThread> it = this.standby.iterator();
			while(it.hasNext()){
				AlinousParallelThread thread = it.next();
				thread.dispose();
				
			}
		}
		
		if(this.BollowLauncher != null){
			this.BollowLauncher.dispose();
			this.BollowLauncher = null;
		}
		
		this.standby.clear();
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}

	public int getMaxThreads() {
		return maxThreads;
	}

	@Override
	public IAlinousThreadScope getParent() {
		return null;
	}

	@Override
	public AlinousThreadScope newScope(IExecuteParallel exec, int scopeMaxThread) {
		// new scope
		AlinousThreadScope scope = new AlinousThreadScope(this, exec, scopeMaxThread);
		
		return scope;
	}

	@Override
	public void fireFinisded(AlinousParallelThread thread) {
		
	}

	@Override
	public void startExec(Object[] params, IMainThreadContext context, int numParentScope) throws InterruptedException {
		// do nothing
	}
	
	
	public void dump()
	{
		AlinousDebug.debugOut(null, "-------------------------------------");
		AlinousDebug.debugOut(null, "stanby : " + this.standby.size());
	}

	@Override
	public void join(IMainThreadContext context) {
		
	}

	public BollowLauncher getBollowLauncher() {
		return BollowLauncher;
	}
	
	public int availablePermits()
	{
		return this.semaphore.availablePermits();
	}
	
}

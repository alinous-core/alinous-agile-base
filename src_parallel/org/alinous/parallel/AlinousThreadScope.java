package org.alinous.parallel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.alinous.expections.AlinousException;
import org.alinous.parallel.exam.NullExec;


public class AlinousThreadScope implements IAlinousThreadScope{
	private IAlinousThreadScope parent;
	private List<IAlinousThreadScope> childScopes = new ArrayList<IAlinousThreadScope>();
	
	protected IExecuteParallel exec = new NullExec();
	private int scopeMaxThread = 32;
	
	private Semaphore semaphore;
	//protected List<AlinousPararellThread> runningThreads = new ArrayList<AlinousPararellThread>();
	
	//private Thread joinWaitThread;
	
	
	public AlinousThreadScope(IAlinousThreadScope parent, IExecuteParallel exec, int scopeMaxThread)
	{
		this.parent = parent;
		this.exec = exec;
		this.scopeMaxThread = scopeMaxThread;
		this.semaphore = new Semaphore(this.scopeMaxThread); ////new AlinousPararellSemaphore(this.scopeMaxThread, "AlinousThreadScope");
	}

	
	public void startExec(Object[] params, IMainThreadContext mainThreadContextContext
							, int numParentScope) throws InterruptedException
	{
		//AlinousDebug.debugOut("startExec() mainThreadContextContext : " + mainThreadContextContext);
		
		ScopeThreadCounter counter = mainThreadContextContext.getThreadJoinCounter(this);
		counter.requestLaunch();
		
		BollowQueueElement element = new BollowQueueElement(this, params, mainThreadContextContext, numParentScope);
		getAlinousPararellThreadManager().getBollowLauncher().addTask(element);
		
	}
	
	public IAlinousThreadScope getParent() {
		return parent;
	}
	
	public void addChildScope(IAlinousThreadScope scope)
	{
		this.childScopes.add(scope);
	}

	@Override
	public AlinousThreadScope newScope(IExecuteParallel exec, int scopeMaxThread) {
		// create child scope
		AlinousThreadScope scope = new AlinousThreadScope(this, exec, scopeMaxThread);
		return scope;
	}
	
	protected AlinousParallelThreadManager getAlinousPararellThreadManager()
	{
		IAlinousThreadScope scope = this;
		while(scope != null){
			scope = scope.getParent();
			if(scope instanceof AlinousParallelThreadManager){
				return (AlinousParallelThreadManager) scope;
			}
		}
		
		return null;
	}

	@Override
	public void join(IMainThreadContext context) {
				
		// debug
		//AlinousDebug.debugOut("Join wait start " + context.getThreadJoinCounter(this)
		//			+ " scope is " + this);
		
		ScopeThreadCounter counter = context.getThreadJoinCounter(this);
		counter.setJoinCalled(true);
		
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
		synchronized (counter) {
			while(!counter.isTerminated()){
				try {
					counter.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
		
		
		//AlinousDebug.debugOut("Join Completed");

	}

	@Override
	public void fireFinisded(AlinousParallelThread thread) throws AlinousException {
		//AlinousDebug.debugOut("this.runningThreads wait for join is " + this.runningThreads.size());
		
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
		ScopeThreadCounter counter = thread.getMainThread().getThreadJoinCounter(this);
		synchronized (counter) {
			counter.threadFinished(thread);
			counter.notify(); // notify join waiter
		}
		
		// set null before aquiring thread aquire and set this value
		thread.setMainThread(null);
		
		AlinousParallelThreadManager mgr = getAlinousPararellThreadManager();
		
		// release as soon as possible after the first one
		this.semaphore.release(1);
		mgr.back(thread);
		
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
	}

	public Semaphore getSemaphore() {
		return semaphore;
	}
	
	public void threadDispose(Thread thread)
	{
		
	}


	public int getScopeMaxThread() {
		return scopeMaxThread;
	}

}

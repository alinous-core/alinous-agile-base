package org.alinous.parallel;

public class BollowQueueElement {
	private AlinousThreadScope scope;
	private Object[] params;
	private IMainThreadContext mainThreadContextContext;
	private int numParentScope = 0;
	
	public BollowQueueElement(AlinousThreadScope scope, Object[] params,
				IMainThreadContext mainThreadContextContext, int numParentScope)
	{
		this.scope = scope;
		this.params = params;
		this.mainThreadContextContext = mainThreadContextContext;
	}
	
	/**
	 * You can block in this function
	 * @throws Throwable
	 */
	public void execute() throws Throwable
	{
		// Before execute 
		//AlinousDebug.debugOut("BollowQueueElement for " + this.scope + scope.getSemaphore().availablePermits()
		//		+ "/" + scope.getScopeMaxThread());
		
		scope.getSemaphore().acquire(1);
		
		// 
		// AlinousDebug.debugOut("BollowQueueElement success " + this.scope + scope.getSemaphore().availablePermits()
		//		+ "/" + scope.getScopeMaxThread());
		
		AlinousParallelThreadManager mgr = scope.getAlinousPararellThreadManager();
		
		AlinousParallelThread thread = mgr.borrow();
		
		// set main thread for join counter
		ScopeThreadCounter counter = this.mainThreadContextContext.getThreadJoinCounter(scope);
		synchronized (counter) {
			thread.setMainThread(this.mainThreadContextContext);
			counter.addJoinTarget(thread);
		}

		
		thread.threadStart(this.scope.exec, this.scope, this.params);
	}
	
	public boolean isExecutableRightNow()
	{
		int pertmittedRightNow = scope.getSemaphore().availablePermits();
		
		if(pertmittedRightNow == 0){
			return false;
		}
		
		AlinousParallelThreadManager mgr = scope.getAlinousPararellThreadManager();
		if(mgr.availablePermits() == 0){
			return false;
		}
		
		return true;
				
	}

	public AlinousThreadScope getScope() {
		return scope;
	}

	public int getNumParentScope() {
		return numParentScope;
	}
	
	
}

package org.alinous.parallel;

import org.alinous.expections.AlinousException;

public interface IAlinousThreadScope {
	public IAlinousThreadScope getParent();
	public AlinousThreadScope newScope(IExecuteParallel exec, int scopeMaxThread);
	
	public void startExec(Object[] params, IMainThreadContext context, int numParentScope) throws InterruptedException;
	public void fireFinisded(AlinousParallelThread thread) throws AlinousException;
	
	public void join(IMainThreadContext context);
}

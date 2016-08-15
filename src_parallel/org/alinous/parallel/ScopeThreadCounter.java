package org.alinous.parallel;

import java.util.LinkedList;
import java.util.List;

public class ScopeThreadCounter {
	private List<AlinousParallelThread> joinTargetList = new LinkedList<AlinousParallelThread>();
	private int startCount = 0;
	private int finished = 0;
	
	private boolean joinCalled;
	
	
	public ScopeThreadCounter()
	{

	}
	
	public synchronized void requestLaunch()
	{
		this.startCount++;
	}
	
	public void addJoinTarget(AlinousParallelThread thread)
	{
		synchronized (this.joinTargetList) {
			this.joinTargetList.add(thread);
		}
	}
	
	public void threadFinished(AlinousParallelThread thread){
		synchronized (this.joinTargetList) {
			this.joinTargetList.remove(thread);
			this.finished++;
		}
	}
	
	
	public boolean isJoinCalled() {
		return joinCalled;
	}

	public void setJoinCalled(boolean joinCalled) {
		this.joinCalled = joinCalled;
	}

	public boolean isTerminated()
	{
		synchronized (this.joinTargetList) {
			/*
			AlinousDebug.debugOut("check terminated--");
			AlinousDebug.debugOut("check joinTargetList --> " + joinTargetList.toString());
			AlinousDebug.debugOut("check finished --> " + finished);
			AlinousDebug.debugOut("check startCount --> " + startCount);
			*/
			return this.joinTargetList.isEmpty() && this.startCount == this.finished;
		}
	}
	
}

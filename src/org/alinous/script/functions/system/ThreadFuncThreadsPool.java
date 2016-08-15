package org.alinous.script.functions.system;

import java.util.HashMap;
import java.util.Map;

public class ThreadFuncThreadsPool {
	private Map<Long, Thread> threadMap = new HashMap<Long, Thread>();
	
	private static ThreadFuncThreadsPool instance;
	
	public static ThreadFuncThreadsPool getThreadPool()
	{
		if(instance == null){
			instance = new ThreadFuncThreadsPool();
		}
		
		return instance;
	}
	
	public void registerThread(Thread th)
	{
		long threadId = th.getId();
		this.threadMap.put(new Long(threadId), th);
	}
	
	public Thread getThread(long threadId)
	{
		return this.threadMap.get(new Long(threadId));
	}
	
	public void releaseThread(long threadId)
	{
		this.threadMap.remove(new Long(threadId));
	}
	
}

package org.alinous.script.functions.system.debug;

import org.alinous.AlinousCore;
import org.alinous.debug.AlinousDebugManager;
import org.alinous.debug.DebugStackFrame;
import org.alinous.debug.DebugThread;
import org.alinous.script.runtime.VariableRepository;

public class DebugStatusHandler {
	private AlinousCore core;
	
	public DebugStatusHandler(AlinousCore core)
	{
		this.core = core;
	}
	
	public DebugThread getThreadStatus(long threadId)
	{
		AlinousDebugManager debugManager = core.getAlinousDebugManager();
		
		DebugThread[] threads = debugManager.getThreads();
		for(int i = 0; i < threads.length; i++){
			if(threadId == threads[i].getThreadId()){
				return threads[i];
			}
		}
		
		return null;
	}
	
	public DebugStackFrame getStackFrame(long threadId, long stackId)
	{
		DebugThread dbThread = getThreadStatus(threadId);
		
		DebugStackFrame frames[] = dbThread.getStackFrames();
		for(int i = 0; i < frames.length; i++){
			if(frames[i].getStackId() == stackId){
				return frames[i];
			}
		}
		
		return null;
	}
	
	public VariableRepository getVariables(long threadId, long stackId)
	{
		DebugStackFrame frame = getStackFrame(threadId, stackId);
		if(frame == null){
			return null;
		}
		
		return frame.getRepo();
	}
}

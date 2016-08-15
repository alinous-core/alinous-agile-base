package org.alinous.parallel;

import java.util.HashMap;
import java.util.Map;


public class MainThreadContext implements IMainThreadContext {
	
	private Map<AlinousThreadScope, ScopeThreadCounter> threadJoinCounters = new HashMap<AlinousThreadScope, ScopeThreadCounter>();
	
	@Override
	public ScopeThreadCounter getThreadJoinCounter(AlinousThreadScope scope)
	{
		ScopeThreadCounter counter = null;
		
		synchronized (this.threadJoinCounters) {
			counter = this.threadJoinCounters.get(scope);
			if(counter == null){
				counter = new ScopeThreadCounter();
				this.threadJoinCounters.put(scope, counter);
			}
		}
		
		return counter;
	}
	
	

}

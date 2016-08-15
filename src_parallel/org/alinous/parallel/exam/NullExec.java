package org.alinous.parallel.exam;

import org.alinous.parallel.AlinousThreadScope;
import org.alinous.parallel.IExecuteParallel;

public class NullExec implements IExecuteParallel{
	
	@Override
	public void setScope(AlinousThreadScope scope) {
		
	}

	@Override
	public AlinousThreadScope getScope() {
		return null;
	}

	@Override
	public void execute(Object[] params) {
		
	}

	@Override
	public void handleError(Throwable e) {
		
	}

	@Override
	public boolean isExecuted() {
		return false;
	}
	
}

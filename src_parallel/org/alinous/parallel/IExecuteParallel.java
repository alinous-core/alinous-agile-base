package org.alinous.parallel;

public interface IExecuteParallel {
	public void setScope(AlinousThreadScope scope);
	public AlinousThreadScope getScope();
	public void execute(Object[] params);
	public void handleError(Throwable e);
	
	public boolean isExecuted();
}

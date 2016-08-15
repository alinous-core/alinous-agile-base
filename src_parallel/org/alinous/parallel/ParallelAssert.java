package org.alinous.parallel;

public class ParallelAssert {
	public static ParallelAssert instance;
	
	public static ParallelAssert getInstance()
	{
		if(instance == null){
			instance = new ParallelAssert();
		}	
		return instance;
	}
	
	
	private ParallelAssert()
	{
		
	}
	
	public void assertVariableHandle()
	{
		Thread thread = Thread.currentThread();
		
		StackTraceElement elements[] = thread.getStackTrace();
		for(int i = 0; i < elements.length; i++){
			
		}
		
	}
	
	
	
	public void dispose()
	{
		instance = null;
	}
}

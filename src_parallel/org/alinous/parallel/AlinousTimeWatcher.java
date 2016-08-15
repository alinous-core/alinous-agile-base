package org.alinous.parallel;

public class AlinousTimeWatcher {
	private long start;
	
	public AlinousTimeWatcher()
	{
		start = System.currentTimeMillis();
	}
	
	public void stop(String msg)
	{
		long now = System.currentTimeMillis();
		long interval = (now - start) / 1000;
		
		System.out.println(msg + " : " + interval + " sec");
		
	}
}

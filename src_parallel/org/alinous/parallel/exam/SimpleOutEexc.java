package org.alinous.parallel.exam;

import org.alinous.parallel.AlinousThreadScope;
import org.alinous.parallel.IExecuteParallel;

public class SimpleOutEexc implements IExecuteParallel {
	private AlinousThreadScope scope;
	
		
	@Override
	public void execute(Object[] params) {
		
		
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//job((String) params[0]);
		job2((String) params[0]);
		
	}
	
	public static int job2(String str)
	{
		System.out.println(str);
		return 0;
	}
	
	public static int job(String str)
	{
		int x = 0;
		for (int j = 0; j < 100000; j++) {
			for (int i = 0; i < 100000; i++) {
				x = i + i + 456 * 5;
				x = x + i + j;
			}
			
			x = (int) Math.sqrt(x + j);
		}

		
		return x;
	}
	
	@Override
	public AlinousThreadScope getScope() {
		return this.scope;
	}
	
	@Override
	public void setScope(AlinousThreadScope scope) {
		this.scope = scope;
	}


	@Override
	public void handleError(Throwable e) {
		e.printStackTrace();
	}

	@Override
	public boolean isExecuted() {
		return false;
	}

	
}

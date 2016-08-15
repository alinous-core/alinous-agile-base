package org.alinous.parallel.exam;

import org.alinous.parallel.AlinousParallelThreadManager;
import org.alinous.parallel.IAlinousThreadScope;
import org.alinous.parallel.IMainThreadContext;
import org.alinous.parallel.MainThreadContext;


public class ParallelTest {
	
	public void test01() throws InterruptedException
	{
		AlinousParallelThreadManager mgr = new AlinousParallelThreadManager(30);
		
		mgr.dispose();
		
		System.out.println("test01() finished");
	}
	
	public void Test02() throws InterruptedException
	{
		IMainThreadContext context = new MainThreadContext();
		AlinousParallelThreadManager mgr = new AlinousParallelThreadManager(32);
		
		SimpleOutEexc exec = new SimpleOutEexc();
		IAlinousThreadScope scope = mgr.newScope(exec, 32);
		
		try {
			for (int i = 0; i < 300; i++) {
				scope.startExec(new String[]{"*******************************" + i}, context, 0);
			}
			
			System.out.println("Test02() finished insert queue %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
			
			scope.join(context);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		mgr.dump();
		mgr.dispose();
		
		System.out.println("Test02() finished");
	}

	public void Test03() throws InterruptedException
	{
		AlinousParallelThreadManager mgr = new AlinousParallelThreadManager(4);
		SimpleOutEexc exec = new SimpleOutEexc();
		IAlinousThreadScope scope = mgr.newScope(exec, 4);
		
		int it = 0;
		int errCnt = 0;
		for (it = 0; it < 100; it++) {
			try {
				IMainThreadContext context = new MainThreadContext();
				
				for (int i = 0; i < 30000; i++) {
					scope.startExec(new String[]{"*******************************" + i}, context, 0);
				}
				
				scope.join(context);
			} catch (InterruptedException e) {
				e.printStackTrace();
				errCnt++;
			}
			
			System.out.println("Test02() finished");
			
			mgr.dump();
			
		}
		
		mgr.dispose();
		
		System.out.println("Test03() finished : it -> " + it + " errCnt : " + errCnt);
	}
	
	public void Test04() throws InterruptedException
	{
		int it = 0;
		int errCnt = 0;
		for (it = 0; it < 100; it++) {
			Test02();
			System.out.println("Test02() clear : it -> " + it + " errCnt : " + errCnt);
		}
		
		System.out.println("Test04() finished : it -> " + it + " errCnt : " + errCnt);
	}
	
	public void test05() throws InterruptedException
	{
		Thread.currentThread().interrupt();
		
	     if (Thread.interrupted())
	            throw new InterruptedException();
	}
	
	public static void main(String[] args) throws InterruptedException {
		new ParallelTest().Test02();
	}
}

package org.alinous.parallel.exam;


import org.alinous.parallel.AlinousParallelSemaphore;

public class TestSemaphore {
	
	public void test01() throws InterruptedException
	{
		int max = 3;
		AlinousParallelSemaphore semaphore = new AlinousParallelSemaphore(max, "test01()");
		
		semaphore.acquire(1);
		semaphore.acquire(1);
		semaphore.acquire(1);
		semaphore.acquire(1);
		semaphore.acquire(1);
		semaphore.acquire(1);
		
		
		semaphore.release(1);
		semaphore.release(1);
		
		if(semaphore.getPermitSize() != max){
			//fail("失敗");
		}
	}

	public void test02()
	{
		int max = 3;
		final AlinousParallelSemaphore semaphore = new AlinousParallelSemaphore(max, "test01()");
		
		Thread th1  =new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < 10; i++) {
					try {
						
						semaphore.acquire(1);
						System.out.println("acquired 100" );
						Thread.sleep(100);
						semaphore.release(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		Thread th2  =new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < 10; i++) {
					try {
						semaphore.acquire(1);
						System.out.println("acquired 50");
						Thread.sleep(50);
						semaphore.release(1);
					} catch (InterruptedException e) {
					}

				}
			}
		});
		
		th1.start();
		th2.start();
		
		try {
			th1.join();
			th2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if(semaphore.getPermitSize() != max){
			//fail("失敗");
		}
	}
}

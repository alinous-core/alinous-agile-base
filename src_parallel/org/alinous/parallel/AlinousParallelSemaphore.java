package org.alinous.parallel;

import java.util.ArrayList;
import java.util.List;



public class AlinousParallelSemaphore {
	private int permitSize;
	private int remains;
	private Object remainsLock = new Object();
	
	private String name;
	
	private int waiting;
	private List<Thread> waitingThreads = new ArrayList<Thread>();
	
	public AlinousParallelSemaphore(int permitSize, String name)
	{
		this.permitSize = permitSize;
		this.remains = permitSize;
		this.name = name;
		this.waiting = 0;
	}
	
	public void acquire(int count) throws InterruptedException
	{

		synchronized (this.waitingThreads) {
			this.waiting++;
			this.waitingThreads.add(Thread.currentThread());
		}
		
		boolean check = check(count);
		while(check){
			//if(this.name.equals("AlinousParallelThreadManager")){
			//	AlinousDebug.debugOut("AlinousParallelSemaphore#acquire : "  + name + " waiting ");
			//}
			
			/// ここの間でリリースされるとコマる
			//synchronized (this) {
//			}
			Thread.sleep(10);
			//AlinousDebug.debugOut("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ LOSS yield");
			//Thread.yield();
			
			check = check(count);
		}
		synchronized (this.waitingThreads) {
			this.waitingThreads.remove(Thread.currentThread());
			this.waiting--;
		}
		
		//if(this.name.equals("AlinousParallelThreadManager")){
		//	AlinousDebug.debugOut("passed");
		//}
	
		//if(this.name.equals("AlinousParallelThreadManager")){
		//	AlinousDebug.debugOut("AlinousParallelSemaphore#acquire : "  + name + " " +  count);
		//}
	}
	
	private boolean check(int count)
	{

		boolean ans = false;
		synchronized (remainsLock) {
			ans = this.remains < count;
			if(!ans){
				this.remains = this.remains - count;
				
				//if(this.name.equals("AlinousParallelThreadManager")){
				//	AlinousDebug.debugOut("check : " + this.remains + "<" + count);
				//}
			}
			
		}
		
		//if(this.name.equals("AlinousParallelThreadManager")){
		//	AlinousDebug.debugOut("check : " + this.remains + "<" + count);
		//}
		return ans;
	}
	
	public void release(int count)
	{
		synchronized (remainsLock) {
			this.remains = this.remains + count;
			//if(this.name.equals("AlinousParallelThreadManager")){
			//	AlinousDebug.debugOut("AlinousParallelSemaphore#release : " + name + " -" + count);
			//	AlinousDebug.debugOut("remains : " + this.remains);
			//}
		}
		
		synchronized (this) {

			/*
			while(needsWait()){
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}*/
			
			notifyAll();
		}
	}
	/*
	private boolean needsWait()
	{
		synchronized (this.waitingThreads) {
			if(this.waiting == 0){
				return false;
			}
			
			Iterator<Thread> it = this.waitingThreads.iterator();
			while(it.hasNext()){
				Thread th = it.next();
				if(th.getState() != Thread.State.WAITING){
					return true;
				}
			}
		}
		return false;
	}*/
	
	
	public int availablePermits()
	{
		return this.remains;
	}
	
	public int getPermitSize() {
		return permitSize;
	}

	public int getWaiting() {
		return waiting;
	}

	public String getName() {
		return name;
	}

}

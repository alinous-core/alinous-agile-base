package org.alinous.parallel;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.alinous.AlinousDebug;

public class BollowLauncher implements Runnable{
	
	private List<BollowQueueElement> bollowReservequeue = new LinkedList<BollowQueueElement>();
	private List<BollowQueueElement> retryQueue = new LinkedList<BollowQueueElement>();
	private boolean loop;
	
	public BollowLauncher() {
		this.loop = true;
		
		Thread th = new Thread(null, this, "BollowLauncher");
		th.start();
		
		while(th.getState() != Thread.State.WAITING){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void addTask(BollowQueueElement element)
	{
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
		
		// 
		//AlinousDebug.debugOut("About to addTask() : " + element.getScope());
		
		synchronized (this.bollowReservequeue) {
			this.bollowReservequeue.add(element);
			this.bollowReservequeue.notifyAll();
		}
		
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
	}
	
	public void notifyWakeup()
	{
		synchronized (this.bollowReservequeue) {
			this.bollowReservequeue.notifyAll();
		}
	}
	
	/**
	 * fetch and execute que context
	 */
	@Override
	public void run() {
		BollowQueueElement nextExec = null;
		int waitCount = 0;
		while(this.loop){
			synchronized (this.bollowReservequeue){
				if(this.bollowReservequeue.isEmpty() && 
						this.retryQueue.isEmpty() &&
						waitCount > 10){
					try {
						AlinousDebug.debugOut(null, "wait for next request");
						this.bollowReservequeue.wait();
						waitCount = 0;
						
						if(this.loop == false){
							return;
						}
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				waitCount++;
			}

			
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			// retry queue is accessed by only this launcher thread
			retryQueue();
			
			
			// next ququq
			try {
				synchronized (this.bollowReservequeue){
					nextExec = fetch();
					
					this.bollowReservequeue.notifyAll();
				}
				
				while(nextExec != null){
					// check executable
					if(nextExec.isExecutableRightNow()){
						nextExec.execute();
					}
					else{
						// register retry quque
						this.retryQueue.add(nextExec);
						sortRetryQueue();
					}
					
					
					// fetch next
					synchronized (this.bollowReservequeue){
						nextExec = fetch();
						this.bollowReservequeue.notifyAll();
					}
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
	
	private void retryQueue()
	{
		Iterator<BollowQueueElement> it = this.retryQueue.iterator();
		while(it.hasNext()){
			BollowQueueElement queue = it.next();
			
			if(queue.isExecutableRightNow()){
				try {
					queue.execute();
				} catch (Throwable e) {
					e.printStackTrace();
				}
				it.remove();
			}
		}
	}
	
	private void sortRetryQueue(){
		Collections.sort(this.retryQueue, new Comparator<BollowQueueElement>() {
			@Override
			public int compare(BollowQueueElement o1, BollowQueueElement o2) {
				int num1 = o1.getNumParentScope();
				int num2 = o2.getNumParentScope();
				
				if(num1 == num2){
					return 0;
				}
				else if(num1 > num2){
					return 1;
				}
				
				return -1;
			}
		});
		
	}
	
	private BollowQueueElement fetch()
	{
		if(this.bollowReservequeue.isEmpty()){
			return null;
		}
		
		BollowQueueElement el = this.bollowReservequeue.get(0);
		this.bollowReservequeue.remove(0);
		
		return el;
	}
	
	
	public void dispose()
	{
		synchronized (this.bollowReservequeue){
			this.loop = false;
			this.bollowReservequeue.notifyAll();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		dispose();
		super.finalize();
	}

	

}

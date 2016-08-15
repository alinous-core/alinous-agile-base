package org.alinous.plugin.postgres;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;


public class AlinousConnectionPool
{
	private PostgreSQLConnectionFactory factory;
	
	private List<Object> idle = new LinkedList<Object>();
	private List<Object> running = new LinkedList<Object>();
	
	private int maxIdle = 3;
	private boolean dispose = false;
	
	//private int openCount = 0;
	
	private Semaphore semaphore;
	
	public AlinousConnectionPool(PostgreSQLConnectionFactory factory)
	{
		this.factory = factory;
		this.maxIdle = factory.getMaxclients();
		
		System.out.println("Max data connection : " + this.maxIdle);
		this.semaphore = new Semaphore(this.maxIdle);
	}
	
	public Object borrowObject() throws Exception
	{
		if(dispose){
			return null;
		}
	
		/*
		StackTraceElement elements[]  = Thread.currentThread().getStackTrace();
		System.out.println("--------------------------------------------------------------");
		for(int i = 0; i < elements.length; i++){
			System.out.println(elements[i].getClassName() + "." + elements[i].getMethodName() + " : "
					+ elements[i].getFileName() + " line : " + elements[i].getLineNumber());
		}
		System.out.println("--------------------------------------------------------------");
		*/
		
		//this.openCount++;
		
		Object obj = null;
		this.semaphore.acquire(1);
		
		synchronized (this.idle) {
			if(this.idle.isEmpty()){
				obj = this.factory.makeObject();
			}else{
				sortIdle();
				
				obj = this.idle.get(0);
				this.idle.remove(0);
			}
			
			while(!this.factory.validateObject(obj)){
				this.factory.destroyObject(obj);
				
				checkAllInvalidConnections();
				Thread.sleep(500);
				
				obj = this.factory.makeObject();
			}
			
			this.running.add(obj);
		}
		
		this.factory.activateObject(obj);
		
		return obj;
	}
	
	private void checkAllInvalidConnections()
	{
		Iterator<Object> it = this.idle.iterator();
		while(it.hasNext()){
			Object obj = it.next();
			
			if(!this.factory.validateObject(obj)){
				it.remove();
				try {
					this.factory.destroyObject(obj);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void sortIdle()
	{
		Collections.sort(this.idle, new Comparator<Object>() {
			public int compare(Object o1, Object o2)
			{
				int k1 = ((PostgreSqlConnection)o1).getKnouledge();
				int k2 = ((PostgreSqlConnection)o2).getKnouledge();
				return k2 - k1;
			}
		});
		
		// debug
		/*
		Iterator<Object> it = this.idle.iterator();
		while(it.hasNext()){
			Object obj = it.next();
			
			AlinousDebug.debugOut(">>> obj : " + obj + " k : " + ((PostgreSqlConnection)obj).getKnouledge());
		}
		*/
	}
	
	public void returnObject(Object obj) throws Exception
	{
		//this.openCount--;
		//AlinousDebug.debugOut("openCount : " + openCount);
		
		synchronized (this.idle) {
			this.running.remove(obj);
			
			boolean isValid = this.factory.validateObject(obj);
			
			if(!isValid){
				this.factory.destroyObject(obj);
			}
			else if(this.idle.size() >= this.maxIdle){
				this.factory.passivateObject(obj);
				this.idle.add(obj);
				sortIdle();
				
				Object removeObj = this.idle.remove(this.idle.size() - 1);
				
				this.factory.destroyObject(removeObj);
			}else{			
				this.factory.passivateObject(obj);
				this.idle.add(obj);
			}
		}
		
		this.semaphore.release(1);

		//reportConnections();
	}

//	public void reportConnections()
//	{
//		AlinousDebug.debugOut("/*************************************************************/");
//		AlinousDebug.debugOut("Opened : " + this.openCount);
//		
//		AlinousDebug.debugOut("/*************************************************************/");
//	}

	public void clear() throws Exception
	{
		synchronized (this.idle){
			Iterator<Object> it = this.idle.iterator();
			while(it.hasNext()){
				Object obj = it.next();
				
				this.factory.destroyObject(obj);
				//this.idle.remove(obj);
				it.remove();
			}
		}
		
	}

	public void close() throws Exception
	{
		/*while(!this.running.isEmpty()){
			clear();
		}*/
		
		clear();
	}
}

package org.alinous.stdio;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class AlinousStdIo {
	private Map<String, IStdIoListner> listners= new HashMap<String, IStdIoListner>();
	
	public AlinousStdIo()
	{
		
	}
	
	public void addListner(IStdIoListner listner)
	{
		this.listners.put(listner.getName(), listner);
	}
	
	public void removeListner(String name)
	{
		this.listners.remove(name);
	}
	
	public void out(String message)
	{
		Iterator<String> it = this.listners.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			
			IStdIoListner listner = this.listners.get(key);
			listner.out(message);
		}
	}
	
	public void dispose()
	{
		Iterator<String> it = this.listners.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			IStdIoListner listner = this.listners.get(key);
			listner.dispose();
		}
		this.listners.clear();
	}
}

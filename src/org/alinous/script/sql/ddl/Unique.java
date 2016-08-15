package org.alinous.script.sql.ddl;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Unique
{
	private List<String> key = new CopyOnWriteArrayList<String>();
	
	public void addKey(String key)
	{
		this.key.add(key);		
	}
	
	public boolean isKey(String key)
	{
		return this.key.contains(key);
	}
	
	public List<String> getKey()
	{
		return this.key;
	}
}

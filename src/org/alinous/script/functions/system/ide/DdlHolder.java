package org.alinous.script.functions.system.ide;

import java.util.HashMap;
import java.util.Map;

public class DdlHolder {
	public static Map<String, DdlHolder> instances = new HashMap<String, DdlHolder>();
	
	private Map<String, AlinousTableSchema> tables = new HashMap<String, AlinousTableSchema>();
	
	private boolean initialized;
	
	private DdlHolder(){	}
	
	
	public static DdlHolder getInstance(String alinousHome)
	{
		DdlHolder holder = DdlHolder.instances.get(alinousHome);
		
		if(holder == null){
			holder = new DdlHolder();
			
			DdlHolder.instances.put(alinousHome, holder);
		}
		
		return holder;
	}
	
	public void addTableSchema(AlinousTableSchema table)
	{
		String tableName = table.getTableName().toUpperCase();
		
		this.tables.put(tableName, table);
	}
	
	public AlinousTableSchema getTable(String tableName)
	{
		return this.tables.get(tableName.toUpperCase());
	}
	
	public void reset()
	{
		this.tables.clear();
	}

	public boolean isInitialized() {
		return initialized;
	}


	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}


	public Map<String, AlinousTableSchema> getTables() {
		return tables;
	}
	
	
}

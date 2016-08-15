package org.alinous.sql.config;

import java.util.LinkedList;
import java.util.List;

public class SqlTriggerConfig
{
	private String tableName;
	private List<String> timings = new LinkedList<String>();
	
	private String module;
	private String functioncall;
	
	public void addTiming(String timing)
	{
		this.timings.add(timing);
	}

	public String getTableName()
	{
		return tableName;
	}

	public void setTableName(String tableName)
	{
		this.tableName = tableName;
	}

	public String getModule()
	{
		return module;
	}

	public void setModule(String module)
	{
		this.module = module;
	}

	public String getFunctioncall()
	{
		return functioncall;
	}

	public void setFunctioncall(String functioncall)
	{
		this.functioncall = functioncall;
	}
	
}

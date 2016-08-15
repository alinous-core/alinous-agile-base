package org.alinous.script.sql;

public class DmlMatchRequest
{
	private String tableName;
	private String columnName;
	
	public DmlMatchRequest(){}
	
	public DmlMatchRequest(String tableName, String columnName)
	{
		this.tableName = tableName;
		this.columnName = columnName;
	}
	
	public String getTableName()
	{
		return tableName;
	}
	public void setTableName(String tableName)
	{
		this.tableName = tableName;
	}
	public String getColumnName()
	{
		return columnName;
	}
	public void setColumnName(String columnName)
	{
		this.columnName = columnName;
	}
	

}

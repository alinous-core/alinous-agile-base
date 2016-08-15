package org.alinous.sql.config;

import java.util.LinkedList;
import java.util.List;

public class SqlConstrainConfig
{
	private String tableName;
	private List<SqlConstrainConfigParam> paramList = new LinkedList<SqlConstrainConfigParam>();
	
	public void addParam(SqlConstrainConfigParam param)
	{
		this.paramList.add(param);
	}

	public String getTableName()
	{
		return tableName;
	}

	public void setTableName(String tableName)
	{
		this.tableName = tableName;
	}

	public List<SqlConstrainConfigParam> getParamList()
	{
		return paramList;
	}

	@Override
	public boolean equals(Object obj)
	{
		return ((SqlConstrainConfig)obj).tableName.equals(this.tableName);
	}
	
	
}

package org.alinous.datasrc.types;

import java.util.ArrayList;
import java.util.List;

import org.alinous.script.sql.statement.SQLFunctionCallArguments;

public class PlSQLTrigger
{
	private boolean exixts;
	private String triggerName;
	private String triggerTable;
	
	private String timing;
	private List<UpdateType> updateTypes = new ArrayList<UpdateType>();
	
	private String updateTarget;
	private String funcName;
	private SQLFunctionCallArguments funcArguments = new SQLFunctionCallArguments();
	
	public String getTriggerName()
	{
		return triggerName;
	}
	public void setTriggerName(String triggerName)
	{
		this.triggerName = triggerName;
	}
	public String getTriggerTable()
	{
		return triggerTable;
	}
	public void setTriggerTable(String triggerTable)
	{
		this.triggerTable = triggerTable;
	}
	
	public void addUpdateTypes(UpdateType timing)
	{
		this.updateTypes.add(timing);
	}
	public List<UpdateType> getUpdateTypes()
	{
		return updateTypes;
	}
	public String getTiming()
	{
		return timing;
	}
	public void setTiming(String timing)
	{
		this.timing = timing;
	}
	public void setUpdateTarget(String updateTarget)
	{
		this.updateTarget = updateTarget;
	}
	public String getUpdateTarget()
	{
		return updateTarget;
	}
	public String getFuncName()
	{
		return funcName;
	}
	public void setFuncName(String funcName)
	{
		this.funcName = funcName;
	}
	public boolean isExixts()
	{
		return exixts;
	}
	public void setExixts(boolean exixts)
	{
		this.exixts = exixts;
	}
	public SQLFunctionCallArguments getFuncArguments()
	{
		return funcArguments;
	}
	public void setFuncArguments(SQLFunctionCallArguments funcArguments)
	{
		this.funcArguments = funcArguments;
	}

	
	
}

package org.alinous.exec;

import org.alinous.script.sql.DmlCheckListElement;

public class ScriptError
{
	private IExecutable scriptElement;
	private String message;
	private String tableName;
	private String constrainType;
	private String columnName;
	
	private int line;
	private int linePosition;
	
	
	private DmlCheckListElement checkElement;
	
	public int getLine()
	{
		if(this.scriptElement == null){
			return this.line;
		}
		return this.scriptElement.getLine();
	}
	
	public IExecutable getScriptElement()
	{
		return scriptElement;
	}

	public void setScriptElement(IExecutable scriptElement)
	{
		this.scriptElement = scriptElement;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public String getConstrainType()
	{
		return constrainType;
	}

	public void setConstrainType(String constrainType)
	{
		this.constrainType = constrainType;
	}

	public String getColumnName()
	{
		return columnName;
	}

	public void setColumnName(String columnName)
	{
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

	public DmlCheckListElement getCheckElement()
	{
		return checkElement;
	}

	public void setCheckElement(DmlCheckListElement checkElement)
	{
		this.checkElement = checkElement;
	}

	public int getLinePosition()
	{
		return linePosition;
	}

	public void setLinePosition(int linePosition)
	{
		this.linePosition = linePosition;
	}

	public void setLine(int line)
	{
		this.line = line;
	}
	
	
}

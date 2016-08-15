package org.alinous.datasrc.types;

import java.util.ArrayList;
import java.util.List;

public class UpdateType
{
	private String typeName;
	private List<String> colList = new ArrayList<String>();
	
	
	
	public String getTypeName()
	{
		return typeName;
	}
	public void setTypeName(String typeName)
	{
		this.typeName = typeName;
	}
	public List<String> getColList()
	{
		return colList;
	}
	public void setColList(List<String> colList)
	{
		this.colList = colList;
	}
	public void addColList(String colList)
	{
		this.colList.add(colList);
	}
	
	
}

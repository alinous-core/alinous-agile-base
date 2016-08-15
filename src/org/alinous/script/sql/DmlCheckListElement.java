package org.alinous.script.sql;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DmlCheckListElement
{
	private String tableName;
	private List<DmlCheckParam>	paramList = new ArrayList<DmlCheckParam>();
	
	public DmlCheckListElement(){}
	
	public boolean isDeletable()
	{
		// deletable checking with "or" condition
		Iterator<DmlCheckParam> it = this.paramList.iterator();
		while(it.hasNext()){
			DmlCheckParam prm = it.next();
			
			if(prm.isDeletable()){
				return true;
			}
		}
		
		return false;
	}
	
	public void handleMatchRequest(DmlMatchRequest req)
	{
		// matching table
		if(!this.tableName.equals(req.getTableName())){
			return;
		}
		
		// matching columns
		Iterator<DmlCheckParam> it = this.paramList.iterator();
		while(it.hasNext()){
			DmlCheckParam prm = it.next();
			
			prm.handleMatchRequest(req);
		}
	}
	
	public void addParam(DmlCheckParam param)
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



	
}

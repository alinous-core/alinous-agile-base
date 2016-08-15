package org.alinous.script.sql;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DmlCheckParam
{
	private String constrain;
	private List<DmlCheckParamColumn> columnNames = new ArrayList<DmlCheckParamColumn>();
	
	public void handleMatchRequest(DmlMatchRequest req)
	{
		// handle request, matching for each column with the "and" condition
		Iterator<DmlCheckParamColumn> it = this.columnNames.iterator();
		while(it.hasNext()){
			DmlCheckParamColumn col = it.next();
			
			if(col.getColumnName().equals(req.getColumnName())){
				col.setChecked(true);
			}
		}
	}
	
	public void setColumnNames(String columnName)
	{
		String cols[] = columnName.split(",");
		for(int i = 0; i < cols.length; i++){
			DmlCheckParamColumn col = new DmlCheckParamColumn();
			col.setColumnName(cols[i].trim().toUpperCase());
			
			this.columnNames.add(col);
		}
	}
		
	public String getColumnNames()
	{
		StringBuffer buff = new StringBuffer();
		
		boolean first = true;
		Iterator<DmlCheckParamColumn> it = this.columnNames.iterator();
		while(it.hasNext()){
			DmlCheckParamColumn col = it.next();
			
			if(first){
				first = false;
			}
			else{
				buff.append(",");
			}
			
			buff.append(col.getColumnName());
		}
		
		return buff.toString();
	}
	
	public String getConstrain()
	{
		return constrain;
	}
	public void setConstrain(String constrain)
	{
		this.constrain = constrain;
	}

	public boolean isDeletable()
	{
		Iterator<DmlCheckParamColumn> it = this.columnNames.iterator();
		while(it.hasNext()){
			DmlCheckParamColumn col = it.next();
			
			if(!col.isChecked()){
				return false;
			}
		}
		
		return true;
	}


}

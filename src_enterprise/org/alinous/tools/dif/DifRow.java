package org.alinous.tools.dif;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DifRow {
	private List<IDifField> columns = new ArrayList<IDifField>();
	private boolean eodNext = false;
	
	public void addColumn(IDifField fld)
	{
		this.columns.add(fld);
	}
	
	public boolean isEndOfData()
	{
		if(this.columns.size() == 0){
			return false;
		}
		
		if(this.columns.get(0) instanceof EndOfDataField){
			return true;
		}
		
		return false;
	}
	
	public String toString()
	{
		StringBuffer buff = new StringBuffer();
		
		boolean first = true;
		Iterator<IDifField> it = this.columns.iterator();
		while(it.hasNext()){
			IDifField fld = it.next();
			
			if(first){
				first = false;
			}else{
				buff.append(", ");
			}
			
			buff.append(fld.toString());
		}
		
		return buff.toString();
	}

	public boolean isEodNext() {
		return eodNext;
	}

	public void setEodNext(boolean eodNext) {
		this.eodNext = eodNext;
	}
	
}

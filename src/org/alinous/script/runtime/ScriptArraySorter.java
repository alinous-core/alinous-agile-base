package org.alinous.script.runtime;

import java.util.Comparator;

public class ScriptArraySorter implements Comparator<IScriptVariable>{
	private String sortKey;
	private boolean asc;
	
	public ScriptArraySorter(String sortKey, boolean asc)
	{
		this.sortKey = sortKey;
		this.asc = asc;
	}
	
	@Override
	public int compare(IScriptVariable o1, IScriptVariable o2) {
		if(this.asc){
			return doSort((ScriptDomVariable)o1, (ScriptDomVariable)o2);
		}
		
		return doSort((ScriptDomVariable)o1, (ScriptDomVariable)o2) * -1;
	}
	
	private int doSort(ScriptDomVariable dom1, ScriptDomVariable dom2)
	{
		ScriptDomVariable val1 =  (ScriptDomVariable)dom1.get(this.sortKey);
		ScriptDomVariable val2 =  (ScriptDomVariable)dom2.get(this.sortKey);

		//AlinousDebug.debugOut("doSort() --------------------------- " + this.sortKey);
		//AlinousDebug.debugOut("type : " + val1.getValueType() + " : " + val2.getValueType());
		
		if(val1.getValueType().equals(IScriptVariable.TYPE_NULL) && val2.getValueType().equals(IScriptVariable.TYPE_NULL)){
			return 0;
		}
		else if(val1.getValueType().equals(IScriptVariable.TYPE_NULL) && !val2.getValueType().equals(IScriptVariable.TYPE_NULL)){
			return 1;
		}
		else if(!val1.getValueType().equals(IScriptVariable.TYPE_NULL) && val2.getValueType().equals(IScriptVariable.TYPE_NULL)){
			return -1;
		}
		
		
		if(val1.getValueType().equals(IScriptVariable.TYPE_DOUBLE)){
			double dbl1 = Double.parseDouble(val1.getValue());
			double dbl2 = Double.parseDouble(val2.getValue());
			
			return Double.compare(dbl1, dbl2);
		}
		else if(val1.getValueType().equals(IScriptVariable.TYPE_NUMBER))
		{
			int i1 = Integer.parseInt(val1.getValue());
			int i2 = Integer.parseInt(val2.getValue());
			
			return Integer.compare(i1, i2);
		}
		
		String str1 = val1.getValue();
		String str2 = val2.getValue();
		
		return str1.compareTo(str2);
	}

}

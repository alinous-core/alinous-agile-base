package org.alinous.tools.dif;

public class NumericField extends AbstractDifDataField{
	protected NumericField(String num)
	{
		setType(0);
		setNumeric(num);
	}
	
	public String toString()
	{
		String num = getNumeric();
		
		if(num.endsWith(".")){
			return num.substring(0, num.length() - 1);
		}
		
		return getNumeric();
	}
}

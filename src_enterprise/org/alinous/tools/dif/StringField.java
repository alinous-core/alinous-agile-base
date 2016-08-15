package org.alinous.tools.dif;


public class StringField extends AbstractDifDataField{
	protected StringField(String valStr)
	{
		valStr = setupStr(valStr);
		
		setType(1);
		setStrValue(valStr);
		
	}
	
	private String setupStr(String valStr)
	{
		StringBuffer buff = new StringBuffer();
		
		for(int i = 0; i < valStr.length(); i++){
			if(valStr.charAt(i) != 0){
				buff.append(valStr.charAt(i));
			}
		}
		
		return buff.toString();
	}
	
	public String toString()
	{
		return getStrValue();
	}
}

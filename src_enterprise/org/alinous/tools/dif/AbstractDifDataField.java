package org.alinous.tools.dif;

public abstract class AbstractDifDataField implements IDifField {
	private int type;
	private String numeric;
	private String strValue;
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getNumeric() {
		return numeric;
	}
	public void setNumeric(String numeric) {
		this.numeric = numeric;
	}
	public String getStrValue() {
		return strValue;
	}
	public void setStrValue(String strValue) {
		this.strValue = strValue;
	}
	
	public static AbstractDifDataField createDataField(String first, String second)
	{
		String nums[] = first.split(",");
		
		int type = Integer.parseInt(nums[0].trim());
		String numeric = nums[1].trim();
		
		AbstractDifDataField fld = null;
		
		if(type == 0){
			fld = new NumericField(numeric);
		}else if(type == 1){
			fld = new StringField(second);
		}else{
			if(second.endsWith("BOT")){
				fld = new BotField();
			}
			else if(second.endsWith("EOD")){
				fld = new EndOfDataField();
			}
		}
		
		return fld;
	}
}

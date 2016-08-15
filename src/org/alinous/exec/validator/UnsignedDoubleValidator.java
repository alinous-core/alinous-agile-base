package org.alinous.exec.validator;

import org.alinous.exec.pages.IParamValue;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.script.runtime.VariableRepository;

public class UnsignedDoubleValidator implements IValidator{
	public void setFormName(String formName)
	{
			
	}

	public void setInputName(String inputName)
	{
		
	}

	public void setRegExp(String regExp)
	{
				
	}

	public boolean validate(IParamValue param, PostContext context, VariableRepository valRepo,boolean isArray) throws AlinousException
	{
		if(param == null || param.toString().length() == 0){
			return true;
		}

		String str = param.toString();
		
		for(int i = 0; i < str.length() - 1; i++){
			char ch = str.charAt(i);
			
			if((ch < '0' || ch > '9') && (ch != '.' && ch != 'D' && ch != 'd')){
				return false;
			}
		}
		
		double db = 0d;
		try{
			db = Double.parseDouble(str);
		}catch(Throwable e){
			return false;
		}
		
		if(db < 0){
			return false;
		}
		
		return true;
	}
}

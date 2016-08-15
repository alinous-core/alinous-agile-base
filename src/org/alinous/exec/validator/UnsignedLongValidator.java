package org.alinous.exec.validator;

import org.alinous.exec.pages.IParamValue;
import org.alinous.exec.pages.PostContext;
/**
 * CROSSFIRE JAPAN INCORPORATED
 * This source code is under GPL License.
 * info@crossfire.jp
 * Official web site
 * http://alinous.org
 * 
 *  Copyright (C) 2007 Tomohiro Iizuka
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import org.alinous.expections.AlinousException;
import org.alinous.script.runtime.VariableRepository;

public class UnsignedLongValidator implements IValidator
{

	
	public void setFormName(String formName)
	{
		
	}

	
	public void setInputName(String inputName)
	{
		
	}

	
	public void setRegExp(String regExp)
	{
		
	}

	
	public boolean validate(IParamValue param, PostContext context,VariableRepository valRepo,
			boolean isArray) throws AlinousException
	{
		if(param == null || param.toString().length() == 0){
			return true;
		}

		String str = param.toString();
		
		for(int i = 0; i < str.length(); i++){
			char ch = str.charAt(i);
			
			if(ch < '0' || ch > '9'){
				return false;
			}
		}
		
		try{
			long num = Long.parseLong(param.toString());
			if(num < 0){
				return false;
			}
		}
		catch(NumberFormatException e){
			return false;
		}

		return true;
	}

}

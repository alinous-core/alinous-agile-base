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
package org.alinous.exec.validator;

import java.io.StringReader;
import java.util.HashMap;

import org.alinous.exec.pages.AlinousExecutableModule;
import org.alinous.exec.pages.IParamValue;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.objects.AlinousAttrs;
import org.alinous.objects.html.InputTagObject;
import org.alinous.parser.script.attr.AlinousAttrScriptParser;
import org.alinous.script.attribute.ParsedAttribute;
import org.alinous.script.runtime.VariableRepository;

public class ValidationRequest
{
	private String validatorName;
	private String inputName;
	private String formName;
	private String regExp;
	
	private String validateIf;
	
	private boolean array;
	
	private IValidator validator;
	
	private ValidationRequest()
	{
		
	}
	
	public static ValidationRequest getRequest(String paramName, IParamValue validationType, 
						HashMap<String, IParamValue> params) throws ExecutionException
	{
		if(!paramName.startsWith(InputTagObject.FORM_HIDDEN_VALIDATOR)){
			return null;
		}
		
		String inputString = paramName.substring(InputTagObject.FORM_HIDDEN_VALIDATOR.length());
		
		String valInfo[] = inputString.split(";");
		if(valInfo.length != 3){
			throw new ExecutionException("Validation Hidden's format is wrong."); // i18n
		}
		
		ValidationRequest req = new ValidationRequest();
		req.setValidatorName(validationType.toString());
		req.setInputName(valInfo[1]);
		req.setFormName(valInfo[0]);
		
				
		// regex
		String regexKey = AlinousAttrs.ALINOUS_REGEX + ":" + valInfo[0] + ";" + valInfo[1];
		IParamValue alnsRegEx = params.get(regexKey);
		if(alnsRegEx != null){
			req.setRegExp(alnsRegEx.toString());
		}
		
		// validate if
		String validateIfName = AlinousAttrs.ALINOUS_VALIDATE_IF + ":" + valInfo[0] + ";" + valInfo[1];
		IParamValue alnsValidateIf = params.get(validateIfName);
		if(alnsValidateIf != null){
			// 
			//AlinousDebug.debugOut("ALINOUS_VALIDATE_IF -> " + alnsValidateIf.toString());
			
			req.setValidateIf(alnsValidateIf.toString());
		}
		
		
		return req;
	}
	
	public boolean validate(PostContext context, VariableRepository valRepo) throws AlinousException
	{
		this.validator = ValidatorFactory.getValidator(this.validatorName);
		
		if(validator == null){
			return true;
		}
		
		if(!validateCondition(context, valRepo, this.formName, this.inputName)){
			return true;
		}
		
		// setup
		this.validator.setRegExp(this.regExp);
		this.validator.setFormName(this.formName);
		this.validator.setInputName(this.inputName);
		
		IParamValue formValue = context.getParams(this.inputName);
		
		boolean result = this.validator.validate(formValue, context, valRepo, this.array);	
		
		return result;
	}
	
	public boolean isCustom()
	{
		return this.validatorName.equals(IValidator.VALIDATOR_CUSTOM);
	}
	
	public boolean validateCondition(PostContext context, VariableRepository valRepo,
						String formName, String inputName) throws ExecutionException, RedirectRequestException
	{
		if(this.validateIf == null){
			return true;
		}
		
		AlinousExecutableModule.setupVariableRepository(context, valRepo, inputName);
		
		
		StringReader reader = new StringReader("<" + this.validateIf + ">");
		
		boolean bl = true;
		AlinousAttrScriptParser parser = new AlinousAttrScriptParser(reader);
		try {
			ParsedAttribute attr = parser.parse();
			
			bl = attr.evaluate(context, valRepo);
		} catch (Throwable e) {
			e.printStackTrace();
			
			reader.close();
			return true;
		}
		reader.close();
		
		
		return bl;
	}
	
	
	public String getInputName()
	{
		return inputName;
	}
	
	public void setInputName(String inputName)
	{
		if(inputName.endsWith("[]")){
			this.inputName = inputName.substring(0, inputName.length() - "[]".length());
			this.array = true;
			return;
		}
		
		this.inputName = inputName;
	}
	
	public String getValidatorName()
	{
		return validatorName;
	}
	
	public void setValidatorName(String validatorName)
	{
		this.validatorName = validatorName;
	}

	public String getRegExp()
	{
		return regExp;
	}

	public void setRegExp(String regExp)
	{
		this.regExp = regExp;
	}

	public String getFormName()
	{
		return formName;
	}

	public void setFormName(String formName)
	{
		this.formName = formName;
	}

	public IValidator getValidator()
	{
		return validator;
	}

	public void setValidator(IValidator validator)
	{
		this.validator = validator;
	}
	
	public boolean isArray()
	{
		return this.array;
	}

	public String getValidateIf() {
		return validateIf;
	}

	public void setValidateIf(String validateIf) {
		this.validateIf = validateIf;
	}
}

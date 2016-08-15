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
package org.alinous.script.validator;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.alinous.AlinousUtils;
import org.alinous.exec.pages.IParamValue;
import org.alinous.exec.pages.PostContext;
import org.alinous.exec.validator.IValidator;
import org.alinous.exec.validator.ValidatorFactory;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.expections.ServerValidationException;
import org.alinous.objects.AlinousAttrs;
import org.alinous.objects.IAttribute;
import org.alinous.objects.XMLTagBase;
import org.alinous.objects.html.InputTagObject;
import org.alinous.objects.html.SelectTagObject;
import org.alinous.objects.html.TextAreaTagObject;
import org.alinous.parser.script.attr.AlinousAttrScriptParser;
import org.alinous.script.attribute.ParsedAttribute;
import org.alinous.script.runtime.VariableRepository;

public class ServerValidationRequest {
	private String validatorType;
	private String regexp;
	private String inputName;
	private String formName;
	private String action;
	private String alinousValidateIf;
	
	private boolean array = false;
	
	public static List<ServerValidationRequest> createRequest(XMLTagBase tag, String formName, String frmAction, PostContext context, VariableRepository valRepo)
	{
		List<ServerValidationRequest> reqList = null;
		
		if(tag instanceof InputTagObject){
			reqList = getValidators(tag, formName, frmAction, context, valRepo);
		}else if(tag instanceof SelectTagObject){
			reqList = getValidators(tag, formName,frmAction, context, valRepo);
		}else if(tag instanceof TextAreaTagObject){
			reqList = getValidators(tag, formName,frmAction, context, valRepo);
		}
		
		return reqList;
	}
	
	private static List<ServerValidationRequest> getValidators(XMLTagBase tag, String formName, String frmAction,
			PostContext context, VariableRepository valRepo)
	{
		IAttribute attr = tag.getAlinousAttribute(AlinousAttrs.ALINOUS_VALIDATE_TYPE);
		if(attr == null){
			return null;
		}
		String value = attr.getValue().getParsedValue(context, valRepo);
		if(value == null || value.equals("")){
			return null;
		}
		
		List<ServerValidationRequest> reqList = new ArrayList<ServerValidationRequest>();
		String validatorTypes[] = value.split(",");
		for(int i = 0; i < validatorTypes.length; i++){
			ServerValidationRequest req = handleInputTag(tag, formName, frmAction, validatorTypes[i].trim(), context, valRepo);
			
			if(req != null){
				reqList.add(req);
			}
		}
		
		return reqList;
	}
	
	private static ServerValidationRequest handleInputTag(XMLTagBase tag, String formName, String frmAction,
				String validatorName, PostContext context, VariableRepository valRepo)
	{
		ServerValidationRequest req = new ServerValidationRequest();
		req.setValidatorType(validatorName);
		
		IAttribute attr = tag.getAlinousAttribute(AlinousAttrs.ALINOUS_REGEX);
		if(attr != null){
			String value = attr.getValue().getParsedValue(context, valRepo);
			req.setRegexp(value);
		}
		
		attr = tag.getAttribute("name");
		if(attr == null){
			return null;
		}
		
		req.setAction(frmAction);
		
		String name = attr.getValue().getParsedValue(context, valRepo);
		req.setInputName(name);
		
		req.setFormName(formName);
		
		// validate if
		IAttribute validateIfAttr = tag.getAlinousAttribute(AlinousAttrs.ALINOUS_VALIDATE_IF);
		if(validateIfAttr != null){
			//AlinousDebug.debugOut("set server validator : " + validateIfAttr.getValue().getValue());
			
			req.setAlinousValidateIf(validateIfAttr.getValue().getValue());
		}
		
		return req;
	}

	public String getValidatorType()
	{
		return validatorType;
	}

	public void setValidatorType(String validatorType)
	{
		this.validatorType = validatorType;
	}

	public String getRegexp()
	{
		return regexp;
	}

	public void setRegexp(String regexp)
	{
		this.regexp = regexp;
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

	public String getFormName()
	{
		return formName;
	}

	public void setFormName(String formName)
	{
		this.formName = formName;
	}

	public String getAction()
	{
		return action;
	}

	public void setAction(String action)
	{
		this.action = action;
	}

	public boolean isArray()
	{
		return array;
	}

	public void setArray(boolean array)
	{
		this.array = array;
	}

	@Override
	public String toString()
	{
		return this.validatorType + " : " + this.inputName;
	}
	
	public void executeValidation(PostContext context, VariableRepository valRepo, String validationHtmlPath) throws ExecutionException, RedirectRequestException
	{
		IValidator validator = ValidatorFactory.getValidator(this.validatorType);
		
		if(validator == null){
			return;
		}
		
		if(this.alinousValidateIf != null){
			if(!validateCondition(context, valRepo)){
				return;
			}
		}
		
		validator.setFormName(this.formName);
		validator.setInputName(this.inputName);
		validator.setRegExp(this.regexp);
		
		// actual params
		IParamValue param = context.getParams(this.inputName);
		
		// context for validation
		PostContext context4Validation = new PostContext(context.getCore(), context.getUnit());
		context4Validation.initHttpHeaders(context.getHttpHeaders());
		context4Validation.initIncludes(context);
		context4Validation.initParams(context);
		
		String requestPath = this.action;
		if(!requestPath.startsWith("/")){
			String home = AlinousUtils.getDirectory(validationHtmlPath);
			requestPath = home + this.action;
		}
		context4Validation.setRequestPath(requestPath);
		
		boolean result = false;
		try {
			result = validator.validate(param, context4Validation, valRepo, this.array);
		} catch (AlinousException e) {
			throw new ExecutionException(e, "Failed in executing validator."); // i18n
		} finally{
			context4Validation.dispose();
		}
		
		if(!result){
			throw new ServerValidationException(this, "Failed in server side validation");
		}
	}
	
	public boolean validateCondition(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		if(this.alinousValidateIf == null){
			return true;
		}
		
		StringReader reader = new StringReader("<" + this.alinousValidateIf + ">");
		
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
	public String getAlinousValidateIf() {
		return alinousValidateIf;
	}

	public void setAlinousValidateIf(String alinousValidateIf) {
		this.alinousValidateIf = alinousValidateIf;
	}
}

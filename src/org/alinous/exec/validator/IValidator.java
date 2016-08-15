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

import org.alinous.exec.pages.IParamValue;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.script.runtime.VariableRepository;

public interface IValidator
{
	public static final String VALIDATOR_NOT_NULL = "notnull";
	public static final String VALIDATOR_MAX_LENGTH = "maxlength";
	public static final String VALIDATOR_EMAIL = "email";
	public static final String VALIDATOR_INT = "int";
	public static final String VALIDATOR_UINT = "uint";
	public static final String VALIDATOR_LONG = "long";
	public static final String VALIDATOR_ULONG = "ulong";
	public static final String VALIDATOR_NUMBER = "number";
	
	public static final String VALIDATOR_DOUBLE = "double";
	public static final String VALIDATOR_UDOUBLE = "udouble";
	public static final String VALIDATOR_INT_RANGE = "intrange";
	public static final String VALIDATOR_STR_RANGE = "strrange";
	public static final String VALIDATOR_REGEX = "regex";
	public static final String VALIDATOR_NOT_HANKAKU = "nothankaku";
	
	
	public static final String VALIDATOR_CUSTOM = "custom";
	
	public static final String VALIDATOR_DUMMY = "dummy";
	
	public static final String VARIABLE_NAME = "VALIDATE";
	
	public void setRegExp(String regExp);
	public void setFormName(String formName);
	public void setInputName(String inputName);
	
	public boolean validate(IParamValue param, PostContext context, VariableRepository valRepo, boolean isArray) throws AlinousException;
	
}

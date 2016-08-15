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
package org.alinous.objects.validate;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.alinous.objects.AlinousAttrs;
import org.alinous.objects.IAttribute;
import org.alinous.objects.XMLTagBase;

public class AlinousAttributeValidator
{
	private XMLTagBase tag;
	private Hashtable<String, IAttribute> alinousAttributes;
	
	public AlinousAttributeValidator(XMLTagBase tag, Hashtable<String, IAttribute> alinousAttributes)
	{
		this.tag = tag;
		this.alinousAttributes = alinousAttributes;
	}
	
	public void validate(List<ValidationError> errors)
	{		
		Iterator<String> it = this.alinousAttributes.keySet().iterator();
		while(it.hasNext()){
			String attr = it.next();
			
			if(attr.equals(AlinousAttrs.ALINOUS_FORM)){
				validateAlnsForm(errors);
			}
			else if(attr.equals(AlinousAttrs.ALINOUS_INNER)){
				validateAlnsInner(errors);
			}
			else if(attr.equals(AlinousAttrs.ALINOUS_ITERATE)){
				validateAlnsIterate(errors);
			}
			else if(attr.equals(AlinousAttrs.ALINOUS_MSG)){
				validateAlnsMsg(errors);
			}
			else if(attr.equals(AlinousAttrs.ALINOUS_REGEX)){
				validateAlnsRegex(errors);
			}
			else if(attr.equals(AlinousAttrs.ALINOUS_TAGID)){
				validateAlnsTagid(errors);
			}
			else if(attr.equals(AlinousAttrs.ALINOUS_VALIDATE_TYPE)){
				
			}
			else if(attr.equals(AlinousAttrs.ALINOUS_VARIABLE)){
				validateAlnsVariable(errors);
			}
		}

	}
	
	private void validateAlnsVariable(List<ValidationError> errors)
	{
		List<String> list = checkExistsAll(new String[]{AlinousAttrs.ALINOUS_ITERATE});
		
		handleErrorFromList(list, errors);
	}
	
	private void validateAlnsTagid(List<ValidationError> errors)
	{
		List<String> list = checkExistsAll(new String[]{AlinousAttrs.ALINOUS_INNER});
		
		handleErrorFromList(list, errors);
	}
	
	private void validateAlnsRegex(List<ValidationError> errors)
	{
		List<String> list = checkExistsAll(new String[]{AlinousAttrs.ALINOUS_VALIDATE_TYPE});
		
		handleErrorFromList(list, errors);
	}
	
	private void validateAlnsMsg(List<ValidationError> errors)
	{
		List<String> list = checkExistsAll(new String[]{AlinousAttrs.ALINOUS_VALIDATE_TYPE, 
				AlinousAttrs.ALINOUS_FORM});
		
		handleErrorFromList(list, errors);
	}
	
	private void validateAlnsIterate(List<ValidationError> errors)
	{
		List<String> list = checkExistsAll(new String[]{AlinousAttrs.ALINOUS_VARIABLE});
		
		handleErrorFromList(list, errors);
	}
	
	private void validateAlnsInner(List<ValidationError> errors)
	{
		List<String> list = checkExistsAll(new String[]{AlinousAttrs.ALINOUS_TAGID});
		
		handleErrorFromList(list, errors);
	}
	
	private void validateAlnsForm(List<ValidationError> errors)
	{
		List<String> list = checkExistsAll(new String[]{AlinousAttrs.ALINOUS_MSG, AlinousAttrs.ALINOUS_VALIDATE_TYPE});
	
		handleErrorFromList(list, errors);
	}
	
	private void handleErrorFromList(List<String> list, List<ValidationError> errors)
	{
		if(list.size() > 0){
			StringBuffer buff = new StringBuffer();
			
			
			Iterator<String> it = list.iterator();
			
			while(it.hasNext()){
				String str = it.next();
				
				buff.append(str);
				buff.append(" ");
			}
			
			buff.append("is necessary."); // i18n
			
			ValidationError err = new ValidationError(this.tag.getLine(), buff.toString());
			errors.add(err);
		}
	}
	
	private List<String> checkExistsAll(String attrs[])
	{
		List<String> notExistList = new ArrayList<String>();
		
		for(int i = 0; i < attrs.length; i++){
			String attrStr = attrs[i];
			
			IAttribute attr = this.alinousAttributes.get(attrStr);
			if(attr == null){
				notExistList.add(attrStr);
			}
		}
		
		return notExistList;
	}
}

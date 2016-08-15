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

public class HankakuValidator implements IValidator{

	public void setFormName(String formName) {
	
	}

	public void setInputName(String inputName) {
		
	}

	public void setRegExp(String regExp) {
	
	}

	public boolean validate(IParamValue param, PostContext context,VariableRepository valRepo,
			boolean isArray) throws AlinousException {
		// validate
		if(param == null || param.toString().length() == 0){
			return true;
		}

		String str = param.toString();
		char chArray[] =  str.toCharArray();
		for(int i = 0; i < chArray.length; i++){
			if(matchHankaku(chArray[i])){
				return false;
			}
		}
		
		return true;
	}
	
	private boolean matchHankaku(char ch)
	{
		for(int i = 0; i < HANKAKU_KATAKANA.length; i++){
			if(ch == HANKAKU_KATAKANA[i]){
				return true;
			}
		}
		
		return false;
	}
	
	private static final char[] HANKAKU_KATAKANA = { '｡', '｢', '｣', '､', '･',
		'ｦ', 'ｧ', 'ｨ', 'ｩ', 'ｪ', 'ｫ', 'ｬ', 'ｭ', 'ｮ', 'ｯ', 'ｰ', 'ｱ', 'ｲ',
		'ｳ', 'ｴ', 'ｵ', 'ｶ', 'ｷ', 'ｸ', 'ｹ', 'ｺ', 'ｻ', 'ｼ', 'ｽ', 'ｾ', 'ｿ',
		'ﾀ', 'ﾁ', 'ﾂ', 'ﾃ', 'ﾄ', 'ﾅ', 'ﾆ', 'ﾇ', 'ﾈ', 'ﾉ', 'ﾊ', 'ﾋ', 'ﾌ',
		'ﾍ', 'ﾎ', 'ﾏ', 'ﾐ', 'ﾑ', 'ﾒ', 'ﾓ', 'ﾔ', 'ﾕ', 'ﾖ', 'ﾗ', 'ﾘ', 'ﾙ',
		'ﾚ', 'ﾛ', 'ﾜ', 'ﾝ', 'ﾞ', 'ﾟ' };
}

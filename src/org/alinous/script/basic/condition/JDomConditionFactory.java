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
package org.alinous.script.basic.condition;

import org.alinous.expections.AlinousException;
import org.jdom.Element;

public class JDomConditionFactory
{
	public static IScriptCondition createConditionFromJDomElement(Element cond) throws AlinousException
	{
		String className = cond.getAttributeValue(IScriptCondition.ATTR_COND_CLASS);
		
		if(className == null){
			return null;
		}
		
		IScriptCondition exec = null;
		try {
			exec = (IScriptCondition)Class.forName(className).newInstance();
		} catch (InstantiationException e) {
			throw new AlinousException(e, "InstantiationException occur : " + className);
		} catch (IllegalAccessException e) {
			throw new AlinousException(e, "IllegalAccessException occur : " + className);
		} catch (ClassNotFoundException e) {
			throw new AlinousException(e, "ClassNotFoundException occur : " + className);
		}
		
		return exec;
	}
}

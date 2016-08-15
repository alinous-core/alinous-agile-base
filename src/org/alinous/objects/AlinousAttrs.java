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
package org.alinous.objects;

public class AlinousAttrs
{
	public static final String ALINOUS_IF = "alns:if";
	public static final String ALINOUS_INNER = "alns:inner";
	public static final String ALINOUS_TAGID = "alns:tagid";
	public static final String ALINOUS_TARGET = "alns:target";
	public static final String ALINOUS_ITERATE = "alns:iterate";
	public static final String ALINOUS_VARIABLE = "alns:variable";
	public static final String ALINOUS_MSG = "alns:msg";
	public static final String ALINOUS_FORM = "alns:form";
	
	public static final String ALINOUS_BACK = "alns:back";
	
	public static final String ALINOUS_VALIDATE_TYPE = "alns:validate";
	public static final String ALINOUS_VALIDATE_IF = "alns:validateif";
	public static final String ALINOUS_REGEX = "alns:regexp";
	public static final String ALINOUS_PARAM = "alns:param";
	public static final String ALINOUS_TYPE = "alns:type";
	public static final String ALINOUS_NO_FORM_CACHE = "alns:nocache";
	
	public static final String ALINOUS_IGNOREBLANK = "alns:ignoreblank";
	
	public static final String ALINOUS_IGNORE_SELF_ITERATE = "alns:ignoreself";
	public static final String ALINOUS_EXTRACT = "alns:extract";
	
	public static final String ALINOUS_COMPONENT = "alns:cmp";
	public static final String ALINOUS_COMPONENT_ID = "alns:cmpid";
	public static final String ALINOUS_COMPONENT_WIDTH = "alns:cmpwidth";
	public static final String ALINOUS_COMPONENT_EDIT = "alns:cmpedit";
	public static final String ALINOUS_COMPONENT_VIEW = "alns:cmpvew";
	public static final String ALINOUS_COMPONENT_VIEWTYPE = "alns:cmpvewtype";
	public static final String ALINOUS_COMPONENT_ROOT = "alns:cmproot";
	
	public static final String VALUE_TRUE = "true";
	public static final String VALUE_FALSE = "false";
	
	public static final String VALUE_VIEWTYPE_FRAME = "frame";
	public static final String VALUE_VIEWTYPE_INNER = "inner";
	
	// enable form
	public static final String ALINOUS_ALINOUS_FORM = "alns:alinousform";
	
	public static boolean isAlinousAttr(IAttribute attr)
	{
		String attrStr = attr.getKey().toLowerCase();
		
		return attrStr.equals(ALINOUS_IF)	||
				attrStr.equals(ALINOUS_INNER) ||
				attrStr.equals(ALINOUS_TAGID) ||
				attrStr.equals(ALINOUS_TARGET) ||
				attrStr.equals(ALINOUS_ITERATE) ||
				attrStr.equals(ALINOUS_VARIABLE) ||
				attrStr.equals(ALINOUS_MSG) ||
				attrStr.equals(ALINOUS_FORM) ||
				
				attrStr.equals(ALINOUS_BACK)	||
				attrStr.equals(ALINOUS_VALIDATE_TYPE)	||
				attrStr.equals(ALINOUS_VALIDATE_IF)	||
				attrStr.equals(ALINOUS_REGEX)	||
				attrStr.equals(ALINOUS_PARAM)	||
				attrStr.equals(ALINOUS_TYPE)	||
				attrStr.equals(ALINOUS_NO_FORM_CACHE)	||
				
				attrStr.equals(ALINOUS_IGNOREBLANK) ||
				
				attrStr.equals(ALINOUS_IGNORE_SELF_ITERATE) ||
				attrStr.equals(ALINOUS_EXTRACT) ||
				
				attrStr.equals(ALINOUS_COMPONENT) ||
				attrStr.equals(ALINOUS_COMPONENT_ID) ||
				attrStr.equals(ALINOUS_COMPONENT_WIDTH) ||
				attrStr.equals(ALINOUS_COMPONENT_EDIT) ||
				attrStr.equals(ALINOUS_COMPONENT_VIEW) ||
				attrStr.equals(ALINOUS_COMPONENT_VIEWTYPE) ||
				attrStr.equals(ALINOUS_COMPONENT_ROOT) ||
				attrStr.equals(ALINOUS_ALINOUS_FORM);
	}
}

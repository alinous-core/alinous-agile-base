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
package org.alinous.script.runtime;

import org.alinous.script.basic.type.IStatement;
import org.jdom.Element;

public interface IScriptVariable extends IStatement
{
	static final String TYPE_STRING = "STRING";
	static final String TYPE_HASH = "HASH";
	static final String TYPE_ARRAY = "ARRAY";
	static final String TYPE_BOOLEAN = "BOOLEAN";
	static final String TYPE_NUMBER = "NUMBER";
	static final String TYPE_DOUBLE = "DOUBLE";
	static final String TYPE_TIMESTAMP = "TIMESTAMP";
	static final String TYPE_DATE = "DATE";
	static final String TYPE_NULL	= "NULL";
	
	static final String TYPE_REPOSITORY = "REPOSITORY";
	
	public String getName();
	public void setName(String name);
	public String getType();
	
	public IScriptVariable get(String key);
	
	
	public void exportIntoJDomElement(Element parent);
	public void exportIntoJDomElement(Element parent, String alias);
	public void importFromJDomElement(Element element);
	
	public Object clone() throws CloneNotSupportedException;
	
	public IScriptVariable getParent();
	public void setParent(IScriptVariable parent);
	public int getParentArrayIndex();
	public void setParentArrayIndex(int parentArrayIndex);
	
	public IPathElement getPath();

}

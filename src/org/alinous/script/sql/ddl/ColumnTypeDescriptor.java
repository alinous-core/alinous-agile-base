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
package org.alinous.script.sql.ddl;

import org.alinous.datasrc.types.TypeHelper;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.ISQLScriptObject;
import org.alinous.script.sql.adjustopt.AdjustSet;
import org.alinous.script.sql.adjustopt.AdjustWhere;
import org.alinous.script.sql.statement.ISQLStatement;

public class ColumnTypeDescriptor implements ISQLScriptObject
{
	private String typeName;
	private ISQLStatement length;
		
	public ISQLStatement getLength()
	{
		return length;
	}
	public void setLength(ISQLStatement length)
	{
		this.length = length;
	}
	public String getTypeName()
	{
		return typeName;
	}
	public void setTypeName(String typeName)
	{
		this.typeName = typeName;
	}

	public String extract(PostContext context, VariableRepository valRepo, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		StringBuffer buff = new StringBuffer();
		
		buff.append(this.typeName);
		
		if(this.length != null){
			buff.append(" (");
			buff.append(this.length.extract(context, valRepo, adjustWhere, adjustSet, helper));
			buff.append(")");
		}
		
		return buff.toString();
	}
	
	public boolean isReady(PostContext context, VariableRepository valRepo, AdjustWhere adjustWhere) throws ExecutionException
	{
		if(this.length == null){
			return true;
		}
		
		return this.length.isReady(context, valRepo, adjustWhere);
	}
	
	public boolean hasLength(PostContext context, VariableRepository valRepo)
	{
		return this.length != null;
	}
	
}

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

public class DdlColumnDescriptor implements ISQLScriptObject
{
	private ColumnTypeDescriptor typeDescriptor;
	private String name;
	private ISQLStatement defaultValue;
	private boolean unique;
	private boolean notnull;
	private CheckDefinition check;
	private int line;
	private int linePosition;
	
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public ColumnTypeDescriptor getTypeDescriptor()
	{
		return typeDescriptor;
	}
	public void setTypeDescriptor(ColumnTypeDescriptor typeDescriptor)
	{
		this.typeDescriptor = typeDescriptor;
	}
	public ISQLStatement getDefaultValue()
	{
		return defaultValue;
	}
	public void setDefaultValue(ISQLStatement defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	public String extract(PostContext context, VariableRepository valRepo, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		StringBuffer buff = new StringBuffer();
		
		buff.append(this.name);
		buff.append(" ");
		
		buff.append(this.typeDescriptor.extract(context, valRepo, adjustWhere, adjustSet, helper));
		
		if(this.defaultValue != null && this.defaultValue.isReady(context, valRepo, adjustWhere)){
			buff.append(" default ");
			buff.append(this.defaultValue.extract(context, valRepo, adjustWhere, adjustSet, helper));
		}
		
		return buff.toString();
	}
	
	public boolean isReady(PostContext context, VariableRepository valRepo, AdjustWhere adjustWhere) throws ExecutionException
	{
		return this.typeDescriptor.isReady(context, valRepo, adjustWhere);
	}
	public boolean isUnique()
	{
		return unique;
	}
	public void setUnique(boolean unique)
	{
		this.unique = unique;
	}
	public boolean isNotnull()
	{
		return notnull;
	}
	public void setNotnull(boolean notnull)
	{
		this.notnull = notnull;
	}
	public CheckDefinition getCheck()
	{
		return check;
	}
	public void setCheck(CheckDefinition check)
	{
		this.check = check;
	}
	public int getLine() {
		return line;
	}
	public void setLine(int line) {
		this.line = line;
	}
	public int getLinePosition() {
		return linePosition;
	}
	public void setLinePosition(int linePosition) {
		this.linePosition = linePosition;
	}
	
	
}

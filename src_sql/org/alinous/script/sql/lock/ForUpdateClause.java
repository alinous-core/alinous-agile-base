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
package org.alinous.script.sql.lock;

import org.alinous.datasrc.types.TypeHelper;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.ISQLScriptObject;
import org.alinous.script.sql.adjustopt.AdjustSet;
import org.alinous.script.sql.adjustopt.AdjustWhere;
import org.alinous.script.sql.other.TablesList;

public class ForUpdateClause  implements ISQLScriptObject
{
	private String type = "UPDATE"; // UPDATE or SHARE
	
	private String wait; // NOWAIT
	//OF TABLES
	private TablesList tables;
	
	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getWait()
	{
		return wait;
	}

	public void setWait(String wait)
	{
		this.wait = wait;
	}

	public String extract(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		StringBuffer buff = new StringBuffer();
		
		buff.append("FOR ");
		buff.append(this.type);
		
		if(this.tables != null){
			buff.append(" OF ");
			buff.append(this.tables.extract(context, providor, adjustWhere, adjustSet, helper));
		}
		
		if(this.wait != null){
			buff.append(" ");
			buff.append(this.wait);
		}
		
		return buff.toString();
	}

	public boolean isReady(PostContext context, VariableRepository providor, AdjustWhere adjustWhere) throws ExecutionException
	{
		return true;
	}

	public TablesList getTables()
	{
		return tables;
	}

	public void setTables(TablesList tables)
	{
		this.tables = tables;
	}
	
	
}

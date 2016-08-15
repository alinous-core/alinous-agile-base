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
package org.alinous.script.sql;

import org.alinous.datasrc.types.TypeHelper;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.adjustopt.AdjustSet;
import org.alinous.script.sql.adjustopt.AdjustWhere;
import org.alinous.script.sql.statement.ISQLStatement;


public class LimitOffsetClause implements IClause
{
	private ISQLStatement limit;
	private ISQLStatement offset;

	public String extract(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		StringBuffer buffer = new StringBuffer();
		
		if(limit != null && this.limit.extract(context, providor, adjustWhere, adjustSet, helper) != null){
			buffer.append("LIMIT ");
			buffer.append(this.limit.extract(context, providor, adjustWhere, adjustSet, helper));
		}
		if(this.offset != null){
			buffer.append(" OFFSET ");
			buffer.append(this.offset.extract(context, providor, adjustWhere, adjustSet, helper));
		}
		
		return buffer.toString();
	}

	public ISQLStatement getLimit()
	{
		return limit;
	}

	public void setLimit(ISQLStatement limit)
	{
		this.limit = limit;
	}

	public ISQLStatement getOffset()
	{
		return offset;
	}

	public void setOffset(ISQLStatement offset)
	{
		this.offset = offset;
	}

	public boolean isReady(PostContext context, VariableRepository providor, AdjustWhere adjustWhere) throws ExecutionException
	{
		if(this.limit != null || this.offset != null){
			return true;
		}
		return false;
	}
	
	

}

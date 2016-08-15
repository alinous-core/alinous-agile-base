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
package org.alinous.script.sql.condition;

import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.datasrc.types.TypeHelper;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.adjustopt.AdjustSet;
import org.alinous.script.sql.adjustopt.AdjustWhere;
import org.alinous.script.sql.statement.ISQLStatement;

public class BetweenClauseExpression implements ISQLExpression
{
	private ISQLStatement top;
	private ISQLStatement left;
	private ISQLStatement right;	
	
	public boolean isReady(PostContext context, VariableRepository providor, AdjustWhere adjustWhere) throws ExecutionException
	{
		if(adjustWhere == null || !adjustWhere.adjust()){
			return true;
		}
		
		return this.top.isReady(context, providor, adjustWhere)
			&& this.left.isReady(context, providor, adjustWhere)
			&& this.right.isReady(context, providor, adjustWhere);
	}

	public String extract(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		// type valification
		String topStr = this.top.extract(context, providor, adjustWhere, adjustSet, helper);
		
		String fieldType;
		try {
			fieldType = helper.getDataFieldType(topStr, context);
		} catch (DataSourceException e) {
			throw new ExecutionException(e, "Failed in extract IN clause."); // i18n
		}
		if(fieldType == null){
			return defaultExtract(context, providor, adjustWhere, adjustSet, helper);
		}
		
		TypeHelper helper2 = helper.newHelper(false);
		helper2.setFieldType(fieldType);
		
		return topStr 
			+ " BETWEEN " + this.left.extract(context, providor, adjustWhere, adjustSet, helper2) 
			+ " AND " + this.right.extract(context, providor, adjustWhere, adjustSet, helper2);
	}

	public String defaultExtract(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
		AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		return this.top.extract(context, providor, adjustWhere, adjustSet, helper) 
			+ " BETWEEN " + this.left.extract(context, providor, adjustWhere, adjustSet, helper) 
			+ " AND " + this.right.extract(context, providor, adjustWhere, adjustSet, helper);
	}
	
	public ISQLStatement getLeft()
	{
		return left;
	}

	public void setLeft(ISQLStatement left)
	{
		this.left = left;
	}

	public ISQLStatement getRight()
	{
		return right;
	}

	public void setRight(ISQLStatement right)
	{
		this.right = right;
	}

	public ISQLStatement getTop()
	{
		return top;
	}

	public void setTop(ISQLStatement top)
	{
		this.top = top;
	}

	
}

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
import org.alinous.script.sql.statement.ColumnIdentifier;
import org.alinous.script.sql.statement.ISQLStatement;
import org.alinous.script.sql.statement.Identifier;
import org.alinous.script.sql.statement.SQLStatement;


public class TwoClauseExpression implements ISQLExpression
{
	private ISQLStatement left;
	private ISQLStatement right;
	private String ope;
	
	//private AdjustWhere adjustWhere;
	
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
	
	public String getOpe()
	{
		return ope;
	}
	
	public void setOpe(String ope)
	{
		this.ope = ope;
	}
	
	
	public String extract(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		String leftStr;
		String rightStr;
		if(isColumnName(this.left) && isColumnName(this.right)){
			return defaultExtract(context, providor, adjustWhere, adjustSet, helper);
		}
		else if(!isColumnName(this.left) && isColumnName(this.right) && helper != null){
			rightStr = this.right.extract(context, providor, adjustWhere, adjustSet, helper);
			String fieldType;
			try {
				fieldType = helper.getDataFieldType(rightStr, context);
			} catch (DataSourceException e) {
				throw new ExecutionException(e, "Failed in extracting expression.");
			}
			
			TypeHelper helper2 = helper.newHelper(false);
			helper2.setFieldType(fieldType);
			
			leftStr = this.left.extract(context, providor, adjustWhere, adjustSet, helper2);
		}
		else if(isColumnName(this.left) && !isColumnName(this.right) && helper != null){
			leftStr = this.left.extract(context, providor, adjustWhere, adjustSet, helper);
			String fieldType;
			try {
				fieldType = helper.getDataFieldType(leftStr, context);
			} catch (DataSourceException e) {
				throw new ExecutionException(e, "Failed in extracting expression.");
			}
			
			TypeHelper helper2 = helper.newHelper(false);
			helper2.setFieldType(fieldType);
			
			rightStr = this.right.extract(context, providor, adjustWhere, adjustSet, helper2);
		}
		else{
			return defaultExtract(context, providor, adjustWhere, adjustSet, helper);
		}
		
		if(rightStr != null && rightStr.toLowerCase().equals("null") &&
				this.ope.equals("=")){
			return leftStr + " IS NULL";
		}else if(rightStr != null && rightStr.toLowerCase().equals("null") &&
				this.ope.equals("<>")){
			return leftStr + " IS NOT NULL";
		}
		
		return leftStr + " " + this.ope + " " + rightStr;
	}
	
	private boolean isColumnName(ISQLStatement stmt)
	{
		if(stmt instanceof Identifier || stmt instanceof ColumnIdentifier){
			return true;
		}
		
		if(stmt instanceof SQLStatement && ((SQLStatement)stmt).getSubStatement() == null){
			ISQLStatement first = ((SQLStatement)stmt).getFirstStmt();
			if(first instanceof Identifier || first instanceof ColumnIdentifier){
				return true;
			}
		}
		
		return false;
	}
	
	private String defaultExtract(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		// type valification
		
		String leftStr = this.left.extract(context, providor, adjustWhere, adjustSet, helper);
		String rightStr = this.right.extract(context, providor, adjustWhere, adjustSet, helper);
		
		if(rightStr != null && rightStr.toLowerCase().equals("null") &&
				this.ope.equals("=")){
			return leftStr + " IS NULL";
		}else if(rightStr != null && rightStr.toLowerCase().equals("null") &&
				this.ope.equals("<>")){
			return leftStr + " IS NOT NULL";
		}
		
		return leftStr + " " + this.ope + " " + rightStr;
	}

	public boolean isReady(PostContext context, VariableRepository providor, AdjustWhere adjustWhere) throws ExecutionException
	{
		if(adjustWhere == null || !adjustWhere.adjust()){
			return true;
		}
		
		return this.left.isReady(context, providor, adjustWhere)
				&& this.right.isReady(context, providor, adjustWhere);
	}
	
}

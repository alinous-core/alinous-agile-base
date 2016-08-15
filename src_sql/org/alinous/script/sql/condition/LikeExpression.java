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

import org.alinous.AlinousUtils;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.datasrc.types.TypeHelper;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.adjustopt.AdjustSet;
import org.alinous.script.sql.adjustopt.AdjustWhere;
import org.alinous.script.sql.statement.ISQLStatement;
import org.alinous.script.sql.statement.SQLStatement;
import org.alinous.script.sql.statement.SQLVariable;

public class LikeExpression implements ISQLExpression
{
	private ISQLStatement left;
	private ISQLStatement right;
	private ISQLStatement escape;
	
	public boolean isReady(PostContext context, VariableRepository providor, AdjustWhere adjustWhere) throws ExecutionException
	{
		if(adjustWhere == null || !adjustWhere.adjust()){
			return true;
		}
			
		return this.left.isReady(context, providor, adjustWhere) && this.right.isReady(context, providor, adjustWhere);
	}

	public String extract(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		// array like

		if(this.right instanceof SQLStatement && ((SQLStatement)this.right).getFirstStmt() instanceof SQLVariable &&
				((SQLVariable) ((SQLStatement)this.right).getFirstStmt()).getPrefix().equals("@")){
			return extractArrayLike(context, providor, adjustWhere, adjustSet, helper);
		}
		
		// normal like
		StringBuffer buff = new StringBuffer();
		
		TypeHelper newHelper = helper.newHelper(true);
		
		String leftStr = this.left.extract(context, providor, adjustWhere, adjustSet, newHelper);
		buff.append(leftStr);
		buff.append(" LIKE ");

		String fieldType;
		try {
			fieldType = helper.getDataFieldType(leftStr, context);
		} catch (DataSourceException e) {
			throw new ExecutionException(e, "Failed in extracting expression.");
		}
		
		TypeHelper helper2 = helper.newHelper(false);
		helper2.setFieldType(fieldType);
		
		buff.append(this.right.extract(context, providor, adjustWhere, adjustSet, helper2));
		
		if(this.escape != null){
			buff.append("  ESCAPE ");
			buff.append(this.escape.extract(context, providor, adjustWhere, adjustSet, newHelper));
		}
		
		return buff.toString();
	}
	
	public String extractArrayLike(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		StringBuffer buff = new StringBuffer();
		
		TypeHelper newHelper = helper.newHelper(true);
		
		String leftStr = this.left.extract(context, providor, adjustWhere, adjustSet, newHelper);
		
		SQLVariable array = (SQLVariable)((SQLStatement)this.right).getFirstStmt();
		ScriptArray ar = null;
		try {
			ar = (ScriptArray) providor.get(array.getPathElement().getPathString(context, providor));
		} catch (RedirectRequestException e) {
			e.printStackTrace();
		}
		
		StringBuffer escapeBuff = new StringBuffer();
		if(this.escape != null){
			escapeBuff.append("  ESCAPE ");
			escapeBuff.append(this.escape.extract(context, providor, adjustWhere, adjustSet, newHelper));
		}
		
		boolean fisrt = true;
		int size = ar.getSize();
		for (int i = 0; i < size; i++) {
			if(fisrt){
				fisrt = false;
			}else{
				buff.append(" AND ");
			}
			
			buff.append(leftStr);
			buff.append(" LIKE ");
			
			ScriptDomVariable dom = (ScriptDomVariable) ar.get(i);
			
			buff.append("'");
			buff.append(AlinousUtils.sqlEscape(dom.getValue()));
			buff.append("'");
			
			buff.append(escapeBuff.toString());
		}
		
		
		return buff.toString();
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

	public ISQLStatement getEscape()
	{
		return escape;
	}

	public void setEscape(ISQLStatement escape)
	{
		this.escape = escape;
	}

	
}

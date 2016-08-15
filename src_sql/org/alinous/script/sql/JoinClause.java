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
import org.alinous.script.sql.other.JoinCondition;

public class JoinClause implements IClause
{
	public final static int DEFAULT = 0;
	public final static int INNER_JOIN = 1;
	public final static int LEFT_JOIN = 2;
	public final static int RIGHT_JOIN = 3;
	public final static int NATURAL_JOIN = 4;
	public final static int CROSS_JOIN = 5;
	
	private int joinType;	
	private JoinCondition condition;
	
	private ISQLScriptObject left;
	private ISQLScriptObject right;
	
	// [TABLE1] LEFT JOIN [TABLE2] 
	public String extract(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		StringBuffer buffer = new StringBuffer();
		
		if(this.left instanceof JoinClause){
			buffer.append("(");
			buffer.append(this.left.extract(context, providor, adjustWhere, adjustSet, helper));
			buffer.append(")");
		}else{
			buffer.append(this.left.extract(context, providor, adjustWhere, adjustSet, helper));
		}
		
		switch(this.joinType){
		case DEFAULT:
			buffer.append(" JOIN ");
			break;
		case INNER_JOIN:
			buffer.append(" INNER JOIN ");
			break;
		case LEFT_JOIN:
			buffer.append(" LEFT JOIN ");
			break;
		case RIGHT_JOIN:
			buffer.append(" RIGHT JOIN ");
			break;
		case NATURAL_JOIN:
			buffer.append(" NATURAL JOIN ");
			break;
		case CROSS_JOIN:
			buffer.append(" CROSS JOIN ");
			break;
		default:
			buffer.append(", ");
			break;
		}
		
		if(this.right instanceof JoinClause){
			buffer.append("(");
			buffer.append(this.right.extract(context, providor, adjustWhere, adjustSet, helper));
			buffer.append(")");
		}else{
			buffer.append(this.right.extract(context, providor, adjustWhere, adjustSet, helper));
						
		}
		
		if(this.condition != null){
			buffer.append(" ");
			buffer.append(this.condition.extract(context, providor, adjustWhere, adjustSet, helper));
		}
		
		return buffer.toString();
	}

	public JoinCondition getCondition() {
		return condition;
	}

	public void setCondition(JoinCondition condition) {
		this.condition = condition;
	}

	public int getJoinType() {
		return joinType;
	}

	public void setJoinType(int joinType) {
		this.joinType = joinType;
	}

	public ISQLScriptObject getLeft() {
		return left;
	}

	public void setLeft(ISQLScriptObject left) {
		this.left = left;
	}

	public ISQLScriptObject getRight() {
		return right;
	}

	public void setRight(ISQLScriptObject right) {
		this.right = right;
	}

	public boolean isReady(PostContext context, VariableRepository providor, AdjustWhere adjustWhere) throws ExecutionException
	{
		return true;
	}
	
}

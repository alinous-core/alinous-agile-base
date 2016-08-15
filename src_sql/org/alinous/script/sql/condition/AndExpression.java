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

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.alinous.datasrc.types.TypeHelper;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.adjustopt.AdjustSet;
import org.alinous.script.sql.adjustopt.AdjustWhere;

public class AndExpression implements ISQLContainerExpression
{
	private List<ISQLExpression> expressions = new CopyOnWriteArrayList<ISQLExpression>();
	
	public void addExpressions(ISQLExpression expressions) 
	{
		this.expressions.add(expressions);
	}

	public String extract(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		StringBuffer buffer = new StringBuffer();
		
		String AND = " AND ";
		boolean first = true;
		Iterator<ISQLExpression> it = this.expressions.iterator();
		while(it.hasNext()){
			ISQLExpression exp = it.next();
			
			if(!exp.isReady(context, providor, adjustWhere)){
				continue;
			}
			
			if(first){
				first = false;
			}else{
				buffer.append(AND);
			}
			
			buffer.append(exp.extract(context, providor, adjustWhere, adjustSet, helper));
		}
		
		return buffer.toString();
	}

	public boolean isReady(PostContext context, VariableRepository providor, AdjustWhere adjustWhere) throws ExecutionException
	{
		if(adjustWhere == null || !adjustWhere.adjust()){
			return true;
		}
		
		Iterator<ISQLExpression> it = this.expressions.iterator();
		while(it.hasNext()){
			ISQLExpression exp = it.next();
			
			if(exp.isReady(context, providor, adjustWhere)){
				return true;
			}
		}
		
		return false;
	}

	public List<ISQLExpression> getExpressions()
	{
		return expressions;
	}

}

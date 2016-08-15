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
import org.alinous.script.sql.other.VariableList;
import org.alinous.script.sql.statement.ISQLStatement;

public class InClauseExpression implements ISQLExpression
{
	private ISQLStatement top;
	private VariableList valList;
	private ISQLStatement subQuery;
	
	public boolean isReady(PostContext context, VariableRepository providor, AdjustWhere adjustWhere) throws ExecutionException
	{
		if(adjustWhere == null || !adjustWhere.adjust()){
			return true;
		}
		
		if(this.valList != null){
			return this.top.isReady(context, providor, adjustWhere) && this.valList.isReady(context, providor, adjustWhere)
					&& this.valList.isReady(context, providor, adjustWhere);
		}
		if(this.subQuery != null){
			return top.isReady(context, providor, adjustWhere) && this.subQuery.isReady(context, providor, adjustWhere);
		}
		
		return false;
	}

	public String extract(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		//if(context.getPrecompile().isCompile() && !this.valList.isNotArray()){
		//	extractPrecompile(context, providor, adjustWhere, adjustSet, helper);
		//}
		
		
		StringBuffer buff = new StringBuffer();
		
		// Type valification
		String topStr = this.top.extract(context, providor, adjustWhere, adjustSet, helper);
		String fieldType;
		try {
			fieldType = helper.getDataFieldType(topStr, context);
		} catch (DataSourceException e) {
			throw new ExecutionException(e, "Failed in extract IN clause."); // i18n
		}
		if(fieldType == null){
			return defalutExtract(context, providor, adjustWhere, adjustSet, helper);
		}
		
		TypeHelper helper2 = helper.newHelper(false);
		helper2.setDisableCounter(true);
		helper2.setFieldType(fieldType);
		
		buff.append(topStr);
		buff.append(" IN ");

		if(this.valList != null){
			context.getPrecompile().setHasArray(true);
			buff.append(this.valList.extract(context, providor, adjustWhere, adjustSet, helper2));
		}
		else if(this.subQuery != null){
			buff.append(this.subQuery.extract(context, providor, adjustWhere, adjustSet, helper2));
		}
		
		return buff.toString();
	}
	
	public String extractPrecompile(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		// activate variable
		// Type valification
		String topStr = this.top.extract(context, providor, adjustWhere, adjustSet, helper);
		String fieldType;
		try {
			fieldType = helper.getDataFieldType(topStr, context);
		} catch (DataSourceException e) {
			throw new ExecutionException(e, "Failed in extract IN clause."); // i18n
		}
		if(fieldType == null){
			return defalutExtract(context, providor, adjustWhere, adjustSet, helper);
		}
		
		TypeHelper helper2 = helper.newHelper(false);
		helper2.setDisableCounter(true);
		helper2.setFieldType(fieldType);
		
	//	this.valList.extractPrecompile(context, providor, adjustWhere, adjustSet, helper2);
		
		// OR
		StringBuffer buff = new StringBuffer();
		
		buff.append(" (");
		
		
		buff.append(")");
		
		return buff.toString();
	}
	
	private String defalutExtract(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		StringBuffer buff = new StringBuffer();

		buff.append(this.top.extract(context, providor, adjustWhere, adjustSet, helper));
		buff.append(" IN ");

		if(this.valList != null){
			buff.append(this.valList.extract(context, providor, adjustWhere, adjustSet, helper));
		}
		else if(this.subQuery != null){
			buff.append(this.subQuery.extract(context, providor, adjustWhere, adjustSet, helper));
		}
		
		return buff.toString();
	}

	public ISQLStatement getSubQuery()
	{
		return subQuery;
	}

	public void setSubQuery(ISQLStatement subQuery)
	{
		this.subQuery = subQuery;
	}

	public ISQLStatement getTop()
	{
		return top;
	}

	public void setTop(ISQLStatement top)
	{
		this.top = top;
	}

	public VariableList getValList()
	{
		return valList;
	}

	public void setValList(VariableList valList)
	{
		this.valList = valList;
	}
	
	

}

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
package org.alinous.script.sql.other;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.alinous.datasrc.types.TypeHelper;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.ISQLScriptObject;
import org.alinous.script.sql.adjustopt.AdjustSet;
import org.alinous.script.sql.adjustopt.AdjustWhere;
import org.alinous.script.sql.statement.ColumnIdentifier;
import org.alinous.script.sql.statement.ISQLStatement;
import org.alinous.script.sql.statement.SQLVariable;


public class ColumnList implements ISQLScriptObject
{
	private List<SelectColumnElement> columnList = new CopyOnWriteArrayList<SelectColumnElement>();
	
	public void addColumns(SelectColumnElement col, String order)
	{
		col.setOrder(order);
		
		this.columnList.add(col);
	}
	
	public void addColumns(SelectColumnElement col)
	{
		this.columnList.add(col);
	}

	public void addColumns(ColumnIdentifier col)
	{
		SelectColumnElement selCol = new SelectColumnElement();
		selCol.setColumnName(col);
		this.columnList.add(selCol);
	}
	
	public void addColumns(ColumnIdentifier col, String order)
	{
		SelectColumnElement selCol = new SelectColumnElement();
		selCol.setColumnName(col);
		
		selCol.setOrder(order);
		
		this.columnList.add(selCol);
	}
	
	public boolean isReady(PostContext context, VariableRepository providor, AdjustWhere adjustWhere) throws ExecutionException
	{
		if(columnList.size() == 0){
			return false;
		}
		
		SelectColumnElement el = columnList.get(0);
		ISQLStatement stmt = el.getColumnName();
		
		if(stmt instanceof SQLVariable){
			SQLVariable val = (SQLVariable)stmt;
			
			
			IScriptVariable variable = val.getScriptVariable(context, providor);
			if(variable == null){
				return false;
			}
			
			
			if(variable instanceof ScriptArray){
				ScriptArray ar = (ScriptArray)variable;
				
				if(ar.getSize() == 0){
					return false;
				}
			}
		}
		
		return true;
	}

	public String extract(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException {
		StringBuffer buffer = new StringBuffer();
		
		boolean first = true;
		Iterator<SelectColumnElement> it = this.columnList.iterator();
		while(it.hasNext()){
			SelectColumnElement colId = it.next();
			
			if(first){
				first = false;
			}else{
				buffer.append(", ");
			}
			
			try {
				buffer.append(colId.extract(context, providor, adjustWhere, adjustSet, helper));
			} catch (RedirectRequestException e) {
				e.printStackTrace();
			}
		}
		
		return buffer.toString();
	}
	
	public int size()
	{
		return this.columnList.size();
	}
	
	public Iterator<SelectColumnElement> iterator()
	{
		return this.columnList.iterator();
	}
	
	public SelectColumnElement getSelectColumnElement(int index)
	{
		return this.columnList.get(index);
	}
}

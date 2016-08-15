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
import org.alinous.script.sql.other.TablesList;
import org.alinous.script.sql.statement.Identifier;

public class FromClause implements IClause
{
	private TablesList tableList;
	private SelectSentence selectSentence;
	private Identifier asId;

	public TablesList getTableList() {
		return tableList;
	}

	public void setTableList(TablesList tableList)
	{
		this.tableList = tableList;
	}

	public SelectSentence getSelectSentence() {
		return selectSentence;
	}

	public void setSelectSentence(SelectSentence selectSentence) {
		this.selectSentence = selectSentence;
	}

	public Identifier getAsId() {
		return asId;
	}

	public void setAsId(Identifier asId) {
		this.asId = asId;
	}

	public String extract(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		if(this.selectSentence != null){
			return extractSelect(context, providor, adjustWhere, adjustSet, helper);
		}
		
		return "FROM " + this.tableList.extract(context, providor, adjustWhere, adjustSet, helper);
	}
	
	private String extractSelect(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		StringBuffer buff = new StringBuffer();
		
		buff.append("FROM (");
		
		String strSelect = this.selectSentence.extract(context, providor, adjustWhere, adjustSet, helper);
		buff.append(strSelect);
		
		buff.append(") AS ");
		
		String asString = this.asId.extract(context, providor, adjustWhere, adjustSet, helper);
		buff.append(asString);
		
		return buff.toString();
	}

	public boolean isReady(PostContext context, VariableRepository providor, AdjustWhere adjustWhere) throws ExecutionException
	{
		if(this.selectSentence != null){
			return this.selectSentence.isReady(context, providor, adjustWhere);
		}		
		
		return true;
	}
	
	
}

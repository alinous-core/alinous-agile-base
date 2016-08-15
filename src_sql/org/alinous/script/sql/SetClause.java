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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.alinous.datasrc.types.TypeHelper;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.adjustopt.AdjustSet;
import org.alinous.script.sql.adjustopt.AdjustWhere;
import org.alinous.script.sql.other.SetPair;

public class SetClause implements IClause
{
	private List<SetPair> setList = new LinkedList<SetPair>();
	private AdjustSet adjustSet;
	
	public void addSet(SetPair set)
	{
		this.setList.add(set);
	}	
	
	public List<SetPair> getSetList()
	{
		return setList;
	}

	public String extract(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("SET ");
		
		boolean first = true;
		Iterator<SetPair> it = this.setList.iterator();
		while(it.hasNext()){
			SetPair p = it.next();
			
			if(first){
				first = false;
			}else{
				buffer.append(", ");
			}
			
			buffer.append(p.extract(context, providor, adjustWhere, adjustSet, helper));			
		}
		
		return buffer.toString();
	}
	
	public String extractPreCompile(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		StringBuffer buffer = new StringBuffer();

		buffer.append("SET ");
		
		boolean first = true;
		Iterator<SetPair> it = this.setList.iterator();
		while(it.hasNext()){
			SetPair p = it.next();
			
			if(first){
				first = false;
			}else{
				buffer.append(", ");
			}
			
			
			
			buffer.append(p.extractPrecompile(context, providor, adjustWhere, adjustSet, helper));			
		}
		
		return buffer.toString();
	}

	public boolean isReady(PostContext context, VariableRepository providor, AdjustWhere adjustWhere) throws ExecutionException
	{
		if(this.adjustSet != null && !this.adjustSet.adjust()){
			return true;
		}
		
		Iterator<SetPair> it = this.setList.iterator();
		while(it.hasNext()){
			SetPair p = it.next();
			
			if(p.isReady(context, providor, adjustWhere)){
				return true;
			}
		}
		
		return false;
	}
}

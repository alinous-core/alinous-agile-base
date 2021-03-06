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
package org.alinous.script.sql.statement;

import org.alinous.datasrc.types.TypeHelper;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.adjustopt.AdjustSet;
import org.alinous.script.sql.adjustopt.AdjustWhere;

public class SQLParenthesisStatement implements ISQLStatement
{
	private ISQLStatement stmt;
	
	public boolean isReady(PostContext context, VariableRepository providor, AdjustWhere adjustWhere) throws ExecutionException
	{
		return stmt.isReady(context, providor, adjustWhere);
	}

	public String extract(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		return "(" + this.stmt.extract(context, providor, adjustWhere, adjustSet, helper) + ")";
	}
	
	@Override
	public String extractPrecompile(PostContext context,
			VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		return "(" + this.stmt.extractPrecompile(context, providor, adjustWhere, adjustSet, helper) + ")";
	}

	public ISQLStatement getStmt()
	{
		return stmt;
	}

	public void setStmt(ISQLStatement stmt)
	{
		this.stmt = stmt;
	}
	
}

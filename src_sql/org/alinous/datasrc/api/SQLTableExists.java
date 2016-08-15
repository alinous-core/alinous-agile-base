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

package org.alinous.datasrc.api;

import java.util.Stack;

import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.datasrc.types.DataTable;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.functions.system.AbstractSystemFunction;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class SQLTableExists extends AbstractSystemFunction
{
	public static String QUALIFIED_NAME = "SQL.TABLEEXISTS";
	
	public static String DATA_SRC = "dataSrc";
	public static String TABLE = "table";
	
	public SQLTableExists()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", DATA_SRC);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", TABLE);
		this.argmentsDeclare.addArgument(arg);
	}
	
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo)
			throws ExecutionException, RedirectRequestException
	{
		IScriptVariable retVal;
		Stack<IStatement> stmtStack = context.getFuncArgStack();
		
		if(stmtStack.size() != this.argmentsDeclare.getSize()){
			throw new ExecutionException("Number of the function's arguments is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		
		IPathElement ipath = PathElementFactory.buildPathElement(DATA_SRC);
		IScriptVariable dataSrcVariable = newValRepo.getVariable(ipath, context);
		
		ipath = PathElementFactory.buildPathElement(TABLE);
		IScriptVariable tableVariable = newValRepo.getVariable(ipath, context);
		
		if(!(dataSrcVariable instanceof ScriptDomVariable) ||
				!(tableVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		String conStr = ((ScriptDomVariable)dataSrcVariable).getValue();
		String table = ((ScriptDomVariable)tableVariable).getValue();
		
		DataSrcConnection con = null;
		try {
			con = context.getUnit().getConnectionManager().connect(conStr, context);
			
			DataTable dtTable = con.getDataTable(table);
			
			if(dtTable == null){
				ScriptDomVariable val = new ScriptDomVariable("tmp");
				val.setValue("false");
				val.setValueType(IScriptVariable.TYPE_BOOLEAN);
				
				retVal = val;
			}else{
				ScriptDomVariable val = new ScriptDomVariable("tmp");
				val.setValue("true");
				val.setValueType(IScriptVariable.TYPE_BOOLEAN);
				
				retVal = val;
			}
		} catch (DataSourceException e) {
			throw new ExecutionException(e, "Failed in operation at " + QUALIFIED_NAME); // i18n
		}finally{
			con.close();
		}
		
		
		return retVal;
	}

	public String getName()
	{
		return QUALIFIED_NAME;
	}

	@Override
	public String codeAssistString() {
		return "Sql.TableExists($dataSrc ,$table)";
	}

	@Override
	public String descriptionString() {
		return "Check if $table exists or not";
	}
}


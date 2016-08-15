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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
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
import org.alinous.script.sql.WhereClause;
import org.alinous.script.sql.condition.AndExpression;
import org.alinous.script.sql.condition.TwoClauseExpression;
import org.alinous.script.sql.statement.Identifier;
import org.alinous.script.sql.statement.SQLStringConst;

public class SQLLoadBlob extends AbstractSystemFunction
{
	public static String QUALIFIED_NAME = "SQL.LOADBLOB";
	
	public static String DATA_SRC = "dataSrc";
	public static String TABLE = "table";
	public static String KEY = "key";
	public static String KEY_VALUE = "keyValue";
	public static String BLOB_COLUMN = "blobColumn";
	public static String FILE_NAME = "fileName";
	
	public SQLLoadBlob()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", DATA_SRC);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", TABLE);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", KEY);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", KEY_VALUE);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", BLOB_COLUMN);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", FILE_NAME);
		this.argmentsDeclare.addArgument(arg);
	}
	
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
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
		
		ipath = PathElementFactory.buildPathElement(KEY);
		IScriptVariable keyVariable = newValRepo.getVariable(ipath, context);
		
		ipath = PathElementFactory.buildPathElement(KEY_VALUE);
		IScriptVariable keyValVariable = newValRepo.getVariable(ipath, context);
		
		ipath = PathElementFactory.buildPathElement(BLOB_COLUMN);
		IScriptVariable blobColumnVariable = newValRepo.getVariable(ipath, context);
		
		ipath = PathElementFactory.buildPathElement(FILE_NAME);
		IScriptVariable fileVariable = newValRepo.getVariable(ipath, context);
		
		if(!(dataSrcVariable instanceof ScriptDomVariable) ||
				!(tableVariable instanceof ScriptDomVariable) ||
				!(keyVariable instanceof ScriptDomVariable) ||
				!(fileVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		String table = ((ScriptDomVariable)tableVariable).getValue();
		String keyColumn = ((ScriptDomVariable)keyVariable).getValue();
		String keyValue = ((ScriptDomVariable)keyValVariable).getValue();
		String blobColumn = ((ScriptDomVariable)blobColumnVariable).getValue();
		String absPath = SQLFunctionUtils.getAbsolutePath(((ScriptDomVariable)fileVariable).getValue(), context.getCore());

		String conStr = ((ScriptDomVariable)dataSrcVariable).getValue();
		DataSrcConnection con = null;
		try {
			con = context.getUnit().getConnectionManager().connect(conStr, context);
			
			HashMap<String, String> param = new HashMap<String, String>();
			param.put(keyColumn, keyValue);
			WhereClause where = createWhereClauseFromParamMap(param);
			
			con.readLargeObject(absPath, table, blobColumn, where, context, newValRepo);
		} catch (DataSourceException e) {
			throw new ExecutionException(e, "Failed in operation at " + QUALIFIED_NAME); // i18n
		}
		finally{
			con.close();
		}
		
		
		return null;
	}

	private WhereClause createWhereClauseFromParamMap(Map<String, String> queryParams)
	{
		WhereClause where = new WhereClause();
		AndExpression andExp = new AndExpression();
		Iterator<String> it = queryParams.keySet().iterator();
		while(it.hasNext()){
			String field = it.next();
			String val = queryParams.get(field);
			
			Identifier id = new Identifier();
			id.setName(field);
			
			SQLStringConst sqVal = new SQLStringConst();
			sqVal.setStr(val);
			
			TwoClauseExpression eqExp = new TwoClauseExpression();
			eqExp.setOpe("=");
			eqExp.setLeft(id);
			eqExp.setRight(sqVal);
			
			andExp.addExpressions(eqExp);
		}
		
		where.setExpression(andExp);
		
		return where;
	}
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}

	public IScriptVariable getResult()
	{
		return null;
	}
	
	@Override
	public String codeAssistString() {
		return "Sql.loadBlob($dataSrc, $table, $key, $keyValue, $blobColumn, $fileName)";
	}

	@Override
	public String descriptionString() {
		return "Loading blob into file.";
	}

}

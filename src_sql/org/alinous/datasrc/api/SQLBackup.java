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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Stack;

import org.alinous.cloud.file.AlinousFile;
import org.alinous.cloud.file.AlinousFileOutputStream;
import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.datasrc.util.BackupManager;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
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

public class SQLBackup extends AbstractSystemFunction
{
	public static String QUALIFIED_NAME = "SQL.BACKUP";
	public static String DATA_SRC = "dataSrc";
	public static String TABLE = "table";
	public static String FILE_NAME = "fileName";
	
	public SQLBackup()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", DATA_SRC);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", TABLE);
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
		
		ipath = PathElementFactory.buildPathElement(FILE_NAME);
		IScriptVariable fileVariable = newValRepo.getVariable(ipath, context);
		
		if(!(dataSrcVariable instanceof ScriptDomVariable) ||
				!(tableVariable instanceof ScriptDomVariable) ||
				!(fileVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		String table = ((ScriptDomVariable)tableVariable).getValue();
		String absPath = SQLFunctionUtils.getAbsolutePath(((ScriptDomVariable)fileVariable).getValue(), context.getCore());
		OutputStream stream = null;
		String conStr = ((ScriptDomVariable)dataSrcVariable).getValue();
		
		DataSrcConnection con = null;
		try {
			con = context.getUnit().getConnectionManager().connect(conStr, context);
			stream = new AlinousFileOutputStream(new AlinousFile(absPath));
			
			BackupManager mgr = new BackupManager();
			mgr.backup(context.getCore(), con, table, stream);
		} catch (DataSourceException e) {
			throw new ExecutionException(e, "Failed in operation at " + QUALIFIED_NAME); // i18n
		} catch (AlinousException e) {
			throw new ExecutionException(e, "Failed in operation at " + QUALIFIED_NAME); // i18n
		} catch (IOException e) {
			throw new ExecutionException(e, "Failed in operation at " + QUALIFIED_NAME); // i18n
		}
		finally{
			if(stream != null){
				try {
					stream.close();
				} catch (IOException e) {
					context.getCore().getLogger().reportError(e);
				}
			}
			
			con.close();
		}		
		
		return null;
	}

	public String getName()
	{
		return QUALIFIED_NAME;
	}
	
	@Override
	public String codeAssistString() {
		return "Sql.Backup($dataSrc, $table, $fileName)";
	}

	@Override
	public String descriptionString() {
		return "Backup table into the file with filename $fileName.";
	}

}

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

public class SQLGetTableInfo extends AbstractSystemFunction {
	public static String QUALIFIED_NAME = "SQL.GETTABLEINFO";
	
	public static String DATA_SRC = "dataSrc";
	public static String TABLE = "table";
	
	public SQLGetTableInfo()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", DATA_SRC);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", TABLE);
		this.argmentsDeclare.addArgument(arg);
	}
	
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo)
			throws ExecutionException, RedirectRequestException
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
		
		if(!(dataSrcVariable instanceof ScriptDomVariable) ||
				!(tableVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		String conStr = ((ScriptDomVariable)dataSrcVariable).getValue();
		String table = ((ScriptDomVariable)tableVariable).getValue();
		
		ScriptDomVariable dom = null;
		DataSrcConnection con = null;
		try {
			con = context.getUnit().getConnectionManager().connect(conStr, context);
			
			DataTable dtTable = con.getDataTable(table);
			
			dom = SQLGetTables.toDom(dtTable);
		} catch (DataSourceException e) {
			throw new ExecutionException(e, "Failed in operation at " + QUALIFIED_NAME); // i18n
		}finally{
			con.close();
		}
		
		return dom;
	}
	
	
	@Override
	public String codeAssistString() {
		return "Sql.getTableInfo($dataSrc ,$table)";
	}

	@Override
	public String descriptionString() {
		return "Get scheme information of the table.";
	}
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}
}

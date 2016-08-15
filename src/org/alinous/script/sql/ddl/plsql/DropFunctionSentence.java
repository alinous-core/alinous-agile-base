package org.alinous.script.sql.ddl.plsql;

import java.util.List;

import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.datasrc.types.PlSQLFunction;
import org.alinous.exec.ScriptCheckContext;
import org.alinous.exec.ScriptError;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.script.AlinousScript;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.ddl.ColumnTypeDescriptor;
import org.alinous.script.statement.FunctionCall;
import org.alinous.test.coverage.FileCoverage;
import org.alinous.test.coverage.LineCoverage;



public class DropFunctionSentence extends FunctionBase
{
	private PlSQLFunction func = new PlSQLFunction();
	
	public boolean execute(PostContext context, VariableRepository valRepo)
			throws ExecutionException
	{
		context.getCore().reporttExecuted(this);
		
		String dataSrc = context.getDataSrc();
		if(dataSrc == null){
			dataSrc = context.getCore().getConfig().getSystemRepositoryConfig().getDefaultSrc();
		}
		
		DataSrcConnection con = null;
		try {
			con = context.getUnit().getConnectionManager().connect(dataSrc, context);
		} catch (DataSourceException e) {
			throw new ExecutionException(e, "");
		}
		
		try {
			con.dropFunction(this.func);
		} catch (DataSourceException e) {
			con.close();
			throw new ExecutionException(e, "Failed in dropping finction at line " + this.getLine());
		}
		
		return true;
	}
	
	public void setFuncName(String funcName)
	{
		this.func.setFuncName(funcName);
	}
	
	public void addTypeDesc(ColumnTypeDescriptor typeDesc)
	{
		this.func.addTypeDesc(typeDesc);
	}

	public void checkStaticErrors(ScriptCheckContext scContext,
			List<ScriptError> errorList)
	{
		
	}

	@Override
	public void getFunctionCall(ScriptCheckContext scContext, List<FunctionCall> call, AlinousScript script)
	{
		
	}
	
	@Override
	public void setupCoverage(FileCoverage coverage)
	{
		LineCoverage lineCoverage = new LineCoverage(this.line, this.linePosition, this);
		coverage.addLineCoverage(lineCoverage);
		
	}
	
}

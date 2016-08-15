package org.alinous.script.sql.ddl.plsql;

import java.util.List;

import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.datasrc.types.PlSQLTrigger;
import org.alinous.datasrc.types.UpdateType;
import org.alinous.exec.ScriptCheckContext;
import org.alinous.exec.ScriptError;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.script.AlinousScript;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.statement.SQLFunctionCallArguments;
import org.alinous.script.statement.FunctionCall;
import org.alinous.test.coverage.FileCoverage;
import org.alinous.test.coverage.LineCoverage;


public class CreateTriggerSentence extends FunctionBase
{
	private PlSQLTrigger trigger = new PlSQLTrigger();
	
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
			con.createTrigger(this.trigger);
		} catch (DataSourceException e) {
			con.close();
			throw new ExecutionException(e, "Failed in creating trigger at line " + this.getLine());
		}
		
		return true;
	}
	
	public void setTriggerName(String triggerName)
	{
		this.trigger.setTriggerName(triggerName);
	}
	
	public void setTriggerTable(String triggerTable)
	{
		this.trigger.setTriggerTable(triggerTable);
	}
	
	public void addUpdateType(UpdateType updateType)
	{
		this.trigger.addUpdateTypes(updateType);
	}
	
	public void setTiming(String timing)
	{
		this.trigger.setTiming(timing);
	}
	public void setUpdateTarget(String updateTarget)
	{
		this.trigger.setUpdateTarget(updateTarget);
	}
	public void setFuncName(String funcName)
	{
		this.trigger.setFuncName(funcName);
	}
	public void setFuncArguments(SQLFunctionCallArguments funcArguments)
	{
		this.trigger.setFuncArguments(funcArguments);
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

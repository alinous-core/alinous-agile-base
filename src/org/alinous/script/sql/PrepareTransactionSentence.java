package org.alinous.script.sql;

import java.util.List;

import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.datasrc.types.TypeHelper;
import org.alinous.debug.StepInCandidates;
import org.alinous.exec.ScriptCheckContext;
import org.alinous.exec.ScriptError;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.plugin.xa.AlinousRegisterdGlobalId;
import org.alinous.script.AlinousScript;
import org.alinous.script.ISQLSentence;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.adjustopt.AdjustSet;
import org.alinous.script.sql.adjustopt.AdjustWhere;
import org.alinous.script.statement.FunctionCall;
import org.alinous.test.coverage.FileCoverage;
import org.alinous.test.coverage.LineCoverage;
import org.jdom.Element;

public class PrepareTransactionSentence implements ISQLSentence {
	
	private boolean recompilable = true;
	private int line;
	private int linePosition;
	private String filePath;
	
	private IStatement trxIdentifier;
	
	
	@Override
	public boolean execute(PostContext context, VariableRepository valRepo)
			throws ExecutionException, RedirectRequestException
	{
		context.getCore().reporttExecuted(this);
		
		IScriptVariable trxVariable = this.trxIdentifier.executeStatement(context, valRepo);
		if(!(trxVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Global Transaction Id must not be array", this.filePath, this.line);
		}
		
		ScriptDomVariable domId = (ScriptDomVariable)trxVariable;
		String value = domId.getValue();
		if(value == null){
			throw new ExecutionException("Global Transaction Id must not be null", this.filePath, this.line);
		}
		
		String dataSrc = context.getDataSrc();
		if(dataSrc == null){
			dataSrc = context.getCore().getConfig().getSystemRepositoryConfig().getDefaultSrc();
		}
		
		// execute
		DataSrcConnection con = null;
		try {
			con = context.getUnit().getConnectionManager().connect(dataSrc, context);
		} catch (DataSourceException e) {
			throw new ExecutionException(e, "");
		}catch(Throwable e){
			throw new ExecutionException(e, "execute commit failed on getting connection");
		}
		
		// execute prepare
		try {
			con.prepareTransaction(value);
		} catch (DataSourceException e) {
			throw new ExecutionException(e, "executing Prepare Transaction failed.");
		}catch(Throwable e){
			throw new ExecutionException(e, "Prepare Transaction failed");
		}
		
		// 
		//AlinousDebug.debugOut("register global trx");
		
		context.addGlobalTrxId(new AlinousRegisterdGlobalId(context.getDataSrc(), value));
		
		//AlinousDebug.debugOut("prepare context.getGlobalTrxIds() : " + context.showGlobalIdStatus());
		//AlinousDebug.debugOut("prepare context : " + context);
		
		return true;
	}
	
	@Override
	public String getFilePath() {
		return this.filePath;
	}

	@Override
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	@Override
	public StepInCandidates getStepInCandidates() {
		return null;
	}

	@Override
	public IScriptVariable getReturnedVariable(PostContext context) {
		return null;
	}

	@Override
	public int getLine() {
		return this.line;
	}

	@Override
	public void setLine(int line) {
		this.line = line;
	}

	@Override
	public int getLinePosition() {
		return this.linePosition;
	}

	@Override
	public void setLinePosition(int linePosition) {
		this.linePosition = linePosition;
	}

	@Override
	public void getFunctionCall(ScriptCheckContext scContext,
			List<FunctionCall> call, AlinousScript script) {
		
	}

	@Override
	public void setupCoverage(FileCoverage coverage) {
		LineCoverage lineCoverage = new LineCoverage(this.line, this.linePosition, this);
		coverage.addLineCoverage(lineCoverage);
	}

	@Override
	public void exportIntoJDomElement(Element parent) throws AlinousException {
	
	}

	@Override
	public void importFromJDomElement(Element threadElement)
			throws AlinousException {
		
	}

	@Override
	public void checkStaticErrors(ScriptCheckContext scContext,
			List<ScriptError> errorList) {

	}

	@Override
	public String extract(PostContext context, VariableRepository valRepo,
			AdjustWhere adjustWhere, AdjustSet adjustSet, TypeHelper helper)
			throws ExecutionException {
		return null;
	}

	@Override
	public boolean isReady(PostContext context, VariableRepository valRepo,
			AdjustWhere adjustWhere) throws ExecutionException {
		return true;
	}

	@Override
	public boolean isPrecompilable() {
		return this.recompilable;
	}

	@Override
	public void setPrecompilable(boolean b) {
		this.recompilable = b;
	}

	public IStatement getTrxIdentifier() {
		return trxIdentifier;
	}

	public void setTrxIdentifier(IStatement trxIdentifier) {
		this.trxIdentifier = trxIdentifier;
	}


}

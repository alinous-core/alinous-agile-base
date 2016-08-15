package org.alinous.script.sql.ddl;

import java.util.List;

import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.datasrc.types.TypeHelper;
import org.alinous.debug.StepInCandidates;
import org.alinous.exec.IExecutable;
import org.alinous.exec.ScriptCheckContext;
import org.alinous.exec.ScriptError;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.script.AlinousScript;
import org.alinous.script.ISQLSentence;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.adjustopt.AdjustSet;
import org.alinous.script.sql.adjustopt.AdjustWhere;
import org.alinous.script.sql.statement.Identifier;
import org.alinous.script.statement.FunctionCall;
import org.alinous.test.coverage.FileCoverage;
import org.alinous.test.coverage.LineCoverage;
import org.jdom.Element;

public class DropIndexSentence implements ISQLSentence {
	private int line;
	private int linePosition;
	private String filePath;
	
	private Identifier indexName;
	
	
	public boolean execute(PostContext context, VariableRepository valRepo) throws ExecutionException
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
			throw new ExecutionException(e, e.getMessage());
		}
		
		try {
			con.dropIndex(this.indexName);
		} catch (DataSourceException e) {
			e.printStackTrace();
			context.getCore().getLogger().reportError(e);
		}
		
		return true;
	}
	
	public IScriptVariable getReturnedVariable(PostContext context)
	{
		return context.getReturnedVariable(this);
	}

	public StepInCandidates getStepInCandidates()
	{
		return null;
	}
	
	public int getLine() {
		return line;
	}
	public void setLine(int line) {
		this.line = line;
	}
	public int getLinePosition() {
		return linePosition;
	}
	public void setLinePosition(int linePosition) {
		this.linePosition = linePosition;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
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
		Element selectElement = new Element(IExecutable.TAG_EXECUTABLE);
		
		selectElement.setAttribute(IExecutable.ATTR_CLASS, this.getClass().getName());
		
		parent.addContent(selectElement);
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
		return this.indexName.isReady(context, valRepo, adjustWhere);
	}

	@Override
	public boolean isPrecompilable() {
		return false;
	}

	@Override
	public void setPrecompilable(boolean b) {
		
	}

	public Identifier getIndexName() {
		return indexName;
	}

	public void setIndexName(Identifier indexName) {
		this.indexName = indexName;
	}

	
}

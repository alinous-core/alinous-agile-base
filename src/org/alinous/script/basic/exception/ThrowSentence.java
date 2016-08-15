package org.alinous.script.basic.exception;

import java.util.List;

import org.alinous.debug.StepInCandidates;
import org.alinous.exec.IExecutable;
import org.alinous.exec.ScriptCheckContext;
import org.alinous.exec.ScriptError;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.AlinousScript;
import org.alinous.script.IScriptSentence;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.statement.FunctionCall;
import org.alinous.test.coverage.FileCoverage;
import org.alinous.test.coverage.LineCoverage;
import org.jdom.Element;

public class ThrowSentence implements IScriptSentence {

	private int line;
	private int linePosition;
	private String filePath;
	
	private IStatement throwStatement;
	
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
	public boolean execute(PostContext context, VariableRepository valRepo)
			throws ExecutionException, RedirectRequestException {
		String errorString = null;
		
		this.throwStatement.setCallerSentence(this);
		IScriptVariable throwStatementValue = this.throwStatement.executeStatement(context, valRepo);
		errorString = ((ScriptDomVariable)throwStatementValue).getValue();
		
		ExecutionException e = new ExecutionException(errorString, this.filePath, this.line);
		
		throw e;
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
		scContext.setCurrentExecutable(this);
		this.throwStatement.checkStaticErrors(scContext, errorList);
		scContext.setCurrentExecutable(null);
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
		StepInCandidates candidates = new StepInCandidates();
		
		return candidates;
	}

	@Override
	public IScriptVariable getReturnedVariable(PostContext context) {
		return null;
	}

	public IStatement getThrowStatement() {
		return throwStatement;
	}

	public void setThrowStatement(IStatement throwStatement) {
		this.throwStatement = throwStatement;
	}

	
}

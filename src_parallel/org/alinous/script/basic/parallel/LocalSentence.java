package org.alinous.script.basic.parallel;

import java.util.List;

import org.alinous.debug.StepInCandidates;
import org.alinous.exec.ScriptCheckContext;
import org.alinous.exec.ScriptError;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.parallel.AlinousThreadScope;
import org.alinous.script.AlinousScript;
import org.alinous.script.IScriptSentence;
import org.alinous.script.basic.type.VariableDescriptor;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.statement.FunctionCall;
import org.alinous.test.coverage.FileCoverage;
import org.alinous.test.coverage.LineCoverage;
import org.jdom.Element;

public class LocalSentence implements IScriptSentence
{
	private VariableDescriptor operand;

	private int line;
	private int linePosition;
	private String filePath;
	

	@Override
	public boolean execute(PostContext context, VariableRepository valRepo)
			throws ExecutionException, RedirectRequestException
	{
		context.getCore().reporttExecuted(this);

		return true;
	}
	
	public void handleRegister(AlinousThreadScope scope, PostContext mainThreadContext, PostContext newContext, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		//AlinousDebug.debugOut("FinalSentence : start scope is ");

		// force getting latest value by inputing null as context
		IScriptVariable val = valRepo.getVariable(operand.getPath(), mainThreadContext);
		if(val == null && this.operand.getPrefix().equals("$")){
			ScriptDomVariable nullVal = new ScriptDomVariable(operand.getPath().getLast().getPathString(mainThreadContext, valRepo));
			nullVal.setValue(null);
			nullVal.setValueType(IScriptVariable.TYPE_NULL);
			val = nullVal;
		}else if(val == null && this.operand.getPrefix().equals("@")){
			ScriptArray nullArray = new ScriptArray(operand.getPath().getLast().getPathString(mainThreadContext, valRepo));
			val = nullArray;
		}
		
		try {
			valRepo.putFinalValue(operand.getPath(), (IScriptVariable) val.clone(), newContext);
		} catch (CloneNotSupportedException e) {
			throw new ExecutionException(e, "Failed to clone().");
		}
		
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
		
		this.operand.canStepInStatements(candidates);
		
		return candidates;
	}

	@Override
	public IScriptVariable getReturnedVariable(PostContext context)
	{
		return context.getReturnedVariable(this);
	}

	public VariableDescriptor getOperand() {
		return operand;
	}

	public void setOperand(VariableDescriptor operand) {
		this.operand = operand;
	}
	
	
}

package org.alinous.script.basic;

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
import org.alinous.script.basic.type.StatementJDomFactory;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.statement.FunctionCall;
import org.alinous.test.coverage.FileCoverage;
import org.alinous.test.coverage.LineCoverage;
import org.jdom.Element;

public class RedirectSentence implements IScriptSentence
{
	private int line;
	private int linePosition;
	private String filePath;
	
	private IStatement redirectUrl;
	
	private IStatement redirectCode;
	
	public String getFilePath()
	{
		return this.filePath;
	}

	public IScriptVariable getReturnedVariable(PostContext context)
	{
		return context.getReturnedVariable(this);
	}

	public StepInCandidates getStepInCandidates()
	{
		StepInCandidates candidates = new StepInCandidates();
		
		this.redirectUrl.canStepInStatements(candidates);
		
		return candidates;
	}

	public void setFilePath(String filePath)
	{
		this.filePath = filePath;
		
		if(this.redirectUrl != null){
			this.redirectUrl.setFilePath(filePath);
		}
	}

	public boolean execute(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		context.getCore().reporttExecuted(this);
		
		// implement it
		this.redirectUrl.setCallerSentence(this);
		//this.returnedVariable = this.redirectUrl.executeStatement(context, valRepo);
		
		ScriptDomVariable dom = (ScriptDomVariable) this.redirectUrl.executeStatement(context, valRepo);
		ScriptDomVariable domCode = new ScriptDomVariable("redirectCode");
		domCode.setValueType(IScriptVariable.TYPE_NUMBER);
		domCode.setValue("302");
		
		if(this.redirectCode != null){
			domCode = (ScriptDomVariable) this.redirectCode.executeStatement(context, valRepo);
			domCode.setName("redirectCode");
		}
		dom.put(domCode);
		
		context.setReturnedVariable(this, dom);
		
		return true;
	}

	public void exportIntoJDomElement(Element parent) throws AlinousException
	{
		Element returnElement = new Element(IExecutable.TAG_EXECUTABLE);
		returnElement.setAttribute(IExecutable.ATTR_CLASS, this.getClass().getName());
		
		parent.addContent(returnElement);
		
		// statement
		this.redirectUrl.exportIntoJDomElement(returnElement);
	}

	public int getLine()
	{
		return this.line;
	}

	public void importFromJDomElement(Element element) throws AlinousException
	{
		Element el = element.getChild(IStatement.TAG_STATEMENT);
		
		this.redirectUrl = StatementJDomFactory.createStatementFromDom(el);
		if(this.redirectUrl != null){
			this.redirectUrl.importFromJDomElement(el);
		}
	}

	public int getLinePosition()
	{
		return this.linePosition;
	}

	public void setLine(int line)
	{
		this.line = line;
		
	}

	public void setLinePosition(int pos)
	{
		this.linePosition = pos;
		
	}

	public IStatement getRedirectUrl()
	{
		return redirectUrl;
	}

	public void setRedirectUrl(IStatement redirectUrl)
	{
		this.redirectUrl = redirectUrl;
	}

	public void checkStaticErrors(ScriptCheckContext scContext,
			List<ScriptError> errorList)
	{
		scContext.setCurrentExecutable(this);
		
		if(this.redirectUrl != null){
			this.redirectUrl.checkStaticErrors(scContext, errorList);
		}
		
		scContext.setCurrentExecutable(null);
	}

	@Override
	public void getFunctionCall(ScriptCheckContext scContext, List<FunctionCall> call, AlinousScript script)
	{
		if(this.redirectUrl != null){
			this.redirectUrl.getFunctionCall(scContext, call, script);
		}
	}
	
	@Override
	public void setupCoverage(FileCoverage coverage)
	{
		LineCoverage lineCoverage = new LineCoverage(this.line, this.linePosition, this);
		coverage.addLineCoverage(lineCoverage);
		
	}

	public IStatement getRedirectCode() {
		return redirectCode;
	}

	public void setRedirectCode(IStatement redirectCode) {
		this.redirectCode = redirectCode;
	}
}

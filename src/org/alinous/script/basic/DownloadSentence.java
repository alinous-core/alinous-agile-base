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
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.statement.FunctionCall;
import org.alinous.test.coverage.FileCoverage;
import org.alinous.test.coverage.LineCoverage;
import org.jdom.Element;

public class DownloadSentence implements IScriptSentence
{
	private int line;
	private int linePosition;
	private String filePath;
	
	private IStatement downloadFile;
	private IStatement downloadFileName;
	private IStatement contentType;
	
	public String getFilePath()
	{
		return this.filePath;
	}

	public StepInCandidates getStepInCandidates()
	{
		StepInCandidates candidates = new StepInCandidates();
		
		this.downloadFile.canStepInStatements(candidates);
		
		if(this.contentType != null){
			this.contentType.canStepInStatements(candidates);
		}
		
		return candidates;
	}

	public void setFilePath(String filePath)
	{
		this.filePath = filePath;
	}
	
	
	
	public IStatement getDownloadFileName()
	{
		return downloadFileName;
	}

	public void setDownloadFileName(IStatement downloadFileName)
	{
		this.downloadFileName = downloadFileName;
	}
	
	public IScriptVariable getDownloadFileNameValue(PostContext context)
	{
		return context.getDownloadFileNameValue();
	}

	public boolean execute(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{		
		// implement it
		this.downloadFile.setCallerSentence(this);
		IScriptVariable returnedVariable = this.downloadFile.executeStatement(context, valRepo);
		context.setReturnedVariable(this, returnedVariable);
		
		if(this.contentType != null){
			this.contentType.setCallerSentence(this);
			//this.contentTypeValue = this.contentType.executeStatement(context, valRepo);
			context.setContentTypeValue(this.contentType.executeStatement(context, valRepo));
		}
		
		if(this.downloadFileName != null){
			this.downloadFileName.setCallerSentence(this);
			//this.downloadFileNameValue = this.downloadFileName.executeStatement(context, valRepo);
			context.setDownloadFileNameValue(this.downloadFileName.executeStatement(context, valRepo));
		}
		
		context.getCore().reporttExecuted(this);
		return true;
	}

	public void exportIntoJDomElement(Element parent) throws AlinousException
	{
		Element returnElement = new Element(IExecutable.TAG_EXECUTABLE);
		returnElement.setAttribute(IExecutable.ATTR_CLASS, this.getClass().getName());
		
		parent.addContent(returnElement);
		
		// statement
		this.downloadFile.exportIntoJDomElement(returnElement);
		
		if(this.contentType != null){
			this.contentType.exportIntoJDomElement(returnElement);
		}
		
		if(this.downloadFileName != null){
			this.downloadFileName.exportIntoJDomElement(returnElement);
		}
	}

	public int getLine()
	{
		return this.line;
	}

	public void importFromJDomElement(Element element) throws AlinousException
	{
		Element el = element.getChild(IStatement.TAG_STATEMENT);
		
		this.downloadFile = StatementJDomFactory.createStatementFromDom(el);
		if(this.downloadFile != null){
			this.downloadFile.importFromJDomElement(el);
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

	public IScriptVariable getContentTypeValue(PostContext context)
	{
		return context.getContentTypeValue();
	}


	public void setContentType(IStatement contentType)
	{
		this.contentType = contentType;
	}

	public void setDownloadFile(IStatement downloadFile)
	{
		this.downloadFile = downloadFile;
	}

	public IScriptVariable getReturnedVariable(PostContext context)
	{
		return context.getReturnedVariable(this);
	}

	public void setCurrentDataSource(String dataSource)
	{
	}

	public void checkStaticErrors(ScriptCheckContext scContext,
			List<ScriptError> errorList)
	{
		scContext.setCurrentExecutable(this);
		
		if(this.downloadFile != null){
			this.downloadFile.checkStaticErrors(scContext, errorList);
		}
		if(this.downloadFileName != null){
			this.downloadFileName.checkStaticErrors(scContext, errorList);
		}
		if(this.contentType != null){
			this.contentType.checkStaticErrors(scContext, errorList);
		}
		
		scContext.setCurrentExecutable(null);
	}

	@Override
	public void getFunctionCall(ScriptCheckContext scContext, List<FunctionCall> call, AlinousScript script)
	{
		if(this.downloadFile != null){
			this.downloadFile.getFunctionCall(scContext, call, script);
		}
		if(this.downloadFileName != null){
			this.downloadFileName.getFunctionCall(scContext, call, script);
		}
		if(this.contentType != null){
			this.contentType.getFunctionCall(scContext, call, script);
		}
	}
	
	@Override
	public void setupCoverage(FileCoverage coverage)
	{
		LineCoverage lineCoverage = new LineCoverage(this.line, this.linePosition, this);
		coverage.addLineCoverage(lineCoverage);
		
	}

}

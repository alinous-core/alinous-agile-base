package org.alinous.script.sql.ddl.plsql;

import org.alinous.datasrc.types.TypeHelper;
import org.alinous.debug.StepInCandidates;
import org.alinous.exec.IExecutable;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.script.ISQLSentence;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.adjustopt.AdjustSet;
import org.alinous.script.sql.adjustopt.AdjustWhere;
import org.jdom.Element;

public abstract class FunctionBase implements ISQLSentence
{
	protected String filePath;
	protected int line;
	protected int linePosition;
	
	
	public boolean isPrecompilable()
	{
		return false;
	}

	public void setPrecompilable(boolean b)
	{
	}

	public String getFilePath()
	{
		return this.filePath;
	}

	public IScriptVariable getReturnedVariable(PostContext context)
	{
		return null;
	}

	public StepInCandidates getStepInCandidates()
	{
		return null;
	}

	public void setFilePath(String filePath)
	{
		this.filePath = filePath;		
	}

	public int getLine()
	{
		return this.line;
	}

	public int getLinePosition()
	{
		return this.linePosition;
	}

	public void setLine(int line)
	{
		this.line = line;
	}

	public void setLinePosition(int linePosition)
	{
		this.linePosition = linePosition;
	}

	public void exportIntoJDomElement(Element parent) throws AlinousException
	{
		Element selectElement = new Element(IExecutable.TAG_EXECUTABLE);
		
		selectElement.setAttribute(IExecutable.ATTR_CLASS, this.getClass().getName());
		
		parent.addContent(selectElement);
		
	}

	public void importFromJDomElement(Element threadElement)
			throws AlinousException
	{

	}

	public String extract(PostContext context, VariableRepository valRepo,
			AdjustWhere adjustWhere, AdjustSet adjustSet, TypeHelper helper)
			throws ExecutionException
	{
		return null;
	}

	public boolean isReady(PostContext context, VariableRepository valRepo,
			AdjustWhere adjustWhere) throws ExecutionException
	{
		return true;
	}

}

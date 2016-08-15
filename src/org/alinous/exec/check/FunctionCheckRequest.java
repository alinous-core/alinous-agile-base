package org.alinous.exec.check;

import org.alinous.script.AlinousScript;
import org.alinous.script.statement.FunctionCall;

public class FunctionCheckRequest {
	private FunctionCall functionCall;
	private AlinousScript rootScript;
	
	public FunctionCall getFunctionCall()
	{
		return functionCall;
	}

	public void setFunctionCall(FunctionCall functionCall)
	{
		this.functionCall = functionCall;
	}

	public AlinousScript getRootScript()
	{
		return rootScript;
	}

	public void setRootScript(AlinousScript rootScript)
	{
		this.rootScript = rootScript;
	}
	
	
}

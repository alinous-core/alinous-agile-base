package org.alinous.script.functions.system;

import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class SystemGetLine extends AbstractSystemFunction {
	public static String QUALIFIED_NAME = "SYSTEM.GETLINE";
	
	@Override
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo)
			throws ExecutionException, RedirectRequestException
	{
		ScriptDomVariable dom = new ScriptDomVariable("ret");
		dom.setValueType(IScriptVariable.TYPE_STRING);
		dom.setValue(Integer.toString(context.getCurrentLine()));
		
		return dom;
	}
	
	@Override
	public String getName()
	{
		return QUALIFIED_NAME;
	}
	
	@Override
	public String codeAssistString() {
		return "System.getLine()";
	}

	@Override
	public String descriptionString() {
		return "Get the line of this function in the Alinous Script.";
	}

}

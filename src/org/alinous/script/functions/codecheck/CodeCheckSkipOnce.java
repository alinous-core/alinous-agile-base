package org.alinous.script.functions.codecheck;

import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.script.functions.system.AbstractSystemFunction;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.VariableRepository;

public class CodeCheckSkipOnce extends AbstractSystemFunction
{
	public static final String QUALIFIED_NAME = "CODECHECK.SKIPONCE";
	
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo) throws ExecutionException
	{
		return null;
	}
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}

	@Override
	public String codeAssistString() {
		return "CodeCheck.skipOnce";
	}

	@Override
	public String descriptionString() {
		return "Skip code check of SQL sentence next to this function called.";
	}
}

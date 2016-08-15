package org.alinous.script.functions.system;

import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class ThreadGetId  extends AbstractSystemFunction{
	
	public static String QUALIFIED_NAME = "THREAD.GETID";
	
	public ThreadGetId()
	{
		
	}
	
	@Override
	public IScriptVariable executeFunction(PostContext context,
			VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		long id = Thread.currentThread().getId();
		
		ScriptDomVariable retVal = new ScriptDomVariable("id");
		retVal.setValueType(IScriptVariable.TYPE_STRING);
		retVal.setValue(Long.toString(id));
		
		return retVal;
	}
	
	@Override
	public String getName()
	{
		return QUALIFIED_NAME;
	}
	
	@Override
	public String codeAssistString() {
		return "Thread.getId()";
	}

	@Override
	public String descriptionString() {
		return "Get current thread id.";
	}

}

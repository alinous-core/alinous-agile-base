package org.alinous.net.pop3.api;

import java.util.Stack;

import org.alinous.exec.pages.IExtResource;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.functions.system.AbstractSystemFunction;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class Pop3Close extends AbstractSystemFunction
{
	public static final String QUALIFIED_NAME = "POP3.CLOSE";
	
	public static final String RESOURCE_NAME = "resourceName";
	
	public Pop3Close()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", RESOURCE_NAME);
		this.argmentsDeclare.addArgument(arg);
	}
	
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		Stack<IStatement> stmtStack = context.getFuncArgStack();
		
		if(stmtStack.size() != this.argmentsDeclare.getSize()){
			throw new ExecutionException("Number of the function's arguments is wrong : " + QUALIFIED_NAME);// i18n
		}
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		
		// variables
		IPathElement ipath = PathElementFactory.buildPathElement(RESOURCE_NAME);
		IScriptVariable resourceNameVariable = newValRepo.getVariable(ipath, context);
		if(!(resourceNameVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME + " : " + RESOURCE_NAME);// i18n
		}
		
		// String variables
		String resourceName = ((ScriptDomVariable)resourceNameVariable).getValue();
		
		IExtResource resource = context.getExtResource(Pop3Connection.POP3_RESOURCE_TYPE_NAME, resourceName);
		if(resource == null || !(resource instanceof Pop3Connection)){
			ScriptDomVariable ret = new ScriptDomVariable("RETURN");
			ret.setValue("false");
			ret.setValueType(IScriptVariable.TYPE_BOOLEAN);
			return ret;
		}
		
		// not necessary because disposeExtResource() automatically call
		//((Pop3Connection)resource).discard();
		
		context.disposeExtResource(resource.getType(), resource.getName());
		
		ScriptDomVariable ret = new ScriptDomVariable("RETURN");
		ret.setValue("true");
		ret.setValueType(IScriptVariable.TYPE_BOOLEAN);
		return ret;
	}
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}

	@Override
	public String codeAssistString() {
		return "Pop3.close($resourceName)";
	}

	@Override
	public String descriptionString() {
		return "Close pop3 Connection.";
	}

}

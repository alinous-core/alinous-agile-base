package org.alinous.script.functions.system;

import java.util.Stack;

import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.basic.type.VariableDescriptor;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class VariableGetPath extends AbstractSystemFunction {
	public static String QUALIFIED_NAME = "VARIABLE.GETPATH";
	public static String GET_PATH = "path";
	
	public VariableGetPath()
	{
		ArgumentDeclare arg = new ArgumentDeclare("*", GET_PATH);
		this.argmentsDeclare.addArgument(arg);
	}
	
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		Stack<IStatement> stmtStack = context.getFuncArgStack();
		
		if(stmtStack.size() != 1){
			throw new ExecutionException("Number of the function is wrong.");// i18n
		}
		
		IStatement stmt = stmtStack.get(0);
		if(!(stmt instanceof VariableDescriptor)){
			throw new ExecutionException("The argument must be a variable descriptor.");// i18n
		}
		
		VariableDescriptor variableDesc = (VariableDescriptor)stmt;
		
		String path = variableDesc.toString(context, valRepo);
		
		ScriptDomVariable pathDom = new ScriptDomVariable("path");
		pathDom.setValue(path);
		pathDom.setValueType(IScriptVariable.TYPE_STRING);
		
		return pathDom;
	}
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}
	
	@Override
	public String codeAssistString() {
		return "Variable.getPath($variable)";
	}

	@Override
	public String descriptionString() {
		return "Get the path of the variable.";
	}

}

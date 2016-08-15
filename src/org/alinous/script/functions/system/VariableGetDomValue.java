package org.alinous.script.functions.system;

import java.util.Stack;

import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class VariableGetDomValue extends AbstractSystemFunction {
	public static String QUALIFIED_NAME = "VARIABLE.GETDOMVARIABLE";
	public static String DOM_ARG = "arg0";
	
	public VariableGetDomValue()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", DOM_ARG);
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
		
		IPathElement ipath = PathElementFactory.buildPathElement(DOM_ARG);
		IScriptVariable pathVariable = newValRepo.getVariable(ipath, context);
		
		if(!(pathVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		ScriptDomVariable dom = (ScriptDomVariable)pathVariable;
		
		ScriptDomVariable retDom = new ScriptDomVariable("dom");
		retDom.setValue(dom.getValue());
		retDom.setValueType(dom.getValueType());
		
		return retDom;
	}

	public String getName()
	{
		return QUALIFIED_NAME;
	}
	
	@Override
	public String codeAssistString() {
		return "Variable.getDomVariable($path)";
	}

	@Override
	public String descriptionString() {
		return "Get Variable corresponding to $path string.\n $path does not include(nor start with) '$'.";
	}

}

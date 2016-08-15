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
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.ScriptVariable;
import org.alinous.script.runtime.VariableRepository;

public class VariableIsArray extends AbstractSystemFunction {
	public static String QUALIFIED_NAME = "VARIABLE.ISARRAY";
	public static String GET_PATH = "path";
	
	public VariableIsArray()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", GET_PATH);
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
		
		IPathElement ipath = PathElementFactory.buildPathElement(GET_PATH);
		IScriptVariable pathVariable = newValRepo.getVariable(ipath, context);
		
		if(!(pathVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		String strPath = ((ScriptDomVariable)pathVariable).getValue();
		ipath = PathElementFactory.buildPathElement(strPath);
		
		
		IScriptVariable retVal = valRepo.getVariable(ipath, context);
		if(retVal instanceof ScriptArray){
			ScriptVariable blDom = new ScriptDomVariable("bl");
			blDom.setValue("true");
			blDom.setValueType(IScriptVariable.TYPE_BOOLEAN);
			
			return blDom;
		}
		
		ScriptVariable blDom = new ScriptDomVariable("bl");
		blDom.setValue("false");
		blDom.setValueType(IScriptVariable.TYPE_BOOLEAN);
		
		return blDom;
	}
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}


	@Override
	public String codeAssistString() {
		return "Variable.isArray($path)";
	}


	@Override
	public String descriptionString() {
		return "Check if the Variable corresponding to $path string is Array.\n $path does not include(nor start with) '$' and '@'" +
				"\n If the variable is Array, returns true.";
	}
	
}

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
import org.alinous.script.runtime.VariableRepository;

public class VariableSort extends AbstractSystemFunction
{
	public static String QUALIFIED_NAME = "VARIABLE.SORT";
	public static String ARRAY_VAL = "array";
	public static String SORT_KEY = "sortKey";
	public static String ASC = "asc";
	
	public VariableSort()
	{
		ArgumentDeclare arg = new ArgumentDeclare("@", ARRAY_VAL);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", SORT_KEY);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", ASC);
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
		
		IPathElement ipath = PathElementFactory.buildPathElement(ARRAY_VAL);
		IScriptVariable arrayVal = newValRepo.getVariable(ipath, context);
		if(!(arrayVal instanceof ScriptArray)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		ipath = PathElementFactory.buildPathElement(SORT_KEY);
		IScriptVariable sortKey = newValRepo.getVariable(ipath, context);
		if(!(sortKey instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		ipath = PathElementFactory.buildPathElement(ASC);
		IScriptVariable ascVal = newValRepo.getVariable(ipath, context);
		if(!(ascVal instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		String sortKeyStr = ((ScriptDomVariable)sortKey).getValue();
		String ascStr = ((ScriptDomVariable)ascVal).getValue();
		
		ScriptArray scriptArray = (ScriptArray)arrayVal;
		
		scriptArray.sort(sortKeyStr, ascStr.equals("true"));
		
		return scriptArray;
	}	
	
	@Override
	public String getName()
	{
		return QUALIFIED_NAME;
	}

	@Override
	public String codeAssistString() {
		return "Variable.sort(@array, $sortKey, $asc)";
	}

	@Override
	public String descriptionString() {
		return "Sort Array Variable @array with $sortKey.\n$asc is true or false";
	}
}

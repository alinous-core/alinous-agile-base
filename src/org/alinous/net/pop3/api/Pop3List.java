package org.alinous.net.pop3.api;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.alinous.exec.pages.IExtResource;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.net.pop3.ListCommand;
import org.alinous.net.pop3.ListResult;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.functions.system.AbstractSystemFunction;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class Pop3List extends AbstractSystemFunction
{
	public static final String QUALIFIED_NAME = "POP3.LIST";
	
	public static final String RESOURCE_NAME = "resourceName";
	
	public Pop3List()
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
		
		ListCommand listCom = null;
		try {
			listCom = ((Pop3Connection)resource).list();
		} catch (IOException e) {
			e.printStackTrace();
			context.getCore().getLogger().reportError(e);
			
			ScriptDomVariable ret = new ScriptDomVariable("RETURN");
			ret.setValue("false");
			ret.setValueType(IScriptVariable.TYPE_BOOLEAN);
			return ret;
		}
		
		// list 2 Dom
		ScriptDomVariable ret = new ScriptDomVariable("RETURN");
		ret.setValue("true");
		ret.setValueType(IScriptVariable.TYPE_BOOLEAN);
		
		ScriptArray ar = new ScriptArray("LIST");
		
		List<ListResult> list = listCom.getMessages();
		Iterator<ListResult> it = list.iterator();
		while(it.hasNext()){
			ListResult res = it.next();
			/*
			ScriptDomVariable offset = new ScriptDomVariable("OFFSET");
			ret.setValue(Integer.toString(res.getOffset()));
			ret.setValueType(IScriptVariable.TYPE_NUMBER);
			*/
			ScriptDomVariable bytes = new ScriptDomVariable("BYTES");
			bytes.setValue(Integer.toString(res.getBytes()));
			bytes.setValueType(IScriptVariable.TYPE_NUMBER);
			
			ar.add(bytes);
		}
		
		ret.put(ar);
		
		return ret;
	}
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}

	@Override
	public String codeAssistString() {
		return "Pop3.list($resourceName)";
	}

	@Override
	public String descriptionString() {
		return "Send the 'LIST' command.\n" +
				"Return array variable." +
				"\n" +
				"@RET = Pop3.list($resourceName);";
	}
}

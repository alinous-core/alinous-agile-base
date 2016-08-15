package org.alinous.net.pop3.api;

import java.io.IOException;
import java.util.Stack;

import org.alinous.exec.pages.IExtResource;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.MailException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.net.pop3.DeleCommand;
import org.alinous.net.pop3.Pop3Protocol;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.functions.system.AbstractSystemFunction;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class Pop3DeleteMail extends AbstractSystemFunction
{
	public static final String QUALIFIED_NAME = "POP3.DELETE";
	
	public static final String RESOURCE_NAME = "resourceName";
	public static final String OFFSET = "offset";
	
	public Pop3DeleteMail()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", RESOURCE_NAME);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", OFFSET);
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
		
		IPathElement ipath = PathElementFactory.buildPathElement(RESOURCE_NAME);
		IScriptVariable resourceNameVariable = newValRepo.getVariable(ipath, context);
		if(!(resourceNameVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME + " : " + RESOURCE_NAME);// i18n
		}
		
		ipath = PathElementFactory.buildPathElement(OFFSET);
		IScriptVariable offsetVariable = newValRepo.getVariable(ipath, context);
		if(!(offsetVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME + " : " + OFFSET);// i18n
		}
		
		String resourceName = ((ScriptDomVariable)resourceNameVariable).getValue();
		String offSetStr = ((ScriptDomVariable)offsetVariable).getValue();
		int offSet = Integer.parseInt(offSetStr);
		
		IExtResource resource = context.getExtResource(Pop3Connection.POP3_RESOURCE_TYPE_NAME, resourceName);
		if(resource == null || !(resource instanceof Pop3Connection)){
			ScriptDomVariable ret = new ScriptDomVariable("RETURN");
			ret.setValue("false");
			ret.setValueType(IScriptVariable.TYPE_BOOLEAN);
			return ret;
		}
		
		try {
			doExecute(context, resourceName, offSet, ((Pop3Connection)resource).getProtocol());
		} catch (IOException e) {
			e.printStackTrace();
			context.getCore().getLogger().reportError(e);
			
			ScriptDomVariable ret = new ScriptDomVariable("RETURN");
			ret.setValue("false");
			ret.setValueType(IScriptVariable.TYPE_BOOLEAN);
			return ret;
		}
		
		
		ScriptDomVariable ret = new ScriptDomVariable("RETURN");
		ret.setValue("true");
		ret.setValueType(IScriptVariable.TYPE_BOOLEAN);
		return ret;
	}
	
	private void doExecute(PostContext context, String resourceName, int offSet, Pop3Protocol popProtocol) throws MailException, IOException
	{
		// List
		/* listCom = new ListCommand(protocol);
		listCom.sendCommand();
		listCom.receiveCommand();*/
		DeleCommand deleCom = new DeleCommand(popProtocol, offSet);
		deleCom.sendCommand();
		deleCom.receiveCommand();
		
	}
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}

	@Override
	public String codeAssistString() {
		return "Pop3.delete($resourceName, $offset)";
	}

	@Override
	public String descriptionString() {
		return "Delete server mail. $offset is the number returned by Pop3.list().";
	}
}

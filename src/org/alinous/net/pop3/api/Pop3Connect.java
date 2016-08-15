package org.alinous.net.pop3.api;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Stack;

import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.MailException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.functions.system.AbstractSystemFunction;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class Pop3Connect extends AbstractSystemFunction
{
	public static final String QUALIFIED_NAME = "POP3.CONNECT";
	
	public static final String RESOURCE_NAME = "resourceName";
	public static final String USER = "usr";
	public static final String PASS = "pass";
	public static final String POP_SERVER = "popServer";
	public static final String POP_PORT = "port";
	
	public Pop3Connect()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", RESOURCE_NAME);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", USER);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", PASS);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", POP_SERVER);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", POP_PORT);
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
		
		ipath = PathElementFactory.buildPathElement(USER);
		IScriptVariable userVariable = newValRepo.getVariable(ipath, context);
		if(!(userVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME + " : " + USER);// i18n
		}
		
		ipath = PathElementFactory.buildPathElement(PASS);
		IScriptVariable passVariable = newValRepo.getVariable(ipath, context);
		if(!(passVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME + " : " + PASS);// i18n
		}
		
		ipath = PathElementFactory.buildPathElement(POP_SERVER);
		IScriptVariable popServerVariable = newValRepo.getVariable(ipath, context);
		if(!(popServerVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME + " : " + POP_SERVER);// i18n
		}
		
		ipath = PathElementFactory.buildPathElement(POP_PORT);
		IScriptVariable popPortVariable = newValRepo.getVariable(ipath, context);
		if(!(popPortVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME + " : " + POP_PORT);// i18n
		}
		
		// String variables
		String resourceName = ((ScriptDomVariable)resourceNameVariable).getValue();
		String user = ((ScriptDomVariable)userVariable).getValue();
		String pass = ((ScriptDomVariable)passVariable).getValue();
		String popServer = ((ScriptDomVariable)popServerVariable).getValue();
		String popPortStr = ((ScriptDomVariable)popPortVariable).getValue();
		int popPort = Integer.parseInt(popPortStr);
		
		
		try {
			doConnect(context, resourceName, user, pass, popServer, popPort);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			
			ScriptDomVariable ret = new ScriptDomVariable("RETURN");
			ret.setValue("false");
			ret.setValueType(IScriptVariable.TYPE_BOOLEAN);
			return ret;
		} catch (IOException e) {
			e.printStackTrace();
			
			ScriptDomVariable ret = new ScriptDomVariable("RETURN");
			ret.setValue("false");
			ret.setValueType(IScriptVariable.TYPE_BOOLEAN);
			return ret;
		}catch (MailException e) {
			e.printStackTrace();
			
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
	
	private void doConnect(PostContext context,
			String resourceName, String user, String pass, String popServer, int popPort) throws UnknownHostException, IOException, MailException
	{
		Pop3Connection con = new Pop3Connection(user, pass, popServer, popPort);
		
		con.setName(resourceName);
		
		con.connect();
		
		context.registerExternalResource(con.getType(), con.getName(), con);
	}
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}
	
	@Override
	public String codeAssistString() {
		return "Pop3.connect($resourceName, $usr, $pass, $popServer, $port)";
	}


	@Override
	public String descriptionString() {
		return "Connect to the pop3 server.";
	}
}

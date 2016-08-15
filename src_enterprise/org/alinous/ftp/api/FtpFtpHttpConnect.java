package org.alinous.ftp.api;

import java.io.IOException;
import java.util.Stack;

import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.ftp.FtpManager;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.functions.system.AbstractSystemFunction;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class FtpFtpHttpConnect extends AbstractSystemFunction {
	public static final String QUALIFIED_NAME = "FTP.FTPHTTPCONNECT";
	
	public static final String SESSION_STRING = "sessionString";
	public static final String CONNECT_URL = "connectUrl";
	public static final String PROXY_PORT = "proxyPort";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";	
	
	public FtpFtpHttpConnect()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", SESSION_STRING);
		this.argmentsDeclare.addArgument(arg);
		arg = new ArgumentDeclare("$", CONNECT_URL);
		this.argmentsDeclare.addArgument(arg);
		arg = new ArgumentDeclare("$", PROXY_PORT);
		this.argmentsDeclare.addArgument(arg);
		arg = new ArgumentDeclare("$", USERNAME);
		this.argmentsDeclare.addArgument(arg);
		arg = new ArgumentDeclare("$", PASSWORD);
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
		
		IPathElement ipath = PathElementFactory.buildPathElement(SESSION_STRING);
		IScriptVariable sessionStringVariable = newValRepo.getVariable(ipath, context);
		ipath = PathElementFactory.buildPathElement(CONNECT_URL);
		IScriptVariable connectUrlStringVariable = newValRepo.getVariable(ipath, context);
		ipath = PathElementFactory.buildPathElement(PROXY_PORT);
		IScriptVariable proxyPortStringVariable = newValRepo.getVariable(ipath, context);
		ipath = PathElementFactory.buildPathElement(USERNAME);
		IScriptVariable usernameStringVariable = newValRepo.getVariable(ipath, context);
		ipath = PathElementFactory.buildPathElement(PASSWORD);
		IScriptVariable passwordStringVariable = newValRepo.getVariable(ipath, context);
		
		
		String sessionId = ((ScriptDomVariable)sessionStringVariable).getValue();
		String connectUrl = ((ScriptDomVariable)connectUrlStringVariable).getValue();
		String proxyPort = ((ScriptDomVariable)proxyPortStringVariable).getValue();
		String username = ((ScriptDomVariable)usernameStringVariable).getValue();
		String password = ((ScriptDomVariable)passwordStringVariable).getValue();
		
		FtpManager mgr = FtpManager.getInstance();
		mgr.maintain();
		
		ScriptDomVariable retVal = null;
		
		try {
			mgr.connect(sessionId, connectUrl, Integer.parseInt(proxyPort), username, password);
			
			retVal = new ScriptDomVariable("result");
			retVal.setValueType(IScriptVariable.TYPE_BOOLEAN);
			retVal.setValue("true");
		} catch (IOException e) {
			e.printStackTrace();
			context.getCore().getLogger().reportError(e);
			
			retVal = new ScriptDomVariable("result");
			retVal.setValueType(IScriptVariable.TYPE_BOOLEAN);
			retVal.setValue("false");
		}
		
		return retVal;
	}
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}
	
	@Override
	public String codeAssistString() {
		return "Ftp.ftpHttpConnect($sessionString, $connectUrl, $proxyPort, $username, $password)";
	}

	@Override
	public String descriptionString() {
		return "Connect with http tunneling using proxy.\n" +
				"The username and password is of proxy.";
	}
	
}

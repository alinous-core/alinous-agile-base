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

public class FtpRemoveDirectory extends AbstractSystemFunction
{
	public static final String QUALIFIED_NAME = "FTP.REMOVEDIRECTORIES";
	
	public static final String SESSION_STRING = "sessionString";
	public static final String PATHNAME = "pathname";
	
	public FtpRemoveDirectory()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", SESSION_STRING);
		this.argmentsDeclare.addArgument(arg);
		arg = new ArgumentDeclare("$", PATHNAME);
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
		ipath = PathElementFactory.buildPathElement(PATHNAME);
		IScriptVariable pathnameStringVariable = newValRepo.getVariable(ipath, context);
		
		String sessionId = ((ScriptDomVariable)sessionStringVariable).getValue();
		String pathname = ((ScriptDomVariable)pathnameStringVariable).getValue();
		
		
		FtpManager mgr = FtpManager.getInstance();
		
		ScriptDomVariable retVal = null;
		try {
			mgr.removeDirectory(sessionId, pathname);
			
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
		return "Ftp.removeDirectories($sessionString, $pathname)";
	}

	@Override
	public String descriptionString() {
		return "Remove the remote directory.";
	}
}

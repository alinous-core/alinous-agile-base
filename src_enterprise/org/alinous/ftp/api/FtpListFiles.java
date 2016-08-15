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
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.apache.commons.net.ftp.FTPFile;

public class FtpListFiles extends AbstractSystemFunction
{
	public static final String QUALIFIED_NAME = "FTP.LISTFILES";
	
	public static final String SESSION_STRING = "sessionString";
	
	public FtpListFiles()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", SESSION_STRING);
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
		
		String sessionId = ((ScriptDomVariable)sessionStringVariable).getValue();
		
		FtpManager mgr = FtpManager.getInstance();
		
		// ftp listDirectories
		ScriptArray retVal = null;
		try {
			
			FTPFile[] files = mgr.listFiles(sessionId);
			
			retVal = new ScriptArray("result");
			
			for(int i = 0; i < files.length; i++){
				if(files[i].isFile()){
					ScriptDomVariable fdom = new ScriptDomVariable("file");
					fdom.setValueType(IScriptVariable.TYPE_STRING);
					fdom.setValue(files[i].getName());
					
					retVal.add(fdom);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			context.getCore().getLogger().reportError(e);
		}
		
		return retVal;
	}
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}

	@Override
	public String codeAssistString() {
		return "Ftp.listFiles($sessionString)";
	}

	@Override
	public String descriptionString() {
		return "List up files and directories in the current dir.";
	}
}

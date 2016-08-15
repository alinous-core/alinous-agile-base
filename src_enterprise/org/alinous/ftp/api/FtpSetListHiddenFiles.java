package org.alinous.ftp.api;

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

public class FtpSetListHiddenFiles extends AbstractSystemFunction
{
	public static final String QUALIFIED_NAME = "FTP.SETLISTHIDDENFILES";
	
	public static final String SESSION_STRING = "sessionString";
	public static final String LIST_HIDDEN_FILES = "listHiddenFiles";
	
	public FtpSetListHiddenFiles()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", SESSION_STRING);
		this.argmentsDeclare.addArgument(arg);
		arg = new ArgumentDeclare("$", LIST_HIDDEN_FILES);
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
		ipath = PathElementFactory.buildPathElement(LIST_HIDDEN_FILES);
		IScriptVariable listHiddenFilesStringVariable = newValRepo.getVariable(ipath, context);
		
		String sessionId = ((ScriptDomVariable)sessionStringVariable).getValue();
		String listHiddenFiles = ((ScriptDomVariable)listHiddenFilesStringVariable).getValue();
		
		FtpManager mgr = FtpManager.getInstance();
		
		ScriptDomVariable retVal = null;
		mgr.setListHiddenFiles(sessionId, Boolean.parseBoolean(listHiddenFiles));
		
		retVal = new ScriptDomVariable("result");
		retVal.setValueType(IScriptVariable.TYPE_BOOLEAN);
		retVal.setValue("true");
		
		return retVal;
	}
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}
	
	@Override
	public String codeAssistString() {
		return "Ftp.setListHiddenFiles($sessionString, $listHiddenFiles)";
	}

	@Override
	public String descriptionString() {
		return "Set the policy of listing file and directories.\n" +
				"By setting $listHiddenFiles to true, hidden files are shown.";
	}
}

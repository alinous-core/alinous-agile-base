package org.alinous.net.pop3.api;

import java.io.IOException;
import java.util.Iterator;
import java.util.Stack;

import org.alinous.exec.pages.IExtResource;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.MailException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.net.pop3.RetrCommand;
import org.alinous.net.pop3.format.MailHeader;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.functions.system.AbstractSystemFunction;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class Pop3GetMail extends AbstractSystemFunction
{
	public static final String QUALIFIED_NAME = "POP3.GETMAIL";
	
	public static final String RESOURCE_NAME = "resourceName";
	public static final String OFFSET = "offset";
	
	
	public Pop3GetMail()
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
		
		RetrCommand retrCom = null;
		try {
			retrCom = doExecute(context, resourceName, offSet, (Pop3Connection)resource);
		} catch (Throwable e) {
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
		
		ScriptDomVariable bodyDom = new ScriptDomVariable("BODY");
		bodyDom.setValue(retrCom.getBodyString());
		bodyDom.setValueType(IScriptVariable.TYPE_STRING);
		ret.put(bodyDom);
		
		ScriptDomVariable subjectDom = new ScriptDomVariable("SUBJECT");
		subjectDom.setValue(retrCom.getSubject());
		subjectDom.setValueType(IScriptVariable.TYPE_STRING);
		ret.put(subjectDom);
		
		ScriptDomVariable muiltipartDom = new ScriptDomVariable("MULTIPART");
		muiltipartDom.setValue(Boolean.toString(retrCom.getMailData().isMultipart()));
		muiltipartDom.setValueType(IScriptVariable.TYPE_BOOLEAN);
		ret.put(muiltipartDom);
		
		ScriptArray headers = new ScriptArray("HEADERS");
		Iterator<String> it = retrCom.getMailData().getHeaders().keySet().iterator();
		while(it.hasNext()){
			String headerKey = it.next();
			
			MailHeader val = retrCom.getMailData().getHeaders().get(headerKey);
			
			ScriptDomVariable headerKeyDom = new ScriptDomVariable("HEADER");
			headerKeyDom.setValue(headerKey);
			headerKeyDom.setValueType(IScriptVariable.TYPE_STRING);
			
			ScriptDomVariable valueDom = new ScriptDomVariable("VALUE");
			valueDom.setValue(val.getHeaderBody());
			valueDom.setValueType(IScriptVariable.TYPE_STRING);
			
			headerKeyDom.put(valueDom);
			
			headers.add(headerKeyDom);
		}
		ret.put(headers);
		
		return ret;
	}
	
	private RetrCommand doExecute(PostContext context, String resourceName, int offSet, Pop3Connection resource) throws MailException, IOException
	{
		// Retr multipart
		RetrCommand retrCom = new RetrCommand(resource.getProtocol(), offSet); // if error, please set correct NO !!
		retrCom.sendCommand();
		retrCom.receiveCommand();
		
		return retrCom;
	}
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}

	@Override
	public String codeAssistString() {
		return "Pop3.getMail($resourceName, $offset)";
	}

	@Override
	public String descriptionString() {
		return "Get mail content at $offset number. Call Pop3.list() to get offset numbers";
	}
}

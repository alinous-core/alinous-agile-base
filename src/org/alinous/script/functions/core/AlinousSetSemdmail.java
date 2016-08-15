package org.alinous.script.functions.core;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Stack;

import org.alinous.AlinousConfig;
import org.alinous.AlinousUtils;
import org.alinous.cloud.file.AlinousFile;
import org.alinous.cloud.file.AlinousFileOutputStream;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.net.mail.AlinousMailConfig;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.functions.system.AbstractSystemFunction;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class AlinousSetSemdmail extends AbstractSystemFunction
{
	public static final String QUALIFIED_NAME = "ALINOUS.SETSENDMAIL";
	
	public static final String CONFIG_PARAMS = "configParams";
	
	public AlinousSetSemdmail()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", CONFIG_PARAMS);
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
		IPathElement ipath = PathElementFactory.buildPathElement(CONFIG_PARAMS);
		IScriptVariable paramVariable = newValRepo.getVariable(ipath, context);
		if(!(paramVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME + " : " + CONFIG_PARAMS);// i18n
		}
		
		String authMethod = getDomValue((ScriptDomVariable) paramVariable, "AUTH_METHOD");
		String authUser = getDomValue((ScriptDomVariable) paramVariable, "AUTH_USER");
		String authPass = getDomValue((ScriptDomVariable) paramVariable, "AUTH_PASS");
		
		String popMethod = getDomValue((ScriptDomVariable) paramVariable, "POP_METHOD");
		String popUser = getDomValue((ScriptDomVariable) paramVariable, "POP_USER");
		String popPass = getDomValue((ScriptDomVariable) paramVariable, "POP_PASS");
		
		String server = getDomValue((ScriptDomVariable) paramVariable, "SERVER");
		String port = getDomValue((ScriptDomVariable) paramVariable, "PORT");
		String langCode = getDomValue((ScriptDomVariable) paramVariable, "LANG_CODE");
		
		AlinousConfig config = context.getCore().getConfig();
		
		AlinousMailConfig mailconfig = new AlinousMailConfig();
		mailconfig.setAuthMethod(authMethod);
		mailconfig.setAuthUser(authUser);
		mailconfig.setAuthPass(authPass);
		
		mailconfig.setPopMethod(popMethod);
		mailconfig.setPopUser(popUser);
		mailconfig.setPopPass(popPass);
		
		mailconfig.setServer(server);
		mailconfig.setPort(Integer.parseInt(port));
		mailconfig.setLangCode(langCode);
		
		config.setMailconfig(mailconfig);
		
		try {
			update(config, context.getCore().getHome());
		} catch (IOException e) {
			e.printStackTrace();
			context.getCore().reportError(e);
			
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
	
	public void update(AlinousConfig alinousConfig, String alinousHome) throws IOException
	{
		String configPath = AlinousUtils.getAbsolutePath(alinousHome, "alinous-config.xml");
		AlinousFile configFile = new AlinousFile(configPath);
		
		OutputStream stream = new AlinousFileOutputStream(configFile);
		
		OutputStreamWriter wr = null;
		PrintWriter pr = null;
		try{
			wr = new OutputStreamWriter(stream, "utf-8");
			pr = new PrintWriter(wr);
			alinousConfig.writeAsString(pr);
			pr.flush();
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}finally{
			try {
				wr.close();
			} catch (IOException e) {}
			
			try {
				pr.close();
				stream.close();
			} catch (IOException e) {}
		}
		
	}
	
	private String getDomValue(ScriptDomVariable dom, String paramName)
	{
		IScriptVariable val = dom.get(paramName);
		if(val instanceof ScriptDomVariable){
			return ((ScriptDomVariable)val).getValue();
		}
		
		return null;
	}
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}

	@Override
	public String codeAssistString() {
		return null;
	}

	@Override
	public String descriptionString() {
		return null;
	}
}

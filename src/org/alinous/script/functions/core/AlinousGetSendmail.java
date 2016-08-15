package org.alinous.script.functions.core;

import org.alinous.AlinousConfig;
import org.alinous.AlinousCore;
import org.alinous.AlinousDebug;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.net.mail.AlinousMailConfig;
import org.alinous.script.functions.system.AbstractSystemFunction;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class AlinousGetSendmail extends AbstractSystemFunction
{
	public static final String QUALIFIED_NAME = "ALINOUS.GETSENDMAIL";
	
	public AlinousGetSendmail()
	{
		
	}
	
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo) throws ExecutionException
	{
		AlinousConfig config = context.getCore().getConfig();
		
		AlinousMailConfig mailconfig = config.getMailconfig();
		if(mailconfig == null){
			ScriptDomVariable ret = new ScriptDomVariable("RETURN");
			ret.setValue("true");
			ret.setValueType(IScriptVariable.TYPE_BOOLEAN);
		}
		
		ScriptDomVariable ret = new ScriptDomVariable("RETURN");
		ret.setValue("true");
		ret.setValueType(IScriptVariable.TYPE_BOOLEAN);
		
		String authMethod = mailconfig.getAuthMethod();
		String authUser =  mailconfig.getAuthUser();
		String authPass = mailconfig.getAuthPass();
		
		String popMethod = mailconfig.getPopMethod();
		String popUser = mailconfig.getPopUser();
		String popPass = mailconfig.getPopPass();
		
		String server = mailconfig.getServer();
		int port = mailconfig.getPort();
		String langCode = mailconfig.getLangCode();
		
		
		makeDomVariable(context.getCore(), "AUTH_METHOD", authMethod, ret);
		makeDomVariable(context.getCore(), "AUTH_USER", authUser, ret);
		makeDomVariable(context.getCore(), "AUTH_PASS", authPass, ret);
		
		makeDomVariable(context.getCore(), "POP_METHOD", popMethod, ret);
		makeDomVariable(context.getCore(), "POP_USER", popUser, ret);
		makeDomVariable(context.getCore(), "POP_PASS", popPass, ret);
		
		makeDomVariable(context.getCore(), "SERVER", server, ret);
		makeDomVariable(context.getCore(), "PORT", Integer.toString(port), ret);
		makeDomVariable(context.getCore(), "LANG_CODE", langCode, ret);
		
		return ret;
	}
	
	private void makeDomVariable(AlinousCore core, String key, String value, ScriptDomVariable parent)
	{
		AlinousDebug.debugOut(core, "makeDomVariable : " + key + " : "  +value);
		if(value == null){
			return;
		}
		
		ScriptDomVariable ret = new ScriptDomVariable(key);
		ret.setValue(value);
		ret.setValueType(IScriptVariable.TYPE_STRING);
		
		parent.put(ret);
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

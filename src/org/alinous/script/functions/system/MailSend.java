/**
 * CROSSFIRE JAPAN INCORPORATED
 * This source code is under GPL License.
 * info@crossfire.jp
 * Official web site
 * http://alinous.org
 * 
 *  Copyright (C) 2007 Tomohiro Iizuka
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.alinous.script.functions.system;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alinous.AlinousCore;
import org.alinous.AlinousDebug;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.net.mail.AlinousMailConfig;
import org.alinous.net.mail.MailWrapper;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class MailSend extends AbstractSystemFunction
{
	public static String QUALIFIED_NAME = "MAIL.SEND";
	public static String MAIL_ARG = "arg0";
	
	
	public MailSend()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", MAIL_ARG);
		this.argmentsDeclare.addArgument(arg);
	}
	
	public ScriptDomVariable executeFunction(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		
		IPathElement ipath = PathElementFactory.buildPathElement(MAIL_ARG);
		IScriptVariable val = newValRepo.getVariable(ipath, context);
		
		if(!(val instanceof ScriptDomVariable)){
			throw new ExecutionException(QUALIFIED_NAME + "() argument is wrong."); //i18n
		}
		
		ScriptDomVariable mailObject = (ScriptDomVariable)val;
		AlinousCore core = context.getCore();
		AlinousMailConfig mailconfig = core.getConfig().getMailConfig();
		
		if(mailconfig == null){
			throw new ExecutionException(QUALIFIED_NAME + "() configration about mail server at alinous-config.xml is wrong."); //i18n
		}
		
		MailWrapper mgr = new MailWrapper(mailconfig);
		Map<String, String> map = findField(mailObject, mgr);
		
		// IF DEBUG
		if(mailconfig.isDebug()){
			StringBuffer buff = new StringBuffer();
			buff.append("from: " + map.get("FROM"));
			
			List<String> list = mgr.getToArray();
			Iterator<String> it = list.iterator();
			while(it.hasNext()){
				String receiver = it.next();
				
				buff.append("to:" + receiver);
			}
			
			buff.append("subject:" + map.get("SUBJECT"));
			buff.append("\n\n" + map.get("BODY"));
			
			debugOutMail(buff, mailconfig, mgr, map, context);
			
			return null;
		}
		
		//check error
		if(map.get("FROM") == null ||  map.get("SUBJECT") == null || map.get("BODY") == null){
			throw new ExecutionException("FROM, SUBJECT or BODY is empty");// i18n
		}
		
		// POP before SMTP
		if(mailconfig.getPopMethod() != null && mailconfig.getPopUser() != null &&
				mailconfig.getAuthPass() != null){
			mgr.popcheck();
		}
		
		// send message
		try{
			mgr.connect();
			
			mgr.sendMail(map.get("FROM"), map.get("SUBJECT"), map.get("BODY"));
		} catch (AlinousException e) {
			ScriptDomVariable retVal = new ScriptDomVariable("result");
			retVal.setValueType(IScriptVariable.TYPE_BOOLEAN);
			retVal.setValue("false");
			
			context.getCore().reportError(e);
			
			return retVal;
		}
		finally{
			mgr.disconnect();
		}
		
		ScriptDomVariable retVal = new ScriptDomVariable("result");
		retVal.setValueType(IScriptVariable.TYPE_BOOLEAN);
		retVal.setValue("true");
		
		return retVal;
	}
	
	private void debugOutMail(StringBuffer buff, AlinousMailConfig mailconfig, MailWrapper mgr,
			Map<String, String> map, PostContext context) throws ExecutionException
	{
		AlinousDebug.debugOut(context.getCore(), buff.toString());
		
		if(mailconfig.getDebugReceiver() == null){
			return;
		}
		
		// POP before SMTP
		if(mailconfig.getPopMethod() != null && mailconfig.getPopUser() != null &&
				mailconfig.getAuthPass() != null){
			mgr.popcheck();
		}
		
		// send message
		try{
			mgr.clearBcc();
			mgr.clearCc();
			mgr.clearTo();
			
			mgr.addTo(mailconfig.getDebugReceiver());
			
			mgr.connect();
			
			mgr.sendMail(map.get("FROM"), "[Alinous-Core]debug output mail", buff.toString());
		} catch (AlinousException e) {
			ScriptDomVariable retVal = new ScriptDomVariable("result");
			retVal.setValueType(IScriptVariable.TYPE_BOOLEAN);
			retVal.setValue("false");
			
			context.getCore().reportError(e);
			
		}
		finally{
			mgr.disconnect();
		}
	}
	
	private Map<String, String> findField(ScriptDomVariable mailObject, MailWrapper mgr)
	{
		Map<String, String> map = new HashMap<String, String>();
		
		Iterator<String> it = mailObject.getPropertiesIterator();
		while(it.hasNext()){
			String propName = it.next();
			
			if(propName.toUpperCase().equals("TO")){
				addTo(mailObject.get(propName), mgr);
				continue;
			}
			else if(propName.toUpperCase().equals("CC")){
				addCC(mailObject.get(propName), mgr);
				continue;
			}
			else if(propName.toUpperCase().equals("BCC")){
				addBcc(mailObject.get(propName), mgr);
				continue;
			}
			else if(propName.toUpperCase().equals("FROM")){
				IScriptVariable val = mailObject.get(propName);
				if(val instanceof ScriptDomVariable){
					map.put("FROM", ((ScriptDomVariable)val).getValue());
				}
				
				continue;
			}
			else if(propName.toUpperCase().equals("SUBJECT")){
				IScriptVariable val = mailObject.get(propName);
				if(val instanceof ScriptDomVariable){
					map.put("SUBJECT", ((ScriptDomVariable)val).getValue());
				}
				
				continue;
			}
			else if(propName.toUpperCase().equals("BODY")){
				IScriptVariable val = mailObject.get(propName);
				if(val instanceof ScriptDomVariable){
					map.put("BODY", ((ScriptDomVariable)val).getValue());
				}
				
				continue;
			}
		}
		
		return map;
	}
	
	private void addBcc(IScriptVariable val, MailWrapper mgr)
	{
		if(val instanceof ScriptDomVariable){
			String str = ((ScriptDomVariable)val).getValue();
			mgr.addBcc(str);
			return;
		}
		
		// Handle Array
		if(val instanceof ScriptArray){
			ScriptArray array = (ScriptArray)val;
			
			Iterator<IScriptVariable> it = array.iterator();
			while(it.hasNext()){
				IScriptVariable v = it.next();
				
				if(v instanceof ScriptDomVariable){
					String str = ((ScriptDomVariable)v).getValue();
					if(str != null){
						mgr.addBcc(str);
					}
				}
			}
		}
	}
	
	private void addTo(IScriptVariable val, MailWrapper mgr)
	{
		if(val instanceof ScriptDomVariable){
			String str = ((ScriptDomVariable)val).getValue();
			mgr.addTo(str);
			return;
		}
		
		// Handle Array
		if(val instanceof ScriptArray){
			ScriptArray array = (ScriptArray)val;
			
			Iterator<IScriptVariable> it = array.iterator();
			while(it.hasNext()){
				IScriptVariable v = it.next();
				
				if(v instanceof ScriptDomVariable){
					String str = ((ScriptDomVariable)v).getValue();
					if(str != null){
						mgr.addTo(str);
					}
				}
			}
		}
	}
	
	private void addCC(IScriptVariable val, MailWrapper mgr)
	{
		if(val instanceof ScriptDomVariable){
			String str = ((ScriptDomVariable)val).getValue();
			mgr.addCc(str);
			return;
		}
		
		// Handle Array
		if(val instanceof ScriptArray){
			ScriptArray array = (ScriptArray)val;
			
			Iterator<IScriptVariable> it = array.iterator();
			while(it.hasNext()){
				IScriptVariable v = it.next();
				
				if(v instanceof ScriptDomVariable){
					String str = ((ScriptDomVariable)v).getValue();
					if(str != null){
						mgr.addCc(str);
					}
				}
			}
		}
	}
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}

	@Override
	public String codeAssistString() {
		return "Mail.send($mail)";
	}

	@Override
	public String descriptionString() {
		return "Send mail.\n" +
				"ie.\n" +
				"$mail.from = \"info@open-ec.jp\";\n" +
				"$mail.to[0] = \"test@aaaa.com\";\n" +
				"$mail.to[1] = \"test@aaaa.comp\";\n" +
				"$mail.body = $body;\n" +
				"$mail.subject = \"[Alinous]inquery\";\n" +
				"Mail.send($mail);";
	}
}

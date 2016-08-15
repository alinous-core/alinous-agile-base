package org.alinous.script.functions.system.debug;

import org.alinous.AlinousCore;
import org.alinous.debug.AlinousDebugManager;
import org.alinous.debug.DebugThread;
import org.alinous.debug.command.server.IServerCommand;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.AlinousScript;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.ScriptVariable;
import org.alinous.script.runtime.VariableRepository;

public class DebugCallbackHandlerRunnable implements Runnable{
	private AlinousScript script;
	private IServerCommand command;
	private long threadid;
	private PostContext context;
	private VariableRepository valRepo;
	private AlinousCore core;
	
	public DebugCallbackHandlerRunnable(AlinousScript script, IServerCommand command, long threadid, PostContext context,  VariableRepository valRepo, AlinousCore core)
	{
		this.script = script;
		this.command = command;
		this.threadid = threadid;
		this.context = context;
		this.valRepo = valRepo;
		this.core = core;
	}
	
	@Override
	public void run() {
		
		this.script.setDataSourceManager(this.core.getDataSourceManager());
		
		// for debugger
		if(AlinousCore.debug(this.context)){
			this.context.getCore().getAlinousDebugManager().startAlinousOperation(this.context);
			
			AlinousDebugManager mgr = this.context.getCore().getAlinousDebugManager();
			DebugThread th = mgr.getCurrentThread();
			
			th.newStackFrame(this.script,
				this.valRepo, this.context);
		}
		
		ScriptDomVariable inDom = getInParams(this.command);
		
		try {
			this.valRepo.putValue(inDom, context);
			

			script.execute(context, valRepo, true, true);
		} catch (ExecutionException e) {
			core.getLogger().reportError(e);
			e.printStackTrace();
		} catch (RedirectRequestException e) {
			core.getLogger().reportError(e);
			e.printStackTrace();
		}finally{
			// for debugger
			if(AlinousCore.debug(this.context)){
				this.context.getCore().getAlinousDebugManager().endAlinousOperation(this.context);				
			}
			
			this.context.dispose();
		}
	}
	
	private ScriptDomVariable getInParams(IServerCommand command)
	{
		String commandStr = command.getName();
		
		ScriptDomVariable cmd = new ScriptDomVariable("cmd");
		cmd.setValue(commandStr);
		cmd.setValueType(IScriptVariable.TYPE_STRING);
		
		ScriptVariable thred = new ScriptDomVariable("threadId");
		thred.setValue(Long.toString(this.threadid));
		thred.setValueType(IScriptVariable.TYPE_NUMBER);
		
		ScriptDomVariable inDom = new ScriptDomVariable("IN");
		inDom.setValueType(IScriptVariable.TYPE_NULL);
		inDom.put(cmd);
		inDom.put(thred);
		
		return inDom;
	}

}

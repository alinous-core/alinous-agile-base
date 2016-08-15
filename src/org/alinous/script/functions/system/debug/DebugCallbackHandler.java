package org.alinous.script.functions.system.debug;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import org.alinous.AlinousUtils;
import org.alinous.debug.command.server.IServerCommand;
import org.alinous.exec.AccessExecutionUnit;
import org.alinous.exec.pages.AlinousExecutableModule;
import org.alinous.exec.pages.PostContext;
import org.alinous.parser.script.AlinousScriptParser;
import org.alinous.parser.script.ParseException;
import org.alinous.script.AlinousScript;
import org.alinous.script.runtime.VariableRepository;

public class DebugCallbackHandler {
	private String callbackScript;
	private AlinousScript script;
	
	public DebugCallbackHandler(String callbackScript, PostContext context) throws FileNotFoundException, ParseException
	{
		this.callbackScript = callbackScript;
		this.script = compileScript(context);
		this.script.setFilePath(this.callbackScript);		
	}
	
	public void runCallback(IServerCommand command, PostContext context)
	{
		VariableRepository newValRepo = new VariableRepository();
		AccessExecutionUnit newExecutableUnit = context.getCore().createAccessExecutionUnit(callbackScript);
		AlinousExecutableModule module = new AlinousExecutableModule(this.callbackScript, null, this.script, 0);
		newExecutableUnit.setExecModule(module);
				
		PostContext execContext = new PostContext(context.getCore(), newExecutableUnit);
		execContext.initParams(context);
		
		long threadId = Thread.currentThread().getId();
		
		DebugCallbackHandlerRunnable runnable 
			= new DebugCallbackHandlerRunnable(script, command, threadId, execContext, newValRepo, context.getCore());
		
		Thread thread = new Thread(runnable);
		thread.start();
		
	}
	
	
	private AlinousScript compileScript(PostContext context) throws FileNotFoundException, ParseException
	{
		String pathname = AlinousUtils.getAbsoluteNotOSPath(context.getCore().getHome(), this.callbackScript);
		FileReader reader = new FileReader(new File(pathname));

		AlinousScriptParser parser = new AlinousScriptParser(reader);
		
		return parser.parse();
	}
	
	
}

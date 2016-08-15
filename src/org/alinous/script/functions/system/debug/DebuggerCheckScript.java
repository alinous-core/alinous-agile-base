package org.alinous.script.functions.system.debug;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.alinous.AlinousUtils;
import org.alinous.exec.ScriptCheckContext;
import org.alinous.exec.ScriptError;
import org.alinous.exec.check.AlinousScriptCompiledCache;
import org.alinous.exec.check.IncludeUseChecker;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.parser.script.AlinousScriptParser;
import org.alinous.parser.script.ParseException;
import org.alinous.script.AlinousScript;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.functions.system.AbstractSystemFunction;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class DebuggerCheckScript extends AbstractSystemFunction {
	public static String QUALIFIED_NAME = "DEBUGGER.CHECKSCRIPT";
	public static String FILE_NAME = "fileName";
	
	public DebuggerCheckScript()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", FILE_NAME);
		this.argmentsDeclare.addArgument(arg);
	}
	
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		
		IPathElement ipath = PathElementFactory.buildPathElement(FILE_NAME);
		IScriptVariable fileNameVariable = newValRepo.getVariable(ipath, context);
		
		String fileName = ((ScriptDomVariable)fileNameVariable).getValue();
		String alinousHome = context.getCore().getHome();
		
		String filePath = AlinousUtils.getAbsolutePath(alinousHome, fileName);
		String scriptString = readAllText(filePath, "utf-8");
		
		StringReader reader = new StringReader(scriptString);
		AlinousScriptParser parser = new AlinousScriptParser(reader);
		

		AlinousScriptCompiledCache cache = new AlinousScriptCompiledCache();
		List<ScriptError> errorList = new LinkedList<ScriptError>();
		
		try {
			AlinousScript script = parser.parse();
			script.setFilePath(fileName);
			
			ScriptCheckContext scContext = new ScriptCheckContext();
			scContext.setAlinousConfig(context.getCore().getConfig());
			scContext.setAlinousScript(script);
			scContext.setCache(cache);
			scContext.setAlinousHome(alinousHome);
			
			IncludeUseChecker includeChecker = new IncludeUseChecker();
			script.compositeIncludes2Check(includeChecker);
			scContext.setIncludeChecker(includeChecker);
			
			scContext.setJavaConnectorManager(new JavaConnectorImpl(context.getCore().getJavaConnector()));
			
			script.checkStaticErrors(scContext, errorList);
		} catch (ParseException e) {
			ScriptError error = new ScriptError();
			
			error.setMessage(e.getMessage());
			error.setLine(e.currentToken.beginLine);
			error.setLinePosition(e.currentToken.beginColumn);
			
			errorList.add(error);
		}
		
		ScriptArray errors = new ScriptArray("errors");
		Iterator<ScriptError> it = errorList.iterator();
		while(it.hasNext()){
			ScriptError se = it.next();
			
			ScriptDomVariable e = error2Dom(se);
			errors.add(e);
		}
		
		
		return errors;
	}
	
	
	private ScriptDomVariable error2Dom(ScriptError error)
	{
		ScriptDomVariable base = new ScriptDomVariable("error");
		
		ScriptDomVariable line = new ScriptDomVariable("line");
		line.setValueType(IScriptVariable.TYPE_NUMBER);
		line.setValue(Integer.toString(error.getLine()));
		base.put(line);
		
		ScriptDomVariable linePosition = new ScriptDomVariable("linePosition");
		linePosition.setValueType(IScriptVariable.TYPE_NUMBER);
		linePosition.setValue(Integer.toString(error.getLinePosition()));
		base.put(linePosition);
		
		ScriptDomVariable message = new ScriptDomVariable("message");
		message.setValueType(IScriptVariable.TYPE_STRING);
		message.setValue(error.getMessage());
		base.put(message);
		
		return base;
	}
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}
	
	@Override
	public String codeAssistString() {
		return "Debugger.checkScript($scriptPath)";
	}

	@Override
	public String descriptionString() {
		return "Check the script and return Error Array if error exists.";
	}

	private String readAllText(String srcPath, String encode)
	{
		StringBuffer buff = new StringBuffer();
		
		File file = new File(srcPath);
		FileInputStream fStream = null;
		InputStreamReader reader = null;
		try {
			fStream = new FileInputStream(file);
			reader = new InputStreamReader(fStream, encode);
			
			char readBuffer[] = new char[1024];
			int n = 1;
			
			while(n > 0){
				n = reader.read(readBuffer, 0, readBuffer.length);
				
				if(n > 0){
					buff.append(readBuffer, 0, n);
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally{
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e) {e.printStackTrace();}
			}
			if(fStream != null){
				try {
					fStream.close();
				} catch (IOException e) {e.printStackTrace();}
			}
		}
		
		return buff.toString();	
	}
}

package org.alinous.script.functions.system.debug;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.alinous.AlinousUtils;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.objects.html.AlinousTopObject;
import org.alinous.objects.validate.ValidationError;
import org.alinous.parser.object.AlinousObjectParser;
import org.alinous.parser.object.ParseException;
import org.alinous.parser.object.TokenMgrError;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.functions.system.AbstractSystemFunction;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class DebuggerCheckHtml extends AbstractSystemFunction {
	public static String QUALIFIED_NAME = "DEBUGGER.CHECKHTML";
	public static String FILE_NAME = "fileName";
	
	public DebuggerCheckHtml()
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
		
		if(scriptString.indexOf("<html") < 0){
			scriptString = "<html><body>" + scriptString + "</body></html>\n";
		}
		
		// AlinousDebug.debugOut(scriptString);
		
		StringReader reader = new StringReader(scriptString);
		
		AlinousObjectParser parser = new AlinousObjectParser(reader);
		
		
		AlinousTopObject topObj = null;
		List<ValidationError> errorsList = new ArrayList<ValidationError>();
		
		try {
			topObj = parser.parse();
		} catch (ParseException e) {
			ValidationError error = new ValidationError(e.currentToken.beginLine, e.getMessage());
			
			errorsList.add(error);
		} catch (TokenMgrError e){
			if(!e.getMessage().endsWith("Encountered: <EOF> after : \"\"")){
				ValidationError error = new ValidationError(0, e.getMessage());
				errorsList.add(error);
			}else{
				topObj = parser.topObj;
			}
			
		}
		
		if(topObj != null){
			topObj.validateHtmlObject(errorsList);
		}
		
		ScriptArray errors = new ScriptArray("errors");
		Iterator<ValidationError> it = errorsList.iterator();
		while(it.hasNext()){
			ValidationError se = it.next();
			
			ScriptDomVariable e = error2Dom(se);
			errors.add(e);
		}
		
		return errors;
	}
	
	private ScriptDomVariable error2Dom(ValidationError error)
	{
		ScriptDomVariable base = new ScriptDomVariable("error");
		
		ScriptDomVariable line = new ScriptDomVariable("line");
		line.setValueType(IScriptVariable.TYPE_NUMBER);
		line.setValue(Integer.toString(error.getLine()));
		base.put(line);
		
		ScriptDomVariable message = new ScriptDomVariable("message");
		message.setValueType(IScriptVariable.TYPE_STRING);
		message.setValue(error.getMsg());
		base.put(message);
		
		return base;
	}
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}
	
	@Override
	public String codeAssistString() {
		return "Debugger.checkHtml($htmlPath)";
	}

	@Override
	public String descriptionString() {
		return "Check the Alinous Html and return Error Array if error exists.";
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

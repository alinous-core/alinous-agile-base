package org.alinous.script.functions.parser;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.objects.html.AlinousTopObject;
import org.alinous.parser.object.AlinousObjectParser;
import org.alinous.parser.object.ParseException;
import org.alinous.parser.object.TokenMgrError;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.functions.system.AbstractSystemFunction;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class AlinousHtmlParse extends AbstractSystemFunction{
	public static String QUALIFIED_NAME = "ALINOUS.HTMLPARSE";
	
	public static String ARRAY_ARG = "HTML_STRING";
	
	public AlinousHtmlParse()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", ARRAY_ARG);
		this.argmentsDeclare.addArgument(arg);
		
	}
	
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		
		IPathElement ipath = PathElementFactory.buildPathElement(ARRAY_ARG);
		IScriptVariable val = newValRepo.getVariable(ipath, context);
		
		if(!(val instanceof ScriptDomVariable)){
			throw new ExecutionException(QUALIFIED_NAME + "() argument is wrong."); //i18n
		}
		
		String html = ((ScriptDomVariable)val).getValue();
		
		AlinousTopObject topObj = null;		
		StringReader reader = new StringReader(html);

		AlinousObjectParser parser = new AlinousObjectParser(reader);
		try {
			topObj = parser.parse();
		} catch (ParseException e) {
			ScriptDomVariable ret = new ScriptDomVariable("RETURN");
			//ret.setValue(Integer.toString(size));
			ret.setValueType(IScriptVariable.TYPE_NULL);
			return ret;
		} catch(TokenMgrError e){
			if(!e.getMessage().endsWith("Encountered: <EOF> after : \"\"")){
				ScriptDomVariable ret = new ScriptDomVariable("RETURN");
				//ret.setValue(Integer.toString(size));
				ret.setValueType(IScriptVariable.TYPE_NULL);
				return ret;
			}else{
				topObj = parser.topObj;
			}
		}
		
		StringWriter writer = new StringWriter();
		try {
			topObj.renderContents(context, valRepo, writer, 0);
		} catch (IOException e) {
			ScriptDomVariable ret = new ScriptDomVariable("RETURN");
			//ret.setValue(Integer.toString(size));
			ret.setValueType(IScriptVariable.TYPE_NULL);
			return ret;
		} catch (AlinousException e) {
			ScriptDomVariable ret = new ScriptDomVariable("RETURN");
			//ret.setValue(Integer.toString(size));
			ret.setValueType(IScriptVariable.TYPE_NULL);
			return ret;
		}
		
		writer.flush();
		
		ScriptDomVariable ret = new ScriptDomVariable("RETURN");
		ret.setValue(writer.toString());
		ret.setValueType(IScriptVariable.TYPE_STRING);
		
		return ret;
	}
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}

	@Override
	public String codeAssistString() {
		return "Alinous.htmlParse($HTML_STRING)";
	}

	@Override
	public String descriptionString() {
		return "Parse html as Alinous html, and set variables which currently the script calling this function is using.";
	}
}

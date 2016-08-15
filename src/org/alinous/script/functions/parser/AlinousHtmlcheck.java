package org.alinous.script.functions.parser;

import java.io.StringReader;

import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
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

public class AlinousHtmlcheck extends AbstractSystemFunction {
	public static String QUALIFIED_NAME = "ALINOUS.HTMLCHECK";
	
	public static String ARRAY_ARG = "HTML_STRING";
	
	public AlinousHtmlcheck()
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
		
		StringReader reader = new StringReader(html);

		AlinousObjectParser parser = new AlinousObjectParser(reader);
		try {
			parser.parse();
		} catch (ParseException e) {
			ScriptDomVariable ret = new ScriptDomVariable("RETURN");
			ret.setValue(e.getMessage());
			ret.setValueType(IScriptVariable.TYPE_STRING);
			
			int beginLine = e.currentToken.beginLine;
			int beginColumn = e.currentToken.beginColumn;
			
			ScriptDomVariable beginLineDom = new ScriptDomVariable("beginLine");
			beginLineDom.setValue(Integer.toString(beginLine));
			beginLineDom.setValueType(IScriptVariable.TYPE_NUMBER);
			ret.put(beginLineDom);
			
			ScriptDomVariable beginColumnDom = new ScriptDomVariable("beginColumn");
			beginColumnDom.setValue(Integer.toString(beginColumn));
			beginColumnDom.setValueType(IScriptVariable.TYPE_NUMBER);
			ret.put(beginColumnDom);
			
			return ret;
		} catch(TokenMgrError e){
			if(!e.getMessage().endsWith("Encountered: <EOF> after : \"\"")){
				ScriptDomVariable ret = new ScriptDomVariable("RETURN");
				ret.setValue(e.getMessage());
				ret.setValueType(IScriptVariable.TYPE_STRING);
				return ret;
			}else{
				
			}
		}

		
		ScriptDomVariable ret = new ScriptDomVariable("RETURN");
		ret.setValue("OK");
		ret.setValueType(IScriptVariable.TYPE_STRING);
		
		return ret;
	}
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}

	@Override
	public String codeAssistString() {
		return "Alinous.htmlcheck($HTML_STRING)";
	}

	@Override
	public String descriptionString() {
		return "Parse html as Alinous html, and check if the html is valid";
	}
}

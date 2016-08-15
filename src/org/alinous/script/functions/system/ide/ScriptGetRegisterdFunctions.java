package org.alinous.script.functions.system.ide;

import java.util.Iterator;
import java.util.Map;

import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.jdk.FunctionConitainer;
import org.alinous.jdk.JavaConnectorFunctionManager;
import org.alinous.jdk.model.FunctionModel;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.functions.FunctionRegistory;
import org.alinous.script.functions.IFunction;
import org.alinous.script.functions.system.AbstractSystemFunction;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class ScriptGetRegisterdFunctions extends AbstractSystemFunction {
	public static String QUALIFIED_NAME = "SCRIPT.GETREGISTEREDFUNCTIONS";
	
	public ScriptGetRegisterdFunctions()
	{
		
	}
	
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		
		FunctionRegistory registory = FunctionRegistory.getInstance();
		
		ScriptArray functions = new ScriptArray("ret");
		
		Map<String, IFunction> functionMap = registory.getFunctions();
		Iterator<String> it = functionMap.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			IFunction func = functionMap.get(key);
			
			ScriptDomVariable dom = function2Dom(func);
			functions.add(dom);
		}
		
		
		// Java Connector
		JavaConnectorFunctionManager jc = context.getCore().getJavaConnector();
		Iterator<String> jcit = jc.getFunctions().keySet().iterator();		
		while(jcit.hasNext()){
			String key = jcit.next();
			FunctionConitainer container = jc.getFunctions().get(key);
			
			Iterator<FunctionModel> fit = container.listFunctionModels().iterator();
			while(fit.hasNext()){
				FunctionModel model = fit.next();
				
				if(model.codeAssistString() == null || model.getName() == null){
					continue;
				}
				ScriptDomVariable dom = functionModel2Dom(container.getPrefix(), model);
				functions.add(dom);
			}
			
		}
		
		return functions;
	}
	
	private ScriptDomVariable functionModel2Dom(String prefix, FunctionModel model)
	{
		ScriptDomVariable dom = new ScriptDomVariable(model.getName());
		
		ScriptDomVariable val = makeStringDom("name", prefix + "." + model.getName());
		dom.put(val);
		
		val = makeStringDom("desc", model.descriptionString());
		dom.put(val);
		
		val = makeStringDom("assist", prefix + "." + model.codeAssistString());
		dom.put(val);
		
		return dom;
	}
	
	
	private ScriptDomVariable function2Dom(IFunction func)
	{
		ScriptDomVariable dom = new ScriptDomVariable(func.getName());
		
		ScriptDomVariable val = makeStringDom("name", func.getName());
		dom.put(val);
		
		val = makeStringDom("desc", func.descriptionString());
		dom.put(val);
		
		val = makeStringDom("assist", func.codeAssistString());
		dom.put(val);
		
		ScriptArray argments = new ScriptArray("arguments");
		dom.put(argments);
		
		int size = func.getArguments().getSize();
		for(int i = 0; i < size; i++){
			ArgumentDeclare arg = func.getArguments().get(i);
			
			val = makeStringDom("arg", arg.getPrefix() + arg.getName());
			
			argments.add(val);			
		}
		
		return dom;
	}
	
	private ScriptDomVariable makeStringDom(String name, String value)
	{
		ScriptDomVariable dom = new ScriptDomVariable(name);
		dom.setValue(value);
		dom.setValueType(IScriptVariable.TYPE_STRING);
		
		return dom;
	}
	
	@Override
	public String getName()
	{
		return QUALIFIED_NAME;
	}
	
	@Override
	public String codeAssistString() {
		return "Script.getRegisteredFunctions()";
	}

	@Override
	public String descriptionString() {
		return "Get Internally Registered functions by Array.";
	}

}

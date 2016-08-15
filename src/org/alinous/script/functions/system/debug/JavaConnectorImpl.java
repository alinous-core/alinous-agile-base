package org.alinous.script.functions.system.debug;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.alinous.exec.check.IAlinousCheckFunctionContainer;
import org.alinous.exec.check.IJavaConnectorManager;
import org.alinous.jdk.FunctionConitainer;
import org.alinous.jdk.JavaConnectorFunctionManager;
import org.alinous.jdk.model.FunctionModel;

public class JavaConnectorImpl implements IJavaConnectorManager{
	private Map<String, FunctionModel> models = new HashMap<String, FunctionModel>();
	private Map<String, IAlinousCheckFunctionContainer> functionList = new HashMap<String, IAlinousCheckFunctionContainer>();
	
	public JavaConnectorImpl(JavaConnectorFunctionManager jdk)
	{
		Map<String, FunctionConitainer> funcMap = jdk.getFunctions();
		Iterator<String> it = funcMap.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			
			FunctionConitainer jcContainer = funcMap.get(key);
			
			AlinousEditorFunctionContainer container = new AlinousEditorFunctionContainer();
			container.setPrefix(jcContainer.getPrefix());
			
			this.functionList.put(container.getPrefix(), container);
			
			try{
				setupFunctions(jcContainer, container);
			}catch(Throwable e){
				
			}
			

		}
	}
	
	private void setupFunctions(FunctionConitainer jcContainer, AlinousEditorFunctionContainer container)
	{
		Iterator<FunctionModel> it = jcContainer.listFunctionModels().iterator();
		while(it.hasNext()){
			FunctionModel jfm = it.next();
			
			String name = jfm.getName();
			String codeAssistString = jfm.codeAssistString();
			String descriptionString = jfm.descriptionString();
			
			AlinousEditorFunctionModel model = new AlinousEditorFunctionModel();
			model.setName(name);
			model.setCodeAssistString(codeAssistString);
			model.setDescriptionString(descriptionString);
			
			container.addAlinousEditorFunctionModel(model);
			
			String qualifiedName = container.getPrefix() + "." + name;
			
			this.models.put(qualifiedName, jfm);
		}
	}
	
	@Override
	public Map<String, IAlinousCheckFunctionContainer> getFuntionList() {
		return this.functionList;
	}

	@Override
	public Map<String, FunctionModel> getModels() {
		return this.models;
	}
	
}

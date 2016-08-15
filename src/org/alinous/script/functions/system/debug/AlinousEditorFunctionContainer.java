package org.alinous.script.functions.system.debug;

import java.util.ArrayList;
import java.util.Iterator;

import org.alinous.exec.check.IAlinousCheckFunctionContainer;
import org.alinous.exec.check.IAlinousCheckFunctionModel;

public class AlinousEditorFunctionContainer implements IAlinousCheckFunctionContainer{
	private String prefix;

	private ArrayList<IAlinousCheckFunctionModel> list = new ArrayList<IAlinousCheckFunctionModel>();

	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public ArrayList<IAlinousCheckFunctionModel> listFunctionModels() {
		return this.list;
	}
	
	public void addAlinousEditorFunctionModel(IAlinousCheckFunctionModel model)
	{
		this.list.add(model);
	}
	
	
	public IAlinousCheckFunctionModel getFunction(String funcName)
	{
		Iterator<IAlinousCheckFunctionModel> it = this.list.iterator();
		while(it.hasNext()){
			IAlinousCheckFunctionModel model = it.next();
			
			model.getName().equals(funcName);
			
			return model;
		}
		
		return null;
	}
}

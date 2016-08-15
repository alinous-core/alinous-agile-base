package org.alinous.exec.check;

import java.util.ArrayList;



public interface IAlinousCheckFunctionContainer {
	public String getPrefix();
	public void setPrefix(String prefix);
	
	public ArrayList<IAlinousCheckFunctionModel> listFunctionModels();
	public void addAlinousEditorFunctionModel(IAlinousCheckFunctionModel model);
	public IAlinousCheckFunctionModel getFunction(String funcName);
	
}

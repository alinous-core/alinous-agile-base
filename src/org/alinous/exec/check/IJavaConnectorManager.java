package org.alinous.exec.check;

import java.util.Map;

import org.alinous.jdk.model.FunctionModel;

public interface IJavaConnectorManager {
	public Map<String, IAlinousCheckFunctionContainer> getFuntionList();
	public Map<String, FunctionModel> getModels();
}

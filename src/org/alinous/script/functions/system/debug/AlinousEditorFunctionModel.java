package org.alinous.script.functions.system.debug;

import org.alinous.exec.check.IAlinousCheckFunctionModel;

public class AlinousEditorFunctionModel implements IAlinousCheckFunctionModel{
	private String name;
	private String codeAssistString;
	private String descriptionString;
	
	
	public String getName() {
		return name;
	}

	public String getCodeAssistString() {
		return codeAssistString;
	}

	public String getDescriptionString() {
		return descriptionString;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setCodeAssistString(String codeAssistString) {
		this.codeAssistString = codeAssistString;
	}

	public void setDescriptionString(String descriptionString) {
		this.descriptionString = descriptionString;
	}
}

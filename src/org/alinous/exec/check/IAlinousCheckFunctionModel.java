package org.alinous.exec.check;

public interface IAlinousCheckFunctionModel {
	public String getName();

	public String getCodeAssistString();

	public String getDescriptionString();
	public void setName(String name);

	public void setCodeAssistString(String codeAssistString);

	public void setDescriptionString(String descriptionString);
}

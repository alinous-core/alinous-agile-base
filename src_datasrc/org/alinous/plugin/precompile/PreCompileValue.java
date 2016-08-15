package org.alinous.plugin.precompile;



import java.util.ArrayList;

import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;

public class PreCompileValue
{
	private IPathElement path;
	private String type;
	private ArrayList<String> arrayValue;
	
	public PreCompileValue(String type, IPathElement path)
	{
		this.type = type;
		this.path = path;
	}
	
	public String getType()
	{
		return type;
	}
	public void setType(String type)
	{
		if(type == null){
			type = IScriptVariable.TYPE_STRING;
		}
		this.type = type;
	}
	
	public void addArrayValue(String value)
	{
		if(this.arrayValue == null){
			this.arrayValue = new ArrayList<String>();
		}
		
		this.arrayValue.add(value);
	}

	public ArrayList<String> getArrayValue()
	{
		return arrayValue;
	}

	public IPathElement getPath()
	{
		return path;
	}
	
}

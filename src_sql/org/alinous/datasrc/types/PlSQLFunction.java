package org.alinous.datasrc.types;

import java.util.ArrayList;
import java.util.List;

import org.alinous.script.sql.ddl.ColumnTypeDescriptor;

public class PlSQLFunction
{
	private String funcName;
	private List<ColumnTypeDescriptor> typeDesc = new ArrayList<ColumnTypeDescriptor>();
	private ColumnTypeDescriptor retType;
	private boolean setof;
	
	private String program;
	private String language;
	
	private boolean relpace;
	
	public String getFuncName()
	{
		return funcName;
	}
	public void setFuncName(String funcName)
	{
		this.funcName = funcName;
	}
	public List<ColumnTypeDescriptor> getTypeDesc()
	{
		return typeDesc;
	}
	public void addTypeDesc(ColumnTypeDescriptor typeDesc)
	{
		this.typeDesc.add(typeDesc);
	}
	public ColumnTypeDescriptor getRetType()
	{
		return retType;
	}
	public void setRetType(ColumnTypeDescriptor retType)
	{
		this.retType = retType;
	}
	public boolean isSetof()
	{
		return setof;
	}
	public void setSetof(boolean setof)
	{
		this.setof = setof;
	}
	public String getProgram()
	{
		return program;
	}
	public void setProgram(String program)
	{
		this.program = program;
	}
	public String getLanguage()
	{
		return language;
	}
	public void setLanguage(String language)
	{
		this.language = language;
	}
	public boolean isRelpace()
	{
		return relpace;
	}
	public void setRelpace(boolean relpace)
	{
		this.relpace = relpace;
	}
	
	
}

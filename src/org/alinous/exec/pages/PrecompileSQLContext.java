package org.alinous.exec.pages;

import java.util.ArrayList;
import java.util.List;

import org.alinous.plugin.precompile.PreCompileValue;
import org.alinous.script.ISQLSentence;

public class PrecompileSQLContext
{
	private boolean compile;
	private List<PreCompileValue> precompileValues = new ArrayList<PreCompileValue>();
	
	private ISQLSentence sqlSentence;
	
	private boolean hasArray = false;
	private boolean markVariable = false;
	
	public PrecompileSQLContext()
	{
		this.compile = false;
		this.hasArray = false;
	}
		
	public void clear()
	{
		this.precompileValues = new ArrayList<PreCompileValue>();
	}
	
	public void clearStatus()
	{
		this.hasArray = false;
		this.markVariable = false;
	}

	public boolean isCompile() // if this flag is true, do precompaile extraction
	{
		return compile;
	}

	public void setCompile(boolean compile)
	{
		this.compile = compile;
	}
	
	public void addVariable(PreCompileValue val)
	{
		this.markVariable = true;
		this.precompileValues.add(val);
		
		if(val.getArrayValue() != null){
			this.hasArray = true;
		}
	}
	
	public void setHasArray(boolean hasArray)
	{
		this.hasArray = hasArray;
	}
	
	public List<PreCompileValue> getPrecompileValues()
	{
		return precompileValues;
	}

	public ISQLSentence getSqlSentence()
	{
		return sqlSentence;
	}

	public void setSqlSentence(ISQLSentence sqltSentence)
	{
		this.sqlSentence = sqltSentence;
	}

	public boolean isHasArray()
	{
		return hasArray;
	}

	public boolean isMarkVariable()
	{
		return markVariable;
	}

	public void setMarkVariable(boolean markVariable)
	{
		this.markVariable = markVariable;
	}
	
}

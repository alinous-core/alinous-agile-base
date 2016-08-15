package org.alinous.plugin.precompile;

import java.util.ArrayList;
import java.util.List;

public class PreCompileValueBag
{
	private List<PreCompileValue> list = new ArrayList<PreCompileValue>();
	
	public PreCompileValueBag(List<PreCompileValue> list)
	{
		this.list = list;
	}

	public List<PreCompileValue> getList()
	{
		return this.list;
	}
	
	
}

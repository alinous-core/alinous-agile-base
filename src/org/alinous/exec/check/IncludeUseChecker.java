package org.alinous.exec.check;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alinous.script.basic.IncludeSentence;
import org.alinous.script.basic.type.StringConst;

public class IncludeUseChecker {
	//private List<IncludeCheck> includes = new ArrayList<IncludeCheck>();
	private Map<String, IncludeCheck> includes = new HashMap<String, IncludeCheck>();
	
	public void registerInclude(IncludeSentence originalInclude)
	{
		IncludeCheck check = new IncludeCheck();
		check.setFilePath(originalInclude.getFilePath());
		check.setLine(originalInclude.getLine());
		check.setLinePosition(originalInclude.getLinePosition());
		
		StringConst module = (StringConst)originalInclude.getArgs().getStatement(0);
		String modulePath = module.getStr();
		check.setIncludeModule(modulePath);
		
		
		this.includes.put(modulePath, check);
	}
	
	public void useInclude(IncludeSentence originalInclude)
	{
		StringConst module = (StringConst)originalInclude.getArgs().getStatement(0);
		String modulePath = module.getStr();
		
		IncludeCheck check = this.includes.get(modulePath);
		if(check != null){
			check.setUsed(true);
		}
	}
	
	public List<IncludeCheck> getUnusedIncludes()
	{
		List<IncludeCheck> list = new ArrayList<IncludeCheck>();
		
		Iterator<String> it = this.includes.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			
			IncludeCheck check = this.includes.get(key);
			if(!check.isUsed()){
				list.add(check);
			}
		}
		
		return list;
	}
}

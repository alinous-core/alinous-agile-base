/**
 * CROSSFIRE JAPAN INCORPORATED
 * This source code is under GPL License.
 * info@crossfire.jp
 * Official web site
 * http://alinous.org
 * 
 *  Copyright (C) 2007 Tomohiro Iizuka
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.alinous.script.functions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alinous.exec.ScriptCheckContext;
import org.alinous.exec.ScriptError;
import org.alinous.script.AlinousScript;
import org.alinous.script.statement.FunctionCall;
import org.alinous.test.coverage.FileCoverage;


public class FuncDeclarations
{
	private Map<String, FunctionDeclaration> functions = new HashMap<String, FunctionDeclaration>();
	
	protected String filePath;
	
	public FuncDeclarations(String filePath)
	{
		this.filePath = filePath;
		
		Iterator<String> it = this.functions.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			FunctionDeclaration dec = this.functions.get(key);
			
			dec.setFilePath(filePath);
		}
	}
	
	public void addFunction(FunctionDeclaration declare)
	{
		declare.setFilePath(this.filePath);
		
		this.functions.put(declare.getName(), declare);
	}
	
	public void addFunction(FunctionDeclaration declare, boolean updateFilepath)
	{
		if(updateFilepath){
			declare.setFilePath(this.filePath);
		}
		
		this.functions.put(declare.getName(), declare);
	}
	
	public FunctionDeclaration findFunctionDeclare(String qualifiedName)
	{
		return this.functions.get(qualifiedName);
	}

	public String getFilePath()
	{
		return filePath;
	}

	public void setFilePath(String filePath)
	{
		this.filePath = filePath;
		
		Iterator<String> it = this.functions.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			FunctionDeclaration dec = this.functions.get(key);
			
			dec.setFilePath(filePath);
		}
	}
	
	public Iterator<String> iterateFuncNames()
	{
		return this.functions.keySet().iterator();
	}
	
	public void checkStaticErrors(ScriptCheckContext scContext,
			List<ScriptError> errorList)
	{
		Iterator<String> it = this.functions.keySet().iterator();
		while(it.hasNext()){
			String funcName = it.next();
			
			FunctionDeclaration dec = this.functions.get(funcName);
			
			dec.checkStaticErrors(scContext, errorList);
		}
	}
	
	public List<FunctionDeclaration> getListSortedByLine()
	{
		List<FunctionDeclaration> list = new ArrayList<FunctionDeclaration>();
		
		Iterator<String> it = this.functions.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			
			list.add(this.functions.get(key));
		}
		
		Collections.sort(list, new Comparator<FunctionDeclaration>() {

			@Override
			public int compare(FunctionDeclaration o1, FunctionDeclaration o2) {
				
				return o1.getLine() - o2.getLine();
			}
			
		});
		
		return list;
	}
	
	public void getFunctionCall(ScriptCheckContext scContext, List<FunctionCall> call, AlinousScript script)
	{
		Iterator<String> it = this.functions.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			
			this.functions.get(key).getFunctionCall(scContext, call, script);
		}
	}
	
	public void getFunctionCallByInclude(ScriptCheckContext scContext, List<FunctionCall> call, AlinousScript script, List<FunctionCall> callRoot)
	{
		Iterator<String> it = this.functions.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			
			FunctionDeclaration funcDec = this.functions.get(key);
			if(!isCalledByRoot(callRoot, funcDec)){
				continue;
			}
			
			funcDec.getFunctionCall(scContext, call, script);
		}
	}
	
	private boolean isCalledByRoot(List<FunctionCall> callRoot, FunctionDeclaration funcDec)
	{
		Iterator<FunctionCall> it = callRoot.iterator();
		while(it.hasNext()){
			FunctionCall called = it.next();
			
			String funcDecName = funcDec.getName();
			
			String qualifiedName = called.getPackageName() + "." + called.getFuncName();
			if(called.getPackageName() == null){
				qualifiedName = called.getFuncName();
			}
			
			if(funcDecName.equals(qualifiedName)){
				return true;
			}
		}
		
		return false;
	}
	
	public void setupCoverage(FileCoverage coverage)
	{
		Iterator<String> it = this.functions.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			FunctionDeclaration dec = this.functions.get(key);
			
			dec.setupCoverage(coverage);
		}
	}
}

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
package org.alinous.script.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.alinous.AlinousCore;
import org.alinous.debug.StepInCandidates;
import org.alinous.exec.ScriptCheckContext;
import org.alinous.exec.ScriptError;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.script.AlinousScript;
import org.alinous.script.IScriptSentence;
import org.alinous.script.statement.FunctionCall;
import org.alinous.test.coverage.FileCoverage;
import org.jdom.Element;



public class ScriptArray implements IScriptVariable, Cloneable
{
	private int line;
	private int linePosition;
	private String filePath;
	
	public static final String TAG_ARRAY = "ARRAY";
	public static final String ATTR_NAME = "name";
	
	private String name;
	private List<IScriptVariable> variables = new ArrayList<IScriptVariable>();
	
	private IScriptVariable parent;
	private int parentArrayIndex;

	public ScriptArray()
	{
		this.parentArrayIndex = -1;
	}
	
	public ScriptArray(String name)
	{
		this.name = name;
		this.parentArrayIndex = -1;
	}
	
	public String getType()
	{
		return IScriptVariable.TYPE_ARRAY;
	}
	
	public void add(IScriptVariable variable)
	{
		variable.setParent(this);
		variable.setParentArrayIndex(this.variables.size());
		
		variable.setName("[" + this.variables.size() + "]");
		
		this.variables.add(variable);
	}
	
	public void remove(IScriptVariable variable)
	{
		this.variables.remove(variable);
		
		// reindex
		Iterator<IScriptVariable> it = this.variables.iterator();
		int i = 0;
		while(it.hasNext()){
			IScriptVariable scriptVal = it.next();
			
			scriptVal.setName("[" + i + "]");
			scriptVal.setParentArrayIndex(i);
			i++;
		}
	}
	
	public void putAt(IScriptVariable variable, int index, PostContext context)
	{
		int defSize = this.variables.size();
		if(defSize - 1 < index){
			int appendNum = index - this.variables.size();
			
			for(int i = 0; i < appendNum + 1; i++){
				ScriptDomVariable v = new ScriptDomVariable("");
				v.setParent(this);
				v.setParentArrayIndex(i + defSize);
				
				v.setName("[" + i + "]");
				
				this.variables.add(v);
				
			}
		}
		
		IScriptVariable container = this.variables.get(index);
		if(container instanceof ScriptDomVariable && ((ScriptDomVariable)container).getNumProperties() != 0
				&& variable instanceof ScriptDomVariable && ((ScriptDomVariable)variable).getNumProperties() == 0){
			((ScriptDomVariable)container).value = ((ScriptDomVariable)variable).getValue();
			((ScriptDomVariable)container).valueType = ((ScriptDomVariable)variable).getValueType();
		}
		else{
			variable.setParent(this);
			variable.setParentArrayIndex(index);
			//this.variables.add(index, variable);
			this.variables.set(index, variable);
		}
		
		if(AlinousCore.debug(context)){
			variable.setName("[" + index + "]");
		}
	}
	
	public Iterator<IScriptVariable> iterator()
	{
		return this.variables.iterator();
	}
	
	public void clear()
	{
		this.variables.clear();
	}
	
	public int getSize()
	{
		return this.variables.size();
	}
	
	public boolean isEmpty()
	{
		return this.variables.isEmpty();
	}
	
	// Getter and Setter
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
	
	@Override
	public void exportIntoJDomElement(Element parent)
	{
		Element arrayElement = new Element(TAG_ARRAY);
		
		arrayElement.setAttribute(ATTR_NAME, this.name);

		Iterator<IScriptVariable> it = this.variables.iterator();
		while(it.hasNext()){
			IScriptVariable val = it.next();
			val.exportIntoJDomElement(arrayElement);
		}
		
		parent.addContent(arrayElement);
	}
	
	@Override
	public void exportIntoJDomElement(Element parent, String alias) {
		Element arrayElement = new Element(TAG_ARRAY);
		
		arrayElement.setAttribute(ATTR_NAME, alias);

		Iterator<IScriptVariable> it = this.variables.iterator();
		while(it.hasNext()){
			IScriptVariable val = it.next();
			val.exportIntoJDomElement(arrayElement);
		}
		
		parent.addContent(arrayElement);
	}

	@SuppressWarnings("unchecked")
	public void importFromJDomElement(Element arrayElement)
	{
		this.name = arrayElement.getAttributeValue(ATTR_NAME);
		
		List<Element> list = arrayElement.getChildren();
		
		Iterator<Element> it = list.iterator();
		while(it.hasNext()){
			Element element = it.next();
			IScriptVariable val = JDomScriptObjectFactory.createScriptVariable(element);
			
			val.importFromJDomElement(element);
			val.setParent(this);
			this.variables.add(val);
		}
		
	}
	
	public IScriptVariable get(String name)
	{
		Iterator<IScriptVariable> it = this.iterator();
		while(it.hasNext()){
			IScriptVariable val = it.next();
			if(val.getName().equals(name)){
				return val;
			}
		}
		
		return null;
	}
	
	public IScriptVariable get(int i)
	{
		if(i > this.variables.size() - 1 ){
			return null;
		}
		
		return this.variables.get(i);
	}
	

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		ScriptArray ar = new ScriptArray(this.name);
		
		Iterator<IScriptVariable> it = this.variables.iterator();
		while(it.hasNext()){
			IScriptVariable val = it.next();
			
			ar.add((IScriptVariable)val.clone());
		}
		
		return ar;
	}

	public IScriptVariable getParent()
	{
		return parent;
	}

	public void setParent(IScriptVariable parent)
	{
		this.parent = parent;
	}

	public int getParentArrayIndex()
	{
		return parentArrayIndex;
	}

	public void setParentArrayIndex(int parentArrayIndex)
	{
		this.parentArrayIndex = parentArrayIndex;
	}

	
	public IPathElement getPath()
	{
		IPathElement thisPath = null;
		
		if(this.parentArrayIndex < 0){
			thisPath =new PathElement(this.name);
		}
		else{
			thisPath = new ArrayPathElement(this.parentArrayIndex);
		}
		
		if(this.parent == null){
			return thisPath;
		}
		
		IPathElement path = this.parent.getPath();
		if(path == null){
			return thisPath;
		}
		
		getLeaf(path).setChild(thisPath);
		
		return path;
	}
	
	private IPathElement getLeaf(IPathElement first)
	{
		IPathElement ret = first;
		while(!ret.isLeaf()){
			ret = ret.getChild();
		}
		return ret;
	}

	public void canStepInStatements(StepInCandidates candidates)
	{
		
	}

	public IScriptVariable executeStatement(PostContext context, VariableRepository valRepo) throws ExecutionException
	{
		return null;
	}

	public void setCallerSentence(IScriptSentence callerSentence)
	{
		
	}

	public void setCurrentDataSource(String dataSource)
	{
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public int getLinePosition() {
		return linePosition;
	}

	public void setLinePosition(int linePosition) {
		this.linePosition = linePosition;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public void checkStaticErrors(ScriptCheckContext scContext,
			List<ScriptError> errorList)
	{
		
	}

	@Override
	public void getFunctionCall(ScriptCheckContext scContext, List<FunctionCall> call, AlinousScript script)
	{
		
	}

	@Override
	public void setupCoverage(FileCoverage coverage)
	{
		
	}
	
	public void sort(String sortKey, boolean asc)
	{
		ScriptArraySorter sorter = new ScriptArraySorter(sortKey, asc);
		Collections.sort(this.variables, sorter);
		
		int i = 0;
		Iterator<IScriptVariable> it = this.variables.iterator();
		while(it.hasNext()){
			IScriptVariable val = it.next();
			
			val.setParentArrayIndex(i);
			val.setName("[" + i + "]");
			
			i++;
		}
		
	}
	
	public String[] toStringArray()
	{
		ArrayList<String> array = new ArrayList<String>();
		Iterator<IScriptVariable> it = this.variables.iterator();
		while(it.hasNext()){
			IScriptVariable val = it.next();
			
			if(val instanceof ScriptDomVariable){
				array.add(((ScriptDomVariable)val).getValue());
			}
			
		}
		
		return array.toArray(new String[array.size()]);
	}

}

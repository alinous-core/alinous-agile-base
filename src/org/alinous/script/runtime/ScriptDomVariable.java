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

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.alinous.debug.StepInCandidates;
import org.alinous.exec.ScriptCheckContext;
import org.alinous.exec.ScriptError;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.script.AlinousScript;
import org.alinous.script.IScriptSentence;
import org.alinous.script.statement.FunctionCall;
import org.alinous.test.coverage.FileCoverage;
import org.apache.commons.codec.binary.Base64;
import org.jdom.Element;

public class ScriptDomVariable extends ScriptVariable implements Cloneable
{
	private int line;
	private int linePosition;
	private String filePath;
	
	public static final String TAG_VARIABLE = "VARIABLE";
	public static final String ATTR_NAME = "name";
	public static final String ATTR_VALUE = "value";
	public static final String ATTR_TYPE = "valuetype";
	
	private Map<String, IScriptVariable> map = new ConcurrentHashMap<String, IScriptVariable>();
	
	private IScriptVariable parent;
	private int parentArrayIndex;
	
	
	public ScriptDomVariable(String name)
	{
		this.name = name;
		this.parentArrayIndex = -1;
		this.valueType = IScriptVariable.TYPE_STRING;
	}
	
	public IScriptVariable get(String key)
	{
		return this.map.get(key);
	}
	
	public void put(IScriptVariable value)
	{
		// if new value does not have child
		if(value instanceof ScriptDomVariable && ((ScriptDomVariable)value).getNumProperties() == 0){
			IScriptVariable destVal = this.map.get(value.getName());
			
			if(destVal instanceof ScriptDomVariable){
				((ScriptDomVariable)destVal).value = ((ScriptDomVariable)value).getValue();
				((ScriptDomVariable)destVal).valueType = ((ScriptDomVariable)value).getValueType();
				
				return;
			}

		}
		
		// if new value has children or Array
		value.setParent(this);
		value.setParentArrayIndex(-1);
		this.map.put(value.getName(), value);
	}
	
	public void clearClildren()
	{
		this.map.clear();
	}
	
	public Iterator<String> getPropertiesIterator()
	{
		return this.map.keySet().iterator();
	}
	
	public int getNumProperties()
	{
		return this.map.size();
	}
	
	// Getter and Setter
	public String getType()
	{
		return IScriptVariable.TYPE_HASH;
	}

	public void exportIntoJDomElement(Element parent)
	{
		Element variableElement = new Element(TAG_VARIABLE);
		
		variableElement.setAttribute(ATTR_NAME, this.name);
		variableElement.setAttribute(ATTR_TYPE, this.valueType);
		
		
		if(this.value != null){
			//String encVal = new BASE64Encoder().encode(this.value.getBytes());
			
			// encode
			String encVal = null;
			try {
				encVal = new String(Base64.encodeBase64(this.value.getBytes("UTF-8")), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
			variableElement.setText(encVal);
		}
		
		
		
		Iterator<String> it = this.map.keySet().iterator();
		while(it.hasNext()){
			String prop = it.next();
			IScriptVariable val = this.map.get(prop);
			
			val.exportIntoJDomElement(variableElement);
		}		
		
		parent.addContent(variableElement);
	}
	

	@Override
	public void exportIntoJDomElement(Element parent, String alias) {
		Element variableElement = new Element(TAG_VARIABLE);
		
		variableElement.setAttribute(ATTR_NAME, alias);
		variableElement.setAttribute(ATTR_TYPE, this.valueType);
		
		
		if(this.value != null){
			//String encVal = new BASE64Encoder().encode(this.value.getBytes());
			
			// encode
			String encVal = null;
			try {
				encVal = new String(Base64.encodeBase64(this.value.getBytes("UTF-8")), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
			variableElement.setText(encVal);
		}
		
		
		
		Iterator<String> it = this.map.keySet().iterator();
		while(it.hasNext()){
			String prop = it.next();
			IScriptVariable val = this.map.get(prop);
			
			val.exportIntoJDomElement(variableElement);
		}		
		
		parent.addContent(variableElement);
	}

	@SuppressWarnings("unchecked")
	public void importFromJDomElement(Element variableElement)
	{
		this.name = variableElement.getAttributeValue(ATTR_NAME);
		//this.value = variableElement.getAttributeValue(ATTR_VALUE);
		this.valueType = variableElement.getAttributeValue(ATTR_TYPE);
		
		String encString = variableElement.getText();
		if(encString != null){
			try {
				this.value = new String(Base64.decodeBase64(encString.getBytes("UTF-8")), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		List<Element> list = variableElement.getChildren();
		Iterator<Element> it = list.iterator();
		while(it.hasNext()){
			Element element = it.next();
			IScriptVariable val = JDomScriptObjectFactory.createScriptVariable(element);
			
			val.importFromJDomElement(element);
			
			this.map.put(val.getName(), val);
			val.setParent(this);
		}
		
	}

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		ScriptDomVariable dom = new ScriptDomVariable(this.name);
		dom.value = this.value;
		dom.valueType = this.valueType;
		dom.parent = this.parent;
		dom.line = this.line;
		dom.filePath = this.filePath;
		dom.linePosition = this.linePosition;
		
		Iterator<String> it = this.map.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			IScriptVariable innerVariable = this.map.get(key);
			
			
			IScriptVariable clonedVal = (IScriptVariable)innerVariable.clone();
			dom.map.put(key, clonedVal);
			clonedVal.setParent(dom);
			
		}
		
		return dom;
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
	
	public void releaseProperty(String prop)
	{
		this.map.remove(prop);
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
		getValue();
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

	@Override
	public String toString() {
		return this.value;
	}

}

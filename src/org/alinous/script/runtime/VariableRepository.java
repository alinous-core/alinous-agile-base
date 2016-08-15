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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alinous.AlinousCore;
import org.alinous.AlinousDebug;
import org.alinous.debug.StepInCandidates;
import org.alinous.exec.ScriptCheckContext;
import org.alinous.exec.ScriptError;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.AlinousScript;
import org.alinous.script.IScriptSentence;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.statement.FunctionCall;
import org.alinous.test.coverage.FileCoverage;
import org.apache.commons.codec.binary.Base64;
import org.jdom.Attribute;
import org.jdom.Element;


public class VariableRepository implements IScriptVariable
{
	private int line;
	private int linePosition;
	private String filePath;
	
	public static final String TAG_VALUE_REPOSITORY = "VALUE_REPOSITORY";
	
	//private ConcurrentHashMap<String, IScriptVariable> variables = new ConcurrentHashMap<String, IScriptVariable>();
	private Map<String, IScriptVariable> variables = new HashMap<String, IScriptVariable>();
	
	private Map<PostContext, FinalRepository> finalRepositoryMap = new WrappedMap();
	
	private Map<String, IScriptVariable> aliasMap = new HashMap<String, IScriptVariable>();
	
	public VariableRepository()
	{
		
	}
	
	
	public void clear()
	{
		this.variables.clear();
	}
	
	private boolean isFinalExists(PostContext context, String variablePath) throws ExecutionException, RedirectRequestException
	{
		IPathElement variablePathEl = PathElementFactory.buildPathElement(variablePath);
		return isFinalExists(context, variablePathEl);
	}
	
	private boolean isFinalExists(PostContext context, IPathElement variablePathEl) throws ExecutionException, RedirectRequestException
	{
		if(context == null){
			return false;
		}
		
		VariableRepository finalRepo = null;
		
		synchronized (this.finalRepositoryMap) {
			finalRepo = this.finalRepositoryMap.get(context);
		}
		
		if(finalRepo == null){
			// 
			// AlinousDebug.debugOut("isFinalExists no finalRepo for " + variablePathEl.getPathString(context, this));
			return false;
		}
		
		
		//AlinousDebug.debugOut("isFinalExists(): finalRepo");
		//finalRepo.dump();
		
		//AlinousDebug.debugOut("isFinalExists(): finalRepo");
		
		IPathElement current = variablePathEl;
		while(true){
			
			// debug
			//AlinousDebug.debugOut("current path is " + current.getPathString(context, this));
			
			IScriptVariable val = finalRepo.getVariable(current, context);
			if(val != null){
				// debug
				//AlinousDebug.debugOut("isFinalExists variable Exists OKKKK : " + val + " path : " + current.getPathString(context, this));
				return true;
			}
			
			if(current.getChild() == null){
				break;
			}
			current = current.removeLast();

		}
		/*
		IScriptVariable val = finalRepo.getVariable(variablePathEl, context);
		if(val != null){
			//AlinousDebug.debugOut("isFinalExists variable Exists OKKKK");
			return true;
		}*/
		
		//AlinousDebug.debugOut("isFinalExists variable not Exists repo : " + finalRepo);
		//AlinousDebug.debugOut("isFinalExists variable not Exists path : " + variablePathEl.getPathString(context, this));
		//finalRepo.dump();
		
		return false;
	}
	
	private FinalRepository getFinalRepository(PostContext context) throws ExecutionException
	{
		if(context == null){
			throw new ExecutionException("Assertion. Context cannot null here.");
		}
		
		FinalRepository finalRepo = null;
		synchronized (this.finalRepositoryMap) {
			finalRepo = this.finalRepositoryMap.get(context);
		}
		if(finalRepo == null){
			//  alinosuDebug
			//AlinousDebug.debugOut("New final repo : " + scopeName);
			
			finalRepo = new FinalRepository();
			synchronized (this.finalRepositoryMap) {
				this.finalRepositoryMap.put(context, finalRepo);
			}
			
		}
		
		return finalRepo;
	}
	
	public void clearFinalRepository(PostContext context)
	{
		synchronized (this.finalRepositoryMap) {
			this.finalRepositoryMap.remove(context);
		}
	}
	
	public void putValue(IScriptVariable value, PostContext context) throws ExecutionException, RedirectRequestException
	{
		// debug
		if(context == null){
			throw new ExecutionException("Assertion. Context cannot null here.");
		}
		// 
		//AlinousDebug.debugOut(" putValue(IScriptVariable value, PostContext context) : "
		//			+ variablePath + "," + value + "," + context);
	
		IPathElement variablePath = PathElementFactory.buildPathElement(value.getName());
		
		if(isFinalExists(context, variablePath)){
			getFinalRepository(context).putValue(value, context);
			// 
			//AlinousDebug.debugOut("    ---> final");
			
			return;
		}
		
		// alias
		if(isAlias(value.getName())){
			putAlias(value, value.getName());
			
			return;
		}
		
		value.setParentArrayIndex(-1);
		value.setParent(this);
		this.variables.put(value.getName(), value);
	}
	
	private void putFinalValue(String variablePath, String value, String valueType, PostContext context) throws ExecutionException, RedirectRequestException
	{
		VariableRepository finalRepo = getFinalRepository(context);
		finalRepo.putValue(variablePath, value, valueType, context);
	}
	
	public void putValue(String variablePath, String value, String valueType, PostContext context) throws ExecutionException, RedirectRequestException
	{
		if(context == null){
			throw new ExecutionException("Assertion. Context cannot null here.");
		}
		// 
		//AlinousDebug.debugOut(" putValue(String variablePath, IScriptVariable value, PostContext context) : "
		//			+ variablePath + "," + value + "," + context);
		
		if(isFinalExists(context, variablePath)){
			putFinalValue(variablePath, value, valueType, context);
			
			// 
			//AlinousDebug.debugOut("    ---> final");
			
			return;
		}
		
		// alias
		if(isAlias(variablePath)){
			ScriptDomVariable newVariable =new ScriptDomVariable(variablePath);
			newVariable.setValue(value);
			newVariable.setValueType(valueType);
			
			putAlias(newVariable, variablePath);
			
			return;
		}
		
		IPathElement element = PathElementFactory.buildPathElement(variablePath);
		IScriptVariable variable = this;
		
		while(!element.isLeaf()){
			variable = element.input(variable, context, this);
			
			element = element.getChild();
		}
		
		// variable is container
		if(variable instanceof ScriptDomVariable){
			String pathFlagment = ((PathElement)element).getPath();
			
			
			IScriptVariable currentV = variable.get(pathFlagment);
			if(currentV instanceof ScriptDomVariable){
				ScriptDomVariable newVariable = (ScriptDomVariable)currentV;
				newVariable.setValue(value);
				newVariable.setValueType(valueType);
			}
			else{			
				ScriptDomVariable newVariable = new ScriptDomVariable(pathFlagment);
				newVariable.setValue(value);
				newVariable.setValueType(valueType);
				
				((ScriptDomVariable)variable).put(newVariable);
			}
		}
		else if(variable instanceof VariableRepository){
			String pathFlagment = ((PathElement)element).getPath();
			
			
			IScriptVariable currentV = variable.get(pathFlagment);
			if(currentV instanceof ScriptDomVariable){
				ScriptDomVariable newVariable = (ScriptDomVariable)currentV;
				newVariable.setValue(value);
				newVariable.setValueType(valueType);
			}
			else{			
				ScriptDomVariable newVariable = new ScriptDomVariable(pathFlagment);
				newVariable.setValue(value);
				newVariable.setValueType(valueType);
				
				this.putValue(newVariable, context);
			}
		}
		else{ // means variable instanceof ScriptArray
			ScriptDomVariable newVariable = new ScriptDomVariable("");
			newVariable.setValue(value);
			newVariable.setValueType(valueType);
			
			ScriptArray arrayParent = (ScriptArray)variable;
			ArrayPathElement arrayPathElenent = (ArrayPathElement)element;
			
			ScriptDomVariable indexDomValue 
				= (ScriptDomVariable) arrayPathElenent.getNumber().executeStatement(context, this);
			arrayParent.putAt(newVariable, Integer.parseInt(indexDomValue.getValue()), context);
		}
	}
	
	public void putFinalValue(IPathElement path, IScriptVariable value, PostContext context) throws ExecutionException, RedirectRequestException
	{
		VariableRepository finalRepo = getFinalRepository(context);
		finalRepo.putValue(path, value, context);
	}
	
	public void putValue(IPathElement path, IScriptVariable value, PostContext context) throws ExecutionException, RedirectRequestException
	{
		if(context == null){
			throw new ExecutionException("Assertion. Context cannot null here.");
		}
		// 
		//AlinousDebug.debugOut(" putValue(IPathElement path, IScriptVariable value, PostContext context) : "
		//			+ path.getPathString(context, this) + ", .... ," + context);
		
		if(isFinalExists(context, path)){
			// 
			//AlinousDebug.debugOut("    ----> final");
			putFinalValue(path, value, context);
			return;
		}
		
		
		IPathElement lastPathElement = path.getLast(); // last means SELF
		IPathElement containerPathElement = path.removeLast();
				
		
		// Set name
		if(lastPathElement instanceof PathElement){
			String valueName  = ((PathElement)lastPathElement).getPath();
			value.setName(valueName);
		}
		// Set parent variable
		
		IScriptVariable container = null;
		if(containerPathElement == null || containerPathElement instanceof VariableRepository){
			container = this;			
		}else{
			container = getAndMakeVariableFromPath(containerPathElement, context);
		}
		
		
		if(container instanceof VariableRepository){
			((VariableRepository)container).putValue(value, context);
		}
		else if(container instanceof ScriptDomVariable){
			((ScriptDomVariable)container).put(value);
		}
		else if(container instanceof ScriptArray){
			IStatement stmt = ((ArrayPathElement)lastPathElement).getNumber();
			ScriptDomVariable valNum = (ScriptDomVariable)stmt.executeStatement(context, this);
			
			int pos = Integer.parseInt(valNum.getValue());
			((ScriptArray)container).putAt(value, pos, context);
		}
	}
	
	public void substitute(IPathElement path, IScriptVariable value, PostContext context) throws ExecutionException, RedirectRequestException, CloneNotSupportedException
	{

		
		if(isFinalExists(context, path)){
			getFinalRepository(context).substitute(path, value, context);
			
			return;
		}
		
		IScriptVariable dest = getVariable(path, context);
		if(dest == value){ // if same object
			return;
		}
		
		if(value instanceof ScriptDomVariable && dest instanceof ScriptDomVariable){
			ScriptDomVariable srcDom = (ScriptDomVariable)value.clone(); // clone here
			ScriptDomVariable destDom = (ScriptDomVariable)dest;
			
			destDom.clearClildren();
			
			destDom.setValue(srcDom.getValue());
			destDom.setValueType(srcDom.getValueType());
			
			Iterator<String> it = srcDom.getPropertiesIterator();
			while(it.hasNext()){
				String prop = it.next();
				IScriptVariable propValue = (IScriptVariable) srcDom.get(prop);
				
				destDom.put(propValue);
			}
			
			return;
		}
		if(value instanceof ScriptArray && dest instanceof ScriptArray){
			ScriptArray srcAr = (ScriptArray)value.clone(); // clone here
			ScriptArray destAr = (ScriptArray)dest;
			
			destAr.clear();
			
			Iterator<IScriptVariable> it = srcAr.iterator();
			while(it.hasNext()){
				IScriptVariable element = it.next();
				destAr.add(element);
			}
			return;
		}
		
		// else
		putValue(path, (IScriptVariable) value.clone(), context);
	}
	
	public void release(String path, PostContext context) throws ExecutionException, RedirectRequestException
	{
		IPathElement element = PathElementFactory.buildPathElement(path);
		
		release(element, context);
	}
	
	public void release(IPathElement path, PostContext context)
		throws ExecutionException, RedirectRequestException
	{
		// 
		//AlinousDebug.debugOut("release() context : " + context); 
		//AlinousDebug.debugOut("release() path.getPathString(context, this) : " + path.getPathString(context, this)); 
		
		if(isFinalExists(context, path)){
			//  
			//AlinousDebug.debugOut("Release final");
			
			getFinalRepository(context).release(path, context);
			return;
		}
		
		// alias
		
		IScriptVariable val = getVariable(path, context);
		if(val == null){
			
			return;
		}
		
		IScriptVariable parentVal = val.getParent();
		
		if(parentVal instanceof ScriptDomVariable){
			((ScriptDomVariable)parentVal).releaseProperty(val.getName());
		}		
		else if(parentVal instanceof ScriptArray){
			//int idx = val.getParentArrayIndex();
			
			ScriptArray parentArray = (ScriptArray)parentVal;
			parentArray.remove(val);
			
			//throw new ExecutionException("Cannot release object in the array."); // i18n
		}else if(parentVal instanceof VariableRepository){
			// parentVal means this
			((VariableRepository)parentVal).variables.remove(val.getName());

		}
	}
	
	
	public IScriptVariable getValue(String key)
	{
		// key
		if(isAlias(key)){
			return this.aliasMap.get(key);
		}
		
		return this.variables.get(key);
	}

	@Override
	public IScriptVariable get(String key)
	{
		return getValue(key);
	}

	public Iterator<String> getKeyIterator()
	{
		Set<String> keys = new HashSet<String>();
		
		keys.addAll(this.variables.keySet());
		Iterator<String> it = this.aliasMap.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			if(this.variables.keySet().contains(key)){
				continue;
			}
			keys.add(key);
		}
		
		return keys.iterator();
	}
	
	public void exportIntoJDomElement(Element parent)
	{
		Element repositoryElement = new Element(TAG_VALUE_REPOSITORY);
		
		Iterator<String> it =this.variables.keySet().iterator();
		while(it.hasNext()){
			String prop = it.next();
			
			
			IScriptVariable val = this.variables.get(prop);
			
			val.exportIntoJDomElement(repositoryElement);
		}
		
		it = this.aliasMap.keySet().iterator();
		while(it.hasNext()){
			String prop = it.next();
			
			
			IScriptVariable val = this.aliasMap.get(prop);
			
			val.exportIntoJDomElement(repositoryElement, prop);
		}
		
		parent.addContent(repositoryElement);
	}
	
	public void exportIntoJDomElementWithFinal(Element parent, PostContext context) throws ExecutionException
	{
		Element repositoryElement = new Element(TAG_VALUE_REPOSITORY);
		
		FinalRepository frepo = getFinalRepository(context);
		
		Iterator<String> it =this.variables.keySet().iterator();
		while(it.hasNext()){
			String prop = it.next();
			
			IScriptVariable val = this.variables.get(prop);
			
			// if final exisits
			if(frepo != null){
				IScriptVariable fval = frepo.get(prop);
				if(fval != null){
					val = fval;
				}
			}
			
			
			val.exportIntoJDomElement(repositoryElement);
		}
		
		it = this.aliasMap.keySet().iterator();
		while(it.hasNext()){
			String prop = it.next();
			
			IScriptVariable val = this.aliasMap.get(prop);
			
			val.exportIntoJDomElement(repositoryElement, prop);
		}
		
		//  overwrite
		Iterator<?> itEl = repositoryElement.getChildren().iterator();
		while(itEl.hasNext()){
			Object child = itEl.next();
			
			if(child instanceof Element){
				Element element = (Element)child;
				
				overWriteElement(element, "", context);
			}
		}
		writeVariableInFinal(repositoryElement, context);		
		
		parent.addContent(repositoryElement);
	}
	
	private void writeVariableInFinal(Element repositoryElement, PostContext context)
	{
		VariableRepository frepo = null;
		synchronized (this.finalRepositoryMap) {
			frepo = this.finalRepositoryMap.get(context);
		}
		
		if(frepo == null){
			return;
		}
		
		Iterator<String> it = Collections.synchronizedSet(frepo.variables.keySet()).iterator();
		while(it.hasNext()){
			String prop = it.next();
			
			if(this.variables.containsKey(prop)){
				continue;
			}
			
			IScriptVariable val = frepo.variables.get(prop);
			
			val.exportIntoJDomElement(repositoryElement);
		}
		
	}
	
	private void overWriteElement(Element element, String parentPath, PostContext context)
	{
		// self
	//	String alinousVariableType = element.getName();
		Attribute name = element.getAttribute(ScriptDomVariable.ATTR_NAME);
	//	Attribute type = element.getAttribute(ScriptDomVariable.ATTR_TYPE);
	//	Attribute valueType = element.getAttribute(ScriptDomVariable.ATTR_TYPE);
		
	/*
		AlinousDebug.debugOut("element alinousVariableType : " + alinousVariableType);
		AlinousDebug.debugOut("element name : " + name.toString());
		if(type == null){
			AlinousDebug.debugOut("element type : " + null);
		}else{
			AlinousDebug.debugOut("element type : " + type.toString());
		}
		AlinousDebug.debugOut("element value : " + element.getText());
		if(type == null){
			AlinousDebug.debugOut("element valueType : " + null);
		}else{
			AlinousDebug.debugOut("element valueType : " + valueType.toString());
		}
		*/
		// SelfPath
		// Array object name is [1] [11] [23]
		//AlinousDebug.debugOut(" parentPath " + parentPath);
		String self = parentPath + name.getValue();
		if(name.getValue().startsWith("[") && parentPath.length() > 0){
			self = parentPath.substring(0, parentPath.length() - 1) + name.getValue();
		}
		//AlinousDebug.debugOut("###### variablePath -> : " + self);
		
		doOverWrite(self, context, element);
		
		
		String nextParentPath = self + ".";
		// children
		Iterator<?> it = element.getChildren().iterator();
		while(it.hasNext()){
			Object ch = it.next();
			if(!(ch instanceof Element)){
				continue;
			}
			overWriteElement((Element) ch, nextParentPath, context);
		}
		
	}
	
	private void doOverWrite(String selfPath, PostContext context, Element element)
	{
		if(context.getParallelFinalVariableScope().isEmpty()){
			return;
		}
		//AlinousDebug.debugOut("select -> " + selfPath);
		
		VariableRepository frepo = null;
		synchronized (this.finalRepositoryMap) {
			frepo = this.finalRepositoryMap.get(context);
		}
		
		if(frepo == null){
			return;
		}
		IScriptVariable val = frepo.get(selfPath);
		
		if(val == null){
			return;
		}
		
		//AlinousDebug.debugOut("Hit !!!!!!!!!!!!!!!!!!!!!!!!! -> " + selfPath);
		if(val instanceof ScriptDomVariable ){
			ScriptDomVariable dom = (ScriptDomVariable)val;
			
			element.setAttribute(ScriptDomVariable.ATTR_TYPE, dom.getValueType());
			
			// encode
			if(dom.getValue() != null){
					String encVal = null;
					try {
						encVal = new String(Base64.encodeBase64(dom.getValue().getBytes("UTF-8")), "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					
					element.setText(encVal);
			}else{
				element.setText("");
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void importFromJDomElement(Element repositoryElement)
	{
		List<Element> list = repositoryElement.getChildren();
		Iterator<Element> it = list.iterator();
		while(it.hasNext()){
			Element element = it.next();
			IScriptVariable val = JDomScriptObjectFactory.createScriptVariable(element);
			
			val.importFromJDomElement(element);
			this.variables.put(val.getName(), val);
			
			val.setParent(this);
			val.setParentArrayIndex(-1);
		}
	}

	public String getName()
	{
		return IScriptVariable.TYPE_REPOSITORY;
	}

	public void setName(String name)
	{
		// Do nothing
	}
	
	public String getType()
	{
		return IScriptVariable.TYPE_REPOSITORY;
	}
	
	/**
	 * path :ex test.dom.ar[2].te
	 * @return
	 * @throws ExecutionException 
	 * @throws RedirectRequestException 
	 */
	public IScriptVariable getAndMakeVariableFromPath(String variablePath, PostContext context)
			throws ExecutionException, RedirectRequestException
	{
		IPathElement element = PathElementFactory.buildPathElement(variablePath);
		IScriptVariable variable = this;
		
		while(element != null){
			variable = element.input(variable, context, this);
			
			element = element.getChild();
		}
		
		return variable;
	}
	
	public IScriptVariable getAndMakeVariableFromPath(IPathElement variablePath, PostContext context)
			throws ExecutionException, RedirectRequestException
	{
		IPathElement element = variablePath;
		IScriptVariable variable = this;
		
		while(element != null){
			variable = element.input(variable, context, this);
			
			element = element.getChild();
		}
			
		return variable;
	}
	
	public IScriptVariable getVariable(IPathElement variablePath, PostContext context) throws ExecutionException, RedirectRequestException
	{
		if(context == null){
			throw new ExecutionException("Assertion. Context cannot null here.");
		}
		
		// 
		//AlinousDebug.debugOut("getVariable : " + variablePath.getPathString(context, this));
		
		if(isFinalExists(context, variablePath)){
			// 
			//AlinousDebug.debugOut("  ----> final");
			//this.finalRepositoryMap.get(context).dump();
			
			synchronized (this.finalRepositoryMap) {
				return this.finalRepositoryMap.get(context).getVariable(variablePath, context);
			}
			
		}
		
		IPathElement element = variablePath;
		IScriptVariable variable = this;
		
		while(element != null && variable != null){
			if(element.getParent() == null && element instanceof PathElement &&
					isAlias(((PathElement)element).getPath())){
				PathElement pathEl = (PathElement)element;
				
				
				variable = this.aliasMap.get(pathEl.getPath());
				
				if(variable == null){
					return null;
				}
			}
			else if(element instanceof PathElement){
				PathElement pathEl = (PathElement)element;
				
				variable = variable.get(pathEl.getPath());
				
				if(variable == null){
					return null;
				}
			}
			else if(element instanceof ArrayPathElement){
				// check the specified path is valid
				if(!(variable instanceof ScriptArray)){
					return null;
				}
				
				ArrayPathElement arrayEl = (ArrayPathElement)element;
				ScriptArray array = (ScriptArray)variable;
				
				ScriptDomVariable numVal = (ScriptDomVariable)arrayEl.getNumber().executeStatement(context, this);
				variable = array.get(Integer.parseInt(numVal.getValue()));
				
				if(variable == null){
					return null;
				}
			}
			
			element = element.getChild();
			
		}
		
		return variable;
	}
	
	
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		VariableRepository repo = new VariableRepository();
		
		Iterator<String> it = this.variables.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			IScriptVariable innerVariable = this.variables.get(key);
			
			repo.variables.put(key, (IScriptVariable)innerVariable.clone());
			
		}
		
		return repo;
	}

	public IScriptVariable getParent()
	{
		return null;
	}

	public void setParent(IScriptVariable parent)
	{
	}

	public IPathElement getPath()
	{
		return null;
	}

	public int getParentArrayIndex()
	{
		return 0;
	}

	public void setParentArrayIndex(int parentArrayIndex)
	{
		
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
	
	public VariableRepository getFinelRepository(PostContext context)
	{
		synchronized (this.finalRepositoryMap) {
			return this.finalRepositoryMap.get(context);
		}
	}
	
	public void dump(AlinousCore core)
	{
		AlinousDebug.debugOut(core, "**************************************");
		
		Iterator<String> it = this.getKeyIterator();
		while(it.hasNext()){
			String key = it.next();
			
			IScriptVariable sv =  this.variables.get(key);
			String value = sv.toString();
			
			AlinousDebug.debugOut(core, "key : " + key + " value : " + value);
		}
		AlinousDebug.debugOut(core, "**************************************");
	}
	
	public void dumpFinals(AlinousCore core)
	{
		AlinousDebug.debugOut(core, "**************FINAL************************");
		
		Iterator<PostContext> it = this.finalRepositoryMap.keySet().iterator();
		while(it.hasNext()){
			PostContext key = it.next();
			
			VariableRepository finalRepo = this.finalRepositoryMap.get(key);
			finalRepo.dump(core);
			
		}
		
		AlinousDebug.debugOut(core, "**************************************");
	}
	
	
	public void dumpFinal(PostContext context){
		VariableRepository finalRepo = this.finalRepositoryMap.get(context);
		
		if(finalRepo == null){
			AlinousDebug.debugOut(context.getCore(), "**************FINAL************************");
			AlinousDebug.debugOut(context.getCore(), "Final is null");
			AlinousDebug.debugOut(context.getCore(), "**************************************");
			return;
		}
		
		AlinousDebug.debugOut(context.getCore(), "**************FINAL************************");
		finalRepo.dump(context.getCore());
		AlinousDebug.debugOut(context.getCore(), "**************************************");
	}
	
	public void putAlias(IScriptVariable variable, String name)
	{
		this.aliasMap.put(name, variable);
	}
	
	private boolean isAlias(String name)
	{
		return this.aliasMap.get(name) != null;
	}

	public Set<String> getAliasKeys()
	{
		return this.aliasMap.keySet();
	}
	
	@Override
	public void exportIntoJDomElement(Element parent, String alias) {
		// Do not use this
	}
	
}


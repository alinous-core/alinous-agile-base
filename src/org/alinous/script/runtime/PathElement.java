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

import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;

public class PathElement implements IPathElement
{
	private IPathElement child;
	private IPathElement parent;
	private String path;
	
	private boolean isArrayContainer;
	
	public PathElement(String path)
	{
		this.path = path;
	}
	
	public String getPathString(PostContext context, VariableRepository valRepo)
		throws ExecutionException, RedirectRequestException
	{
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(path);
		
		if(this.child != null && this.child instanceof PathElement){
			buffer.append(".");
			buffer.append(this.child.getPathString(context, valRepo));
		}
		else if(this.child != null && this.child instanceof ArrayPathElement){
			buffer.append(this.child.getPathString(context, valRepo));
		}
		
		return buffer.toString();
	}

	public String getPath()
	{
		return this.path;
	}
	
	public IPathElement getChild()
	{
		return child;
	}

	public void setChild(IPathElement child)
	{
		this.child = child;
		
		if(child instanceof ArrayPathElement){
			this.isArrayContainer = true;
		}
	}

	public IPathElement getParent()
	{
		return parent;
	}

	public void setParent(IPathElement parent)
	{
		this.parent = parent;
	}

	public IScriptVariable input(IScriptVariable container, PostContext context, VariableRepository valRepo)
			throws ExecutionException, RedirectRequestException
	{
		if(container instanceof VariableRepository){
			return inputIntoRepository((VariableRepository)container, context);
		}
		
		if(!(container instanceof ScriptDomVariable)){
			throw new ExecutionException("Failed to input variable");
		}
		
		ScriptDomVariable domVariable = (ScriptDomVariable)container;
		IScriptVariable cur = domVariable.get(this.path);
		
		// if instance type is different. create another one;
		if(!(cur instanceof ScriptDomVariable) && !isArrayContainer){
			cur = null;
		}
		if(!(cur instanceof ScriptArray) && isArrayContainer){
			cur = null;
		}
		
		if(cur == null){
			// createt nre instance
			if(!isArrayContainer){
				cur = new ScriptDomVariable(this.path);
			}
			else if(isArrayContainer){
				cur = new ScriptArray(this.path);
			}
			
			domVariable.put(cur);
		}
		
		return cur;
	}
	
	private IScriptVariable inputIntoRepository(VariableRepository container, PostContext context) throws ExecutionException, RedirectRequestException
	{
		IPathElement inputPath1 = PathElementFactory.buildPathElement(this.path);
		
		//IScriptVariable cur = container.getValue(this.path);
		IScriptVariable cur = container.getVariable(inputPath1, context);
		
		// if instance type is different. create another one;
		if(!(cur instanceof ScriptDomVariable) && !isArrayContainer){
			cur = null;
		}
		if(!(cur instanceof ScriptArray) && isArrayContainer){
			cur = null;
		}
		
		if(cur == null){
			// createt nre instance
			if(!isArrayContainer){
				cur = new ScriptDomVariable(this.path);
			}
			else if(isArrayContainer){
				cur = new ScriptArray(this.path);
			}
			
			IPathElement inputPath = PathElementFactory.buildPathElement(this.path);
			container.putValue(inputPath, cur, context);
			// container.putValue(cur);
		}
		
		return cur;
	}
	
	public boolean isLeaf()
	{
		return this.child == null;
	}
	
	public int getType()
	{
		return IPathElement.TYPE_DOM;
	}
	
	public IPathElement getLast()
	{
		IPathElement cur = this;
		
		while(cur.getChild() != null){
			cur = cur.getChild();
		}
		
		return cur;
	}

	public IPathElement removeLast()
	{
		IPathElement copy = deepCopy();
		IPathElement last = copy.getLast();
		IPathElement cur = copy;
		
		while(cur.getChild() != last){
			if(cur.getChild() == null){
				return null;
			}
			
			cur = cur.getChild();

		}
		
		cur.setChild(null);
		
		return copy;
	}
	
	protected IPathElement deepCopy()
	{
		PathElement retElement = new PathElement(this.path);
		
		IPathElement curSrc = this;
		IPathElement curDst = retElement;
		
		while(curSrc.getChild() != null){
			curSrc = curSrc.getChild(); // it.next()
			
			IPathElement newElement = null;
			try {
				newElement = (IPathElement) curSrc.clone();
				newElement.setParent(curDst);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			
			curDst.setChild(newElement);
			
			curDst = newElement;
		}
		
		
		return retElement;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		PathElement newElement = new PathElement(this.path);
		newElement.isArrayContainer = this.isArrayContainer;
		
		return newElement;
	}

	public boolean isArrayContainer()
	{
		return isArrayContainer;
	}

	public void setArrayContainer(boolean isArrayContainer)
	{
		this.isArrayContainer = isArrayContainer;
	}
	
	
}

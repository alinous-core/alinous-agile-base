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
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.basic.type.NumericConst;

public class ArrayPathElement implements IPathElement, Cloneable
{
	private IStatement number;
	private IPathElement child;
	private IPathElement parent;
	
	private boolean isArrayContainer;
	
	public ArrayPathElement(String number)
	{
		this.number = new NumericConst(number);
	}
	
	public ArrayPathElement(int number)
	{
		this.number = new NumericConst(number);
	}
	
	public ArrayPathElement(IStatement number)
	{
		this.number = number;
	}
	
	
	public String getPathString(PostContext context, VariableRepository valRepo)
			throws ExecutionException, RedirectRequestException
	{
		StringBuffer buffer = new StringBuffer();
		
		if(context == null){
			throw new ExecutionException("Context is null");
		}
		
		ScriptDomVariable domNumber = (ScriptDomVariable) this.number.executeStatement(context, valRepo);
		
		int iNumber = Integer.parseInt(domNumber.getValue());
		
		
		buffer.append("[" + iNumber + "]");
		
		if(this.child != null && this.child instanceof PathElement){
			buffer.append(".");
			buffer.append(this.child.getPathString(context, valRepo));
		}
		else if(this.child != null && this.child instanceof ArrayPathElement){
			buffer.append(this.child.getPathString(context, valRepo));
		}
		
		return buffer.toString();
	}

	public IStatement getNumber()
	{
		return number;
	}

	public void setNumber(IStatement number)
	{
		this.number = number;
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
		if(!(container instanceof ScriptArray)){
			throw new ExecutionException("Failed to input variable");
		}
		
		ScriptArray scriptArray = (ScriptArray)container;
		
		ScriptDomVariable numVal = (ScriptDomVariable)this.number.executeStatement(context, valRepo);
				
		
		IScriptVariable cur = scriptArray.get(Integer.parseInt(numVal.getValue()));
		
		// if instance type is different. create another one;
		if(!(cur instanceof ScriptDomVariable) && !isArrayContainer){
			cur = null;
		}
		if(!(cur instanceof ScriptArray) && isArrayContainer){
			cur = null;
		}
		
		if(cur == null){
			// createt new instance
			if(!isArrayContainer){
				cur = new ScriptDomVariable("anonymous");
			}
			else if(isArrayContainer){
				cur = new ScriptArray("ScriptArray");
			}
			
			scriptArray.putAt(cur, Integer.parseInt(numVal.getValue()), context);
		}
		
		return cur;
	}

	public boolean isLeaf()
	{
		return this.child == null;
	}

	public int getType()
	{
		return IPathElement.TYPE_ARRAY;
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
			cur = cur.getChild();
		}
		
		cur.setChild(null);
		
		return copy;
	}


	protected IPathElement deepCopy()
	{
		ArrayPathElement retElement = new ArrayPathElement(this.number);
		
		IPathElement curSrc = this;
		IPathElement curDst = retElement;
		
		while(curSrc.getChild() != null){
			curSrc = curSrc.getChild(); // it.next()
			
			IPathElement newElement = null;
			try {
				newElement = (IPathElement) curSrc.clone();
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
		ArrayPathElement newElement = new ArrayPathElement(this.number);
		newElement.isArrayContainer = this.isArrayContainer;
		
		return newElement;
	}
}



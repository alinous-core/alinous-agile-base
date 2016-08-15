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

public interface IPathElement
{
	public static final int TYPE_DOM = 1;
	public static final int TYPE_ARRAY = 2;
	
	public IPathElement getChild();
	public void setChild(IPathElement child);
	public IPathElement getParent();
	public void setParent(IPathElement parent);
	
	public String getPathString(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException;
	
	public IScriptVariable input(IScriptVariable container, PostContext context, VariableRepository valRepo)
	throws ExecutionException, RedirectRequestException;
	
	public boolean isLeaf();
	
	public int getType();
	
	public IPathElement getLast();
	public IPathElement removeLast();
	
	
	public Object clone() throws CloneNotSupportedException;

}

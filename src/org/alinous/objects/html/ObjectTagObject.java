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
package org.alinous.objects.html;

import java.io.IOException;
import java.io.Writer;

import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.objects.IAlinousObject;
import org.alinous.objects.XMLTagBase;
import org.alinous.script.runtime.VariableRepository;


public class ObjectTagObject extends XMLTagBase implements IHtmlObject
{
	public IAlinousObject fork() throws AlinousException
	{
		ObjectTagObject newObj = new ObjectTagObject();
		
		copyAttribute(this, newObj);
		
		forkInnerObjects(newObj);
		
		return newObj;
	}

	public void renderContents(PostContext context, VariableRepository valRepo, Writer wr, int n) throws IOException, AlinousException
	{
		if(!handleIf(context, valRepo)){
			return;
		}
		
		if(handleIterateAttribute(context, valRepo, wr, n)){
			return;
		}
		
		wr.append("<object");
		renderAttributes(context, valRepo, wr, 0);
		wr.append(">");
		
		renderInnerContents(context, valRepo, wr, n + 1);
		
		wr.append("</object>");
	}

	public String getTagName()
	{
		return "OBJECT";
	}

}


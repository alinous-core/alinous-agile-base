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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ArgumentsDeclare
{
	private List<ArgumentDeclare> arguments = new CopyOnWriteArrayList<ArgumentDeclare>();
	
	public void addArgument(ArgumentDeclare argument)
	{
		this.arguments.add(argument);
	}
	
	public int getSize()
	{
		return this.arguments.size();
	}
	
	public ArgumentDeclare get(int index)
	{
		return this.arguments.get(index);
	}
	
}

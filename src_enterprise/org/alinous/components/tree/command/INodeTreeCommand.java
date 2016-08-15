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

package org.alinous.components.tree.command;

import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.runtime.VariableRepository;

public interface INodeTreeCommand
{
	public final static String CMD_NEW_FIRST_NODE = "newFirstNode";
	public final static String CMD_NEW_NODE = "newNode";
	public final static String CMD_UPDATE_TITLE = "updateTitle";
	public final static String CMD_LEVEL_DOWN = "levelDown";
	public final static String CMD_LEVEL_UP = "levelUp";
	public final static String CMD_POS_DOWN = "posDown";
	public final static String CMD_POS_UP = "posUp";
	public final static String CMD_DELETE = "delete";
	
	public boolean validate();
	public void execute(VariableRepository valrepo) throws DataSourceException, ExecutionException, RedirectRequestException;
}

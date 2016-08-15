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

import org.alinous.components.tree.model.NodeTreeRenderModel;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.runtime.VariableRepository;

public class NodeTreeCommandHandler
{
	private NodeTreeRenderModel renderer;
	private PostContext context;
	
	public NodeTreeCommandHandler(NodeTreeRenderModel renderer, PostContext context)
	{
		this.renderer = renderer;
		this.context = context;
	}
	
	public INodeTreeCommand getCommand()
	{
		String cmd = this.renderer.getCommand();
		
		if(cmd == null){
			return null;
		}
		
		if(cmd.equals(INodeTreeCommand.CMD_NEW_FIRST_NODE)){
			return new CmdNewFirstNode(this.renderer, this.context);
		}
		if(cmd.equals(INodeTreeCommand.CMD_NEW_NODE)){
			return new CmdNewNode(this.renderer, this.context);
		}
		if(cmd.equals(INodeTreeCommand.CMD_UPDATE_TITLE)){
			return new CmdUpdateTitle(this.renderer, this.context);
		}
		if(cmd.equals(INodeTreeCommand.CMD_LEVEL_DOWN)){
			return new CmdLevelDown(this.renderer, this.context);
		}
		if(cmd.equals(INodeTreeCommand.CMD_LEVEL_UP)){
			return new CmdLevelUp(this.renderer, this.context);
		}
		if(cmd.equals(INodeTreeCommand.CMD_POS_UP)){
			return new CmdPosUp(this.renderer, this.context);
		}
		if(cmd.equals(INodeTreeCommand.CMD_POS_DOWN)){
			return new CmdPosDown(this.renderer, this.context);
		}
		if(cmd.equals(INodeTreeCommand.CMD_DELETE)){
			return new CmdDelete(this.renderer, this.context);
		}
		
		return null;
	}
	
	
	public void executeCommand(VariableRepository valrepo) throws DataSourceException, ExecutionException, RedirectRequestException
	{
		INodeTreeCommand cmd = getCommand();
		
		if(cmd == null){
			return;
		}
		
		if(!cmd.validate()){
			return;
		}
		
		cmd.execute(valrepo);
	}
	
}

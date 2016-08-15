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
package org.alinous.components.tree.request;

import java.io.IOException;
import java.io.Writer;

import org.alinous.components.tree.command.INodeTreeCommand;
import org.alinous.components.tree.model.NodeModel;
import org.alinous.components.tree.model.NodeTreeRenderModel;
import org.alinous.exec.pages.PostContext;
import org.alinous.script.runtime.VariableRepository;

public class UpdateRequest implements IRequest
{
	private String nodeId;
	private NodeTreeRenderModel renderer;
	
	public UpdateRequest(NodeTreeRenderModel renderer)
	{
		this.renderer = renderer;
	}
	
	public String getNodeId()
	{
		return nodeId;
	}

	public void setNodeId(String nodeId)
	{
		this.nodeId = nodeId;
	}
	
	public boolean isRequestedUpdate(NodeModel model)
	{
		return model.getId() == Integer.parseInt(this.nodeId);
	}
	
	public void renderTitle(Writer wr, VariableRepository valRepo,
			PostContext context, NodeModel model) throws IOException
	{
		wr.write("<form name=\"updFrm\">");
		
		wr.write("<input type=\"text\" name=\"" + NodeTreeRenderModel.TREE_NODE_NAME + "\" value=\"" + model.getTitle() + "\">");
		wr.write("<input type=\"submit\" name=\"sub\" value=\"Update\">");
		wr.write("<input type=\"hidden\" name=\"" + NodeTreeRenderModel.TREE_TREE_NAME + "\" value=\"" + this.renderer.getConfig().getId() + "\">");
		wr.write("<input type=\"hidden\" name=\"" + NodeTreeRenderModel.TREE_PARENT_NODE + "\" value=\"0\">");
		wr.write("<input type=\"hidden\" name=\"" + NodeTreeRenderModel.COMMAND + "\" value=\"" + INodeTreeCommand.CMD_UPDATE_TITLE + "\">");
		wr.write("<input type=\"hidden\" name=\"" + NodeTreeRenderModel.TREE_NODE_ID + "\" value=\"" + model.getId() + "\">");
		
		wr.write("</form>");
	}
}

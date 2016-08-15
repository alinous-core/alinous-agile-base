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
package org.alinous.components.tree;

import java.io.IOException;
import java.io.Writer;

import org.alinous.components.tree.model.NodeModel;
import org.alinous.components.tree.model.NodeTreeRenderModel;
import org.alinous.components.tree.request.IRequest;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.runtime.VariableRepository;

public class PlusMinusRenderer
{
	private NodeTreeRenderModel renderer;
	
	public PlusMinusRenderer(NodeTreeRenderModel renderer)
	{
		this.renderer = renderer;
	}
	
	public void renderPlusMinus(Writer wr, VariableRepository valRepo, PostContext context, NodeModel model) throws IOException, ExecutionException, RedirectRequestException
	{
		NodeTreeSessionManager mgr = this.renderer.getSessionManager();
		
		if(model.hasChildren() && mgr.isCurrent(this.renderer.getConfig(), Integer.toString(model.getId()))){
			renderClosable(wr, valRepo, context, model);
			return;
		}
		else if(model.hasChildren() && !mgr.isOpened(this.renderer.getConfig(), model.getId())){
			renderOpenable(wr, valRepo, context, model);
			return;
		}
		else if(model.hasChildren() && mgr.isOpened(this.renderer.getConfig(), model.getId())){
			renderClosable(wr, valRepo, context, model);
			return;
		}
		
		/*
		wr.write("<img src=\"");
		

		if(model.hasChildren() && !mgr.isOpened(this.renderer.getConfig(), model.getId())){
			wr.write(context.getFilePath(this.renderer.getConfig().getClosedImg()));
		}else{
			wr.write(context.getFilePath(this.renderer.getConfig().getOpenedImg()));
		}
		
		wr.write("\" border=\"0\">");*/
	}
	
	private void renderClosable(Writer wr, VariableRepository valRepo, PostContext context, NodeModel model)
		throws IOException
	{
		wr.write("<a href=\"");
		wr.write("?");
		
		wr.write(NodeTreeRenderModel.REQUEST);
		wr.write("=");
		wr.write(IRequest.REQ_CLOSE_NODE);
		
		
		wr.write("&");
		wr.write(NodeTreeRenderModel.TREE_NODE_ID);
		wr.write("=");
		wr.write(Integer.toString(model.getId()));
		
		wr.write("&");
		wr.write(NodeTreeRenderModel.TREE_TREE_NAME);
		wr.write("=");
		wr.write(renderer.getConfig().getId());
		
		wr.write("\"");		
		wr.write(">");
		
		wr.write("<img src=\"");
		wr.write(context.getFilePath(this.renderer.getConfig().getOpenedImg()));
		wr.write("\" border=\"0\">");
		
		wr.write("</a>");
	}	
	
	private void renderOpenable(Writer wr, VariableRepository valRepo, PostContext context, NodeModel model)
				throws IOException
	{
		wr.write("<a href=\"");
		wr.write("?");
		
		wr.write(NodeTreeRenderModel.REQUEST);
		wr.write("=");
		wr.write(IRequest.REQ_OPEN_NODE);
		
		
		wr.write("&");
		wr.write(NodeTreeRenderModel.TREE_NODE_ID);
		wr.write("=");
		wr.write(Integer.toString(model.getId()));
		
		wr.write("&");
		wr.write(NodeTreeRenderModel.TREE_TREE_NAME);
		wr.write("=");
		wr.write(renderer.getConfig().getId());
		
		wr.write("\"");		
		wr.write(">");
		
		wr.write("<img src=\"");
		wr.write(context.getFilePath(this.renderer.getConfig().getClosedImg()));
		wr.write("\" border=\"0\">");
		
		wr.write("</a>");
	}
	
}

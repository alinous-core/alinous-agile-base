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

import java.util.Iterator;
import java.util.List;

import org.alinous.AlinousUtils;
import org.alinous.components.tree.model.NodeModel;
import org.alinous.components.tree.model.NodeModelUtils;
import org.alinous.components.tree.model.NodeTreeRenderModel;
import org.alinous.components.tree.request.IRequest;
import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.runtime.VariableRepository;

public class CmdNewNode implements INodeTreeCommand
{
	private NodeTreeRenderModel renderer;
	private PostContext context;
	
	public CmdNewNode(NodeTreeRenderModel renderer, PostContext context)
	{
		this.renderer = renderer;
		this.context = context;
	}
	
	public void execute(VariableRepository valrepo) throws DataSourceException, ExecutionException, RedirectRequestException
	{
		// writing command
		if(!this.renderer.isEditable()){
			return;
		}
		
		DataSrcConnection con = null;

		// do not have to close connection
		NodeModel model = null;
		con = this.renderer.getDataSrcConnection(this.context);
		try{
			model = new NodeModel(this.renderer.getConfig());
			model.initNewModel(con, this.renderer.getParentNode(), this.renderer.getNodeName(), this.renderer.getLastPos());

			// Shift bros
			List<NodeModel> brosList = NodeModelUtils.getBrothers(con, this.renderer.getConfig(), model);
			Iterator<NodeModel> it = brosList.iterator();
			while(it.hasNext()){
				NodeModel md = it.next();
				
				int curPos = md.getPosInLevel();
				if(curPos >= model.getPosInLevel()){
					md.setPosInLevel(curPos + 1);
					
					// update
					//AlinousDebug.debugOut("************ update model : " + md.toString());
					NodeModelUtils.updateModel(con, this.renderer.getConfig(), md);
				}
				
			}
			
			// insert
			NodeModelUtils.insertModel(con, this.renderer.getConfig(), model);
			
			// inc parent count
			NodeModel parentModel = NodeModelUtils.getParentModel(con, this.renderer.getConfig(), model);
			if(parentModel != null){
				parentModel.addNumChildren(1);
				NodeModelUtils.updateModel(con, renderer.getConfig(), parentModel);
			}
		}finally{
			con.close();
		}
		
		// set request
		String path = context.getRequestPath() + ".html";
		path = AlinousUtils.getNotOSPath(path);
		path = context.getFilePath(path);
		path = path + "?" + NodeTreeRenderModel.REQUEST + "=" + IRequest.REQ_UPDATE_TITLE;
		path = path + "&" + NodeTreeRenderModel.TREE_TREE_NAME + "=" + this.renderer.getConfig().getId();
		path = path + "&" + NodeTreeRenderModel.REQ_TARGET_NODE_ID + "=" + model.getId();
 		
		RedirectRequestException e = new RedirectRequestException(path, "302");
		throw e;
	}

	public boolean validate()
	{
		if(this.renderer.getNodeName() == null){
			this.renderer.setErrorMsg("Please input Node name"); // i18n
			return false;
		}
		
		if(this.renderer.getParentNode() == null){
			this.renderer.setErrorMsg("Please input ParentNode"); // i18n
			return false;
		}
		
		return true;
	}

}

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

import org.alinous.components.tree.model.NodeModel;
import org.alinous.components.tree.model.NodeTreeRenderModel;
import org.alinous.expections.AlinousException;

public class RequestManager
{
	private IRequest request;
	
	
	public void handleRequest(String request, NodeTreeRenderModel renderer) throws AlinousException
	{
		// if target is not this tree, do nothing
		if(renderer.getCmdTarget() == null){
			return;
		}
		String treeId = renderer.getConfig().getId().toUpperCase();
		if(!renderer.getCmdTarget().toUpperCase().equals(treeId)){
			return;
		}
		
		if(request.equals(IRequest.REQ_UPDATE_TITLE)){
			createUpdateRequest(request, renderer);
		}
		else if(request.equals(IRequest.REQ_OPEN_NODE)){
			handleOpenRequest(request, renderer);
		}
		else if(request.equals(IRequest.REQ_CLOSE_NODE)){
			handleCloseRequest(request, renderer);
		}
		else if(request.equals(IRequest.REQ_CURRENT)){
			handleCurrentRequest(request, renderer);
		}
	}
	
	private void handleCurrentRequest(String request, NodeTreeRenderModel renderer) throws AlinousException
	{
		CurrentRequest req = new CurrentRequest(renderer);
		req.processRequest();
	}
	
	private void handleCloseRequest(String request, NodeTreeRenderModel renderer) throws AlinousException
	{
		CloseNodeRequest req = new CloseNodeRequest(renderer);
		req.processRequest();
	}
	
	private void handleOpenRequest(String request, NodeTreeRenderModel renderer) throws AlinousException
	{
		OpenNodeRequest req = new OpenNodeRequest(renderer);
		req.processRequest();
	}
	
	private void createUpdateRequest(String request, NodeTreeRenderModel renderer)
	{
		UpdateRequest req = new UpdateRequest(renderer);
		req.setNodeId(renderer.getTargetNodeId());
				
		this.request = req;
	}
	
	public boolean updateRequested(NodeModel model)
	{
		if(this.request == null){
			return false;
		}
		
		if(this.request instanceof UpdateRequest){
			UpdateRequest updReq = (UpdateRequest)this.request;
			
			return updReq.isRequestedUpdate(model);
		}
		
		return false;
	}

	public IRequest getRequest()
	{
		return request;
	}
	
	
}

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

import org.alinous.components.tree.NodeConfig;
import org.alinous.components.tree.NodeTreeSessionManager;
import org.alinous.components.tree.model.NodeModel;
import org.alinous.components.tree.model.NodeModelUtils;
import org.alinous.components.tree.model.NodeTreeRenderModel;
import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;

/**
 * Releavant param
 *  renderer.getNodeId()
 * @author iizuka
 *
 */
public class CurrentRequest implements IRequest
{
	private NodeTreeRenderModel renderer;
	
	public CurrentRequest(NodeTreeRenderModel renderer)
	{
		this.renderer = renderer;
	}
	
	public void processRequest() throws AlinousException
	{
		NodeTreeSessionManager sessionMgr = this.renderer.getSessionManager();
		
		// open parents
		NodeConfig config = this.renderer.getConfig();
		String nodeId = this.renderer.getNodeId();
		
		if(nodeId == null){
			return;
		}
		
		DataSrcConnection con = null;
		try {
			con = sessionMgr.getContext().getUnit().getConnectionManager()
										.connect(config.getDatastore(), null);
			
			NodeModel model = NodeModelUtils.getModelSingle(con, config, nodeId);
			openParents(con, sessionMgr, config, model);
		} catch (DataSourceException e) {
			throw new AlinousException(e, "Failed in connection on processing request");// i18n
		}finally{
			con.close();
		}

		// self
		sessionMgr.setCurrent(config, nodeId);
		
		sessionMgr.syncSession();
	}
	
	private void openParents(DataSrcConnection con, NodeTreeSessionManager sessionMgr,
			NodeConfig config, NodeModel model)
			throws ExecutionException, DataSourceException, RedirectRequestException
	{
		while(model.hasParent()){
			model = NodeModelUtils.getParentModel(con, config, model);
			
			sessionMgr.open(config, Integer.toString(model.getId()));
		}
	}
}

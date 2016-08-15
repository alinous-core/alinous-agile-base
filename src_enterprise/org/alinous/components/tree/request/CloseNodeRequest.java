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

import org.alinous.components.tree.NodeTreeSessionManager;
import org.alinous.components.tree.model.NodeTreeRenderModel;
import org.alinous.expections.AlinousException;

public class CloseNodeRequest implements IRequest
{
	private NodeTreeRenderModel renderer;
	
	public CloseNodeRequest(NodeTreeRenderModel renderer)
	{
		this.renderer = renderer;
	}
	
	public void processRequest() throws AlinousException
	{
		NodeTreeSessionManager sessionMgr = this.renderer.getSessionManager();
		
		sessionMgr.close(this.renderer.getConfig(), this.renderer.getNodeId());
		sessionMgr.syncSession();
	}
}

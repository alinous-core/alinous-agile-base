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

import org.alinous.AlinousUtils;
import org.alinous.components.tree.model.NodeModel;
import org.alinous.components.tree.model.NodeModelUtils;
import org.alinous.components.tree.model.NodeTreeRenderModel;
import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.runtime.VariableRepository;

public class CmdUpdateTitle implements INodeTreeCommand
{
	private NodeTreeRenderModel renderer;
	private PostContext context;
	
	public CmdUpdateTitle(NodeTreeRenderModel renderer, PostContext context)
	{
		this.renderer = renderer;
		this.context = context;
	}
	
	public void execute(VariableRepository valrepo) throws DataSourceException, ExecutionException, RedirectRequestException
	{
		// do not have to close connection
		DataSrcConnection con = this.renderer.getDataSrcConnection(this.context);
		
		try{
			NodeModel model = NodeModelUtils.getModelSingle(con, this.renderer.getConfig(), this.renderer.getNodeId());
			
			model.setTitle(this.renderer.getNodeName());
			
			NodeModelUtils.updateModel(con, this.renderer.getConfig(), model);	
			
		}finally{
			con.close();
		}
		
		String path = context.getRequestPath() + ".html";
		path = AlinousUtils.getNotOSPath(path);
		path = context.getFilePath(path);
		
		RedirectRequestException e = new RedirectRequestException(path, "302");
		throw e;
	}

	public boolean validate()
	{
		if(this.renderer.getNodeId() == null){
			this.renderer.setErrorMsg("Please input Node Id"); // i18n
			return false;
		}
		
		if(this.renderer.getNodeName() == null || this.renderer.getNodeName().equals("")){
			this.renderer.setErrorMsg("Please input Node Title"); // i18n
			return false;
		}

		return true;
	}

}

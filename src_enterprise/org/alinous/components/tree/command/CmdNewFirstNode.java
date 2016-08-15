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

public class CmdNewFirstNode implements INodeTreeCommand
{
	private NodeTreeRenderModel renderer;
	private PostContext context;
	
	public CmdNewFirstNode(NodeTreeRenderModel renderer, PostContext context)
	{
		this.renderer = renderer;
		this.context = context;
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
	
	public void execute(VariableRepository valrepo) throws DataSourceException, ExecutionException, RedirectRequestException
	{
		// writing command
		if(!this.renderer.isEditable()){
			return;
		}
		
		DataSrcConnection con = null;
		try{
			// do not have to close connection
			con = this.renderer.getDataSrcConnection(this.context);
			doExecute(con);
		}finally{
			con.close();
		}
		
		// set request
		String path = context.getRequestPath() + ".html";
		path = AlinousUtils.getNotOSPath(path);
		path = context.getFilePath(path);
 		
		RedirectRequestException e = new RedirectRequestException(path, "302");
		throw e;
		
	}
	
	private void doExecute(DataSrcConnection con) throws ExecutionException, DataSourceException
	{
		NodeModel model = new NodeModel(this.renderer.getConfig());
		
		model.initNewModel(con, this.renderer.getParentNode(), this.renderer.getNodeName(), this.renderer.getLastPos());
		
		NodeModelUtils.insertModel(con, this.renderer.getConfig(), model);
		
	}
	
	
}

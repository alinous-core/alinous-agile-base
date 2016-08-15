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
import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.runtime.VariableRepository;

public class CmdPosDown implements INodeTreeCommand
{
	private NodeTreeRenderModel renderer;
	private PostContext context;
	
	public CmdPosDown(NodeTreeRenderModel renderer, PostContext context)
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
		
		// do not have to close connection
		DataSrcConnection con = this.renderer.getDataSrcConnection(this.context);
		try{
			NodeModel model = NodeModelUtils.getModelSingle(con, this.renderer.getConfig(), this.renderer.getNodeId());
			
			doPosDown(con, model);
		}finally{
			con.close();
		}
		
		// send redirection
		String path = context.getRequestPath() + ".html";
		path = AlinousUtils.getNotOSPath(path);
		path = context.getFilePath(path);

		RedirectRequestException e = new RedirectRequestException(path, "302");
		throw e;
	}
	
	private void doPosDown(DataSrcConnection con, NodeModel model) throws ExecutionException, DataSourceException
	{
		NodeModel bros = getYoungerBrother(con, model);
		
		int lastPos = model.getPosInLevel();
		
		model.setPosInLevel(bros.getPosInLevel());
		bros.setPosInLevel(lastPos);
		
		NodeModelUtils.updateModel(con, this.renderer.getConfig(), model);
		NodeModelUtils.updateModel(con, this.renderer.getConfig(), bros);
	}
	
	private NodeModel getYoungerBrother(DataSrcConnection con, NodeModel model) throws ExecutionException, DataSourceException
	{
		List<NodeModel> list = NodeModelUtils.getBrothers(con, this.renderer.getConfig(), model);
		Iterator<NodeModel> it = list.iterator();
		while(it.hasNext()){
			NodeModel md = it.next();
			int posLv = md.getPosInLevel();
			
			if(posLv == model.getPosInLevel() + 1){
				return md;
			}
		}
		
		return null;
	}
	
	public boolean validate()
	{
		return true;
	}
}

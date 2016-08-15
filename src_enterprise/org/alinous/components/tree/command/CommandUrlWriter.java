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


import org.alinous.AlinousCore;
import org.alinous.components.tree.NodeConfig;
import org.alinous.components.tree.model.MenuParamDataWriter;
import org.alinous.components.tree.model.NodeModel;
import org.alinous.components.tree.model.NodeModelUtils;
import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.objects.AlinousAttrs;
import org.alinous.objects.html.FormTagObject;

public class CommandUrlWriter
{
	private String contextPath;
	private String servletPath;
	private AlinousCore core;
	
	public CommandUrlWriter(String contextPath, String servletPath, AlinousCore core)
	{
		this.contextPath = contextPath;
		this.servletPath = servletPath;
		this.core = core;
	}
	
	public String getUpdateAttribute(String treeId, String nodeId, String title, String viewType, String destPage, String viewId,
			String modulePath) throws ExecutionException, DataSourceException
	{
		StringBuffer buff = new StringBuffer();		
		
		NodeModel model = getNodeModel(treeId, nodeId, null);		
		
		if(viewType != null && viewType.toLowerCase().equals(AlinousAttrs.VALUE_VIEWTYPE_INNER)){
			buff.append("href=\"?");
			
			addPortretCommand(buff, destPage, modulePath);
			addAfterPortletCommand(buff, treeId, nodeId, model.getDocRef(), title);
			
			buff.append("\"");
		}else{
			// implements frame
			String editPageUrl = contextPath + servletPath + destPage;
			buff.append("href=\"");
			
			buff.append(editPageUrl);
			buff.append("?");
			
			buff.append("\"");
			
			buff.append(" target=\"");
			buff.append(viewId);
			buff.append("\"");
		}		
		
		return buff.toString();
	}
	
	private NodeModel getNodeModel(String treeId, String nodeId, PostContext context) throws DataSourceException, ExecutionException
	{
		NodeConfig nodeConfig = this.core.getConfig().getNodeTreeConfig().getNode(treeId);
		
		NodeModel model = null;
		DataSrcConnection con = null;
		try {
			con = this.core.getDataSourceManager().connect(nodeConfig.getDatastore(), context);
			
			model = NodeModelUtils.getModelSingle(con, nodeConfig, nodeId);
			
		}finally{
			if(con != null){
				con.close();
			}
		}
		
		return model;
	}
	
	
	private void addAfterPortletCommand(StringBuffer buff, String treeId, String nodeId, String docRef, String title)
	{
		buff.append("&");
		
		buff.append(MenuParamDataWriter.TREE_ID);
		buff.append("=");
		buff.append(treeId);
		
		buff.append("&");
		
		buff.append(NodeModel.NODE_ID);
		buff.append("=");
		buff.append(nodeId);
		
		buff.append("&");
		
		buff.append(NodeModel.TITLE);
		buff.append("=");
		buff.append(title);
		
		if(docRef != null){
			buff.append("&");
			
			buff.append(NodeModel.DOC_REF);
			buff.append("=");
			buff.append(docRef);
		}
	}
	
	private void addPortretCommand(StringBuffer buff, String destPage, String modulePath)
	{
		buff.append(FormTagObject.HIDDEN_FORM_ACTION);
		buff.append("=");
		buff.append(destPage);
		
		buff.append("&");
		
		
		buff.append(FormTagObject.HIDDEN_FORM_TARGET_TAGID);
		buff.append("=");
		buff.append(modulePath);
	}
	
}

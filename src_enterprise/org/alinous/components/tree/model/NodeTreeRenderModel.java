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
package org.alinous.components.tree.model;


import java.io.IOException;
import java.io.Writer;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import org.alinous.AlinousCore;
import org.alinous.components.tree.NodeConfig;
import org.alinous.components.tree.NodeDbAdaptor;
import org.alinous.components.tree.NodeTreeConfig;
import org.alinous.components.tree.NodeTreeSessionManager;
import org.alinous.components.tree.PlusMinusRenderer;
import org.alinous.components.tree.command.INodeTreeCommand;
import org.alinous.components.tree.request.RequestManager;
import org.alinous.components.tree.request.UpdateRequest;
import org.alinous.components.tree.seo.NodeTreeSeoManager;
import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.exec.pages.IParamValue;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.objects.AlinousAttrs;
import org.alinous.objects.IAttribute;

import org.alinous.script.runtime.VariableRepository;

public class NodeTreeRenderModel
{
	public static final String TREE_TREE_NAME = "alnstreename";
	public static final String TREE_NODE_NAME = "nodeName";
	public static final String TREE_NODE_ID = "nodeId";
	public static final String TREE_PARENT_NODE = "parentNode";
	public static final String COMMAND = "command";
	public static final String LAST_POS = "lastPos";
	public static final String REQUEST = "request";
	public static final String REQ_TARGET_NODE_ID = "targetNodeId";
	
	private NodeConfig config;
	private String treeId;
	private String width;
	private boolean editable;
	private String cmdTarget;
	private String viewId = "";
	private String viewType = AlinousAttrs.VALUE_VIEWTYPE_INNER;
	private String rootNode;
	
	// validation Message
	private String errorMsg;
	
	// http params
	private String parentNode;
	private String nodeName;
	private String nodeId;
	private String command;
	private String lastPos;
	private String targetNodeId;
	
	// seo params
	private String writeStatic;
	private String writeStaticPath;
	
	// request manager
	private RequestManager requestManager = new RequestManager();
	private NodeTreeSessionManager sessionManager;
	
	// cached data
	private CachedContext cachedContext = new CachedContext();
	
	public boolean init(Writer wr, AlinousCore alinousCore, Hashtable<String, IAttribute> alinousAttrs
			, PostContext context, VariableRepository valRepo) throws IOException, AlinousException
	{
		NodeTreeConfig conf = alinousCore.getConfig().getNodeTreeConfig();
		
		IAttribute attr = alinousAttrs.get(AlinousAttrs.ALINOUS_COMPONENT_ID);
		if(attr == null){
			wr.write("Please set " + AlinousAttrs.ALINOUS_COMPONENT_ID + " attribute");
			return false;
		}
		this.treeId = attr.getValue().getParsedValue(context, valRepo);
		
		attr = alinousAttrs.get(AlinousAttrs.ALINOUS_COMPONENT_WIDTH);
		if(attr != null){
			this.width = attr.getValue().getParsedValue(context, valRepo);
		}
		
		attr = alinousAttrs.get(AlinousAttrs.ALINOUS_COMPONENT_EDIT);
		if(attr != null){
			String strEdit = attr.getValue().getParsedValue(context, valRepo);
			if(strEdit.toLowerCase().equals("true")){
				this.editable = true;
			}
		}
		
		attr = alinousAttrs.get(AlinousAttrs.ALINOUS_COMPONENT_VIEW);
		if(attr != null){
			this.viewId = attr.getValue().getParsedValue(context, valRepo);
		}
		
		attr = alinousAttrs.get(AlinousAttrs.ALINOUS_COMPONENT_VIEWTYPE);
		if(attr != null){
			this.viewType = attr.getValue().getParsedValue(context, valRepo);
		}
		
		attr = alinousAttrs.get(AlinousAttrs.ALINOUS_COMPONENT_ROOT);
		if(attr != null){
			this.rootNode = attr.getValue().getParsedValue(context, valRepo);
		}
		
		this.config = conf.getNode(this.treeId);
		
		// 
		this.sessionManager = new NodeTreeSessionManager(context, valRepo);
		
		// Get params
		IParamValue va = context.getParams(TREE_TREE_NAME);
		if(va != null && va.toString() != null){
			this.cmdTarget = va.toString();
		}
		
		// parent
		va = context.getParams(TREE_PARENT_NODE);
		if(va != null && va.toString() != null){
			this.parentNode = va.toString();
		}
		
		// nodeName
		va = context.getParams(TREE_NODE_NAME);
		if(va != null && va.toString() != null){
			this.nodeName = va.toString();
		}
		
		// nodeId
		va = context.getParams(TREE_NODE_ID);
		if(va != null && va.toString() != null){
			this.nodeId = va.toString();
		}
		
		// command
		va = context.getParams(COMMAND);
		if(va != null && va.toString() != null){
			this.command = va.toString();
			
		}
		
		// lastId
		va = context.getParams(LAST_POS);
		if(va != null && va.toString() != null){
			this.lastPos = va.toString();
		}
		
		// targetNodeId
		va = context.getParams(REQ_TARGET_NODE_ID);
		if(va != null && va.toString() != null){
			this.targetNodeId = va.toString();
		}
		
		// Seo
		va = context.getParams(NodeTreeSeoManager.WRITE_STATIC);
		if(va != null && va.toString() != null){
			this.writeStatic = va.toString();
		}
		va = context.getParams(NodeTreeSeoManager.WRITE_STATIC_PATH);
		if(va != null && va.toString() != null){
			this.writeStaticPath = va.toString();
		}
		
		// Request Manager
		va = context.getParams(REQUEST);
		if(va != null && va.toString() != null){
			this.requestManager.handleRequest(va.toString(), this);
		}
		
		return true;
	}
	
	public void render(Writer wr, PostContext context, VariableRepository valRepo) throws DataSourceException, IOException, AlinousException
	{
		DataSrcConnection con = null;

		con = context.getUnit().getConnectionManager().connect(this.config.getDatastore(), context);

		// select root
		List<NodeModel> rootModels = getRootModels(con);
		if(rootModels.isEmpty()){
			renderFist(wr, context, valRepo);
		}else{
			renderNormal(wr, context, valRepo, rootModels);
		}
		

	}
	
	private List<NodeModel> getRootModels(DataSrcConnection con) throws ExecutionException, DataSourceException
	{
		if(this.rootNode != null){
			NodeModel curModel = NodeModelUtils.getModelSingle(con, this.config, this.rootNode);
			return NodeModelUtils.getChildren(con, this.config, curModel);
		}
		
		NodeDbAdaptor ad = new NodeDbAdaptor(this.config);
		return ad.getRootNodes(con);
	}
	
	private void renderNormal(Writer wr, PostContext context, VariableRepository valRepo, List<NodeModel> rootModels) throws IOException, DataSourceException, AlinousException
	{
		renderCss(wr, context, valRepo, rootModels);
		
		renderModels(wr, context, valRepo, rootModels, 0);

		renderJavaScript(wr, context, valRepo, rootModels);
	}
	
	private void renderCss(Writer wr, PostContext context, VariableRepository valRepo, List<NodeModel> rootModels) throws IOException
	{
		wr.write("<link type=\"text/css\" href=\"");
		wr.write(context.getFilePath("/alinous-common/treenode/style.css"));
		wr.write("\" rel=\"stylesheet\">");
	}
	
	private void renderJavaScript(Writer wr, PostContext context, VariableRepository valRepo, List<NodeModel> rootModels) throws IOException
	{
		wr.write("<script type=\"text/javascript\" src=\"");
		wr.write(context.getFilePath("/alinous-common/treenode/common.js"));
		wr.write("\" charset=\"utf-8\">");
		wr.write("</script>\n");

	}
	
	
	private void renderSubMenu(Writer wr, String id) throws IOException
	{
		wr.write("<div class=\"alnsMenu\" id=\"");
		wr.write(id);
		wr.write("\"");
		wr.write(" style=\"position:absolute;  display:none;\"");
		wr.write(">");
		
		wr.write("</div>");
	}
	
	private void renderModels(Writer wr, PostContext context, VariableRepository valRepo,
			List<NodeModel> rootModels, int i)
		throws IOException, DataSourceException, AlinousException
	{
		Iterator<NodeModel> it = rootModels.iterator();
		
		while(it.hasNext()){
			NodeModel model = it.next();
			
			if(!isEditable() && !model.isExistsInTree()){
				continue;
			}
			
			wr.write("<table cellspacing=\"0\" cellpadding=\"0\"");
			
			if(this.width != null){
				wr.write(" width=\"" + this.width + "\"");
			}
			wr.write(">");
			
			wr.write("<tr>");
			
			// blank
			if(i > 0){
				int wblank = i * 12;
				
				wr.write("<td width=\"" + wblank);
				
				wr.write("\"></td>");
			}
			
			
			wr.write("<td valign=\"center\" width=\"9\">");
			// plus or minus icon
			renderPlusMinus(wr, valRepo, context, model);
			
			wr.write("</td>");
			wr.write("<td width=\"16\" valign=\"center\">");
			// folder
			renderFolderMark(wr, valRepo, context, model);
			
			wr.write("</td>");
			
			wr.write("<td valign=\"center\" align=\"left\">");			
			// title
			renderTitle(wr, valRepo, context, model);
			
			wr.write("</td>");
			
			wr.write("</tr>");
			
			wr.write("</table>");
			wr.write("");
			
			
			// children
			renderChildren(wr, valRepo, context, model, i + 1);
		}
		

	}
	
	private void renderChildren(Writer wr, VariableRepository valRepo, PostContext context
			, NodeModel model, int i) throws IOException, DataSourceException, AlinousException
	{
		if(model.getNumChildren() < 1){
			return;
		}
		
		if(!this.sessionManager.isOpened(this.config, model.getId()) &&
				!this.sessionManager.isCurrent(this.config, Integer.toString(model.getId()))){
			return;
		}
		
		// do not have to close connection
		DataSrcConnection con = context.getUnit().getConnectionManager().connect(this.config.getDatastore(), context);
		
		try{
			List<NodeModel> modelList = NodeModelUtils.getChildren(con, this.config, model);
			
			renderModels(wr, context, valRepo, modelList, i);
		}finally{
			con.close();
		}


	}
	
	private void renderTitle(Writer wr, VariableRepository valRepo, PostContext context, NodeModel model) throws IOException, DataSourceException, AlinousException
	{
		String spnId = Integer.toString(model.getId());
		String divId = this.config.getId() + spnId;
		
		if(this.requestManager.updateRequested(model)){
			UpdateRequest updReq = (UpdateRequest)this.requestManager.getRequest();
			updReq.renderTitle(wr, valRepo, context, model);
			
			return;
		}
		
		// render the title
		TitleRenderer titleRenderer = new TitleRenderer();
		titleRenderer.renderTitle(wr, valRepo, context, model, this);
		
		if(isEditable()){
			NodeMenuRenderer.renderEditMenu(wr, valRepo, context, model, spnId, divId, this);
			NodeMenuRenderer.renderPositionMenu(wr, valRepo, context, model, "pos" + spnId, divId, this);
		
			renderSubMenu(wr, divId);
		}
	}
	
	private void renderPlusMinus(Writer wr, VariableRepository valRepo, PostContext context, NodeModel model) throws IOException, ExecutionException, RedirectRequestException
	{
		// plus minus
		PlusMinusRenderer pmRenderer = new PlusMinusRenderer(this);
		
		pmRenderer.renderPlusMinus(wr, valRepo, context, model);
	}
	
	private void renderFolderMark(Writer wr, VariableRepository valRepo, PostContext context, NodeModel model) throws IOException
	{
		wr.write("<img src=\"");
		
		String folderImg = this.config.getDocType(model.getDocType()).getFolderImage();
		
		wr.write(context.getFilePath(folderImg));
		
		wr.write("\" border=\"0\">");
	}
	
	private void renderFist(Writer wr, PostContext context, VariableRepository valRepo) throws IOException
	{
		if(isEditable()){
		
			wr.write("At first, you have to add node");
			
			if(this.errorMsg != null){
				wr.write("<font color=\"#FF0000\"><b>");
				wr.write(this.errorMsg);
				wr.write("</b></font>");
			}
			
			wr.write("<form name=\"createNewFrm\">");
			
			wr.write("<input type=\"text\" name=\"" + TREE_NODE_NAME + "\">");
			wr.write("<input type=\"submit\" name=\"sub\" value=\"New\">");
			wr.write("<input type=\"hidden\" name=\"" + TREE_TREE_NAME + "\" value=\"" + this.config.getId() + "\">");
			wr.write("<input type=\"hidden\" name=\"" + TREE_PARENT_NODE + "\" value=\"0\">");
			wr.write("<input type=\"hidden\" name=\"" + COMMAND + "\" value=\"" + INodeTreeCommand.CMD_NEW_FIRST_NODE + "\">");
			wr.write("<input type=\"hidden\" name=\"" + LAST_POS + "\" value=\"-1\">");
			wr.write("</form>");
			
			return;
		}
		
		wr.append("No data.");
	}
	
	public boolean isEditable()
	{
		return this.editable;
	}

	public String getWidth()
	{
		return width;
	}

	public String getCmdTarget()
	{
		return cmdTarget;
	}
	
	public String getParentNode()
	{
		return parentNode;
	}
	
	public String getNodeName()
	{
		return this.nodeName;
	}
	
	public String getNodeId()
	{
		return nodeId;
	}

	public String getErrorMsg()
	{
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg)
	{
		this.errorMsg = errorMsg;
	}

	public String getCommand()
	{
		return command;
	}
	
	public NodeConfig getConfig()
	{
		return config;
	}

	public String getLastPos()
	{
		return lastPos;
	}
	
	public String getTargetNodeId()
	{
		return targetNodeId;
	}

	public String getViewId()
	{
		return viewId;
	}

	public String getViewType()
	{
		return viewType;
	}

	public CachedContext getCachedContext()
	{
		return cachedContext;
	}

	public boolean isCurrentTarget()
	{
		if(this.cmdTarget == null){
			return false;
		}
		
		return this.cmdTarget.equals(this.config.getId());
	}
	
	
	public RequestManager getRequestManager()
	{
		return requestManager;
	}

	public DataSrcConnection getDataSrcConnection(PostContext context) throws DataSourceException
	{
		DataSrcConnection con = null;

		con = context.getUnit().getConnectionManager().connect(this.config.getDatastore(), context);
		
		return con;
	}

	public NodeTreeSessionManager getSessionManager()
	{
		return sessionManager;
	}

	public String getWriteStatic()
	{
		return writeStatic;
	}

	public void setWriteStatic(String writeStatic)
	{
		this.writeStatic = writeStatic;
	}

	public String getWriteStaticPath()
	{
		return writeStaticPath;
	}

	public void setWriteStaticPath(String writeStaticPath)
	{
		this.writeStaticPath = writeStaticPath;
	}
	
}

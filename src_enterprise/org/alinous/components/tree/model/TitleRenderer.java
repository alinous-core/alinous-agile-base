package org.alinous.components.tree.model;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;

import org.alinous.AlinousCore;
import org.alinous.components.tree.DoctypeConfig;
import org.alinous.components.tree.NodeTreeSessionManager;
import org.alinous.components.tree.request.IRequest;
import org.alinous.components.tree.seo.OutPathNamingManager;
import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.exec.InnerModulePath;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.objects.AlinousAttrs;
import org.alinous.objects.html.FormTagObject;
import org.alinous.script.runtime.VariableRepository;

public class TitleRenderer
{
	public TitleRenderer()
	{
		
	}
	
	public void renderTitle(Writer wr, VariableRepository valRepo, PostContext context,
			NodeModel model, NodeTreeRenderModel renderer)
		throws IOException, DataSourceException, AlinousException
	{
		NodeTreeSessionManager mgr = renderer.getSessionManager();
		
		// there is no title
		if(model.getDocRef() == null || model.getDocRef().equals("")){
			wr.write(model.getTitle());
			return;
		}
		
		// if not visible
		if(!renderer.isEditable() && !model.isVisible()){
			wr.write(model.getTitle());
			return;
		}
		
		// if opened
		if(mgr.isCurrent(renderer.getConfig(), Integer.toString(model.getId()))){
			wr.write("<span class=\"");
			wr.write(renderer.getConfig().getOpenedClass());
			wr.write("\">");
		}
		
		wr.write("<a href=\"");
		writeLinkUrl(wr, valRepo, context, model, renderer);
		wr.write("\"");
		
		
		wr.write(">");
		wr.write(model.getTitle());
		wr.write("</a>");
		
		// span end
		if(mgr.isCurrent(renderer.getConfig(), Integer.toString(model.getId()))){
			wr.write("</span>");
		}
	}
	
	private void writeLinkUrl(Writer wr, VariableRepository valRepo, PostContext context,
			NodeModel model, NodeTreeRenderModel renderer) throws IOException, AlinousException, DataSourceException
	{
		// if seo mode
		if(renderer.getWriteStatic() != null && renderer.getWriteStatic().equals("true")){
			renderStaticUrl(wr, valRepo, context, model, renderer);
			
			return;
		}
		
		// if there is a reference
		String viewType = renderer.getViewType();
		if(viewType != null && viewType.toLowerCase().equals(AlinousAttrs.VALUE_VIEWTYPE_INNER)){
			handlePortlet(wr, valRepo, context, model, renderer);
		}else{
			handleFrame(wr, valRepo, context, model, renderer);
		}
		
	}
	
	private void renderStaticUrl(Writer wr, VariableRepository valRepo, PostContext context,
			NodeModel model, NodeTreeRenderModel renderer) throws DataSourceException, ExecutionException, IOException
	{
		DataSrcConnection con = null;
		con = context.getUnit().getConnectionManager().connect(renderer.getConfig().getDatastore(), context);
		
		OutPathNamingManager manager = new OutPathNamingManager(con, renderer.getConfig());
		String path = manager.getName(model, renderer.getWriteStaticPath());
		
		wr.write(path);
	}
	
	private void handlePortlet(Writer wr, VariableRepository valRepo, PostContext context,
			NodeModel model, NodeTreeRenderModel renderer) throws IOException, AlinousException
	{
		String docTypeId = model.getDocType();
		DoctypeConfig docTypeConfig = renderer.getConfig().getDocType(docTypeId);
		
		String showPageUrl = docTypeConfig.getShowPage();
		
		//showPageUrl = context.getFilePath(showPageUrl);
		//wr.write(showPageUrl);

		wr.write(context.getTopTopObject().getPath());
		wr.write("?");
		
		// get modulePath
		InnerModulePath path = null;
		if(context.isInner()){
			path = context.getModulePath();
		}else{
			path = new InnerModulePath(context.getTopTopObject().getPath());
			path.addTarget(renderer.getViewId());
		}
		//this.params.put(VIEW_INNER_MODULE_PATH, path.getStringPath());
		addPortretCommand(wr, showPageUrl, path.getStringPath());
		
		// docRef
		addDocParams(wr, model);
		
		// add Current
		addCurrent(wr, model, renderer);
		
		// edit mode
		addEditMode(wr, model, renderer);
	}
	
	private void addEditMode(Writer wr, NodeModel model, NodeTreeRenderModel renderer) throws IOException
	{
		wr.write("&");
		
		wr.write(MenuParamDataWriter.EDIT_MODE);
		wr.write("=");
		wr.write(Boolean.toString(renderer.isEditable()));
	}
	
	private void addDocParams(Writer wr, NodeModel model) throws IOException
	{
		wr.write("&");
		
		wr.write(NodeModel.DOC_REF);
		wr.write("=");
		wr.write(model.getDocRef());
	}
	
	private void addCurrent(Writer wr, NodeModel model, NodeTreeRenderModel renderer) throws IOException
	{
		wr.write("&");
		
		wr.write(NodeTreeRenderModel.REQUEST);
		wr.write("=");
		wr.write(IRequest.REQ_CURRENT);
		
		wr.write("&");
		
		wr.write(NodeTreeRenderModel.TREE_TREE_NAME);
		wr.write("=");
		wr.write(renderer.getConfig().getId());
		
		wr.write("&");
		
		wr.write(NodeTreeRenderModel.TREE_NODE_ID);
		wr.write("=");
		wr.write(Integer.toString(model.getId()));
	}
	
	private void addPortretCommand(Writer wr, String destPage, String modulePath) throws IOException
	{
		wr.write(FormTagObject.HIDDEN_FORM_ACTION);
		wr.write("=");
		wr.write(destPage);
		
		wr.write("&");
		
		
		wr.write(FormTagObject.HIDDEN_FORM_TARGET_TAGID);
		wr.write("=");
		wr.write(modulePath);
		
	}
	
	private void handleFrame(Writer wr, VariableRepository valRepo, PostContext context,
			NodeModel model, NodeTreeRenderModel renderer)
	{
		
	}
	
	public String urlEncode(String str, AlinousCore core)
	{
		String retStr = null;
		try {
			retStr = URLEncoder.encode(str, core.getConfig().getSystemRepositoryConfig().getEncoding());
		} catch (UnsupportedEncodingException e) {
			core.getLogger().reportError(e);
			
			return str;
		}
		
		return retStr;
	}
}

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
package org.alinous.components.tree.seo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alinous.AlinousCore;
import org.alinous.AlinousUtils;
import org.alinous.cloud.file.AlinousFile;
import org.alinous.components.tree.NodeConfig;
import org.alinous.components.tree.NodeDbAdaptor;
import org.alinous.components.tree.model.NodeModel;
import org.alinous.components.tree.model.NodeModelUtils;
import org.alinous.components.tree.model.NodeTreeRenderModel;
import org.alinous.components.tree.request.IRequest;
import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.exec.pages.IParamValue;
import org.alinous.exec.pages.PostContext;
import org.alinous.exec.pages.StringParamValue;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.script.runtime.VariableRepository;

public class NodeTreeSeoManager
{
	public static final String WRITE_STATIC = "WRITE_STATIC";
	public static final String WRITE_STATIC_PATH = "WRITE_STATIC_PATH";
	private String url;
	private SitemapHtmlManager siteMapMgr;
	private String urlDir;
	
	public void writeStatic(PostContext context, VariableRepository valrepo, String treeName, String templetePath,
			String outPath, String urlDir, String rootNode)
			throws ExecutionException
	{
		this.urlDir = urlDir;
		
		AlinousFile outDir = new AlinousFile(outPath);
		outDir.mkdirs();
		
		
		AlinousCore core = context.getCore();
		String encode = core.getConfig().getSystemRepositoryConfig().getEncoding();
		NodeConfig config = core.getConfig().getNodeTreeConfig().getNode(treeName);
		
		DataSrcConnection con = null;
		try {
			// sitemap manager
			this.siteMapMgr = new SitemapHtmlManager(outPath, urlDir, encode);
			this.siteMapMgr.writeHeader();
			
			// do not have to close this connection
			con = context.getUnit().getConnectionManager().connect(config.getDatastore(), context);
			
			
			// if root is not null
			if(rootNode != null){
				NodeModel md = NodeModelUtils.getModelSingle(con, config, rootNode);
				
				writeSiteMap(context, valrepo, treeName, templetePath, outPath, md, con, config);
				doSeo(context, valrepo, treeName, templetePath, outPath, md, con, config);
			}
			
			// root node
			List<NodeModel> rootModels = getRootModels(con, config, rootNode);
			
			Iterator<NodeModel> it = rootModels.iterator();
			while(it.hasNext()){
				NodeModel model = it.next();
				
				writeSiteMap(context, valrepo, treeName, templetePath, outPath, model, con, config);
				
				doSeo(context, valrepo, treeName, templetePath, outPath, model, con, config);
				doSecChildren(con, config, context, valrepo, treeName, templetePath, outPath, model);				
			}
			

			
			this.siteMapMgr.writeFooter();
			
		} catch (DataSourceException e) {
			throw new ExecutionException(e, "Failed in write static HTML."); // i18n
		} catch (FileNotFoundException e) {
			throw new ExecutionException(e, "Failed in write static HTML."); // i18n
		} catch (UnsupportedEncodingException e) {
			throw new ExecutionException(e, "Failed in write static HTML."); // i18n
		} catch (IOException e) {
			throw new ExecutionException(e, "Failed in write static HTML."); // i18n
		}finally{
			if(this.siteMapMgr != null){
				this.siteMapMgr.dispose();
			}
			
			con.close();
		}
	}
	
	private List<NodeModel> getRootModels(DataSrcConnection con, NodeConfig config, String rootNode)
		throws ExecutionException, DataSourceException
	{
		if(rootNode != null){
			NodeModel curModel = NodeModelUtils.getModelSingle(con, config, rootNode);
			return NodeModelUtils.getChildren(con, config, curModel);
		}
		
		NodeDbAdaptor ad = new NodeDbAdaptor(config);
		return ad.getRootNodes(con);
	}
	
	private void doSecChildren(DataSrcConnection con, NodeConfig config, PostContext context, VariableRepository valrepo, String treeName,
			String templetePath, String outPath, NodeModel model)
			throws ExecutionException, DataSourceException, IOException
	{
		if(!model.hasChildren()){
			return;
		}
		
		List<NodeModel> modelList = NodeModelUtils.getChildren(con, config, model);
		Iterator<NodeModel> it = modelList.iterator();
		while(it.hasNext()){
			NodeModel chModel = it.next();
			
			// sitemap
			writeSiteMap(context, valrepo, treeName, templetePath, outPath, chModel, con, config);
			
			doSeo(context, valrepo, treeName, templetePath, outPath, chModel, con, config);
			doSecChildren(con, config, context, valrepo, treeName, templetePath, outPath, chModel);
			
		}
	}
	
	private void writeSiteMap(PostContext context, VariableRepository valrepo, String treeName, String templetePath,
			String outPath, NodeModel model, DataSrcConnection con, NodeConfig config) throws ExecutionException, IOException, DataSourceException
	{
		this.siteMapMgr.appendModel(model, con, config);
	}
	
	private void doSeo(PostContext context, VariableRepository valrepo, String treeName, String templetePath, String outPath,
			NodeModel model, DataSrcConnection con, NodeConfig config) throws ExecutionException
	{
		if(model.getDocRef() == null || model.getDocRef().equals("")){
			return;
		}
		
		String outFile;
		try {
			outFile = getOutFile(outPath, model, con ,config);
		} catch (DataSourceException e) {
			throw new ExecutionException(e, "Failed in Namingof output file");// i18n
		}
		
		// params
		Map<String, IParamValue> params = new HashMap<String, IParamValue>();
		params.put(NodeTreeRenderModel.REQUEST, new StringParamValue(IRequest.REQ_CURRENT));
		params.put(NodeTreeRenderModel.TREE_TREE_NAME, new StringParamValue(treeName));
		params.put("DOC_REF", new StringParamValue(model.getDocRef()));
		params.put(NodeTreeRenderModel.TREE_NODE_ID, new StringParamValue(Integer.toString(model.getId())));	
		if(model.getTitle() != null && !model.getTitle().equals("")){
			params.put("TITLE", new StringParamValue(model.getTitle()));
		}
		params.put(WRITE_STATIC, new StringParamValue("true"));
		params.put(WRITE_STATIC_PATH, new StringParamValue(urlDir));
		
		// write page
		writeSeoedPage(context, valrepo, treeName, templetePath, outFile, params);
	}
	
	private String getOutFile(String outPath, NodeModel model, DataSrcConnection con, NodeConfig config)
		throws ExecutionException, DataSourceException
	{
		OutPathNamingManager manager = new OutPathNamingManager(con, config);
		
		String path = manager.getName(model, outPath);
		
		return path;
	}
	
	public void writeSeoedPage(PostContext context, VariableRepository valrepo, String treeName, String templetePath,
			String outFile, Map<String, IParamValue> params) throws ExecutionException
	{
		StaticHtmlWriter staticWriter = new StaticHtmlWriter(context.getCore());
		
		String moduleName = AlinousUtils.getModuleName(templetePath);
		String encode = context.getCore().getConfig().getSystemRepositoryConfig().getEncoding();
		
		String sessionId = outFile;
		
		AlinousFile file = new AlinousFile(outFile);
		Writer writer = null;
		try{
			String outFileDir = AlinousUtils.getDirectory(outFile);
			
			AlinousFile outDir = new AlinousFile(outFileDir);
			outDir.mkdirs();
			
			writer = new PrintWriter(file, encode);
			
			staticWriter.writeHtml(moduleName, writer, context, valrepo, params, sessionId);
		} catch (IOException e) {
			throw new ExecutionException(e, "Failed in write static HTML."); // i18n
		} catch (AlinousException e) {
			throw new ExecutionException(e, "Failed in write static HTML."); // i18n
		}
		finally{
			if(writer != null){
				try {
					writer.close();
				} catch (IOException e) {}
			}
		}
	}
	
	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}
	
	
}

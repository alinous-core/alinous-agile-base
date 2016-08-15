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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.alinous.cloud.file.AlinousFile;
import org.alinous.cloud.file.AlinousFileOutputStream;
import org.alinous.components.tree.NodeConfig;
import org.alinous.components.tree.NodeDbAdaptor;
import org.alinous.components.tree.model.NodeModel;
import org.alinous.components.tree.model.NodeModelUtils;
import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;



public class SitemapXmlWriter
{
	private DataSrcConnection con;
	private NodeConfig config;
	private String httpHost;
	private String alinousPath;
	private String changeFrequency;
	private String defaultPriority;
	
	private PrintWriter writer;
	
	public SitemapXmlWriter(DataSrcConnection con, NodeConfig config, String httpHost
			, String alinousPath, String changeFrequency, String defaultPriority)
	{
		this.con = con;
		this.config = config;
		this.httpHost = httpHost;
		this.alinousPath = alinousPath;
		this.changeFrequency = changeFrequency;
		this.defaultPriority = defaultPriority;
	}
	
	/**
	 * 
	 * @param defaultXml
	 * @param outXml
	 * @param originalNodeId if for all nodes, set -1
	 * @throws AlinousException
	 * @throws DataSourceException
	 * @throws IOException
	 */
	public void writeSitemap(String outXml ,int originalNodeId)
		throws AlinousException, DataSourceException, IOException
	{
		// write document
		AlinousFile outFile = new AlinousFile(outXml);
		OutputStream outStream = null;
		try {
			outStream = new AlinousFileOutputStream(outFile);
			this.writer = new PrintWriter(outStream);
			
			// add header
			this.writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			this.writer.println("<urlset xmlns=\"http://www.google.com/schemas/sitemap/0.84\">");
			
			// getFirst Node
			List<NodeModel> modelList = getFirstNodeModel(originalNodeId);
			Iterator<NodeModel> it = modelList.iterator();
			while(it.hasNext()){
				NodeModel model = it.next();
				
				handleSelf(model);
			}
			
			this.writer.println("</urlset>");
		} catch (IOException e) {
			throw e;
		}
		finally{
			if(this.writer != null){
				this.writer.close();
			}
			if(outStream != null){
				outStream.close();
			}
		}
	}
	
	private void handleSelf(NodeModel model) throws ExecutionException, DataSourceException
	{
		// self
		makeUrlElement(model);
		
		// children
		List<NodeModel> modelList = NodeModelUtils.getChildren(this.con, this.config, model);
		Iterator<NodeModel> it = modelList.iterator();
		while(it.hasNext()){
			NodeModel md = it.next();
			
			handleSelf(md);
		}
	}
	
	private void makeUrlElement(NodeModel model)
		throws ExecutionException, DataSourceException
	{
		if(model.getDocRef() == null || model.getDocRef().equals("")){
			return;
		}
		
		this.writer.println("	<url>");
		
		// Loc
		this.writer.print("		<loc>");
		this.writer.print(getLoc(model));
		this.writer.println("</loc>");
		
		// change Frequency
		this.writer.print("		<changefreq>");
		this.writer.print(this.changeFrequency);
		this.writer.println("</changefreq>");
		
		// priority
		if(!this.defaultPriority.equals("-1")){
			this.writer.print("		<priority>");
			this.writer.print(this.defaultPriority);
			this.writer.println("</priority>");
		}
		this.writer.println("	</url>");
	}
	
	
	private String getLoc(NodeModel model) throws ExecutionException, DataSourceException
	{
		StringBuffer buff = new StringBuffer();
		buff.append(this.httpHost);
		
		OutPathNamingManager namingMgr = new OutPathNamingManager(this.con, this.config);
		buff.append(namingMgr.getName(model, this.alinousPath));
		
		return buff.toString();
	}
	
	private List<NodeModel> getFirstNodeModel(int originalNodeId) throws ExecutionException, DataSourceException
	{
		List<NodeModel> list = new ArrayList<NodeModel>();
		if(originalNodeId < 0){
			NodeDbAdaptor ad = new NodeDbAdaptor(this.config);
			return ad.getRootNodes(this.con);
		}
		else{
			NodeModel md = NodeModelUtils.getModelSingle(this.con, this.config, Integer.toString(originalNodeId));
			list.add(md);
		}
		
		return list;
	}
}

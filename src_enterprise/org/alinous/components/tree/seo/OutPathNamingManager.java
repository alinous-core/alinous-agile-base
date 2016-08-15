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

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.alinous.AlinousUtils;
import org.alinous.components.tree.NodeConfig;
import org.alinous.components.tree.model.NodeModel;
import org.alinous.components.tree.model.NodeModelUtils;
import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.expections.ExecutionException;


public class OutPathNamingManager
{
	private DataSrcConnection con;
	private NodeConfig config;
	
	public OutPathNamingManager(DataSrcConnection con, NodeConfig config)
	{
		this.con = con;
		this.config = config;
	}
	
	public String getName(NodeModel model, String outPath) throws ExecutionException, DataSourceException
	{
		outPath = AlinousUtils.getNotOSPath(outPath);
		
		StringBuffer buff = new StringBuffer();
		
		buff.append(outPath);
		if(!outPath.endsWith("/")){
			buff.append("/");
		}
		
		// dirs
		buff.append(getExtDir(model));
		
		
		buff.append(getFileName(model));
		
		return buff.toString();
	}
	
	private String getExtDir(NodeModel model) throws ExecutionException, DataSourceException
	{
		if(model.getLevel() == 0){
			return "";
		}
		
		Stack<String> pathStack = new Stack<String>();
		int level = model.getLevel();
		
		while(level != 0){
			model = NodeModelUtils.getParentModel(this.con, this.config, model);
			
			if(model == null){
				break;
			}
			
			StringBuffer outFile = new StringBuffer();
			outFile.append(getModelName(model));
			pathStack.push(outFile.toString());
			
			level--;
		}
		
		StringBuffer outFile = new StringBuffer();
		while(!pathStack.isEmpty()){
			String path = pathStack.pop();
			
			outFile.append(path);
			outFile.append("/");
		}
		
		return outFile.toString();
	}
	
	private String getFileName(NodeModel model) throws ExecutionException, DataSourceException
	{
		if(model.hasChildren()){
			return indexName(model);
		}
		
		StringBuffer outFile = new StringBuffer();
		outFile.append(getModelName(model));
		outFile.append(".html");
		
		return outFile.toString();
	}
	
	private String indexName(NodeModel model) throws ExecutionException, DataSourceException
	{
		StringBuffer buff = new StringBuffer();
		
		buff.append(getModelName(model));
		buff.append("/index.html");
		
		return buff.toString();
	}
	
	private String getModelName(NodeModel model) throws ExecutionException, DataSourceException
	{
		if(model.getCategory() == null || model.getCategory().equals("")){
			return model.getDocType() + model.getDocRef();
		}
		
		if(brotherHasSameCategory(model)){
			return model.getCategory() + model.getDocRef();
		}
		
		return model.getCategory();
	}
	
	private boolean brotherHasSameCategory(NodeModel model) throws ExecutionException, DataSourceException
	{
		String category = model.getCategory();
		List<NodeModel> list = NodeModelUtils.getBrothers(con, config, model);
		Iterator<NodeModel> it = list.iterator();
		while(it.hasNext()){
			NodeModel brosModel = it.next();
			
			if(brosModel.getCategory() == null ||  model.getCategory().equals("")){
				continue;
			}
			if(brosModel.getId() == model.getId()){
				continue;
			}
			if(brosModel.getCategory().equals(category)){
				return true;
			}
		}
		
		return false;
	}
}


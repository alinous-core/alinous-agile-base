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
package org.alinous.components.tree;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alinous.AlinousCore;
import org.alinous.AlinousUtils;
import org.alinous.components.tree.model.NodeModel;
import org.alinous.components.tree.model.NodeModelUtils;
import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.exec.AccessExecutionUnit;
import org.alinous.exec.pages.IDesign;
import org.alinous.exec.pages.IParamValue;
import org.alinous.exec.pages.PostContext;
import org.alinous.exec.pages.StringParamValue;
import org.alinous.expections.AlinousException;
import org.alinous.script.runtime.VariableRepository;

public class DocumentDeleter
{
	private DataSrcConnection con;
	private NodeConfig config;
	
	public DocumentDeleter(DataSrcConnection con, NodeConfig config)
	{
		this.con = con;
		this.config = config;
	}
	
	public void deleteDocuments(PostContext context, VariableRepository valRepo, NodeModel model) throws DataSourceException, AlinousException, IOException
	{
		handleModel(context, valRepo, model);
	}
	
	private void handleModel(PostContext context, VariableRepository valRepo, NodeModel model) throws DataSourceException, AlinousException, IOException
	{
		String docRef = model.getDocRef();
		
		if(docRef != null && docRef != ""){
			deleteDocument(context, valRepo, model);
		}
		
		if(!model.hasChildren()){
			return;
		}
		
		List<NodeModel> list = NodeModelUtils.getChildren(this.con, this.config, model);
		Iterator<NodeModel> it = list.iterator();
		while(it.hasNext()){
			NodeModel childModel = it.next();
			
			handleModel(context, valRepo, childModel);
		}
		
	}
	
	private void deleteDocument(PostContext context, VariableRepository valRepo, NodeModel model) throws AlinousException, IOException
	{
		accessDeletePage(context, valRepo, model);
	}
	
	private void accessDeletePage(PostContext oldContext, VariableRepository valRepo,NodeModel model) throws AlinousException, IOException
	{
		String modulePath = this.config.getDocType(model.getDocType()).getDeletePage();
		String moduleName = AlinousUtils.getModuleName(modulePath);
		String sessionId = oldContext.getSessionId();
		AlinousCore core = oldContext.getCore();
		
		Map<String, IParamValue> params = new HashMap<String, IParamValue>();
		params.put("DOC_REF", new StringParamValue(model.getDocRef()));
		
		AccessExecutionUnit exec = null;
		PostContext context = null;
		try{
			exec = core.createAccessExecutionUnit(sessionId, oldContext);
			
			context = new PostContext(core, exec);
			context.setContextPath(oldContext.getContextPath());
			context.setServletPath(oldContext.getServletPath());
			
			context.setRequestPath(moduleName);
			core.registerAlinousObject(context, moduleName);
			
			// params
			context.initParams(moduleName, params);
			
			IDesign design = null;
			design = exec.gotoPage(moduleName, context, valRepo);
			
			StringWriter writer = new StringWriter();
			design.renderContents(context, valRepo, writer, 0);
			writer.close();
		}finally{
			// Do not dispose. Because of reuse of the Connection Manager
			/*if(exec != null){
				exec.dispose();
			}*/
			context.dispose();
		}
	}
}

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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.alinous.components.tree.DoctypeConfig;
import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.exec.InnerModulePath;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;

public class MenuParamDataWriter
{
	public static final String MAX_POS = "MAX_POS";
	public static final String TREE_ID = "TREE_ID";
	
	public static final String VIEW_ID = "VIEW_ID";
	public static final String VIEW_INNER_MODULE_PATH = "VIEW_INNER_MODULE_PATH";
	
	public static final String EDIT_MODE = "EDIT_MODE";
	
	public static final String VIEW_TYPE = "VIEW_TYPE";
	public static final String EDIT_PAGE = "EDIT_PAGE";
	public static final String SHOW_PAGE = "SHOW_PAGE";
	
	private Map<String, String> params = new HashMap<String, String>();
	
	public void initParam(PostContext context, NodeModel model, NodeTreeRenderModel renderer) throws DataSourceException, AlinousException
	{
		DataSrcConnection con = context.getUnit().getConnectionManager().connect(renderer.getConfig().getDatastore(), context);
		try{
			NodeModel parent = NodeModelUtils.getParentModel(con, renderer.getConfig(), model);
			int maxPos = 0;
			if(parent == null){
				maxPos = renderer.getCachedContext().getMaxRootPos();
			}else{
				maxPos = parent.getNumChildren() - 1;
			}
			
			this.params.put(MAX_POS, Integer.toString(maxPos));
			
			this.params.put(TREE_ID, renderer.getConfig().getId());
			
			this.params.put(NodeModel.NODE_ID, Integer.toString(model.getId()));
			this.params.put(NodeModel.LEVEL, Integer.toString(model.getLevel()));
			this.params.put(NodeModel.NUM_CHILDREN, Integer.toString(model.getNumChildren()));
			this.params.put(NodeModel.PARENT_ID, Integer.toString(model.getParentId()));
			this.params.put(NodeModel.POS_IN_LEVEL, Integer.toString(model.getPosInLevel()));
			this.params.put(NodeModel.VISIBLE, model.getVisible());
			this.params.put(NodeModel.CATEGORY, model.getCategory());
			
			
			String encoding = context.getCore().getConfig().getSystemRepositoryConfig().getEncoding();
			try {
				this.params.put(NodeModel.TITLE, URLEncoder.encode(model.getTitle(), encoding));
			} catch (UnsupportedEncodingException e) {
				context.getCore().getLogger().reportError(e);
			}
			
			addNodeRef(context, model, renderer, encoding);
			
			
			// view data
			this.params.put(VIEW_ID, renderer.getViewId());
			this.params.put(VIEW_TYPE, renderer.getViewType());
			
			// set modulePath
			InnerModulePath path = null;
			if(context.isInner()){
				path = context.getModulePath();
			}else{
				path = new InnerModulePath(context.getTopTopObject().getPath());
				path.addTarget(renderer.getViewId());
			}
			this.params.put(VIEW_INNER_MODULE_PATH, path.getStringPath());
			
			// documentType
			String docTypeId = model.getDocType();
			DoctypeConfig docConfig = renderer.getConfig().getDocType(docTypeId);
			
			// edit mode
			this.params.put(EDIT_MODE, Boolean.toString(renderer.isEditable()));
			
			
			this.params.put(EDIT_PAGE, docConfig.getEditPage());
			this.params.put(SHOW_PAGE, docConfig.getShowPage());
	
		}finally{
			con.close();
		}
	}
	
	private void addNodeRef(PostContext context, NodeModel model, NodeTreeRenderModel renderer, String encoding)
	{
		if(model.getDocRef() == null || model.getDocRef().equals("")){
			return;
		}
		
		try {
			this.params.put(NodeModel.DOC_REF, URLEncoder.encode(model.getDocRef(), encoding));
		} catch (UnsupportedEncodingException e) {
			context.getCore().getLogger().reportError(e);
		}
	}
	
	public String getParamData()
	{
		StringBuffer buff = new StringBuffer();
		
		boolean first = true;
		Iterator<String> it = this.params.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			String val = this.params.get(key);
			
			if(first){
				first = false;
				buff.append("&");
			}else{
				buff.append("&");
			}
			buff.append(key);
			buff.append("=");
			buff.append(val);
		}
		
		return buff.toString();
	}
}

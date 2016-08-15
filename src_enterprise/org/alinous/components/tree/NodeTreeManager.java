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
import java.io.Writer;
import java.util.Hashtable;
import java.util.Iterator;

import org.alinous.AlinousCore;
import org.alinous.components.IAlinousComponent;
import org.alinous.components.tree.command.NodeTreeCommandHandler;
import org.alinous.components.tree.model.NodeModelUtils;
import org.alinous.components.tree.model.NodeTreeRenderModel;
import org.alinous.datasrc.AlinousDataSourceManager;
import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.objects.IAttribute;
import org.alinous.script.runtime.VariableRepository;

public class NodeTreeManager implements IAlinousComponent
{
	private AlinousCore alinousCore;
	
	public void setAlinousCore(AlinousCore alinousCore)
	{
		this.alinousCore = alinousCore;
	}

	public void initComponent(AlinousDataSourceManager dataSourceManager) throws AlinousException
	{
		if(alinousCore.getConfig() == null){
			return;
		}
		
		NodeTreeConfig conf = alinousCore.getConfig().getNodeTreeConfig();
		
		if(conf == null){
			return;
		}
		
		Iterator<NodeConfig> it = conf.iterator();
		while(it.hasNext()){
			NodeConfig ndcfg = it.next();
			
			NodeDbAdaptor ad = new NodeDbAdaptor(ndcfg);
			try {
				ad.initDb(dataSourceManager, null);
			} catch (DataSourceException e) {
				throw new AlinousException(e, "Failed in init NodeTree manager"); // i18n
			}
		}
	}
	
	public void renderContents(Hashtable<String, IAttribute> alinousAttrs, 
			PostContext context, VariableRepository valRepo, Writer wr, int n) throws IOException, AlinousException, DataSourceException
	{
		NodeTreeRenderModel renderer = new NodeTreeRenderModel();
		if(!renderer.init(wr, alinousCore, alinousAttrs, context, valRepo)){
			return;
		}
		
		// execute command
		executeCommand(renderer, context);
		
		// init cache
		DataSrcConnection con = context.getUnit().getConnectionManager().connect(renderer.getConfig().getDatastore(), context);
		int maxRootPos = NodeModelUtils.getDbMaxRootPos(con, renderer.getConfig());
		
		renderer.getCachedContext().setMaxRootPos(maxRootPos);
		
		try {
			renderer.render(wr, context, valRepo);
		} catch (DataSourceException e) {
			con.close();
			throw new AlinousException(e, "failed in db operation."); // i18n
		}
		
		//context.getUnit().getConnectionManager().connect(dataSrc)
	}

	private void executeCommand(NodeTreeRenderModel renderer, PostContext context) throws DataSourceException, ExecutionException, RedirectRequestException
	{
		if(!renderer.isCurrentTarget()){
			return;
		}
		
		NodeTreeCommandHandler handler = new NodeTreeCommandHandler(renderer, context);
		
		handler.executeCommand(new VariableRepository());
		
	}
	
}

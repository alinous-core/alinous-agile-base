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

package org.alinous.components.tree.api;

import java.util.Stack;

import org.alinous.components.tree.NodeConfig;
import org.alinous.components.tree.NodeTreeSessionManager;
import org.alinous.components.tree.model.NodeModel;
import org.alinous.components.tree.model.NodeModelUtils;
import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.functions.system.AbstractSystemFunction;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class NodeTreeSetCurrent extends AbstractSystemFunction
{
	public static String QUALIFIED_NAME = "NODETREE.SETCURRENT";
	public static String TREE_NAME = "treeName";
	public static String NODE_ID = "nodeId";
	
	public NodeTreeSetCurrent()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", TREE_NAME);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", NODE_ID);
		this.argmentsDeclare.addArgument(arg);
	}
	
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		Stack<IStatement> stmtStack = context.getFuncArgStack();
		
		if(stmtStack.size() != this.argmentsDeclare.getSize()){
			throw new ExecutionException("Number of the function's arguments is wrong. : " + QUALIFIED_NAME);// i18n
		}
		
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		
		IPathElement ipath = PathElementFactory.buildPathElement(TREE_NAME);
		IScriptVariable treeNameVariable = newValRepo.getVariable(ipath, context);
		
		ipath = PathElementFactory.buildPathElement(NODE_ID);
		IScriptVariable nodeIdVariable = newValRepo.getVariable(ipath, context);
		
		if(!(treeNameVariable instanceof ScriptDomVariable) ||
				!(nodeIdVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		String treeName = ((ScriptDomVariable)treeNameVariable).getValue();
		String nodeId = ((ScriptDomVariable)nodeIdVariable).getValue();
		
		NodeConfig config = context.getCore().getConfig().getNodeTreeConfig().getNode(treeName);
		
		NodeTreeSessionManager sessionManager = new NodeTreeSessionManager(context, valRepo);
		
		DataSrcConnection con = null;
		try {
			con = sessionManager.getContext().getUnit().getConnectionManager()
										.connect(config.getDatastore(), context);
			
			NodeModel model = NodeModelUtils.getModelSingle(con, config, nodeId);
			openParents(con, sessionManager, config, model);
		} catch (DataSourceException e) {
			throw new ExecutionException(e, "Failed in connection on processing request");// i18n
		} finally{
			//con.close();
		}
		
		sessionManager.setCurrent(config, nodeId);
		
		return null;
	}

	private void openParents(DataSrcConnection con, NodeTreeSessionManager sessionMgr,
			NodeConfig config, NodeModel model)
			throws ExecutionException, DataSourceException, RedirectRequestException
	{
		while(model.hasParent()){
			model = NodeModelUtils.getParentModel(con, config, model);
			
			sessionMgr.open(config, Integer.toString(model.getId()));
		}
	}
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}

	@Override
	public String codeAssistString() {
		return null;
	}

	@Override
	public String descriptionString() {
		return null;
	}

}

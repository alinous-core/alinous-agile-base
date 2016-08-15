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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class NodeTreeGetModels extends AbstractSystemFunction
{
	public static String QUALIFIED_NAME = "NODETREE.GETMODELS";
	
	public static String TREE_NAME = "treeName";
	public static String TITLE = "title";
	
	public NodeTreeGetModels()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", TREE_NAME);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", TITLE);
		this.argmentsDeclare.addArgument(arg);
	}
	
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		ScriptArray arrayVariable;
		Stack<IStatement> stmtStack = context.getFuncArgStack();
		
		if(stmtStack.size() != this.argmentsDeclare.getSize()){
			throw new ExecutionException("Number of the function's arguments is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		
		IPathElement ipath = PathElementFactory.buildPathElement(TREE_NAME);
		IScriptVariable treeNameVariable = newValRepo.getVariable(ipath, context);
		
		ipath = PathElementFactory.buildPathElement(TITLE);
		IScriptVariable titleVariable = newValRepo.getVariable(ipath, context);
		
		if(!(treeNameVariable instanceof ScriptDomVariable) ||
				!(titleVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		String treeName = ((ScriptDomVariable)treeNameVariable).getValue();
		String title = ((ScriptDomVariable)titleVariable).getValue();
		
		NodeConfig config = context.getCore().getConfig().getNodeTreeConfig().getNode(treeName);
		NodeTreeSessionManager sessionManager = new NodeTreeSessionManager(context, valRepo);
		
		// do not have to close connection
		DataSrcConnection con = null;
		try {
			con = context.getUnit().getConnectionManager().connect(config.getDatastore(), context);
			
			Map<String, String> queryParams = new HashMap<String, String>();
			queryParams.put(NodeModel.TITLE, title);
			
			arrayVariable = new ScriptArray();
			
			if(title.length() == 0){
				handleRootModel(con, sessionManager, config, arrayVariable);
				return arrayVariable;
			}
			
			List<NodeModel> list = NodeModelUtils.getNodesByQuery(con, config, queryParams);
			
			if(list.size() != 0){
				handleModel(con, sessionManager, config, list.get(0), arrayVariable);
			}
			
		} catch (DataSourceException e) {
			throw new ExecutionException(e, "There is no tree resource : " + treeName); // i18n
		}finally{
			con.close();
		}
		
		return arrayVariable;
	}
	
	private void handleRootModel(DataSrcConnection con, NodeTreeSessionManager sessionManager, NodeConfig config,
				ScriptArray parent) throws ExecutionException, DataSourceException, RedirectRequestException
	{
		List<NodeModel> modelList = NodeModelUtils.getRootModels(con, config);
		
		Iterator<NodeModel> it = modelList.iterator();
		while(it.hasNext()){
			NodeModel mod = it.next();
			
			ScriptDomVariable dom = toScriptDomVariable(mod);
			parent.add(dom);
			
			handleChildren(con, sessionManager, config, mod, dom);
		}
	}
	
	private void handleModel(DataSrcConnection con, NodeTreeSessionManager sessionManager,
			NodeConfig config, NodeModel model, ScriptArray parent) throws ExecutionException, DataSourceException, RedirectRequestException
	{
		List<NodeModel> list = NodeModelUtils.getChildren(con, config, model);
		Iterator<NodeModel> it = list.iterator();
		while(it.hasNext()){
			NodeModel mod = it.next();
			
			ScriptDomVariable dom = toScriptDomVariable(mod);
			parent.add(dom);
			
			handleChildren(con, sessionManager, config, mod, dom);
		}
		
	}
	
	private void handleChildren(DataSrcConnection con, NodeTreeSessionManager sessionManager, NodeConfig config, NodeModel model,
			ScriptDomVariable dom)
			throws ExecutionException, DataSourceException, RedirectRequestException
	{
		if(!model.hasChildren() || !model.isExistsInTree()){
			return;
		}
		
		if(!sessionManager.isOpened(config, model.getId())){
			return;
		}
		
		ScriptArray childArray = new ScriptArray("CHILDREN");
		dom.put(childArray);
		
		handleModel(con, sessionManager, config, model, childArray);
		
	}
	
	private ScriptDomVariable toScriptDomVariable(NodeModel model)
	{
		ScriptDomVariable modelDom = new ScriptDomVariable("model");
		
		addValue(modelDom, NodeModel.NODE_ID, Integer.toString(model.getId()));
		addValue(modelDom, NodeModel.TITLE, model.getTitle());
		addValue(modelDom, NodeModel.DOC_REF, model.getDocRef());
		addValue(modelDom, NodeModel.NUM_CHILDREN, Integer.toString(model.getNumChildren()));
		addValue(modelDom, NodeModel.POS_IN_LEVEL, Integer.toString(model.getPosInLevel()));
		addValue(modelDom, NodeModel.LEVEL, Integer.toString(model.getLevel()));
		addValue(modelDom, NodeModel.PARENT_ID, Integer.toString(model.getParentId()));
		addValue(modelDom, NodeModel.VISIBLE, model.getVisible());
		addValue(modelDom, NodeModel.CATEGORY, model.getCategory());
		
		return modelDom;
	}
	
	private void addValue(ScriptDomVariable modelDom, String name, String value)
	{
		ScriptDomVariable valDom = new ScriptDomVariable(name);
		valDom.setValue(value);
		
		modelDom.put(valDom);
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

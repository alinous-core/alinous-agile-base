package org.alinous.components.tree.api;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.alinous.components.tree.NodeConfig;
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

public class NodeTreeGetChildrenById extends AbstractSystemFunction
{
	public static String QUALIFIED_NAME = "NODETREE.GETCHILDRENBYID";
	public static String TREE_NAME = "treeName";
	public static String NODE_ID = "nodeId";
	
	public NodeTreeGetChildrenById()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", TREE_NAME);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", NODE_ID);
		this.argmentsDeclare.addArgument(arg);	
	}
	
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		IScriptVariable retVal;
		Stack<IStatement> stmtStack = context.getFuncArgStack();
		
		if(stmtStack.size() != this.argmentsDeclare.getSize()){
			throw new ExecutionException("Number of the function's arguments is wrong : " + QUALIFIED_NAME);// i18n
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
		
		// do not have to close connection
		DataSrcConnection con = null;
		try {
			con = context.getUnit().getConnectionManager().connect(config.getDatastore(), context);
			
			NodeModel parentNode = NodeModelUtils.getModelSingle(con, config, nodeId);
			
			if(parentNode == null){
				retVal = new ScriptArray();
				return retVal;
			}
			
			List<NodeModel> modelList = NodeModelUtils.getChildren(con, config, parentNode);
			
			retVal = list2Dom(modelList);
		} catch (DataSourceException e) {
			throw new ExecutionException(e, "There is no tree resource : " + treeName); // i18n
		}finally{
			con.close();
		}
		
		return retVal;
	}
	
	private ScriptArray list2Dom(List<NodeModel> list)
	{
		if(list.isEmpty()){
			return new ScriptArray();
		}
		
		ScriptArray array = new ScriptArray();
		Iterator<NodeModel> it = list.iterator();
		while(it.hasNext()){
			NodeModel model = it.next();
			
			ScriptDomVariable domVal = node2Dom(model);
			array.add(domVal);
		}		
		
		return array;
	}
	
	private ScriptDomVariable node2Dom(NodeModel model)
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

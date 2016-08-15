package org.alinous.components.tree.api;

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
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class NodeTreeSetDocRef extends AbstractSystemFunction
{
	
	public static String QUALIFIED_NAME = "NODETREE.SETDOCREF";
	public static String TREE_NAME = "treeName";
	public static String NODE_ID = "nodeId";
	public static String DOC_REF = "docRef";
	
	public NodeTreeSetDocRef()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", TREE_NAME);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", NODE_ID);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", DOC_REF);
		this.argmentsDeclare.addArgument(arg);		
	}
	
	
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		Stack<IStatement> stmtStack = context.getFuncArgStack();
		
		if(stmtStack.size() != 3){
			throw new ExecutionException("Number of the function's arguments is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		
		IPathElement ipath = PathElementFactory.buildPathElement(TREE_NAME);
		IScriptVariable treeNameVariable = newValRepo.getVariable(ipath, context);
		
		ipath = PathElementFactory.buildPathElement(NODE_ID);
		IScriptVariable nodeIdVariable = newValRepo.getVariable(ipath, context);
		
		ipath = PathElementFactory.buildPathElement(DOC_REF);
		IScriptVariable docRefVariable = newValRepo.getVariable(ipath, context);
		
		if(!(treeNameVariable instanceof ScriptDomVariable) ||
				!(nodeIdVariable instanceof ScriptDomVariable) || !(docRefVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		String treeName = ((ScriptDomVariable)treeNameVariable).getValue();
		String nodeId = ((ScriptDomVariable)nodeIdVariable).getValue();
		String docRef = ((ScriptDomVariable)docRefVariable).getValue();
		
		NodeConfig config = context.getCore().getConfig().getNodeTreeConfig().getNode(treeName);
		
		// do not have to close connection
		DataSrcConnection con = null;
		try {
			con = context.getUnit().getConnectionManager().connect(config.getDatastore(), context);
			
			NodeModel nodeModel = NodeModelUtils.getModelSingle(con, config, nodeId);
			nodeModel.setDocRef(docRef);
			
			NodeModelUtils.updateModel(con, config, nodeModel);
		} catch (DataSourceException e) {
			throw new ExecutionException(e, "There is no tree resource : " + treeName); // i18n
		}finally{
			//con.close();
		}
		
		
		return null;
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

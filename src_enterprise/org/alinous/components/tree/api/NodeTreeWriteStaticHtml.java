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

import org.alinous.components.tree.seo.NodeTreeSeoManager;
import org.alinous.datasrc.api.SQLFunctionUtils;
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

public class NodeTreeWriteStaticHtml  extends AbstractSystemFunction
{
	public static String QUALIFIED_NAME = "NODETREE.WRITESTATICHTML";
	public static String TREE_NAME = "treeName";
	public static String TEMPLETE_HTML = "templeteHtml";
	public static String FILE_PATH = "filePath";
	public static String ROOT_NODE = "rootNode";
	
	public NodeTreeWriteStaticHtml()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", TREE_NAME);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", TEMPLETE_HTML);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", FILE_PATH);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", ROOT_NODE);
		this.argmentsDeclare.addArgument(arg);
	}
	
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		Stack<IStatement> stmtStack = context.getFuncArgStack();
		
		if(stmtStack.size() != this.argmentsDeclare.getSize()){
			throw new ExecutionException("Number of the function's arguments is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		
		IPathElement ipath = PathElementFactory.buildPathElement(TREE_NAME);
		IScriptVariable treeNameVariable = newValRepo.getVariable(ipath, context);
		
		ipath = PathElementFactory.buildPathElement(TEMPLETE_HTML);
		IScriptVariable templeteHtmlVariable = newValRepo.getVariable(ipath, context);
		
		ipath = PathElementFactory.buildPathElement(FILE_PATH);
		IScriptVariable filePathVariable = newValRepo.getVariable(ipath, context);
		
		ipath = PathElementFactory.buildPathElement(ROOT_NODE);
		IScriptVariable rootNodeVariable = newValRepo.getVariable(ipath, context);
		
		if(!(treeNameVariable instanceof ScriptDomVariable) ||
				!(templeteHtmlVariable instanceof ScriptDomVariable) || 
				!(filePathVariable instanceof ScriptDomVariable) ||
				!(rootNodeVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		String treeName = ((ScriptDomVariable)treeNameVariable).getValue();
		String templetePath = ((ScriptDomVariable)templeteHtmlVariable).getValue();
		String filePath = ((ScriptDomVariable)filePathVariable).getValue();
		
		String outFilePath = SQLFunctionUtils.getAbsolutePath(filePath, context.getCore());
		
		String rootNode = ((ScriptDomVariable)rootNodeVariable).getValue();
		if(!isPlusNumber(rootNode)){
			rootNode = null;
		}
		
		NodeTreeSeoManager nodeTreeMgr = new NodeTreeSeoManager();
		nodeTreeMgr.writeStatic(context, valRepo, treeName, templetePath, outFilePath, filePath, rootNode);
		
		
		return null;
	}
	
	private boolean isPlusNumber(String num)
	{
		int i = 0;
		try{
			i = Integer.parseInt(num);
		}catch(Throwable e){
			return false;
		}
		
		return i > 0;
	}

	public String getName()
	{
		return QUALIFIED_NAME;
	}

	public IScriptVariable getResult()
	{
		return null;
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

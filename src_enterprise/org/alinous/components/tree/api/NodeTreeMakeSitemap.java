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

import java.io.IOException;
import java.util.Stack;

import org.alinous.AlinousUtils;
import org.alinous.components.tree.NodeConfig;
import org.alinous.components.tree.seo.SitemapXmlWriter;
import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
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

public class NodeTreeMakeSitemap extends AbstractSystemFunction
{
	public static String QUALIFIED_NAME = "NODETREE.WRITESITEMAPXML";
	
	public static String TREE_NAME = "treeName";
	public static String ORIGIN_NODE_ID = "originNodeId";
	public static String DEFAULT_FREQ = "defaultFreq";
	public static String PRIORITY = "priority";
	
	public static String HTTP_HOST_PATH = "httpHostPath";
	public static String CONTENTS_OUT_DIR = "contentsOutDir";
	public static String OUT_FILE = "outFile";
		
	public NodeTreeMakeSitemap()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", TREE_NAME);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", ORIGIN_NODE_ID);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", DEFAULT_FREQ);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", PRIORITY);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", HTTP_HOST_PATH);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", CONTENTS_OUT_DIR);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", OUT_FILE);
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
		
		ipath = PathElementFactory.buildPathElement(ORIGIN_NODE_ID);
		IScriptVariable orignNodeVariable = newValRepo.getVariable(ipath, context);
		
		ipath = PathElementFactory.buildPathElement(DEFAULT_FREQ);
		IScriptVariable freqVariable = newValRepo.getVariable(ipath, context);
		
		ipath = PathElementFactory.buildPathElement(PRIORITY);
		IScriptVariable priorityVariable = newValRepo.getVariable(ipath, context);
		
		ipath = PathElementFactory.buildPathElement(HTTP_HOST_PATH);
		IScriptVariable httpHostPathVariable = newValRepo.getVariable(ipath, context);
		
		ipath = PathElementFactory.buildPathElement(CONTENTS_OUT_DIR);
		IScriptVariable contentsOutDirVariable = newValRepo.getVariable(ipath, context);
		
		ipath = PathElementFactory.buildPathElement(OUT_FILE);
		IScriptVariable outFileVariable = newValRepo.getVariable(ipath, context);
		
		if(!(treeNameVariable instanceof ScriptDomVariable) ||
				!(orignNodeVariable instanceof ScriptDomVariable) ||
				!(freqVariable instanceof ScriptDomVariable) ||
				!(priorityVariable instanceof ScriptDomVariable) ||
				!(httpHostPathVariable instanceof ScriptDomVariable) ||
				!(contentsOutDirVariable instanceof ScriptDomVariable) ||
				!(outFileVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		String treeName = ((ScriptDomVariable)treeNameVariable).getValue();
		String originNode = ((ScriptDomVariable)orignNodeVariable).getValue();
		String freq = ((ScriptDomVariable)freqVariable).getValue();
		String priority = ((ScriptDomVariable)priorityVariable).getValue();
		String httpHostPath = ((ScriptDomVariable)httpHostPathVariable).getValue();
		String contentsOutDir = ((ScriptDomVariable)contentsOutDirVariable).getValue();
		String outFile = ((ScriptDomVariable)outFileVariable).getValue();
		
		// convert Path
		outFile = AlinousUtils.getAbsolutePath(context.getCore().getHome(), outFile);
		
		NodeConfig config = context.getCore().getConfig().getNodeTreeConfig().getNode(treeName);
		
		// do not have to close connection
		DataSrcConnection con = null;
		try {
			con = context.getUnit().getConnectionManager().connect(config.getDatastore(), context);
			
			SitemapXmlWriter writer = new SitemapXmlWriter(con, config, httpHostPath, contentsOutDir,
					freq, priority);

			writer.writeSitemap(outFile, Integer.parseInt(originNode));
		} catch (DataSourceException e) {
			throw new ExecutionException(e, "There is no tree resource : " + treeName); // i18n
		} catch (NumberFormatException e) {
			throw new ExecutionException(e, "Orign Node it must be a number : " + treeName); // i18n
		} catch (AlinousException e) {
			throw new ExecutionException(e, "Failed in writing sitemap.xml : " + treeName); // i18n
		} catch (IOException e) {
			throw new ExecutionException(e, "Failed in writing sitemap.xml : " + treeName); // i18n
		}finally{
			con.close();
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

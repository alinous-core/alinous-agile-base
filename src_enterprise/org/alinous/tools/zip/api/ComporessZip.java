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
package org.alinous.tools.zip.api;

import java.io.IOException;
import java.util.Stack;

import org.alinous.cloud.file.AlinousFile;
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
import org.alinous.tools.zip.ZipComporesser;

public class ComporessZip extends AbstractSystemFunction
{
	public static String QUALIFIED_NAME = "ZIP.COMPRESS";
	
	public static String COMPRESS_DIR = "compressDir";
	public static String COMPRESS_FILE = "compressFile";
	
	private String alinousHome;
	
	public ComporessZip()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", COMPRESS_DIR);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", COMPRESS_FILE);
		this.argmentsDeclare.addArgument(arg);
	}
	
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		Stack<IStatement> stmtStack = context.getFuncArgStack();
		
		if(stmtStack.size() != this.argmentsDeclare.getSize()){
			throw new ExecutionException("Number of the function's arguments is wrong.");// i18n
		}
		
		this.alinousHome = context.getCore().getHome();
		
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		
		IPathElement ipath = PathElementFactory.buildPathElement(COMPRESS_DIR);
		IScriptVariable compressDirVariable = newValRepo.getVariable(ipath, context);
		
		ipath = PathElementFactory.buildPathElement(COMPRESS_FILE);
		IScriptVariable compressFileVariable = newValRepo.getVariable(ipath, context);
		
		if(!(compressDirVariable instanceof ScriptDomVariable) ||
				!(compressFileVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong.");// i18n
		}
		
		String compressDir = ((ScriptDomVariable)compressDirVariable).getValue();
		String compressFile = ((ScriptDomVariable)compressFileVariable).getValue();
		
		// convert into Abstract Path
		compressDir = getAbsolutePath(compressDir);
		compressFile = getAbsolutePath(compressFile);
		
		ZipComporesser ext = new ZipComporesser();
		
		try {
			ext.createZipFile(compressDir, compressFile);
		} catch (IOException e) {
			throw new ExecutionException(e, "Failed in compressing : " + compressDir); // i18n
		}
		
		return null;
	}

	private String getAbsolutePath(String alinousPath)
	{
		String tmp = null;
		if(!this.alinousHome.endsWith(AlinousFile.separator)){
			tmp = alinousHome + AlinousFile.separator;
		}else{
			tmp = alinousHome;
		}
		
		if(alinousPath.startsWith(AlinousFile.separator)){
			tmp = tmp + alinousPath.substring(1);
		}else{
			tmp = tmp + alinousPath;
		}
		
		return tmp;
	}
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}

	@Override
	public String codeAssistString() {
		return "Zip.compress($compressDir, $compressFile)";
	}

	@Override
	public String descriptionString() {
		return "Compress $compressDir directory into $compressFile.";
	}


}

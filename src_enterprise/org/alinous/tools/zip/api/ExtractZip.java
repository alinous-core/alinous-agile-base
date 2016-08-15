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
import java.util.zip.ZipException;

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
import org.alinous.tools.zip.ZipExtractor;

public class ExtractZip extends AbstractSystemFunction
{
	public static String QUALIFIED_NAME = "ZIP.EXTRACT";
	
	public static String EXTRACT_FILE = "extractFile";
	public static String EXTRACT_DIR = "extractDir";
	
	private String alinousHome;
	
	public ExtractZip()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", EXTRACT_FILE);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", EXTRACT_DIR);
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
		
		IPathElement ipath = PathElementFactory.buildPathElement(EXTRACT_FILE);
		IScriptVariable fileVariable = newValRepo.getVariable(ipath, context);
		
		ipath = PathElementFactory.buildPathElement(EXTRACT_DIR);
		IScriptVariable dirVariable = newValRepo.getVariable(ipath, context);
		
		if(!(fileVariable instanceof ScriptDomVariable) ||
				!(dirVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong.");// i18n
		}
		
		String extractFile = ((ScriptDomVariable)fileVariable).getValue();
		String extractDir = ((ScriptDomVariable)dirVariable).getValue();
		
		// convert into Abstract Path
		extractFile = getAbsolutePath(extractFile);
		extractDir = getAbsolutePath(extractDir);
		
		AlinousFile file = new AlinousFile(extractFile);
		
		try {
			ZipExtractor extractor = new ZipExtractor(file);
			
			extractor.extract(extractDir);
		} catch (ZipException e) {
			throw new ExecutionException(e, "Failed in extracting : " + extractFile); // i18n
		} catch (IOException e) {
			throw new ExecutionException(e, "Failed in extracting : " + extractFile); // i18n
		}
		
		return null;
	}

	private String getAbsolutePath(String alinousPath)
	{
		String tmp = null;
		if(!this.alinousHome.endsWith("/") && !this.alinousHome.endsWith("\\")){
			tmp = alinousHome + AlinousFile.separator;
		}else{
			tmp = alinousHome;
		}
		
		if(alinousPath.startsWith("/")){
			tmp = tmp + alinousPath.substring(1);
		}else if(alinousPath.startsWith("\\")){
			tmp = tmp + alinousPath.substring(1);
		}else{
			tmp = tmp + alinousPath;
		}
		
		AlinousFile file = new AlinousFile(tmp);
		
		return file.getAbsolutePath();
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
		return "Zip.extract($extractFile, $extractDir)";
	}

	@Override
	public String descriptionString() {
		return "Extract zip file $extractFile into the directory $extractDir.";
	}
	
}

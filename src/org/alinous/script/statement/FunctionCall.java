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
package org.alinous.script.statement;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.alinous.AlinousDebug;
import org.alinous.debug.StepInCandidates;
import org.alinous.exec.ScriptCheckContext;
import org.alinous.exec.ScriptError;
import org.alinous.exec.check.FunctionCheckRequest;
import org.alinous.exec.pages.AlinousExecutableModule;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.jdk.model.FunctionModel;
import org.alinous.script.AlinousScript;
import org.alinous.script.IScriptSentence;
import org.alinous.script.basic.RedirectSentence;
import org.alinous.script.basic.type.BooleanConst;
import org.alinous.script.basic.type.DoubleConst;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.basic.type.NumericConst;
import org.alinous.script.basic.type.StringConst;
import org.alinous.script.basic.type.VariableDescriptor;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.functions.FuncDeclarations;
import org.alinous.script.functions.FunctionRegistory;
import org.alinous.script.functions.FunctionDeclaration;
import org.alinous.script.functions.IFunction;
import org.alinous.script.functions.codecheck.CodeCheckSkipOnce;
import org.alinous.script.functions.system.JavaConnectorFunction;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.test.coverage.FileCoverage;
import org.jdom.Element;

public class FunctionCall implements IStatement
{
	private String packageName;
	private String funcName;
	private FuncArguments args;	// Not Runtime arguments
	
	private int line;
	private int linePosition;
	private String filePath;
	
	
	private IScriptSentence callerSentence;
	
	public IScriptVariable executeStatement(PostContext context, VariableRepository valRepo)
									throws ExecutionException, RedirectRequestException
	{
		String qualifiedName = this.packageName + "." + this.funcName;
		if(this.packageName == null){
			qualifiedName = this.funcName;
		}
		
		// current line and path
		context.setCurrentPath(this.filePath);
		context.setCurrentLine(this.line);
		
		// get function
		IFunction func = null;
		
		AlinousExecutableModule module = context.getUnit().getExecModule();
		if(module != null){
			//AlinousDebug.debugOut("***module : " + module);
			
			FuncDeclarations funcDec = module.getScript().getFuncDeclarations();
			func = funcDec.findFunctionDeclare(qualifiedName);
		}
		
		// if null load from include
		if(func == null){
			// 
			//AlinousDebug.debugOut("*** before context.getIncludeFuncDeclaration : ");
			func = context.getIncludeFuncDeclaration(qualifiedName);
			//AlinousDebug.debugOut("*** after context.getIncludeFuncDeclaration : " + func);
		}
		
		if(func != null){
			return executeSourceFunc((FunctionDeclaration)func, context, valRepo);
		}
		
		// from system functions
		func = FunctionRegistory.getInstance().findDeclaration(qualifiedName);
		if(func != null){
			return executeSystemFunc(func, context, valRepo);
		}
		
		// execute Java Connector
		FunctionModel funcModel = context.getCore().getJavaConnector().findFunction(qualifiedName);
		
		if(funcModel == null){
			throw new ExecutionException("Function '"+ this.funcName + "' does not declared. @" + this.filePath + " line : " + this.line);  // i18n
		}
		
		return executeJavaConnectorFunction(qualifiedName, funcModel, context, valRepo);
	}
	
	
	private IScriptVariable executeJavaConnectorFunction(String name,
			FunctionModel funcModel, PostContext context, VariableRepository valRepo)
				throws ExecutionException, RedirectRequestException
	{
		// Make Java Connector Function
		JavaConnectorFunction func = new JavaConnectorFunction(name, funcModel);
		
		handleRuntimeArguments(func, context);
		
		func.setCallerSentence(this.callerSentence);
		
		// return
		IScriptVariable val = null;
		
		try{
			val = func.executeFunction(context, valRepo);
		}finally{
			context.popFuncArgStack();
		}
		
		return val;
	}
	
	private IScriptVariable executeSystemFunc(IFunction func, PostContext context, VariableRepository valRepo)
					throws ExecutionException, RedirectRequestException
	{
		handleRuntimeArguments(func, context);
		
		func.setCallerSentence(this.callerSentence);
		
		// return
		IScriptVariable val = null;
		
		try{
			val = func.executeFunction(context, valRepo);
		}finally{
			context.popFuncArgStack();
		}
		
		return val;
	}
	
	private IScriptVariable executeSourceFunc(FunctionDeclaration func, PostContext context, VariableRepository valRepo)
				throws ExecutionException, RedirectRequestException
	{
		func.setDataSourceManager(context.getCore().getDataSourceManager());
		
		handleRuntimeArguments(func, context);
		
		func.setCallerSentence(this.callerSentence);
		
		// return
		IScriptVariable val = null;
		
		try{
			val = func.executeFunction(context, valRepo);
		}finally{
			context.popFuncArgStack();
		}
		
		// handleRedirect
		handleRedirect(context, valRepo, val);
		
		return val;
	}
	
	private void handleRedirect(PostContext context, VariableRepository valRepo, IScriptVariable val) throws RedirectRequestException
	{
		// Redirect
		IScriptSentence lastStatemtne = context.getLastSentence();
				
		if(lastStatemtne instanceof RedirectSentence){
			String targetPath = ((ScriptDomVariable)val).getValue();
			
			ScriptDomVariable code = (ScriptDomVariable) ((ScriptDomVariable)context.getScriptReturnedValue()).get("redirectCode");
			String strCode = code.getValue();
			
			RedirectRequestException ex = new RedirectRequestException(context.getFilePath(targetPath), strCode);
			
			throw ex;
		}
	}
	
	private void handleRuntimeArguments(IFunction func, PostContext context)
	{
		if(this.args == null){
			Stack<IStatement> stmtStack = new Stack<IStatement>();
			func.inputArguments(stmtStack, context);
			return;
		}
		
		Stack<IStatement> stmtStack = new Stack<IStatement>();
		Iterator<IStatement> it = this.args.iterator();
		while(it.hasNext()){
			IStatement stmt = it.next();
			
			stmtStack.push(stmt);
		}
		
		func.inputArguments(stmtStack, context);
	}
	
	
	public String getFuncName()
	{
		return funcName;
	}
	
	public void setFuncName(String funcName)
	{
		this.funcName = funcName;
	}
	
	public FuncArguments getArgs()
	{
		return args;
	}
	
	public void setArgs(FuncArguments args)
	{
		this.args = args;
		if(args != null){
			args.setFilePath(filePath);
		}
	}

	public int getLine()
	{
		return line;
	}


	public void setLine(int line)
	{
		this.line = line;
	}


	public int getLinePosition()
	{
		return linePosition;
	}


	public void setLinePosition(int linePosition)
	{
		this.linePosition = linePosition;
	}


	public String getFilePath()
	{
		if(this.args != null){
			this.args.setFilePath(filePath);
		}
		return filePath;
	}


	public void setFilePath(String filePath)
	{
		this.filePath = filePath;
		
		if(this.args != null){
			this.args.setFilePath(filePath);
		}
	}


	public String getPackageName()
	{
		return packageName;
	}


	public void setPackageName(String packageName)
	{
		this.packageName = packageName;
	}


	public void exportIntoJDomElement(Element parent) throws AlinousException
	{
		Element element = new Element(IStatement.TAG_STATEMENT);
		element.setAttribute(IStatement.ATTR_STATEMENT_CLASS, this.getClass().getName());
		parent.addContent(element);
		
		if(this.args != null){
			this.args.exportIntoJDomElement(element);
		}
	}


	public void importFromJDomElement(Element element) throws AlinousException
	{
		Element ch = element.getChild(FuncArguments.TAG_ARGUMENTS);
		
		if(ch != null){
			this.args = new FuncArguments();
			this.args.importFromJDomElement(ch);
		}
	}

	public void canStepInStatements(StepInCandidates candidates)
	{
		String qualifiedName = this.packageName + "." + this.funcName;
		if(this.packageName == null){
			qualifiedName = this.funcName;
		}
		
		
		// from system functions
		IFunction func = FunctionRegistory.getInstance().findDeclaration(qualifiedName);
		
		if(func == null){
			candidates.addCandidate(this);
		}
		
		if(this.args != null){
			this.args.canStepInStatements(candidates);
		}
	}

	public void setCallerSentence(IScriptSentence callerSentence)
	{
		this.callerSentence = callerSentence;
		if(this.args != null){
			this.args.setCallerSentence(callerSentence);
		}
	}


	public void checkStaticErrors(ScriptCheckContext scContext,
			List<ScriptError> errorList)
	{
		// Skip Code check
		String qualifiedName = this.packageName + "." + this.funcName;
		if(this.packageName == null){
			qualifiedName = this.funcName;
		}
		
		if(qualifiedName.toUpperCase().equals(CodeCheckSkipOnce.QUALIFIED_NAME)){
			scContext.setSkipOnce(true);
			return;
		}
		
		// check arguments
		// get function
		FuncDeclarations funcDecs = scContext.getAlinousScript().getFuncDeclarations();
		FunctionDeclaration funcDec = funcDecs.findFunctionDeclare(qualifiedName);
		
		if(funcDec != null){
			checkArguments(funcDec, scContext, errorList);
			//AlinousDebug.debugOut("Source Self Function qualifiedName : " + qualifiedName);
			return;
		}
		
		// if null load from include
		funcDec = scContext.findFunctionDeclaration(qualifiedName);
		
		if(funcDec != null){
			checkArguments(funcDec, scContext, errorList);
			//AlinousDebug.debugOut("Source included Function qualifiedName : " + qualifiedName);
			return;
		}
		
		// from system functions
		IFunction func = FunctionRegistory.getInstance().findDeclaration(qualifiedName.toUpperCase());
		if(func != null){
			checkArguments(func, scContext, errorList);
			//AlinousDebug.debugOut("Function registory qualifiedName : " + qualifiedName);
			return;
		}
		
		// execute Java Connector
		FunctionModel funcModel = scContext.getJavaConnectorManager().getModels().get(qualifiedName);
		
		if(funcModel != null){
			checkArguments(funcModel, scContext, errorList);
			//AlinousDebug.debugOut("Java connector qualifiedName : " + qualifiedName);
			return;
		}
		
		AlinousDebug.debugOut(null, "Missing qualifiedName : " + qualifiedName);
		ScriptError scError = new ScriptError();
		scError.setScriptElement(scContext.getCurrentExecutable());
		scError.setMessage("Missing qualifiedName : " + qualifiedName);
		errorList.add(scError);
	}
	
	private void checkArguments(IFunction func, ScriptCheckContext scContext, List<ScriptError> errorList)
	{
		int decSize = 0;
		if(func.getArguments() != null){
			decSize= func.getArguments().getSize();
		}
		
		if(decSize == 0){
			if(this.args == null){
				return;
			}
			if(this.args.getSize() == 0){
				return;
			}
			
			// error
			ScriptError scError = new ScriptError();
			scError.setScriptElement(scContext.getCurrentExecutable());
			scError.setMessage("Function Argument is wrong.");
			errorList.add(scError);
			
			return;
		}
		
		int callSize = this.args.getSize();
		if(callSize != decSize){
			// error
			ScriptError scError = new ScriptError();
			scError.setScriptElement(scContext.getCurrentExecutable());
			scError.setMessage("Function Argument is wrong.");
			errorList.add(scError);
			
			return;
		}
		
		int i = 0;
		Iterator<IStatement> it = this.args.iterator();
		while(it.hasNext()){
			IStatement calledArg = it.next();
			ArgumentDeclare argDec = func.getArguments().get(i++);
			
			if(argDec.getPrefix().equals("*")){
				continue;
			}
			
			if(calledArg instanceof VariableDescriptor){
				VariableDescriptor desc = (VariableDescriptor)calledArg;
				
				//AlinousDebug.debugOut("desc Prefix : " + desc.getPrefix() + " arg prefix :" + argDec.getPrefix()); 
				
				if(!desc.getPrefix().equals(argDec.getPrefix())){
					// error
					ScriptError scError = new ScriptError();
					scError.setScriptElement(scContext.getCurrentExecutable());
					scError.setMessage("Function Argument type is wrong.");
					errorList.add(scError);
				}
			}
			else if(calledArg instanceof StringConst || calledArg instanceof NumericConst || calledArg instanceof DoubleConst
						|| calledArg instanceof BooleanConst){
				
				if(!argDec.getPrefix().equals("$")){
					// error
					ScriptError scError = new ScriptError();
					scError.setScriptElement(scContext.getCurrentExecutable());
					scError.setMessage("Function Argument type is wrong.");
					errorList.add(scError);
				}
			}
		}
		
	}
	
	private void checkArguments(FunctionModel func, ScriptCheckContext scContext, List<ScriptError> errorList)
	{
		int decSize = 0;
		if(func.getArguments() != null){
			decSize = func.getArguments().size();
		}
		if(decSize == 0){
			if(this.args == null){
				return;
			}
			if(this.args.getSize() == 0){
				return;
			}
			
			// error
			ScriptError scError = new ScriptError();
			scError.setScriptElement(scContext.getCurrentExecutable());
			scError.setMessage("Function Argument is wrong.");
			errorList.add(scError);
			
			return;
		}
		
		int callSize = this.args.getSize();
		if(callSize != decSize){
			// error
			ScriptError scError = new ScriptError();
			scError.setScriptElement(scContext.getCurrentExecutable());
			scError.setMessage("Function Argument is wrong.");
			errorList.add(scError);
			
			return;
		}
		
	}


	@Override
	public void getFunctionCall(ScriptCheckContext scContext, List<FunctionCall> call, AlinousScript script)
	{
		// call of Thread.execute()
		if(this.packageName != null && this.packageName.toUpperCase().equals("THREAD")
				&& this.funcName.toUpperCase().equals("EXECUTE")){
			handleThreadExecute(scContext, call, script);
			return;
		}
		
		
		if(alreadyHasFunction(call)){
			return;
		}
		
		call.add(this);
		
		String qualifiedName = this.packageName + "." + this.funcName;
		if(this.packageName == null){
			qualifiedName = this.funcName;
		}
		
		FunctionDeclaration sourceDeclare = script.getDeclare(scContext, qualifiedName);
		if(sourceDeclare != null){
			sourceDeclare.getFunctionCall(scContext, call, script);
		}
		else{
			FunctionCheckRequest request = new FunctionCheckRequest();
			request.setFunctionCall(this);
			request.setRootScript(script);
			
			scContext.addFunctionCheckRequest(request);
		}
		
	}
	
	private void handleThreadExecute(ScriptCheckContext scContext, List<FunctionCall> call, AlinousScript script)
	{
		IStatement stmt = this.args.getStatement(0);
		
		if(stmt instanceof StringConst){
			StringConst funcName = (StringConst)stmt;
			
			String qualifiedName = funcName.getStr();
			String vals[] = qualifiedName.split("\\.");
			
			FunctionCall threadCall = new FunctionCall();
			
			if(vals.length == 2){
				threadCall.setPackageName(vals[0]);
				threadCall.setFuncName(vals[1]);
			}else{
				threadCall.setFuncName(qualifiedName);
			}
			
			threadCall.setCallerSentence(this.callerSentence);
			threadCall.setFilePath(this.filePath);
			threadCall.setLine(this.line);
			threadCall.setLinePosition(this.linePosition);
			
			threadCall.getFunctionCall(scContext, call, script);
		}
	}
	
	public String getQualifiedName()
	{
		String qualifiedName = this.packageName + "." + this.funcName;
		if(this.packageName == null){
			qualifiedName = this.funcName;
		}
		
		return qualifiedName;
	}
	
	private boolean alreadyHasFunction(List<FunctionCall> call)
	{
		Iterator<FunctionCall> it = call.iterator();
		while(it.hasNext()){
			FunctionCall functionCall = it.next();
			
			if(getQualifiedName().equals(functionCall.getQualifiedName())
					&& this.line == functionCall.line
					&& this.linePosition == functionCall.linePosition
					&& this.filePath.equals(functionCall.getFilePath())){
				return true;
			}
			
		}
		
		return false;
	}


	@Override
	public void setupCoverage(FileCoverage coverage)
	{
		
	}

}

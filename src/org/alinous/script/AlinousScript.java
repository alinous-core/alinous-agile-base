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
package org.alinous.script;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.alinous.AlinousCore;
import org.alinous.debug.StepInCandidates;
import org.alinous.exec.ScriptCheckContext;
import org.alinous.exec.ScriptError;
import org.alinous.exec.ScriptWarning;
import org.alinous.exec.check.FunctionCheckRequest;
import org.alinous.exec.check.IAlinousCheckFunctionContainer;
import org.alinous.exec.check.IAlinousCheckFunctionModel;
import org.alinous.exec.check.IJavaConnectorManager;
import org.alinous.exec.check.IncludeCheck;
import org.alinous.exec.check.IncludeUseChecker;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.parser.script.AlinousScriptParser;
import org.alinous.script.basic.AbstractScriptBlock;
import org.alinous.script.basic.IncludeSentence;
import org.alinous.script.basic.ReferFromSentence;
import org.alinous.script.basic.ValidatorSentence;
import org.alinous.script.basic.type.StringConst;
import org.alinous.script.functions.FuncDeclarations;
import org.alinous.script.functions.FunctionDeclaration;
import org.alinous.script.functions.FunctionRegistory;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.statement.FunctionCall;
import org.alinous.test.coverage.FileCoverage;
import org.jdom.Element;


public class AlinousScript extends AbstractScriptBlock {
	public static final String BLOCK_NAME = "AlinousScript";

	private FuncDeclarations funcDeclarations;
	private CopyOnWriteArrayList<IncludeSentence> includeSentences = new CopyOnWriteArrayList<IncludeSentence>();
	private CopyOnWriteArrayList<ReferFromSentence> referFroms = new CopyOnWriteArrayList<ReferFromSentence>();
	private CopyOnWriteArrayList<ValidatorSentence> validators = new CopyOnWriteArrayList<ValidatorSentence>();
	
	public AlinousScript(String filePath)
	{
		super(filePath);
		
		this.line = 0;
		this.linePosition = 0;
		this.filePath = filePath;
	}
	
	public boolean execute(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		return execute(context, valRepo, true, true);
	}
	
	public boolean execute(PostContext context, VariableRepository valRepo, boolean parepareDebug, boolean initOperation) throws ExecutionException, RedirectRequestException
	{
		// DEBUG: create stack frame
		if(context != null){
			context.setDebugEnabled(parepareDebug);
		}
		if(AlinousCore.debug(context) && parepareDebug){
			if(initOperation){
				context.getCore().getAlinousDebugManager().initOperation();
			}
			context.getCore().getAlinousDebugManager().createStackFrame(this, valRepo, context);
		}
		
		// server side security check
		AlinousScriptSecurityManager.getInstance().checkSecurity(context, valRepo, this);
		
		Iterator<IncludeSentence> incIt = this.includeSentences.iterator();
		while(incIt.hasNext()){
			IncludeSentence inc = incIt.next();
			
			boolean res = executeSentence(inc, context, valRepo);
			
			if(!res){
				break;
			}
		}
		
		
		Iterator<IScriptSentence> it = this.sentences.iterator();
		while(it.hasNext()){
			IScriptSentence exec = it.next();
			
			boolean res = executeSentence(exec, context, valRepo);
			
			if(!res){
				break;
			}
		}
		
		// DEBUG: destory stack frame
		if(AlinousCore.debug(context) && parepareDebug){
			context.getCore().getAlinousDebugManager().destoryCurrentStackFrame();
		}
		
		// finished
		if(AlinousCore.coverageTest()){
			try {
				context.getCore().flashCoverageIntoFile(this);
			} catch (AlinousException e) {
				throw new ExecutionException(e, e.getMessage());
			}
		}
		
		context.setScriptReturnedValue(context.getReturnedVariable(this));
		
		return true;
	}
	
	public Iterator<IncludeSentence> includeSentencesIterator()
	{
		return this.includeSentences.iterator();
	}

	public void exportIntoJDomElement(Element parent) {
		
	}

	public void importFromJDomElement(Element threadElement) {
		
	}

	public String getName()
	{
		return BLOCK_NAME;
	}

	public FuncDeclarations getFuncDeclarations()
	{
		return funcDeclarations;
	}

	public void setFuncDeclarations(FuncDeclarations funcDeclarations)
	{
		this.funcDeclarations = funcDeclarations;
		this.funcDeclarations.setFilePath(this.filePath);
	}

	public StepInCandidates getStepInCandidates()
	{
		return null;
	}
	
	public void addInclude(IncludeSentence includeSentence)
	{
		this.includeSentences.add(includeSentence);
		
		includeSentence.setFilePath(getFilePath());
	}
	
	public void addReferFrom(ReferFromSentence referFromSentence)
	{
		this.referFroms.add(referFromSentence);
		referFromSentence.setFilePath(getFilePath());
	}
	
	public void addValidatorSentence(ValidatorSentence validatorSentence)
	{
		this.validators.add(validatorSentence);
		validatorSentence.setFilePath(getFilePath());
	}
	
	public List<ReferFromSentence> getReferFromSentence()
	{
		return this.referFroms;
	}
	
	public List<ValidatorSentence> getValidatorSentence()
	{
		return this.validators;
	}

	@Override
	public IScriptVariable getReturnedVariable(PostContext context)
	{
		return context.getReturnedVariable(this);
	}

	@Override
	public void checkStaticErrors(ScriptCheckContext scContext,
			List<ScriptError> errorList)
	{
		synchronized (this) {
			scContext.setAlinousScript(this);
	
			List<FunctionCall> callRoot = new ArrayList<FunctionCall>();
			List<FunctionCall> call = new ArrayList<FunctionCall>();
			getFunctionCall(scContext, callRoot, this);
			getFunctionCall(scContext, call, this);
			
			
			Iterator<IncludeSentence> it = this.includeSentencesIterator();
			while(it.hasNext()){
				IncludeSentence sentence = it.next();
				
				AlinousScript incScript = getIncludeScript(scContext, sentence);
				
				if(incScript == null){
					ScriptError error = new ScriptError();
					error.setScriptElement(sentence);
					error.setMessage("Included file does not exists.");
					
					errorList.add(error);
					return;
				}
				
				incScript.getFunctionCallOfInclude(scContext, call, incScript, callRoot);
				
				scContext.addIncludeScript(incScript);
			}
			
			Iterator<FunctionCall> funcCallIt = call.iterator();
			while(funcCallIt.hasNext()){
				FunctionCall callFunc = funcCallIt.next();
				
				String qualifiedName = callFunc.getPackageName() + "." + callFunc.getFuncName();
				if(callFunc.getPackageName() == null){
					qualifiedName = callFunc.getFuncName();
				}
				
				boolean resultIncluded = isIncluded(scContext, qualifiedName);
				if(!resultIncluded && callFunc.getFilePath() != null && callFunc.getFilePath().equals(this.filePath)){
					ScriptError error = new ScriptError();
					error.setLine(callFunc.getLine());
					error.setLinePosition(callFunc.getLinePosition());
					error.setMessage("This function is not defined.");
					
					errorList.add(error);
				}
				else if(!resultIncluded){
					IncludeSentence cause = getIncludeFromPath(callFunc.getFilePath());
					if(cause == null){
						continue; // Java connector
					}
					
					ScriptError error = new ScriptError();
					error.setLine(cause.getLine());
					error.setLinePosition(cause.getLinePosition());
					error.setMessage("The function " + qualifiedName + "() in the include file is not defined.");
					
					errorList.add(error);
				}
				
			}
			
			super.checkStaticErrors(scContext, errorList);
			
			this.funcDeclarations.checkStaticErrors(scContext, errorList);
			
			handleUndefinedOrNativeFunctions(scContext, errorList);			
			
			includeUsed(scContext, errorList);
		}
	}
	
	private void handleUndefinedOrNativeFunctions(ScriptCheckContext scContext,
			List<ScriptError> errorList)
	{
		Iterator<FunctionCheckRequest> it = scContext.getFunctionCheckRequests().iterator();
		while(it.hasNext()){
			FunctionCheckRequest request = it.next();
			
			String qualifiedName = request.getFunctionCall().getPackageName() + "." + request.getFunctionCall().getFuncName();
			if(request.getFunctionCall().getPackageName() == null){
				qualifiedName = request.getFunctionCall().getFuncName();
			}
			
			// system functions
			FunctionRegistory registory = FunctionRegistory.getInstance();
			if(registory.hasFunction(qualifiedName)){
				continue;
			}
			
			// Java Connector
			IJavaConnectorManager javaConnector = scContext.getJavaConnectorManager();
			Map<String, IAlinousCheckFunctionContainer> map = javaConnector.getFuntionList();
			if(containsJavaConnector(map, qualifiedName)){
				continue;
			}
			
			/*
			// debug
			AlinousDebug.debugOut("[" + request.getRootScript().getFilePath() + "]" + request.getFunctionCall().getFilePath() + " -> " + qualifiedName);
			*/
		}
	}
	
	public IncludeSentence getIncludeFromPath(String filePath)
	{
		Iterator<IncludeSentence> it = this.includeSentences.iterator();
		while(it.hasNext()){
			IncludeSentence inc = it.next();
			
			StringConst path = (StringConst)inc.getArgs().getStatement(0);
			if(path.getStr().equals(filePath)){
				return inc;
			}
		}
		
		return null;
	}

	@Override
	public void setFilePath(String filePath)
	{
		if(this.funcDeclarations != null){
			this.funcDeclarations.setFilePath(filePath);
		}
		
		Iterator<IncludeSentence> it = this.includeSentences.iterator();
		while(it.hasNext()){
			IncludeSentence sentence = it.next();
			
			sentence.setFilePath(filePath);
		}
		
		super.setFilePath(filePath);
	}

	@Override
	public void getFunctionCall(ScriptCheckContext scContext, List<FunctionCall> call, AlinousScript script)
	{
		if(this.funcDeclarations != null){
			this.funcDeclarations.getFunctionCall(scContext, call, script);
		}
		
		Iterator<IScriptSentence> it = this.sentences.iterator();
		while(it.hasNext()){
			IScriptSentence exec = it.next();
			exec.getFunctionCall(scContext, call, script);
		}
		
	}
	
	public void getFunctionCallOfInclude(ScriptCheckContext scContext, List<FunctionCall> call, AlinousScript script, List<FunctionCall> callRoot)
	{
		if(this.funcDeclarations != null){
			this.funcDeclarations.getFunctionCallByInclude(scContext, call, script, callRoot);
		}
		
		
	}
	
	private void includeUsed(ScriptCheckContext scContext, List<ScriptError> errorList)
	{
		List<IncludeCheck> includesList = scContext.getIncludeChecker().getUnusedIncludes();
		Iterator<IncludeCheck> it = includesList.iterator();
		while(it.hasNext()){
			IncludeCheck noUse = it.next();
			
			ScriptWarning warning = new ScriptWarning();
			warning.setLine(noUse.getLine());
			warning.setLinePosition(noUse.getLinePosition());
			warning.setMessage("Include is not used.");
			
			errorList.add(warning);
		}
		
	}
	
	public boolean isPathIncluded(String modulePath)
	{
		VariableRepository valRepo = new VariableRepository();
		PostContext context = new PostContext(null, null);
		
		Iterator<IncludeSentence> it = this.includeSentences.iterator();
		while(it.hasNext()){
			IncludeSentence sentence = it.next();
			
			try{
				ScriptDomVariable val = (ScriptDomVariable) sentence.getArgs().getStatement(0).executeStatement(context, valRepo);
				
				String includeArg = val.getValue();
				if(includeArg.replace('/', File.separatorChar).equals(modulePath)){
					return true;
				}
			}catch(Throwable e){}
		}
		
		return false;
	}
	
	public boolean isIncluded(ScriptCheckContext scContext, String functionName)
	{
		// system functions
		FunctionRegistory registory = FunctionRegistory.getInstance();
		if(registory.hasFunction(functionName)){
			return true;
		}
		
		// Java Connector
		IJavaConnectorManager javaConnector = scContext.getJavaConnectorManager();
		Map<String, IAlinousCheckFunctionContainer> map = javaConnector.getFuntionList();
		if(containsJavaConnector(map, functionName)){
			return true;
		}
		
		
		// self source function
		FunctionDeclaration decSelf = this.funcDeclarations.findFunctionDeclare(functionName);
		if(decSelf != null){
			return true;
		}
		
		// source function
		Iterator<IncludeSentence> it = this.includeSentencesIterator();
		while(it.hasNext()){
			IncludeSentence sentence = it.next();
			
			
			AlinousScript incScript = getIncludeScript(scContext, sentence);
			
			FuncDeclarations decs = incScript.getFuncDeclarations();
			List<FunctionDeclaration> list = decs.getListSortedByLine();
			Iterator<FunctionDeclaration> secit = list.iterator();
			
			while(secit.hasNext()){
				FunctionDeclaration dec = secit.next();
				String qualifiedName = dec.getName();
				
				if(qualifiedName.equals(functionName)){
					IncludeUseChecker includeChecker = scContext.getIncludeChecker();
					includeChecker.useInclude(sentence);
					
					return true;
				}
			}
			
		}
		
		
		return false;
	}
	
	private boolean containsJavaConnector(Map<String, IAlinousCheckFunctionContainer> map, String functionName)
	{
		String vals[] = functionName.split("\\.");
		if(vals.length != 2){
			return false;
		}
		
		IAlinousCheckFunctionContainer container = map.get(vals[0]);
		
		if(container == null){
			return false;
		}
		
		Iterator<IAlinousCheckFunctionModel> it = container.listFunctionModels().iterator();
		while(it.hasNext()){
			IAlinousCheckFunctionModel model = it.next();
			
			if(model.getName().equals(vals[1])){
				return true;
			}
		}
		
		return false;
	}
	
	
	private AlinousScript getIncludeScript(ScriptCheckContext scContext, IncludeSentence sentence)
	{
		AlinousScript script = null;
		synchronized (scContext) {
			StringConst stmt = (StringConst) sentence.getArgs().getStatement(0);
			
			script = scContext.getCache().getScript(stmt.getStr());
			if(script != null){
				return script;
			}
			
			String fileName = scContext.getAlinousHome() + stmt.getStr();
			AlinousScriptParser parser = null;
			
			try {
				FileInputStream is = new FileInputStream(new File(fileName));
				InputStreamReader reader = new InputStreamReader(is, "utf-8");
				
				
				parser = new AlinousScriptParser(reader);
				
				script = parser.parse();
			} catch (Throwable throwable){
				//throwable.printStackTrace();
				if(parser != null){
					script = parser.getLastScript();
				}
			}
			
			if(parser != null){
				script.setFilePath(stmt.getStr());
				scContext.getCache().addScript(script);
			}
		}

		
		return script;
	}
	
	public void compositeIncludes2Check(IncludeUseChecker checker)
	{
		Iterator<IncludeSentence> it = this.includeSentences.iterator();
		while(it.hasNext()){
			IncludeSentence includeOriginal = it.next();
			
			checker.registerInclude(includeOriginal);
		}
	}
	
	public FunctionDeclaration getDeclare(ScriptCheckContext scContext, String qualifiedName)
	{
		if(this.funcDeclarations != null){
			FunctionDeclaration dec = this.funcDeclarations.findFunctionDeclare(qualifiedName);
			if(dec != null){
				return dec;
			}
		}
		
		Iterator<IncludeSentence> it = this.includeSentences.iterator();
		while(it.hasNext()){
			IncludeSentence sentence = it.next();
			
			StringConst stmt = (StringConst) sentence.getArgs().getStatement(0);
			AlinousScript script = scContext.getCache().getScript(stmt.getStr());
			

			
			
			if(script == null){
				script = compileFile(stmt.getStr(), scContext.getAlinousHome());		
			}
			
			if(script != null && script.getFuncDeclarations() != null){
				FunctionDeclaration dec = script.getFuncDeclarations().findFunctionDeclare(qualifiedName);
				
				if(dec != null){
					return dec;
				}
			}
			
			/*
			if(script == null){
				// debug
				AlinousDebug.debugOut("************************* ERROR script=null stmt.getStr()  " + stmt.getStr());
			}
			*/
		}
		
		return null;
	}
	
	private AlinousScript compileFile(String modulePath, String alinousHome)
	{
		AlinousScriptParser parser = null;
		AlinousScript script = null;
		
		try {
			String fileName = alinousHome + modulePath;
			FileInputStream is = new FileInputStream(new File(fileName));
			InputStreamReader reader = new InputStreamReader(is, "utf-8");
			
			
			parser = new AlinousScriptParser(reader);
			
			script = parser.parse();
		} catch (Throwable throwable){
			//throwable.printStackTrace();
			if(parser != null){
				script = parser.getLastScript();
			}
		}
		if(script != null){
			script.setFilePath(modulePath);
		}
		
		return script;
	}

	@Override
	public void setupCoverage(FileCoverage coverage)
	{
		super.setupCoverage(coverage);
		
		if(this.funcDeclarations != null){
			this.funcDeclarations.setupCoverage(coverage);
		}
		
	}
	
	public FileCoverage makeCoverage()
	{
		FileCoverage coverage = new FileCoverage();
		
		setupCoverage(coverage);
		
		return coverage;
	}
	
	
}

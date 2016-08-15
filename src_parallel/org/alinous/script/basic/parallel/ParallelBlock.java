package org.alinous.script.basic.parallel;

import java.util.Iterator;

import org.alinous.AlinousDebug;
import org.alinous.debug.StepInCandidates;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.parallel.AlinousParallelThreadManager;
import org.alinous.parallel.AlinousThreadScope;
import org.alinous.parallel.IMainThreadContext;
import org.alinous.parallel.MainThreadContext;
import org.alinous.script.IScriptSentence;
import org.alinous.script.basic.AbstractScriptBlock;
import org.alinous.script.basic.BreakSentence;
import org.alinous.script.basic.ContinueSentence;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.basic.type.IVariableDescription;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.jdom.Element;

public class ParallelBlock extends AbstractScriptBlock implements IStatement{
	public static final String BLOCK_NAME = "parallelBlock";
	
	private IStatement stmt;
	private IVariableDescription operand;
	
	private ParallelBlockExec exec;
	private AlinousThreadScope scope;
	
	
	public ParallelBlock(String filePath){
		super(filePath);
	}
	
	public ParallelBlock(){
		super();
	}
	
	private synchronized void init(int numParallel, AlinousParallelThreadManager threadManager){
		if(this.scope == null){
			this.exec = new ParallelBlockExec(this);
			this.scope = threadManager.newScope(exec, numParallel);
			
			AlinousDebug.debugOut(null, "init parallel -> " + numParallel);
		}
	}
	
	@Override
	public String getName() {
		return BLOCK_NAME;
	}
	
	private AlinousThreadScope doExecute(PostContext context,
			VariableRepository valRepo) throws ExecutionException,
			RedirectRequestException 
	{
		context.getCore().reporttExecuted(this);
		
		//  
	//	AlinousTimeWatcher timer = new AlinousTimeWatcher();
		
		// debug
		//AlinousDebug.debugOut("Scope doExecute()");
		
		IScriptVariable dom = null;
		try{
			dom = this.stmt.executeStatement(context, valRepo);
		}catch(Throwable e){
			e.printStackTrace();
			context.getCore().getLogger().reportError(e);
			throw new ExecutionException(e, "Failed in adding launch thread queue.");
		}
			
		if(!(dom instanceof ScriptDomVariable)){
			throw new ExecutionException("The parallel block's thread count is wrong.");
		}
		
		int numParallel = Integer.parseInt(((ScriptDomVariable)dom).getValue());
		init(numParallel, context.getCore().getParallelThreadManager());
		
		
		// context.get
		PostContext newContext = new PostContext(context.getCore(), context.getUnit().copyUnit());
		newContext.initParams(context);
		newContext.initIncludes(context);
		
		// add scope to both
		context.addParallelExecutedScope(this.scope);
		
		Iterator<AlinousThreadScope> it = context.getParallelExecutedScope().iterator();
		while(it.hasNext()){
			AlinousThreadScope execScope = it.next();
			newContext.addParallelExecutedScope(execScope);
		}
		
		
		// final variable
		newContext.addParallelExecutedScope(this.scope); // new scope to the main thread
		
		registerFinals(context, newContext, valRepo);
		
		
		try {
			synchronized(this){
				IMainThreadContext mainThreadContext = context.getMainThreadContext(scope.toString());
				if(mainThreadContext == null){
					mainThreadContext = new MainThreadContext();
					context.setMainThreadContext(this.scope.toString(), mainThreadContext);
				}
				
				// must dispose newContext inside this			
				this.scope.startExec(new Object[]{newContext, valRepo}, mainThreadContext
						, newContext.getParallelExecutedScope().size());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//timer.stop("doExecute calling startExec()");
		
		return this.scope;
	}
	
	private void registerFinals(PostContext mainThreadContext, PostContext newContext, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		Iterator<IScriptSentence> it = this.sentences.iterator();
		while(it.hasNext()){
			IScriptSentence sentence = it.next();
			if(sentence instanceof LocalSentence){
				((LocalSentence)sentence).handleRegister(this.scope, mainThreadContext, newContext, valRepo);
			}
		}

	}
	/**
	 * Executed in new thread simaltaneously
	 * @param context
	 * @param valRepo
	 * @throws ExecutionException
	 * @throws RedirectRequestException
	 */
	public void newThreadExecute(PostContext context,
			VariableRepository valRepo) throws ExecutionException,
			RedirectRequestException 
	{
		Iterator<IScriptSentence> it = this.sentences.iterator();
		while(it.hasNext()){
			IScriptSentence exec = it.next();
			
			boolean blRes = executeSentence(exec, context, valRepo);			
			
			if(!blRes){
				
			}
			
			// Break and continue
			if(exec instanceof AbstractScriptBlock){
				AbstractScriptBlock block = (AbstractScriptBlock)exec ;
				if(block.getLastSentence(context) instanceof BreakSentence){
					break;
				}
				else if(block.getLastSentence(context) instanceof ContinueSentence){
					break;
				}
			}
			if(exec instanceof BreakSentence){
				break;
			}
			else if(exec instanceof ContinueSentence){
				break;
			}
			
		}
	}
	

	@Override
	public boolean execute(PostContext context, VariableRepository valRepo)
			throws ExecutionException, RedirectRequestException {
		doExecute(context, valRepo);
		
		return true;
	}
	
	@Override
	public IScriptVariable executeStatement(PostContext context,
			VariableRepository valRepo) throws ExecutionException,
			RedirectRequestException {

		AlinousThreadScope scope = doExecute(context, valRepo);
		
		ScriptDomVariable dom = new ScriptDomVariable("ret");
		dom.setValueType(IScriptVariable.TYPE_STRING);
		dom.setValue(scope.toString());
		
		// 
		//AlinousDebug.debugOutFile("returned in context : " + scope.toString()
		//		, context.getCore().getHome(), "/log/out.txt");
		
		return dom;
	}

	@Override
	public StepInCandidates getStepInCandidates() {
		StepInCandidates candidates = new StepInCandidates();
		this.stmt.canStepInStatements(candidates);
		return candidates;
	}
	
	
	@Override
	public IScriptVariable getReturnedVariable(PostContext context) {
		return super.getReturnedVariable(context);
	}
	
	@Override
	public void exportIntoJDomElement(Element parent) throws AlinousException {
		
	}

	@Override
	public void importFromJDomElement(Element threadElement)
			throws AlinousException {
		
	}

	public IStatement getStmt() {
		return stmt;
	}

	public void setStmt(IStatement stmt) {
		this.stmt = stmt;
	}

	public IVariableDescription getOperand() {
		return operand;
	}

	public void setOperand(IVariableDescription operand) {
		this.operand = operand;
	}



	@Override
	public void canStepInStatements(StepInCandidates candidates) {
	}

	@Override
	public void setCallerSentence(IScriptSentence callerSentence) {
				
	}

}

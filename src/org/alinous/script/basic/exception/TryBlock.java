package org.alinous.script.basic.exception;

import java.util.Iterator;

import org.alinous.debug.StepInCandidates;
import org.alinous.exec.IExecutable;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.IScriptSentence;
import org.alinous.script.basic.AbstractScriptBlock;
import org.alinous.script.basic.BreakSentence;
import org.alinous.script.basic.ContinueSentence;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.VariableRepository;
import org.jdom.Element;

public class TryBlock extends AbstractScriptBlock
{
	public static final String BLOCK_NAME = "tryBlock";
	
	private CatchBlock catchBlock;
	private FinallyBlock finallyBlock;
	
	public TryBlock(String filePath){
		super(filePath);
	}
	
	public TryBlock(){
		super();
	}
	
	@Override
	public String getName() {
		return BLOCK_NAME;
	}

	@Override
	public StepInCandidates getStepInCandidates() {
		StepInCandidates candidates = new StepInCandidates();
		
		return candidates;
	}

	@Override
	public boolean execute(PostContext context, VariableRepository valRepo)
			throws ExecutionException, RedirectRequestException
	
	{
		boolean blResult = true;
		IScriptVariable returnedVariable = null;
		
		try{
			Iterator<IScriptSentence> it = this.sentences.iterator();
			while(it.hasNext()){
				IScriptSentence exec = it.next();
				
				boolean blRes = executeSentence(exec, context, valRepo);			
				
				if(!blRes){
					return false;
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
		}catch(RedirectRequestException e){
			throw e;
		}
		catch(Throwable e){
			context.getCore().reportError(e);
			
			if(this.catchBlock != null){
				context.setLastException(e);
				blResult = this.catchBlock.execute(context, valRepo);
			}
			
			returnedVariable = context.getReturnedVariable(this.catchBlock);
		}
		finally{
			if(this.finallyBlock != null){
				boolean lastBlResult = blResult;
				blResult = this.finallyBlock.execute(context, valRepo);
				
				if(!lastBlResult){
					blResult = false;
				}
				
				
				if(returnedVariable != null){
					returnedVariable = context.getReturnedVariable(this.finallyBlock);
				}
			}
		}
		
		if(returnedVariable != null){
			context.setReturnedVariable(this, returnedVariable);
		}
		
		return blResult;
	}
	
	

	@Override
	public IScriptVariable getReturnedVariable(PostContext context) {
		return super.getReturnedVariable(context);
	}

	@Override
	public void exportIntoJDomElement(Element parent) throws AlinousException {
		Element tryElement = new Element(IExecutable.TAG_EXECUTABLE);
		tryElement.setAttribute(IExecutable.ATTR_CLASS, this.getClass().getName());
		
		parent.addContent(tryElement);
		
		
		if(this.catchBlock != null){
			this.catchBlock.exportIntoJDomElement(tryElement);
		}
		if(this.finallyBlock != null){
			this.finallyBlock.exportIntoJDomElement(tryElement);
		}
	}

	@Override
	public void importFromJDomElement(Element threadElement)
			throws AlinousException
	{
		
	}
	
	@Override
	public void setFilePath(String filePath) {
		super.setFilePath(filePath);
		
		if(this.catchBlock != null){
			this.catchBlock.setFilePath(filePath);
		}
		if(this.finallyBlock != null){
			this.finallyBlock.setFilePath(filePath);
		}
	}

	public CatchBlock getCatchBlock() {
		return catchBlock;
	}

	public void setCatchBlock(CatchBlock catchBlock) {
		this.catchBlock = catchBlock;
	}

	public FinallyBlock getFinallyBlock() {
		return finallyBlock;
	}

	public void setFinallyBlock(FinallyBlock finallyBlock) {
		this.finallyBlock = finallyBlock;
	}

	
}

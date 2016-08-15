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
import org.alinous.script.runtime.VariableRepository;
import org.jdom.Element;

public class FinallyBlock extends AbstractScriptBlock
{
	public static final String BLOCK_NAME = "finallyBlock";
	
	public FinallyBlock(String filePath){
		super(filePath);
	}
	
	public FinallyBlock(){
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
		
		return true;
	}

	@Override
	public void exportIntoJDomElement(Element parent) throws AlinousException {
		Element finallyElement = new Element(IExecutable.TAG_EXECUTABLE);
		finallyElement.setAttribute(IExecutable.ATTR_CLASS, this.getClass().getName());
		
		parent.addContent(finallyElement);
	}

	@Override
	public void importFromJDomElement(Element threadElement)
			throws AlinousException {
		
	}

}

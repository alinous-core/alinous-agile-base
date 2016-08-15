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
import org.alinous.script.basic.type.VariableDescriptor;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElement;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.jdom.Element;

public class CatchBlock extends AbstractScriptBlock
{
	public static final String BLOCK_NAME = "catchBlock";
	
	private VariableDescriptor variableDescriptor;
	
	public CatchBlock(String filePath){
		super(filePath);
	}
	
	public CatchBlock(){
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
		PathElement pathElement = (PathElement)this.variableDescriptor.getPath();
		ScriptDomVariable exceptionDom = createExceptionDom(pathElement.getPath(), context.getLastException());		
		
		valRepo.putValue(exceptionDom, context);
		
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
	
	private ScriptDomVariable createExceptionDom(String name, Throwable e)
	{
		ScriptDomVariable exceptionDom = new ScriptDomVariable(name);
		
		exceptionDom.setValue(e.getClass().getName());
		exceptionDom.setValueType(IScriptVariable.TYPE_STRING);
		
		// message
		ScriptDomVariable msgDom = new ScriptDomVariable("message");
		msgDom.setValue(e.getMessage());
		msgDom.setValueType(IScriptVariable.TYPE_STRING);
		exceptionDom.put(msgDom);
		
		if(e.getCause() != null){
			ScriptDomVariable causeDom = createExceptionDom("cause", e.getCause());
			
			exceptionDom.put(causeDom);
		}
		
		return exceptionDom;
	}
	
	
	@Override
	public void exportIntoJDomElement(Element parent) throws AlinousException {
		Element catchElement = new Element(IExecutable.TAG_EXECUTABLE);
		catchElement.setAttribute(IExecutable.ATTR_CLASS, this.getClass().getName());
		
		parent.addContent(catchElement);
		
	}

	@Override
	public void importFromJDomElement(Element threadElement)
			throws AlinousException {
		
	}

	public VariableDescriptor getVariableDescriptor() {
		return variableDescriptor;
	}

	public void setVariableDescriptor(VariableDescriptor variableDescriptor) {
		this.variableDescriptor = variableDescriptor;
	}
}

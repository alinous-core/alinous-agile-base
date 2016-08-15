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
package org.alinous.debug;

import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.script.IScriptBlock;
import org.alinous.script.runtime.VariableRepository;
import org.jdom.Element;

public class DebugStackFrame
{
	public static final String TAG_STACKFRAME = "STACKFRAME";
	public static final String ATTR_LINE = "line";
	public static final String ATTR_FILENAME = "filename";
	public static final String ATTR_PEEK = "peek";
	public static final String ATTR_NAME= "name";
	public static final String ATTR_STACKID = "stackId";
	public static final String ATTR_STEPIN_CANDIDATES = "stepin";
	public static final String ATTR_STEPIN_EXECUTED = "exeuted";
	
	private int line;
	private String fileName;
	private String name;
	private boolean peek;
	
	private VariableRepository repo;
	private PostContext context;
	
	private long stackId;
	private StepInCandidates currentCandidate;
	private int executedCandidate;
	
	public DebugStackFrame(IScriptBlock block, VariableRepository repo, PostContext context)
	{
		if(block == null){
			return;
		}
		//this.block = block;
		
		this.line = block.getLine();
		this.fileName = block.getFilePath();
		this.name = block.getName();
		
		this.repo = repo;
		this.context = context;
	}
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public int getLine() {
		return line;
	}
	public void setLine(int line) {
		this.line = line;
	}
	
	public boolean isPeek() {
		return peek;
	}

	public void setPeek(boolean peek) {
		this.peek = peek;
	}

	public void exportIntoJDomElement(Element parent) throws ExecutionException
	{
		Element element = new Element(TAG_STACKFRAME);
		element.setAttribute(ATTR_LINE, Integer.toString(this.line));
		element.setAttribute(ATTR_FILENAME, this.fileName);
		element.setAttribute(ATTR_PEEK, Boolean.toString(this.peek));
		element.setAttribute(ATTR_NAME, this.name);
		element.setAttribute(ATTR_STACKID, Long.toString(this.stackId));
		
		if(this.currentCandidate != null){
			element.setAttribute(ATTR_STEPIN_CANDIDATES, Integer.toString(this.currentCandidate.getCount()));
		}
		else{
			element.setAttribute(ATTR_STEPIN_CANDIDATES, Integer.toString(0));
		}
		
		element.setAttribute(ATTR_STEPIN_EXECUTED, Integer.toString(this.executedCandidate));
		
		
		this.repo.exportIntoJDomElementWithFinal(element, this.context);
		
		parent.addContent(element);
	}
	
	public void importFromJDomElement(Element stackFrameElement)
	{
		String strLine = stackFrameElement.getAttributeValue(ATTR_LINE);
		if(strLine != null){
			this.line = Integer.parseInt(strLine);
		}
		
		this.fileName = stackFrameElement.getAttributeValue(ATTR_FILENAME);
		
		String strPeek = stackFrameElement.getAttributeValue(ATTR_PEEK);
		if(strPeek != null){
			this.peek = Boolean.parseBoolean(strPeek);
		}
		
		this.name = stackFrameElement.getAttributeValue(ATTR_NAME);
		
		String stackIdString = stackFrameElement.getAttributeValue(ATTR_STACKID);
		this.stackId = Long.parseLong(stackIdString);		
		
		String strCandidates = stackFrameElement.getAttributeValue(ATTR_STEPIN_CANDIDATES);
		if(strCandidates != null){
			this.currentCandidate = new StepInCandidates();
			this.currentCandidate.setCount(Integer.parseInt(strCandidates));			
		}
		
		String strExecCount = stackFrameElement.getAttributeValue(ATTR_STEPIN_EXECUTED);
		if(strExecCount != null){
			this.executedCandidate = Integer.parseInt(strExecCount);
		}
		
		Element valueRepoElement = stackFrameElement.getChild(VariableRepository.TAG_VALUE_REPOSITORY);
		this.repo = new VariableRepository();
		this.repo.importFromJDomElement(valueRepoElement);
	}

	public String getName() {
		return name;
	}

	public VariableRepository getRepo() {
		return repo;
	}

	public long getStackId()
	{
		return stackId;
	}

	public void setStackId(long stackId)
	{
		this.stackId = stackId;
	}
	
	public StepInCandidates getCurrentCandidate()
	{
		return currentCandidate;
	}

	public void setCurrentCandidate(StepInCandidates currentCandidate)
	{
		this.currentCandidate = currentCandidate;
	}

	public int getExecutedCandidate()
	{
		return executedCandidate;
	}

	public void incCandidate()
	{
		this.executedCandidate = this.executedCandidate + 1;
	}
	
	public void resetExecutedCandidate()
	{
		this.executedCandidate = 0;
	}
	
	public boolean canStepIn()
	{
		return this.currentCandidate.getCount() > this.executedCandidate;
	}

}
